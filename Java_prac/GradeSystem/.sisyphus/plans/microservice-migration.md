# GradeSystem 微服务架构改造工作计划

## 1. 项目概述

### 1.1 改造目标
将现有的 Spring Boot 4.0 单体应用改造为微服务架构，最终交付可通过 `docker pull` 获取的容器镜像。

### 1.2 技术栈

| 组件 | 技术选型 |
|------|----------|
| 服务发现 & 配置中心 | Consul |
| API 网关 | Spring Cloud Gateway |
| 服务间调用 | OpenFeign + Spring Cloud LoadBalancer |
| 消息队列 | RabbitMQ |
| Session 共享 | Spring Session + Redis |
| 数据库 | PostgreSQL (分 Schema) |
| 容器化 | Docker + Docker Compose |
| 镜像仓库 | Docker Hub (`morethan987/*`) |
| CI/CD | GitHub Actions |

### 1.3 服务划分

```
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (:8080)                          │
│                   morethan987/grade-gateway                     │
└─────────────────────────────────────────────────────────────────┘
        │              │              │              │              │
        ▼              ▼              ▼              ▼              ▼
┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
│   Auth    │  │   User    │  │ Academic  │  │   Grade   │  │ Analytics │
│  Service  │  │  Service  │  │  Service  │  │  Service  │  │  Service  │
│  (:8081)  │  │  (:8082)  │  │  (:8083)  │  │  (:8084)  │  │  (:8085)  │
└───────────┘  └───────────┘  └───────────┘  └───────────┘  └───────────┘
      │              │              │              │              │
      ▼              ▼              ▼              ▼              ▼
┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
│auth_schema│  │user_schema│  │ academic  │  │grade_schema│ │ analytics │
│           │  │           │  │  _schema  │  │           │  │  _schema  │
└───────────┘  └───────────┘  └───────────┘  └───────────┘  └───────────┘
                         PostgreSQL (grade_system)
```

### 1.4 各服务职责

| 服务 | 职责 | 数据库表 | 原组件 |
|------|------|----------|--------|
| **Auth Service** | 认证、角色权限管理 | user, role, permission, user_role, role_permission | AuthController/Service, RoleController/Service, SecurityConfig |
| **User Service** | 用户档案、学生、教师信息 | user_profile, student, teacher | UserController/Service, StudentController/Service, TeacherController/Service (基础查询) |
| **Academic Service** | 课程、教学班、选课退课 | course, teaching_class, student_teaching_class | CourseController/Service |
| **Grade Service** | 成绩录入查询、GPA 计算、Excel 导入导出 | grade | GradeController/Service, TeacherController (成绩相关), ConcurrencyController/Service |
| **Analytics Service** | Dashboard 数据聚合、统计分析 | (Redis 缓存) | DashboardController/Service |
| **Gateway** | 路由、负载均衡、统一入口 | - | 新建 |

---

## 2. 项目结构

```
GradeSystem/
├── docker-compose.yml              # 开发环境
├── docker-compose.prod.yml         # 生产环境 (交付给老师)
├── init-schemas.sql                # 数据库初始化脚本
│
├── grade-common/                   # 公共模块
│   ├── pom.xml
│   └── src/main/java/com/example/common/
│       ├── dto/                    # 共享 DTO
│       ├── exception/              # 公共异常
│       ├── config/                 # 公共配置 (Redis, Feign)
│       └── event/                  # 事件定义
│
├── grade-gateway/                  # API 网关
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/example/gateway/
│
├── grade-auth-service/             # 认证服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/example/auth/
│
├── grade-user-service/             # 用户服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/example/user/
│
├── grade-academic-service/         # 教务服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/example/academic/
│
├── grade-grade-service/            # 成绩服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/example/grade/
│
├── grade-analytics-service/        # 分析服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/example/analytics/
│
├── frontend/                       # 前端 (已有)
│   ├── Dockerfile
│   └── ...
│
└── .github/workflows/
    └── docker-publish.yml          # CI/CD
```

---

## 3. 分阶段实施计划

### Phase 1: 基础设施搭建 (Week 1-2)

#### 任务清单

| ID | 任务 | 优先级 | 预计工时 | 状态 |
|----|------|--------|----------|------|
| 1.1 | 创建 Maven 多模块项目结构 | 🔴 高 | 4h | ⬜ |
| 1.2 | 创建 grade-common 公共模块 | 🔴 高 | 4h | ⬜ |
| 1.3 | 编写 docker-compose.yml (Consul + RabbitMQ + Redis + PostgreSQL) | 🔴 高 | 4h | ⬜ |
| 1.4 | 编写 init-schemas.sql 数据库初始化脚本 | 🔴 高 | 2h | ⬜ |
| 1.5 | 测试基础设施启动 | 🔴 高 | 2h | ⬜ |
| 1.6 | 创建各服务的 Dockerfile 模板 | 🟡 中 | 2h | ⬜ |

---

### Phase 2: Gateway + Auth Service (Week 3-4)

#### 任务清单

| ID | 任务 | 优先级 | 预计工时 | 状态 |
|----|------|--------|----------|------|
| 2.1 | 创建 grade-gateway 模块 | 🔴 高 | 4h | ⬜ |
| 2.2 | 配置 Gateway 路由 (静态路由) | 🔴 高 | 4h | ⬜ |
| 2.3 | 创建 grade-auth-service 模块 | 🔴 高 | 2h | ⬜ |
| 2.4 | 迁移 User, Role, Permission 实体到 auth-service | 🔴 高 | 4h | ⬜ |
| 2.5 | 迁移 AuthService, RoleService | 🔴 高 | 6h | ⬜ |
| 2.6 | 迁移 AuthController, RoleController | 🔴 高 | 4h | ⬜ |
| 2.7 | 配置 Spring Session Redis | 🔴 高 | 4h | ⬜ |
| 2.8 | 适配 SecurityConfig (保持 Session 认证) | 🔴 高 | 4h | ⬜ |
| 2.9 | 注册服务到 Consul | 🟡 中 | 2h | ⬜ |
| 2.10 | Gateway 动态路由 (从 Consul 发现服务) | 🟡 中 | 4h | ⬜ |
| 2.11 | 单元测试 + 集成测试 | 🟡 中 | 6h | ⬜ |

---

### Phase 3: User Service (Week 5-6)

#### 任务清单

| ID | 任务 | 优先级 | 预计工时 | 状态 |
|----|------|--------|----------|------|
| 3.1 | 创建 grade-user-service 模块 | 🔴 高 | 2h | ⬜ |
| 3.2 | 迁移 UserProfile, Student, Teacher 实体 | 🔴 高 | 4h | ⬜ |
| 3.3 | 迁移 UserService, StudentService, TeacherService (基础查询) | 🔴 高 | 6h | ⬜ |
| 3.4 | 迁移对应 Controllers | 🔴 高 | 4h | ⬜ |
| 3.5 | 创建 AuthServiceClient (Feign) - 用于验证用户 | 🔴 高 | 4h | ⬜ |
| 3.6 | 配置 Spring Session Redis | 🟡 中 | 2h | ⬜ |
| 3.7 | 单元测试 + 集成测试 | 🟡 中 | 6h | ⬜ |

---

### Phase 4: Academic Service + Grade Service (Week 7-9)

#### 任务清单

| ID | 任务 | 优先级 | 预计工时 | 状态 |
|----|------|--------|----------|------|
| 4.1 | 创建 grade-academic-service 模块 | 🔴 高 | 2h | ⬜ |
| 4.2 | 迁移 Course, TeachingClass 实体 | 🔴 高 | 4h | ⬜ |
| 4.3 | 迁移 CourseService 及 Controller | 🔴 高 | 6h | ⬜ |
| 4.4 | 实现事件发布 (CourseSelected, CourseDropped) | 🔴 高 | 6h | ⬜ |
| 4.5 | 创建 grade-grade-service 模块 | 🔴 高 | 2h | ⬜ |
| 4.6 | 迁移 Grade 实体 | 🔴 高 | 2h | ⬜ |
| 4.7 | 迁移 GradeService, ConcurrencyControlService | 🔴 高 | 8h | ⬜ |
| 4.8 | 迁移 GradeController, ConcurrencyController, TeacherController (成绩部分) | 🔴 高 | 6h | ⬜ |
| 4.9 | 实现事件消费者 (监听选课/退课事件) | 🔴 高 | 6h | ⬜ |
| 4.10 | 创建 UserServiceClient, AcademicServiceClient (Feign) | 🔴 高 | 4h | ⬜ |
| 4.11 | 端到端测试 | 🔴 高 | 8h | ⬜ |

---

### Phase 5: Analytics Service + 前端容器化 (Week 10-11)

#### 任务清单

| ID | 任务 | 优先级 | 预计工时 | 状态 |
|----|------|--------|----------|------|
| 5.1 | 创建 grade-analytics-service 模块 | 🟡 中 | 2h | ⬜ |
| 5.2 | 实现 Dashboard 数据聚合 (调用其他服务) | 🟡 中 | 8h | ⬜ |
| 5.3 | 添加 Redis 缓存 | 🟡 中 | 4h | ⬜ |
| 5.4 | 迁移 DashboardController | 🟡 中 | 2h | ⬜ |
| 5.5 | 前端 Dockerfile 编写 | 🔴 高 | 4h | ⬜ |
| 5.6 | 前端 nginx 配置 (代理到 Gateway) | 🔴 高 | 2h | ⬜ |
| 5.7 | 前端环境变量配置 | 🟡 中 | 2h | ⬜ |

---

### Phase 6: CI/CD + 最终交付 (Week 12)

#### 任务清单

| ID | 任务 | 优先级 | 预计工时 | 状态 |
|----|------|--------|----------|------|
| 6.1 | 配置 GitHub Actions 自动构建 | 🔴 高 | 4h | ⬜ |
| 6.2 | 配置 Docker Hub 推送 | 🔴 高 | 2h | ⬜ |
| 6.3 | 编写 docker-compose.prod.yml | 🔴 高 | 4h | ⬜ |
| 6.4 | 编写部署文档 (README) | 🔴 高 | 4h | ⬜ |
| 6.5 | 全面测试 | 🔴 高 | 8h | ⬜ |
| 6.6 | 打 Tag 发布 v1.0.0 | 🔴 高 | 2h | ⬜ |

---

## 4. 关键配置参考

### 4.1 docker-compose.yml (开发环境)

```yaml
version: "3.8"

services:
  consul:
    image: hashicorp/consul:latest
    container_name: grade-consul
    ports:
      - "8500:8500"
    command: agent -server -ui -bootstrap-expect=1 -client=0.0.0.0
    networks:
      - grade-network

  rabbitmq:
    image: rabbitmq:3-management
    container_name: grade-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
    networks:
      - grade-network

  redis:
    image: redis:7-alpine
    container_name: grade-redis
    ports:
      - "6379:6379"
    networks:
      - grade-network

  postgres:
    image: postgres:16
    container_name: grade-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: grade_admin
      POSTGRES_PASSWORD: grade_password
      POSTGRES_DB: grade_system
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-schemas.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - grade-network

networks:
  grade-network:
    driver: bridge

volumes:
  postgres-data:
```

### 4.2 init-schemas.sql

```sql
-- 创建各服务的 Schema
CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS academic_schema;
CREATE SCHEMA IF NOT EXISTS grade_schema;
CREATE SCHEMA IF NOT EXISTS analytics_schema;

-- 授权
GRANT ALL ON SCHEMA auth_schema TO grade_admin;
GRANT ALL ON SCHEMA user_schema TO grade_admin;
GRANT ALL ON SCHEMA academic_schema TO grade_admin;
GRANT ALL ON SCHEMA grade_schema TO grade_admin;
GRANT ALL ON SCHEMA analytics_schema TO grade_admin;
```

### 4.3 Gateway application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: grade-gateway
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
        health-check-interval: 10s
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: auth-service
          uri: lb://grade-auth-service
          predicates:
            - Path=/api/v1/auth/**, /api/v1/role/**
        - id: user-service
          uri: lb://grade-user-service
          predicates:
            - Path=/api/v1/user/**, /api/v1/student/**, /api/v1/teacher/**
        - id: academic-service
          uri: lb://grade-academic-service
          predicates:
            - Path=/api/v1/courses/**
        - id: grade-service
          uri: lb://grade-grade-service
          predicates:
            - Path=/api/v1/grades/**, /api/v1/concurrency/**
        - id: analytics-service
          uri: lb://grade-analytics-service
          predicates:
            - Path=/api/v1/dashboard/**
  
  session:
    store-type: redis
  data:
    redis:
      host: localhost
      port: 6379
```

### 4.4 微服务通用 application.yml 模板

```yaml
server:
  port: ${SERVICE_PORT:8081}

spring:
  application:
    name: ${SERVICE_NAME:grade-service}
  
  # Consul 服务发现
  cloud:
    consul:
      host: ${CONSUL_HOST:localhost}
      port: ${CONSUL_PORT:8500}
      discovery:
        service-name: ${spring.application.name}
        health-check-interval: 10s
  
  # 数据库
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/grade_system
    username: ${DB_USER:grade_admin}
    password: ${DB_PASSWORD:grade_password}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_schema: ${DB_SCHEMA:auth_schema}
  
  # Session 共享
  session:
    store-type: redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  
  # RabbitMQ (需要时启用)
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:admin}
    password: ${RABBITMQ_PASSWORD:admin123}
```

### 4.5 Dockerfile 模板

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.6 前端 Dockerfile

```dockerfile
FROM node:20-alpine AS builder

WORKDIR /app
COPY package.json bun.lockb ./
RUN npm install -g bun && bun install

COPY . .
RUN bun run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 4.7 前端 nginx.conf

```nginx
server {
    listen 80;
    server_name localhost;
    
    root /usr/share/nginx/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://grade-gateway:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Cookie $http_cookie;
        proxy_pass_header Set-Cookie;
    }
}
```

---

## 5. 交付物清单

### Docker 镜像 (共 8 个)

| 镜像名称 | 说明 |
|----------|------|
| `morethan987/grade-gateway:latest` | API 网关 |
| `morethan987/grade-auth-service:latest` | 认证服务 |
| `morethan987/grade-user-service:latest` | 用户服务 |
| `morethan987/grade-academic-service:latest` | 教务服务 |
| `morethan987/grade-grade-service:latest` | 成绩服务 |
| `morethan987/grade-analytics-service:latest` | 分析服务 |
| `morethan987/grade-frontend:latest` | 前端应用 |

### 交付文件

| 文件 | 说明 |
|------|------|
| `docker-compose.prod.yml` | 一键启动脚本 |
| `init-schemas.sql` | 数据库初始化 |
| `README.md` | 部署说明文档 |

### 老师使用方式

```bash
# 1. 下载配置文件
curl -O https://raw.githubusercontent.com/morethan987/GradeSystem/main/docker-compose.prod.yml
curl -O https://raw.githubusercontent.com/morethan987/GradeSystem/main/init-schemas.sql

# 2. 启动系统
docker compose -f docker-compose.prod.yml up -d

# 3. 访问
# 前端: http://localhost:3000
# API: http://localhost:8080/api/v1/...
# Consul UI: http://localhost:8500
# RabbitMQ UI: http://localhost:15672 (admin/admin123)
```

---

## 6. 时间线总览

```
Week 1-2   │ Phase 1: 基础设施搭建
           │ ├── Maven 多模块结构
           │ ├── Docker Compose (Consul/RabbitMQ/Redis/PostgreSQL)
           │ └── 数据库 Schema 初始化
           │
Week 3-4   │ Phase 2: Gateway + Auth Service
           │ ├── API Gateway 路由配置
           │ ├── Auth Service 迁移
           │ └── Spring Session Redis 配置
           │
Week 5-6   │ Phase 3: User Service
           │ ├── User/Student/Teacher 迁移
           │ └── Feign Client 跨服务调用
           │
Week 7-9   │ Phase 4: Academic + Grade Service
           │ ├── Course/TeachingClass 迁移
           │ ├── Grade 迁移
           │ └── RabbitMQ 事件驱动
           │
Week 10-11 │ Phase 5: Analytics + 前端
           │ ├── Dashboard 聚合服务
           │ └── 前端 Docker 化
           │
Week 12    │ Phase 6: CI/CD + 交付
           │ ├── GitHub Actions
           │ ├── Docker Hub 推送
           │ └── 最终测试 & 文档
```

---

## 7. 风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| Spring Cloud 与 Boot 4 兼容性问题 | 服务无法启动 | 使用 Consul (已验证稳定) |
| 跨服务事务一致性 | 数据不一致 | 采用 Saga 模式 + Outbox Pattern |
| Session 共享失败 | 认证失效 | 确保 Redis 高可用，配置合理超时 |
| Docker 镜像过大 | 部署缓慢 | 使用多阶段构建，基于 Alpine 镜像 |
| 服务间调用超时 | 请求失败 | 配置 Feign 熔断器和重试策略 |

---

*文档版本: v1.0*
*创建时间: 2026-01-17*
*作者: Sisyphus AI*
