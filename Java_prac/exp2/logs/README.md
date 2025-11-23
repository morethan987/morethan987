# 学生成绩管理系统 - 日志功能说明

## 概述

本系统集成了基于Java自带的logging功能的完整日志记录系统，用于记录系统运行状态、用户操作、错误信息等关键信息。

## 日志功能特性

### 1. 多级别日志记录
- **INFO**: 一般信息记录（用户操作、系统事件等）
- **WARNING**: 警告信息（登录失败、权限不足等）
- **SEVERE**: 严重错误（系统异常、数据库错误等）
- **FINE**: 调试信息（详细的执行流程）

### 2. 多种日志类型
- **系统事件日志**: 系统启动、关闭、模块初始化等
- **用户操作日志**: 登录、注册、菜单操作、功能调用等
- **认证日志**: 登录成功/失败、权限检查、会话管理等
- **数据库操作日志**: SQL执行、数据增删改查等

### 3. 输出方式
- **控制台输出**: 实时显示重要日志信息
- **文件记录**: 完整的日志记录保存到文件
- **格式化输出**: 统一的时间戳、级别、模块名称格式

## 日志文件结构

```
logs/
├── system.log          # 主要日志文件
├── system.0.log        # 日志轮转备份文件
├── system.1.log        # 更早的备份文件
└── README.md          # 本说明文档
```

## 日志格式说明

日志格式：`[日期 时间] [级别] [模块名] 日志内容`

示例：
```
[2024-01-15 14:30:25] [INFO   ] [App] 系统事件 - 系统启动: 学生成绩管理系统开始启动
[2024-01-15 14:30:26] [INFO   ] [AuthService] 认证事件 - 用户[admin] 登录: 成功
[2024-01-15 14:30:27] [INFO   ] [StudentScoreSystem] 用户操作 - 用户[session123] 执行操作: 查看学生列表, 详情: 无
```

## 系统内置日志查看功能

系统管理员可以通过以下菜单查看日志：

1. **主菜单** → **系统设置** → **日志管理**
2. 在日志管理菜单中选择：
   - 查看最新日志 (最近50条)
   - 查看用户操作日志
   - 查看系统事件日志
   - 查看认证日志
   - 查看错误日志
   - 显示日志文件信息

## 命令行日志查看工具

系统提供了独立的命令行日志查看工具：

### 基本用法
```bash
java -cp target/classes com.example.util.LogViewer [命令] [参数]
```

### 可用命令

#### 1. 查看最新日志
```bash
java -cp target/classes com.example.util.LogViewer latest [行数]
```
示例：`java -cp target/classes com.example.util.LogViewer latest 100`

#### 2. 按级别过滤日志
```bash
java -cp target/classes com.example.util.LogViewer level [级别] [行数]
```
示例：`java -cp target/classes com.example.util.LogViewer level SEVERE 50`

#### 3. 搜索关键词
```bash
java -cp target/classes com.example.util.LogViewer search [关键词] [行数]
```
示例：`java -cp target/classes com.example.util.LogViewer search "登录失败" 20`

#### 4. 查看今天的日志
```bash
java -cp target/classes com.example.util.LogViewer today
```

#### 5. 查看用户操作日志
```bash
java -cp target/classes com.example.util.LogViewer user [sessionId] [行数]
```
示例：`java -cp target/classes com.example.util.LogViewer user session123 50`

#### 6. 查看系统事件日志
```bash
java -cp target/classes com.example.util.LogViewer system [行数]
```

#### 7. 查看认证日志
```bash
java -cp target/classes com.example.util.LogViewer auth [行数]
```

#### 8. 查看数据库日志
```bash
java -cp target/classes com.example.util.LogViewer db [行数]
```

#### 9. 显示日志文件信息
```bash
java -cp target/classes com.example.util.LogViewer info
```

#### 10. 清空日志文件
```bash
java -cp target/classes com.example.util.LogViewer clear
```

## 日志配置

### 配置文件位置
- `src/main/resources/logging.properties`

### 主要配置项
```properties
# 根日志级别
.level = INFO

# 控制台输出级别
java.util.logging.ConsoleHandler.level = INFO

# 文件日志级别
java.util.logging.FileHandler.level = ALL

# 日志文件大小和轮转
java.util.logging.FileHandler.limit = 10000000  # 10MB
java.util.logging.FileHandler.count = 5         # 保留5个备份文件
```

### 自定义日志级别
可以为不同的模块设置不同的日志级别：
```properties
com.example.auth.level = INFO          # 认证模块
com.example.model.dao.level = DEBUG    # 数据访问层
com.example.session.level = INFO       # 会话管理
```

## 开发者使用指南

### 在代码中记录日志

#### 1. 导入日志工具类
```java
import com.example.util.LoggerUtil;
```

#### 2. 基本日志记录
```java
// 信息日志
LoggerUtil.info("操作完成");
LoggerUtil.info("用户 %s 执行了操作 %s", username, action);

// 警告日志
LoggerUtil.warning("配置文件未找到");

// 错误日志
LoggerUtil.error("数据库连接失败");
LoggerUtil.error("操作失败", exception);

// 调试日志
LoggerUtil.debug("变量值: %s", variable);
```

#### 3. 专用日志记录方法
```java
// 用户操作日志
LoggerUtil.logUserAction(sessionId, "查看学生列表", "显示所有学生");

// 系统事件日志
LoggerUtil.logSystemEvent("模块初始化", "学生管理模块启动完成");

// 认证事件日志
LoggerUtil.logAuthEvent("admin", "登录", true);

// 数据库操作日志
LoggerUtil.logDatabaseOperation("插入", "user", "新增用户: " + username);
```

## 日志文件维护

### 自动轮转
- 日志文件达到10MB时自动创建新文件
- 最多保留5个历史文件
- 旧文件自动压缩和删除

### 手动维护
```bash
# 查看日志文件大小
java -cp target/classes com.example.util.LogViewer info

# 清空当前日志（谨慎使用）
java -cp target/classes com.example.util.LogViewer clear
```

## 故障排除

### 常见问题

1. **日志文件无法创建**
   - 检查logs目录权限
   - 确保磁盘空间充足

2. **日志记录不完整**
   - 检查日志级别配置
   - 确认LoggerUtil已正确初始化

3. **性能影响**
   - 调整控制台日志级别到WARNING
   - 定期清理旧日志文件

### 调试模式
开启详细的调试日志：
```java
LoggerUtil.setLogLevel(Level.FINE);
```

## 最佳实践

1. **适度记录**: 记录关键操作和错误，避免过度记录影响性能
2. **敏感信息**: 不要记录密码、个人隐私等敏感信息
3. **错误处理**: 所有异常都应该记录详细的错误信息
4. **用户操作**: 重要的用户操作应该记录便于审计
5. **定期维护**: 定期检查和清理日志文件

## 更新历史

- 2024-01-15: 初始版本，包含基本日志记录功能
- 增加了用户操作、系统事件、认证等专用日志类型
- 集成了命令行日志查看工具
- 添加了系统内置的日志管理菜单