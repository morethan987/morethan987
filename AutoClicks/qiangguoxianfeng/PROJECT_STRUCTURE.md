# 强国先锋自动播放程序

## 项目结构

```
qiangguoxianfeng/
├── main.py                 # 主程序入口
├── config/                 # 配置模块
│   ├── __init__.py
│   └── selectors.py       # 页面元素选择器配置
├── core/                   # 核心业务逻辑
│   ├── __init__.py
│   └── player.py          # 自动播放器核心类
└── utils/                  # 工具模块
    ├── __init__.py
    ├── decorators.py      # 装饰器工具（重试等）
    ├── waiter.py          # 智能等待工具
    └── browser.py         # 浏览器检测工具
```

## 模块说明

### 1. `main.py` - 主入口
- 程序启动入口
- 用户交互界面
- 浏览器初始化和配置

### 2. `config/` - 配置模块
- **`selectors.py`**: 存储所有页面元素的 XPath 选择器
  - 入口按钮
  - 目录项选择器
  - 播放列表选择器
  - 弹窗按钮选择器

### 3. `core/` - 核心模块
- **`player.py`**: 核心播放器类 `QiangGuoPlayer`
  - `play_videos()`: 主播放逻辑
  - `safe_click()`: 安全点击操作
  - `wait_for_video_complete()`: 等待视频完成
  - `handle_popups()`: 处理弹窗
  - `check_incomplete_item()`: 检查未完成项目
  - `is_all_complete()`: 检查是否全部完成

### 4. `utils/` - 工具模块
- **`decorators.py`**: 装饰器工具
  - `retry_on_failure()`: 失败重试装饰器

- **`waiter.py`**: 智能等待工具类 `SmartWaiter`
  - `wait_for_element()`: 等待元素出现
  - `wait_for_element_disappear()`: 等待元素消失
  - `wait_for_condition()`: 等待条件满足

- **`browser.py`**: 浏览器相关工具
  - `detect_available_browser()`: 检测可用浏览器
  - `get_driver_path()`: 获取驱动路径

## 使用方法

```bash
python main.py
```

## 模块化优势

1. **清晰的职责分离**
   - 配置与代码分离
   - 业务逻辑与工具函数分离
   - 易于维护和扩展

2. **代码复用性**
   - 工具函数可在其他项目中复用
   - 装饰器和等待工具通用性强

3. **易于测试**
   - 每个模块可独立测试
   - 单一职责原则

4. **易于理解**
   - 新手可快速定位功能
   - 代码结构一目了然

## 维护建议

- 修改页面选择器：编辑 `config/selectors.py`
- 调整播放逻辑：编辑 `core/player.py`
- 添加新工具函数：在 `utils/` 下创建新模块
- 修改用户界面：编辑 `main.py`
