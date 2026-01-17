# 万人同时选课场景架构分析报告

## 执行摘要

**结论：当前架构无法应对万人同时选课场景。**

当前系统存在多个关键瓶颈，预计在 500+ 并发请求时就会出现严重性能问题。本报告基于对现有代码的深入分析和业界最佳实践研究，提供分阶段的优化方案。

---

## 🔍 当前架构分析

### 1. 选课流程现状

```
┌──────────┐     ┌──────────┐     ┌─────────────────┐     ┌────────────┐
│ Frontend │────▶│ Gateway  │────▶│ Academic Service │────▶│ PostgreSQL │
│          │     │  :8080   │     │     :8083       │     │   :5432    │
└──────────┘     └──────────┘     └─────────────────┘     └────────────┘
                                          │
                                   @Transactional
                                   (同步阻塞，无锁)
```

**关键代码路径** (`CourseEnrollmentService.enrollStudent()`):
```java
@Transactional  // 默认 READ_COMMITTED，无显式锁
public EnrollmentResponseDTO enrollStudent(EnrollmentRequestDTO request) {
    // 1. 查询教学班 (SELECT) - 无锁！
    TeachingClass tc = teachingClassRepository.findById(teachingClassId);
    
    // 2. 检查状态 (内存) - 可能读到过期数据
    if (!tc.getStatus().canEnroll()) { ... }
    
    // 3. 检查容量 (内存) - ⚠️ 竞态条件窗口开始
    if (!tc.hasCapacity()) { ... }  // enrolledCount < capacity
    
    // 4. 检查是否已选 (SELECT) - DB 唯一约束兜底
    if (enrollmentRepository.existsByStudentIdAndTeachingClassId(...)) { ... }
    
    // 5. 创建选课记录 (INSERT)
    CourseEnrollment enrollment = new CourseEnrollment();
    enrollmentRepository.save(enrollment);
    
    // 6. 更新已选人数 (UPDATE) - ⚠️ 竞态条件！
    tc.incrementEnrolledCount();  // enrolledCount++ (非原子操作)
    teachingClassRepository.save(tc);  // 可能覆盖其他并发更新
}
```

### 2. 已识别的关键问题

| 问题 | 严重程度 | 现状 | 对比 (grade-service) |
|------|---------|------|---------------------|
| **无并发控制** | 🔴 严重 | 无锁保护容量检查 | ✅ 有 `@Lock(PESSIMISTIC_WRITE)` |
| **无乐观锁** | 🔴 严重 | TeachingClass 无 `@Version` | ✅ Grade 有 `@Version` |
| **同步阻塞** | 🔴 严重 | 所有请求直接打 DB | - |
| **无连接池配置** | 🟠 中等 | HikariCP 默认 10 连接 | 同 |
| **无缓存** | 🟠 中等 | 每次请求查 DB | - |
| **无限流** | 🟠 中等 | Gateway 无 Rate Limiting | - |
| **RabbitMQ 未使用** | 🟡 低 | 队列已定义但无消费者 | - |

**代码证据：**
- `CourseEnrollmentRepository.java` 没有任何 `@Lock` 注解
- `TeachingClass.java` 没有 `@Version` 字段
- 对比 `GradeRepository.java` 有 4 个悲观锁方法
- 对比 `Grade.java` 有 `@Version` 乐观锁

### 3. 容量估算

**当前系统理论极限：**
```
数据库连接数: 10 (HikariCP 默认)
单次选课耗时: ~50ms (2次 SELECT + 1次 INSERT + 1次 UPDATE)
理论 QPS: 10 / 0.05 = 200 QPS

万人选课场景（最坏情况）:
- 假设选课窗口 10 秒内高峰
- 请求量: 10,000 / 10 = 1,000 QPS
- 缺口: 1,000 - 200 = 800 QPS (400% 超载)
```

**预期故障模式：**
1. 数据库连接池耗尽 → 请求超时 (3s 后失败)
2. 竞态条件 → 热门课程超卖 (capacity=50, enrolled=55)
3. 级联失败 → Consul 健康检查失败 → 服务下线 → 系统不可用

---

## 🛠️ 优化方案

### Phase 1: 紧急修复（1-2天）- 解决超卖问题

**目标**: 确保数据一致性，防止超卖

#### 1.1 添加数据库悲观锁

```java
// TeachingClassRepository.java - 新增方法
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT tc FROM TeachingClass tc WHERE tc.id = :id")
Optional<TeachingClass> findByIdWithLock(@Param("id") UUID id);
```

```java
// CourseEnrollmentService.java - 修改 enrollStudent
@Transactional
public EnrollmentResponseDTO enrollStudent(EnrollmentRequestDTO request) {
    // 使用悲观锁查询，对应 SQL: SELECT ... FOR UPDATE
    TeachingClass tc = teachingClassRepository
        .findByIdWithLock(teachingClassId)
        .orElseThrow(() -> new RuntimeException("Teaching class not found"));
    
    // 后续逻辑不变，但现在是串行执行...
}
```

#### 1.2 添加乐观锁版本控制

```java
// TeachingClass.java - 添加版本字段
@Entity
@Table(name = "teaching_class")
public class TeachingClass {
    // ... 现有字段 ...
    
    @Version
    @Column(name = "version")
    private Long version;
}
```

#### 1.3 数据库层面约束（兜底）

```sql
-- 防止超卖的 CHECK 约束
ALTER TABLE teaching_class 
ADD CONSTRAINT chk_capacity 
CHECK (enrolled_count <= capacity);
```

**预期效果：** 
- ✅ 解决超卖问题
- ⚠️ 性能会下降 (锁竞争)
- 预计 QPS: 100-150 (热门课程)

---

### Phase 2: 性能优化（3-5天）- 提升吞吐量

**目标**: 减少数据库压力，提升并发能力

#### 2.1 Redis 分布式锁 + 库存预扣

**架构变化：**
```
┌──────────┐     ┌──────────┐     ┌───────────────┐     ┌─────────────────┐
│ Frontend │────▶│ Gateway  │────▶│    Redis      │────▶│ Academic Service│
│          │     │ 限流     │     │ 库存预扣+锁  │     │   异步落库      │
└──────────┘     └──────────┘     └───────────────┘     └─────────────────┘
```

**Redis Lua 脚本 - 原子扣减库存：**
```lua
-- check_and_deduct.lua
local stock_key = KEYS[1]        -- "course:stock:{classId}"
local enrolled_key = KEYS[2]     -- "course:enrolled:{classId}:{studentId}"

-- 检查是否已选
if redis.call('EXISTS', enrolled_key) == 1 then
    return -1  -- 已选课
end

-- 原子扣减库存
local stock = tonumber(redis.call('GET', stock_key) or 0)
if stock > 0 then
    redis.call('DECR', stock_key)
    redis.call('SET', enrolled_key, '1', 'EX', 86400)  -- 24小时过期
    return 1   -- 成功
else
    return 0   -- 无库存
end
```

**Java 实现：**
```java
@Service
public class StockService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String DEDUCT_SCRIPT = """
        local stock_key = KEYS[1]
        local enrolled_key = KEYS[2]
        if redis.call('EXISTS', enrolled_key) == 1 then return -1 end
        local stock = tonumber(redis.call('GET', stock_key) or 0)
        if stock > 0 then
            redis.call('DECR', stock_key)
            redis.call('SET', enrolled_key, '1', 'EX', 86400)
            return 1
        end
        return 0
    """;
    
    // 选课开始前预热库存
    public void preloadStock(UUID classId, int availableSlots) {
        String key = "course:stock:" + classId;
        redisTemplate.opsForValue().set(key, availableSlots);
    }
    
    // 原子扣减库存
    public int tryDeductStock(UUID classId, UUID studentId) {
        List<String> keys = List.of(
            "course:stock:" + classId,
            "course:enrolled:" + classId + ":" + studentId
        );
        
        Long result = redisTemplate.execute(
            new DefaultRedisScript<>(DEDUCT_SCRIPT, Long.class),
            keys
        );
        
        return result != null ? result.intValue() : 0;
    }
}
```

#### 2.2 HikariCP 连接池配置

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50          # 增加连接数
      minimum-idle: 10               # 最小空闲
      connection-timeout: 3000       # 3秒超时
      idle-timeout: 600000           # 10分钟空闲
      max-lifetime: 1800000          # 30分钟最大生命
      pool-name: AcademicServicePool
      leak-detection-threshold: 60000 # 泄漏检测
```

#### 2.3 Gateway 限流 (Token Bucket)

```yaml
# grade-gateway application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: enrollment-rate-limited
          uri: lb://grade-academic-service
          predicates:
            - Path=/api/v1/enrollment/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 500   # 每秒补充
                redis-rate-limiter.burstCapacity: 1000  # 最大突发
                key-resolver: "#{@userKeyResolver}"
```

```java
// RateLimiterConfig.java - Gateway 服务
@Configuration
public class RateLimiterConfig {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 按用户限流
            String userId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }
}
```

**预期效果：** 
- ✅ QPS 提升至 2,000-3,000
- ✅ 数据库压力大幅降低
- ⚠️ Redis 成为新瓶颈

---

### Phase 3: 高并发架构（1-2周）- 支撑万人选课

**目标**: 异步处理 + 消息削峰，支撑万人并发

#### 3.1 消息队列削峰架构

```
┌──────────┐    ┌──────────┐    ┌───────────┐    ┌──────────┐    ┌─────────────────┐
│ Frontend │───▶│ Gateway  │───▶│  Redis    │───▶│ RabbitMQ │───▶│ Academic Service│
│          │    │ 限流     │    │ 预检+扣减 │    │ 削峰队列 │    │ 批量消费        │
└──────────┘    └──────────┘    └───────────┘    └──────────┘    └─────────────────┘
      │                                                                   │
      │◀─────────────────────── WebSocket/SSE 推送结果 ◀─────────────────┘
```

**选课请求流程：**
1. **前置校验** (Gateway): 限流、身份验证
2. **Redis 预检** (50μs): 检查库存、防重复
3. **Redis 预扣** (100μs): Lua 原子扣减
4. **入队** (1ms): 发送到 RabbitMQ
5. **响应** (立即): 返回排队票据 (202 Accepted)
6. **异步处理** (后台): Consumer 批量写库
7. **结果通知** (WebSocket): 推送选课结果

#### 3.2 RabbitMQ 配置

```java
// RabbitMQConfig.java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue enrollmentQueue() {
        return QueueBuilder
            .durable("enrollment.queue")
            .withArgument("x-max-priority", 10)      // 支持优先级
            .withArgument("x-max-length", 100000)    // 最大队列长度
            .build();
    }
    
    @Bean
    public Queue enrollmentDLQ() {
        return QueueBuilder
            .durable("enrollment.dlq")
            .build();
    }
    
    @Bean
    public DirectExchange enrollmentExchange() {
        return new DirectExchange("enrollment.exchange");
    }
    
    @Bean
    public Binding enrollmentBinding() {
        return BindingBuilder
            .bind(enrollmentQueue())
            .to(enrollmentExchange())
            .with("enrollment.create");
    }
}
```

#### 3.3 选课接口 - 快速响应

```java
@RestController
@RequestMapping("/api/v1/enrollment")
public class EnrollmentController {
    
    @Autowired private StockService stockService;
    @Autowired private RabbitTemplate rabbitTemplate;
    
    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentTicket> enroll(
            @RequestBody EnrollmentRequest req,
            @RequestHeader("X-User-Id") UUID userId) {
        
        UUID classId = req.getTeachingClassId();
        
        // 1. Redis 预扣库存 (原子操作)
        int result = stockService.tryDeductStock(classId, userId);
        
        switch (result) {
            case -1:
                return ResponseEntity.badRequest()
                    .body(EnrollmentTicket.alreadyEnrolled());
            case 0:
                return ResponseEntity.ok()
                    .body(EnrollmentTicket.noStock());
        }
        
        // 2. 发送消息到队列
        String ticketId = UUID.randomUUID().toString();
        EnrollmentMessage message = new EnrollmentMessage(
            ticketId, userId, classId, LocalDateTime.now()
        );
        
        rabbitTemplate.convertAndSend(
            "enrollment.exchange",
            "enrollment.create",
            message
        );
        
        // 3. 立即返回排队票据
        return ResponseEntity.accepted()
            .body(EnrollmentTicket.queued(ticketId));
    }
}
```

#### 3.4 消息消费者 - 批量处理

```java
@Service
public class EnrollmentConsumer {
    
    @Autowired private CourseEnrollmentRepository enrollmentRepository;
    @Autowired private TeachingClassRepository classRepository;
    @Autowired private WebSocketNotificationService notificationService;
    
    @RabbitListener(
        queues = "enrollment.queue",
        containerFactory = "batchContainerFactory"
    )
    @Transactional
    public void processBatch(List<EnrollmentMessage> messages) {
        // 1. 批量创建选课记录
        List<CourseEnrollment> enrollments = messages.stream()
            .map(this::createEnrollment)
            .toList();
        
        enrollmentRepository.saveAll(enrollments);
        
        // 2. 批量更新计数 (按课程分组)
        Map<UUID, Long> countByClass = messages.stream()
            .collect(Collectors.groupingBy(
                EnrollmentMessage::getClassId,
                Collectors.counting()
            ));
        
        countByClass.forEach((classId, count) -> 
            classRepository.incrementEnrolledCountBatch(classId, count.intValue())
        );
        
        // 3. 批量通知前端
        messages.forEach(msg -> 
            notificationService.notifySuccess(msg.getTicketId(), msg.getStudentId())
        );
    }
    
    private CourseEnrollment createEnrollment(EnrollmentMessage msg) {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudentId(msg.getStudentId());
        enrollment.setTeachingClass(
            classRepository.getReferenceById(msg.getClassId())
        );
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(msg.getRequestTime());
        return enrollment;
    }
}

// BatchContainerFactory 配置
@Bean
public SimpleRabbitListenerContainerFactory batchContainerFactory(
        ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = 
        new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setBatchListener(true);
    factory.setBatchSize(100);                    // 每批 100 条
    factory.setReceiveTimeout(1000L);             // 1秒超时
    factory.setConcurrentConsumers(3);            // 3 个消费者
    factory.setMaxConcurrentConsumers(10);        // 最多 10 个
    return factory;
}
```

#### 3.5 数据库批量更新优化

```java
// TeachingClassRepository.java
@Modifying
@Query("UPDATE TeachingClass tc SET tc.enrolledCount = tc.enrolledCount + :count WHERE tc.id = :classId")
void incrementEnrolledCountBatch(@Param("classId") UUID classId, @Param("count") int count);
```

```sql
-- 添加索引优化
CREATE INDEX idx_enrollment_student_class 
ON course_enrollment(student_id, teaching_class_id);

CREATE INDEX idx_teaching_class_status 
ON teaching_class(status, academic_year, semester_number);

-- 批量插入优化 (PostgreSQL)
SET synchronous_commit = off;  -- 异步提交 (批量场景)
```

**预期效果：**
- ✅ QPS 提升至 10,000+
- ✅ 用户体验好 (秒级响应)
- ✅ 系统稳定 (削峰填谷)

---

### Phase 4: 生产级加固（可选，1周）

| 组件 | 优化项 | 工具 |
|------|--------|------|
| **熔断降级** | 服务降级策略 | Resilience4j |
| **分布式追踪** | 全链路追踪 | Micrometer + Zipkin |
| **监控告警** | 性能指标监控 | Prometheus + Grafana |
| **压测验证** | 万人压测 | Gatling / Locust |
| **缓存预热** | 选课前预加载 | @Scheduled |
| **多级降级** | 热门课程排队 | 动态策略 |

**Resilience4j 熔断配置：**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      enrollmentService:
        sliding-window-size: 100
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 10
  ratelimiter:
    instances:
      enrollmentApi:
        limit-for-period: 1000
        limit-refresh-period: 1s
        timeout-duration: 0
```

---

## 📊 各阶段效果对比

| 阶段 | 预计 QPS | 超卖风险 | 响应时间 | 用户体验 | 实施成本 |
|------|----------|---------|---------|---------|---------|
| **当前** | 200 | 🔴 高 | 50-100ms | 差 | - |
| **Phase 1** | 100-150 | 🟢 无 | 100-200ms | 差 | 低 (1-2天) |
| **Phase 2** | 2,000-3,000 | 🟢 无 | 10-50ms | 中 | 中 (3-5天) |
| **Phase 3** | 10,000+ | 🟢 无 | <10ms* | 好 | 高 (1-2周) |

*注：Phase 3 的响应时间是立即响应排队票据，实际选课确认通过 WebSocket 异步推送

---

## 🎯 推荐实施路径

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         实施时间线                                      │
└─────────────────────────────────────────────────────────────────────────┘

Week 1 (Day 1-2): Phase 1 紧急修复
├── Day 1: 添加 @Lock 悲观锁 + @Version 乐观锁
├── Day 2: 数据库 CHECK 约束 + 基本测试
└── 交付: 解决超卖问题，系统可用但性能受限

Week 1 (Day 3-5): Phase 2 性能优化
├── Day 3: Redis Lua 脚本 + StockService
├── Day 4: HikariCP 配置 + Gateway 限流
├── Day 5: 集成测试 + 压力测试
└── 交付: QPS 提升至 2000-3000

Week 2-3: Phase 3 高并发架构
├── Day 6-7: RabbitMQ 配置 + 消息体定义
├── Day 8-9: 选课接口改造 (异步响应)
├── Day 10-11: 消费者批量处理 + 批量更新
├── Day 12: WebSocket 结果推送
├── Day 13-14: 全链路测试 + 压测验证
└── 交付: 支撑万人选课

Week 4 (可选): Phase 4 生产加固
├── Resilience4j 熔断
├── 监控告警
├── 文档完善
└── 上线演练
```

---

## 📁 需要修改/新增的文件清单

### Phase 1 修改文件
| 文件 | 修改内容 |
|------|---------|
| `TeachingClass.java` | 添加 `@Version` 字段 |
| `TeachingClassRepository.java` | 添加 `findByIdWithLock()` 方法 |
| `CourseEnrollmentService.java` | 使用锁方法查询 |
| `init-schemas.sql` | 添加 CHECK 约束 |

### Phase 2 新增文件
| 文件 | 说明 |
|------|------|
| `StockService.java` | Redis 库存预扣服务 |
| `grade-gateway/.../RateLimiterConfig.java` | 限流配置 |
| `application.yml` | HikariCP + Redis 配置更新 |

### Phase 3 新增文件
| 文件 | 说明 |
|------|------|
| `RabbitMQConfig.java` | 队列/交换机配置 |
| `EnrollmentMessage.java` | 消息体 DTO |
| `EnrollmentTicket.java` | 排队票据 DTO |
| `EnrollmentConsumer.java` | 消息消费者 |
| `WebSocketNotificationService.java` | 结果推送服务 |
| `EnrollmentController.java` | 接口改造 (异步) |

---

## ❓ 待确认问题

在开始实施前，请确认以下问题：

1. **选课时间窗口**
   - 选课是否集中在特定时段（如每学期开始的某天某时）？
   - 是否需要支持分批选课（如大四优先）？

2. **热门课程策略**
   - 是否有少数热门课程占大部分请求？
   - 热门课程是否需要抽签/排队机制？

3. **结果通知方式**
   - 用户是否必须同步得知选课结果？
   - 是否可以接受「正在处理中」→「选课成功」的异步模式？

4. **数据一致性要求**
   - 是否允许最终一致性？（Redis 预扣后异步落库）
   - 极端情况（Redis 宕机）的回滚策略？

5. **基础设施**
   - Redis 是否有主从/集群配置？
   - RabbitMQ 是否需要高可用（镜像队列）？

---

## 📚 参考资料

1. [Redis Distributed Locking](https://redis.io/ebook/part-2-core-concepts/chapter-6-application-components-in-redis/6-2-distributed-locking)
2. [Token Bucket Rate Limiter (Redis & Java)](https://www.javacodegeeks.com/2025/05/rate-limiting-in-java-implementing-per-user-throttling-with-redis-buckets.html)
3. [Spring Boot Distributed Locks with Redis (2025)](https://medium.com/@tuteja_lovish/spring-boot-distributed-locks-with-redis-stop-duplicate-jobs-race-conditions-overlapping-bda3be541b02)
4. [SELECT FOR UPDATE in PostgreSQL](https://stormatics.tech/blogs/select-for-update-in-postgresql)
5. [quick-enrollments Open Source Project](https://github.com/szymborski/quick-enrollments)
6. [Building Robust Flash Sale System (2025)](https://blog.stackademic.com/building-a-robust-flash-sale-system-from-overselling-to-high-performance-architecture-f7a19b725e0b)

---

*报告生成时间: 2026-01-17*
*分析基于: GradeSystem 微服务架构 v1.0.0-SNAPSHOT*
