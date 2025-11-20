# 选课系统重构说明

## 概述

原始的选课系统将具体的监控和处理逻辑直接内置在 `CourseSelectionSystem` 类中，这使得系统缺乏灵活性。重构后的系统采用依赖注入的方式，将监控和处理逻辑抽象化，让用户可以根据需要自定义实现。

## 架构变化

### 重构前
```
CourseSelectionSystem (硬编码逻辑)
├── _monitor_courses()           # 内置监控逻辑
└── _handle_available_courses()  # 内置处理逻辑
```

### 重构后
```
CourseSelectionSystem (通用框架)
├── CourseMonitorInterface (抽象接口)
│   └── monitor_courses()
├── CourseHandlerInterface (抽象接口)
│   └── handle_available_courses()
└── 底层服务 (core/services/)
    ├── CourseMonitorService
    ├── CourseSelectionService
    ├── ElementLocatorService
    └── 其他服务...
```

## 核心组件

### 1. 抽象接口 (`core/interfaces/`)
- **CourseMonitorInterface**: 定义课程监控行为的抽象接口
- **CourseHandlerInterface**: 定义课程处理行为的抽象接口

### 2. 默认实现 (`core/implementations/`)
- **DefaultCourseMonitor**: 包含原始监控逻辑的默认实现
- **DefaultCourseHandler**: 包含原始处理逻辑的默认实现

### 3. 底层服务 (`core/services/`)
- **CourseMonitorService**: 课程状态检查
- **CourseSelectionService**: 选课操作执行
- **ElementLocatorService**: 网页元素定位
- **LoginService**: 登录管理
- **NotificationService**: 通知管理
- **WebDriverService**: 浏览器驱动管理

## 使用方式

### 方式1: 使用默认实现（与原系统相同）
```python
from core.course_selection_system import CourseSelectionSystem
from core.implementations import DefaultCourseMonitor, DefaultCourseHandler
from utils.config_loader import ConfigLoader

config = ConfigLoader.load_config()
monitor = DefaultCourseMonitor()
handler = DefaultCourseHandler()

system = CourseSelectionSystem(config, monitor, handler)
system.start()
```

### 方式2: 自定义监控逻辑
```python
class CustomMonitor(CourseMonitorInterface):
    def monitor_courses(self, sb, config):
        # 实现自定义监控逻辑
        # 可以使用 core/services 中的底层服务
        pass

monitor = CustomMonitor()
handler = DefaultCourseHandler()
system = CourseSelectionSystem(config, monitor, handler)
```

### 方式3: 完全自定义
```python
class CustomMonitor(CourseMonitorInterface):
    # 自定义监控逻辑
    pass

class CustomHandler(CourseHandlerInterface):
    # 自定义处理逻辑
    pass

system = CourseSelectionSystem(config, CustomMonitor(), CustomHandler())
```

### 方式4: 直接使用底层服务
```python
from core.services.course_monitor_service import CourseMonitorService
from core.services.course_selection_service import CourseSelectionService

# 直接使用服务组装自己的逻辑
monitor_service = CourseMonitorService()
selection_service = CourseSelectionService()

# 自行组装业务逻辑...
```

## 示例代码

查看 `examples/basic_usage_example.py` 文件，其中包含了：

1. **默认实现示例**: 展示如何使用默认实现
2. **自定义监控示例**: 实现优先级监控逻辑
3. **完全自定义示例**: 实现限制选课数量的逻辑
4. **服务组装示例**: 直接使用底层服务

## 重构的优势

### 1. 灵活性提升
- 用户可以根据需要实现自定义监控和处理逻辑
- 可以轻松替换系统的任何部分

### 2. 代码复用
- 底层服务可以在不同实现中重复使用
- 默认实现保证了向后兼容性

### 3. 测试友好
- 可以为不同的组件单独编写测试
- 可以使用模拟对象进行单元测试

### 4. 易于扩展
- 新的监控策略或处理逻辑可以作为新的实现类添加
- 不需要修改核心系统代码

## 兼容性

- ✅ **完全向后兼容**: 使用默认实现时，系统行为与原始版本完全相同
- ✅ **配置兼容**: 无需修改现有配置文件
- ✅ **依赖兼容**: 所有原有依赖保持不变

## 迁移指南

### 从原始系统迁移到重构系统

1. **保持现有行为**:
   ```python
   # 旧版本
   system = CourseSelectionSystem(config)
   
   # 新版本（相同行为）
   system = CourseSelectionSystem(config, DefaultCourseMonitor(), DefaultCourseHandler())
   ```

2. **逐步自定义**:
   - 先使用默认实现确保系统正常运行
   - 然后根据需要逐步替换为自定义实现

3. **利用底层服务**:
   - 在自定义实现中重用现有的底层服务
   - 避免重复实现基础功能

## 最佳实践

1. **继承默认实现**: 如果只需要修改部分逻辑，可以继承默认实现类并重写特定方法
2. **使用底层服务**: 在自定义实现中尽量使用现有的服务，避免重复造轮子
3. **错误处理**: 在自定义实现中添加适当的错误处理逻辑
4. **日志记录**: 使用统一的日志记录格式，便于调试和监控