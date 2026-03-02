# AutoClicks Rod

用 Go + [Rod](https://go-rod.github.io/) 浏览器自动化库实现的重庆大学选课系统自动轮询抢课工具。

> 本项目是 Python (pydoll) 版本的 Go 重写，保留了核心选课逻辑，并增加了 slog 结构化日志、优雅退出、指数退避重试、CLI 参数、YAML 配置（含 CSS 选择器外置）等工程增强。

---

## 功能特性

- **一键初始化** `autoclicks init` 生成默认配置文件模板
- **后台守护模式** `autoclicks daemon` 不阻塞终端，日志写入文件
- **优雅停止** `autoclicks stop` 终止后台守护进程
- **自动登录** 重庆大学选课系统
- **轮询选课** 按设定间隔刷新页面，持续检测目标课程
- **三重匹配** 课程名 + 课程号 + 教师姓名，精确匹配避免误选
- **可选性检测** 跳过已满或已选课程，继续轮询
- **微信通知** 选课成功后通过 [Server酱](https://sct.ftqq.com/) 发送微信推送
- **图片屏蔽** 自动屏蔽页面图片请求，加速加载
- **优雅退出** 收到 `Ctrl+C` 后正确关闭浏览器，不留残留进程
- **指数退避重试** 网络/浏览器崩溃时自动重试，退避上限 30 秒
- **缓存清理** `autoclicks clean` 清理 Rod 会话缓存及浏览器二进制
- **YAML 配置** 所有参数（包括 CSS 选择器）外置到配置文件，网站改版只需改配置

---

## 项目结构

```
AutoClicks_Rod/
├── cmd/
│   ├── main.go                  # 程序入口：CLI、信号处理、重试循环、daemon/stop
│   ├── proc_unix.go             # Unix/macOS 进程分离与终止
│   └── proc_windows.go          # Windows 进程分离与终止
├── internal/
│   ├── config/
│   │   └── config.go            # YAML 配置加载与验证
│   ├── browser/
│   │   ├── browser.go           # 浏览器启动、图片屏蔽
│   │   ├── login.go             # 登录流程
│   │   └── course.go            # 课程查找、匹配、选课、确认
│   └── notification/
│       └── serverchan.go        # Server酱微信推送
├── config.yaml                  # 配置文件模板（编辑此文件填入你的信息）
├── go.mod
└── go.sum
```

---

## 快速开始

### 1. 编译

```bash
# 动态编译
go build -o output/autoclicks ./cmd/...

# 静态链接编译
CGO_ENABLED=0 go build -o output/autoclicks ./cmd/...

# 交叉编译 for windows
CGO_ENABLED=0 GOOS=windows GOARCH=amd64 go build -o output/autoclicks.exe ./cmd/...
```

> Rod 会在首次运行时自动下载适配版本的 Chromium，请确保网络畅通。下载完成后无需重复下载。

### 2. 配置

复制并编辑 `config.yaml`：

```yaml
username: "你的学号"
password: "你的密码"
url: "https://my.cqu.edu.cn/enroll/CourseStuSelectionList"
interval: 3          # 轮询间隔（秒）
max_retry: 9999      # 最大重试次数
headless: true       # true=无头后台，false=可见浏览器窗口
server_key: ""       # Server酱 SCKEY，留空则不发通知

courses:
  - name: "边缘计算"      # 课程名（精确匹配）
    id: "CST31220"       # 课程号（精确匹配）
    teachers:
      - "汪成亮"          # 目标教师（精确匹配，可填多个）
```

### 3. 运行

```bash
# 前台运行（Ctrl+C 退出）
./autoclicks

# 指定配置文件路径
./autoclicks --config /path/to/config.yaml

# 强制无头模式（覆盖配置文件中的 headless 值）
./autoclicks --headless
```

### 4. 后台运行（daemon 模式）

不阻塞终端，日志自动写入文件：

```bash
# 启动守护进程（日志默认写入 autoclicks.log）
./autoclicks daemon

# 指定配置文件和日志路径
./autoclicks daemon --config /path/to/config.yaml --log /var/log/ac.log
```

启动后输出：
```
守护进程已启动
  PID:  12345
  日志: /path/to/autoclicks.log
  配置: /path/to/config.yaml

停止方法：
  autoclicks stop
  autoclicks stop --pid 12345
```

### 5. 停止后台进程

```bash
# 自动读取 .pid 文件终止
./autoclicks stop

# 指定 PID 终止
./autoclicks stop --pid 12345

# 指定日志路径查找 .pid 文件
./autoclicks stop --log /var/log/ac.log
```

### 6. 缓存清理

```bash
# 清理 Rod 会话缓存
./autoclicks clean

# 同时删除已下载的 Chromium 浏览器（约 500MB）
./autoclicks clean --all
```

### 7. 停止前台运行

按 `Ctrl+C`，程序会优雅关闭浏览器后退出。
---

## 手动测试指南

### 准备工作

1. 在 `config.yaml` 中填入**真实**的学号和密码
2. 将目标课程改为一门**你知道存在的课程**（不需要是真正要抢的课）
3. 编译二进制：`go build -o autoclicks ./cmd/...`

---

### 测试一：无头模式（headless: true）

适用场景：验证程序核心逻辑，日常使用模式。

**配置文件设置：**
```yaml
headless: true
interval: 5
```

**启动命令：**
```bash
./autoclicks --config config.yaml 2>&1 | tee test-headless.log
```

**期望日志输出（按时间顺序）：**

```
INFO Config loaded url=https://... courses=1 interval=5 headless=true
INFO Starting run attempt=1
INFO Browser launched headless=true
INFO Navigating to course selection URL url=https://...
WARN WaitLoad timed out, proceeding to element wait ...   ← SPA 正常现象，可忽略
INFO Page load complete, locating login elements
INFO Login elements located, entering credentials
INFO Login credentials submitted, waiting for redirect to course page
INFO Waiting for course page to load selector=span.ant-table-column-title
INFO Course page loaded successfully
INFO Course scan complete targets=1 found=N             ← N 取决于该课程是否在当前列表
INFO Poll round complete, waiting before next refresh interval_s=5
INFO Waiting for course page to load ...               ← 第二轮轮询开始
...（持续循环）
```

**终止测试：**
```bash
Ctrl+C
```

**期望退出日志：**
```
INFO Shutdown signal received, stopping poll loop
INFO Browser closed and cleaned up
INFO Run completed successfully
```

**验证点：**
- [ ] 程序启动后立即输出 `Config loaded`
- [ ] 出现 `Browser launched headless=true`（不弹出浏览器窗口）
- [ ] 出现 `Login elements located`（说明登录页面元素找到，账号密码正确）
- [ ] 出现 `Course page loaded successfully`（成功跳转到选课页）
- [ ] `Ctrl+C` 后出现 `Browser closed and cleaned up`，程序干净退出
- [ ] 验证无残留进程：`pgrep -c chrome` 应返回 0

---

### 测试二：可见模式（headless: false）

适用场景：调试选择器、观察实际页面行为、确认登录流程。

**配置文件设置：**
```yaml
headless: false
interval: 10
```

**或者不修改配置文件，用命令行参数强制无头为关：**
```bash
# 注意：--headless flag 只能强制开启无头，无法强制关闭
# 若要可见模式，请在 config.yaml 中设置 headless: false
./autoclicks --config config.yaml
```

**期望行为（视觉观察）：**

| 阶段 | 预期看到的内容 |
|------|---------------|
| 启动后 1~3 秒 | 弹出 Chromium 浏览器窗口，导航到 CQU 统一认证登录页 |
| 约 5~20 秒 | 自动在用户名框输入学号，密码框输入密码，点击登录按钮 |
| 登录成功后 | 页面跳转到选课系统主界面，显示课程表格 |
| 每轮轮询 | 程序扫描课程列表，若找到目标课程则点击进入侧边栏 |
| 每次 interval 到期 | 页面自动刷新，开始新一轮扫描 |
| 按 Ctrl+C | 浏览器窗口自动关闭 |

**日志同时输出：** 与无头模式相同，只是增加了可见窗口。

**验证点：**
- [ ] 浏览器窗口弹出（不是无头）
- [ ] 能看到自动填写账号密码的过程
- [ ] 登录后跳转到选课页面
- [ ] 页面按 `interval` 设定的秒数定期刷新
- [ ] `Ctrl+C` 后浏览器窗口关闭

---

### 测试三：配置错误检测

验证配置校验是否正常工作。

```bash
# 测试缺少用户名
cat > /tmp/bad-config.yaml << 'EOF'
password: "test"
url: "https://example.com"
interval: 3
max_retry: 3
headless: true
courses:
  - name: "test"
    id: "TEST001"
    teachers: ["张三"]
selectors:
  login:
    username_input: "input"
    password_input: "input"
    login_button: "button"
  course:
    flag: "span"
    data_row: "tr"
  sidebar:
    sidebar_flag: "div"
    checkbox: "input"
    close_button: "svg"
    data_row: "tr"
    full_flag: "span"
    selected_flag: "span"
  selection:
    select_button: "button"
    confirm_button: "button"
EOF

./autoclicks --config /tmp/bad-config.yaml
```

**期望输出：**
```
ERROR Failed to load config path=/tmp/bad-config.yaml error="config: username is required"
```
程序以非零退出码（exit 1）退出。

---

### 测试四：重试与退避

验证基础设施错误触发指数退避重试（**可选，需断网环境**）：

1. 断开网络连接
2. 运行程序
3. 观察日志

**期望日志：**
```
INFO Starting run attempt=1
ERROR Run failed, retrying attempt=1 max=9999 backoff=1s error="..."
INFO Starting run attempt=2
ERROR Run failed, retrying attempt=2 max=9999 backoff=2s error="..."
INFO Starting run attempt=3
ERROR Run failed, retrying attempt=3 max=9999 backoff=4s error="..."
...（退避时间翻倍，上限 30s）
```

---

## Server酱微信通知配置

1. 访问 [sct.ftqq.com](https://sct.ftqq.com/) 注册并获取 SendKey
2. 在 `config.yaml` 中填入：
   ```yaml
   server_key: "你的SendKey"
   ```
3. 选课成功后，微信会收到「选课成功通知」消息

支持两种 Key 格式：
- 标准格式：`SCT...` → 推送到 `sctapi.ftqq.com`
- SCTP 格式：`sctp...t...` → 推送到对应节点

---

## CSS 选择器说明

`config.yaml` 中的 `selectors` 部分包含所有页面元素的 CSS 选择器。当重庆大学选课系统改版导致程序失效时，**只需更新选择器，无需重新编译**。

调试选择器的方法：
1. 用可见模式启动程序（`headless: false`）
2. 在浏览器开发者工具（F12）的 Console 中测试：
   ```javascript
   document.querySelector("你的选择器")
   ```
3. 确认找到元素后更新 `config.yaml` 对应字段

---

## 常见问题

**Q: 首次运行很慢，等了很久？**  
A: Rod 在首次运行时会自动下载 Chromium（约 150MB），下载完成后后续运行正常。

**Q: 日志出现 `WaitLoad timed out`，是出错了吗？**  
A: 不是。这是正常现象。重庆大学选课系统是 SPA（React/Ant Design），`WaitLoad` 事件触发时机与实际内容渲染不一致，程序会自动降级到元素等待模式继续运行。

**Q: 登录后一直显示 `Waiting for course page to load` 超时？**  
A: 可能是 CSS 选择器失效（网站改版）。用可见模式启动，用浏览器开发者工具检查 `span.ant-table-column-title` 是否存在，并更新 `config.yaml` 中的 `selectors.course.flag`。

**Q: 找到课程但没有选课操作？**  
A: 可能不在选课时间段内（`confirm_selection` 找不到选课按钮），日志会显示 `Select button not found — may not be enrollment window`，程序正常继续轮询。

**Q: 如何同时监控多门课？**  
A: 在 `config.yaml` 的 `courses` 列表中添加多个条目即可，程序每轮都会扫描全部目标课程。

**Q: daemon 模式启动后如何查看日志？**  
A: 日志默认写入 `autoclicks.log`（可用 `--log` 指定路径）。使用 `tail -f autoclicks.log` 实时查看。PID 文件存储在 `<日志路径>.pid`。

**Q: daemon 进程意外退出了怎么办？**  
A: 查看日志文件末尾的错误信息。如果 `.pid` 文件残留，`autoclicks stop` 会自动清理。重新运行 `autoclicks daemon` 即可。

**Q: Windows 上如何使用 daemon 模式？**  
A: 使用方法完全相同。Windows 上 `stop` 命令会强制终止进程（Windows 不支持 SIGTERM），效果等同于 `taskkill /PID`。

**Q: `autoclicks clean --all` 后需要重新下载浏览器吗？**  
A: 是的。`--all` 会删除 Rod 缓存的 Chromium 二进制（约 500MB），下次运行时会重新自动下载。不加 `--all` 只清理会话缓存，不影响浏览器。
