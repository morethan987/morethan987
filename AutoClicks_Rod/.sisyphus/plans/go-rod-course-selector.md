# Go + Rod 自动选课系统

## TL;DR

> **Quick Summary**: 用 Go + Rod 浏览器自动化库重写 Python (pydoll) 自动轮询选课系统，采用 YAML 配置（含 CSS 选择器外置）、slog 日志、优雅退出、指数退避重试等 Go 惯用增强。
> 
> **Deliverables**:
> - 可编译运行的 Go 二进制：`autoclicks`
> - YAML 配置文件模板：`config.yaml`
> - 多 package 项目结构：`cmd/` + `internal/config/` + `internal/browser/` + `internal/notification/`
> 
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 → Task 2 → Task 3 → Task 5 → Task 7 → Task 8 → F1-F4

---

## Context

### Original Request
用 Go 语言的 Rod 库重新实现 `py_ref/` 目录下的 Python (pydoll) 自动轮询选课系统。

### Interview Summary
**Key Discussions**:
- **配置方式**: YAML 配置文件，包括 CSS 选择器也外置（网站改版时只需改配置，不用重新编译）
- **项目结构**: 多 package — `cmd/main.go` + `internal/config/` + `internal/browser/` + `internal/notification/`
- **功能增强**: 适度增强 — slog 日志、优雅退出(signal handling)、命令行参数、configurable headless
- **错误处理**: 优雅重试 — 指数退避 + 最大重试次数 + 日志记录每次重试
- **通知**: 保留 Server酱微信推送
- **调试**: 支持非 headless 可见模式（YAML 可配置）
- **测试**: 无单元测试，Agent-Executed QA

**Research Findings**:
- Rod `MustElement(css)` / `Element(css)` 定位元素，`Elements(css)` 返回全部匹配
- Rod `page.MustWaitStable()` 等待 DOM 稳定（非网络空闲，需配合元素等待兜底）
- Rod `page.Has(selector)` 返回 `(bool, *Element, error)` — 对应 Python 的 `raise_exc=False`
- Rod `page.Race()` 竞争选择器 — 适合登录成功/失败检测
- Rod `page.HijackRequests()` 拦截请求屏蔽图片 — 需 `go router.Run()` 启动
- Rod `page.Timeout(duration)` 返回克隆 page，不修改原始对象
- Rod `launcher.New().Headless(bool)` + `defer l.Cleanup()` 管理浏览器生命周期

### Metis Review
**Identified Gaps** (addressed):
- **WaitStable() ≠ 网络空闲**: Rod 的 `WaitStable()` 检测 DOM 稳定而非网络静默。对于重度 SPA（Ant Design/React），可能不够可靠。兜底策略：链式 `WaitLoad()` + `WaitStable()` + 直接元素等待（带充足超时）
- **Must* vs non-Must 纪律**: Must* 方法 panic on error。生产代码应用 non-Must（error-returning）方法处理所有可能合理失败的操作
- **Python 死代码**: `SelectionSelectors.forbidden_flag` 和 `select_button`(dict) 从未使用 → 不迁移
- **Python 内联选择器**: `_is_available()` 中的 `span.text-error` / `span.text-success` 未在 config 类中定义 → Go 版统一到 YAML
- **两级错误策略**: 基础设施错误(浏览器崩溃/网络断开) → 指数退避重试；业务逻辑结果(课程已满/已选/非选课时间) → 正常日志+继续轮询
- **僵尸 Chromium 进程**: `defer l.Cleanup()` + signal handling 处理正常退出；异常退出作为已知限制记录
- **CSS 选择器转换**: Python 的 `tag_name`/`class_name` 属性查找需转为标准 CSS 选择器语法
- **ServerChan URL 路由**: `sctp` 前缀和标准前缀需完整移植

---

## Work Objectives

### Core Objective
构建一个功能完整的 Go 自动选课工具，通过 Rod 驱动 Chrome 浏览器自动登录、轮询、匹配并选择目标课程，成功后通过 Server酱发送微信通知。

### Concrete Deliverables
- `cmd/main.go` — 程序入口，orchestration
- `internal/config/config.go` — YAML 配置加载与验证
- `internal/browser/browser.go` — 浏览器生命周期管理（启动、清理、图片屏蔽）
- `internal/browser/login.go` — 登录流程
- `internal/browser/course.go` — 课程查找、匹配、选课、确认
- `internal/notification/serverchan.go` — Server酱通知
- `config.yaml` — 配置文件模板（含默认选择器和示例课程）
- `go.mod` + `go.sum` — Go module 文件

### Definition of Done
- [ ] `go build ./cmd/...` 成功编译，零错误
- [ ] `go vet ./...` 通过
- [ ] 二进制启动后能正确加载 YAML 配置
- [ ] 发送 SIGINT 后浏览器进程正确清理
- [ ] 所有 Python 核心流程在 Go 版中有对应实现

### Must Have
- 完整的登录→轮询→选课→确认→通知流程
- YAML 配置文件（用户凭据、目标课程、选择器、轮询间隔、重试次数、headless 开关、Server酱 key）
- slog 结构化日志
- 优雅退出（SIGINT/SIGTERM → 关闭浏览器 → 清理临时文件）
- 顶层重试循环 + 指数退避（仅基础设施错误触发）
- 图片屏蔽（HijackRequests）加速页面加载
- 课程匹配：名称 + 课程号 + 教师姓名三重匹配
- 课程可选性检查（已满/已选检测）

### Must NOT Have (Guardrails)
- **NO interfaces/abstraction layers** — 不创建 `BrowserDriver`、`NotificationProvider`、`SelectorEngine` 等接口。代码直接调用 Rod 和 HTTP
- **NO per-operation retry** — 只有顶层重试循环。单个操作失败 → 跳出到顶层重试
- **NO custom error types** — 使用 `fmt.Errorf("context: %w", err)` + `errors.Is()`/`errors.As()`
- **NO fuzzy/regex matching** — 课程名和课程号精确匹配
- **NO multi-account concurrency** — 单账号运行
- **NO Web UI / database / Docker** — 不在本次范围
- **NO `time.Sleep()` for page state** — 使用 Rod 内置等待方法
- **NO unit tests** — QA 通过 agent-executed 场景验证
- **NO dead code migration** — Python 中未使用的 `forbidden_flag` 和 `select_button`(dict) 不迁移
- **NO log rotation / log middleware** — slog 单 handler，直接输出到 stderr
- **NO subcommands / complex CLI** — 仅 `--config` 和可选 `--headless` flag

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: NO (新项目)
- **Automated tests**: NO
- **Framework**: none
- **QA Policy**: Every task includes agent-executed QA scenarios

### QA Policy
Every task MUST include agent-executed QA scenarios.
Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

- **Build verification**: `go build`, `go vet`
- **Config validation**: 测试有效/无效配置的加载行为
- **Signal handling**: 启动二进制 → SIGINT → 验证清理
- **Code review**: 与 Python 参考逐函数对比

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — scaffolding + independent modules):
├── Task 1: Go module 初始化 + 项目骨架 [quick]
├── Task 2: YAML 配置模块 (config.go + config.yaml 模板) [unspecified-high]
├── Task 3: Server酱通知模块 (serverchan.go) [quick]
└── Task 4: 浏览器生命周期管理 (browser.go — 启动/清理/图片屏蔽) [unspecified-high]

Wave 2 (After Wave 1 — core browser automation):
├── Task 5: 登录流程 (login.go) [depends: 2, 4] [unspecified-high]
├── Task 6: 课程查找与匹配 (course.go — findTargetCourses) [depends: 2, 4] [unspecified-high]
└── Task 7: 侧边栏操作与选课确认 (course.go — selectCourse + confirmSelection) [depends: 2, 4, 6] [unspecified-high]

Wave 3 (After Wave 2 — orchestration + integration):
├── Task 8: 主程序 orchestration (cmd/main.go — 重试循环/信号处理/CLI) [depends: 2, 3, 4, 5, 6, 7] [deep]
└── Task 9: 端到端集成验证 + 编译确认 [depends: ALL] [unspecified-high]

Wave FINAL (After ALL tasks — independent review, 4 parallel):
├── Task F1: Plan compliance audit (oracle)
├── Task F2: Code quality review (unspecified-high)
├── Task F3: Real manual QA (unspecified-high)
└── Task F4: Scope fidelity check (deep)

Critical Path: Task 1 → Task 2 → Task 5 → Task 7 → Task 8 → Task 9 → F1-F4
Parallel Speedup: ~50% faster than sequential
Max Concurrent: 4 (Wave 1)
```

### Dependency Matrix

| Task | Depends On | Blocks | Wave |
|------|-----------|--------|------|
| 1 | — | 2, 3, 4 | 1 |
| 2 | 1 | 5, 6, 7, 8 | 1 |
| 3 | 1 | 8 | 1 |
| 4 | 1 | 5, 6, 7, 8 | 1 |
| 5 | 2, 4 | 8 | 2 |
| 6 | 2, 4 | 7, 8 | 2 |
| 7 | 2, 4, 6 | 8 | 2 |
| 8 | 2, 3, 4, 5, 6, 7 | 9 | 3 |
| 9 | ALL | F1-F4 | 3 |
| F1-F4 | 9 | — | FINAL |

### Agent Dispatch Summary

- **Wave 1**: **4** — T1 → `quick`, T2 → `unspecified-high`, T3 → `quick`, T4 → `unspecified-high`
- **Wave 2**: **3** — T5 → `unspecified-high`, T6 → `unspecified-high`, T7 → `unspecified-high`
- **Wave 3**: **2** — T8 → `deep`, T9 → `unspecified-high`
- **FINAL**: **4** — F1 → `oracle`, F2 → `unspecified-high`, F3 → `unspecified-high`, F4 → `deep`

---

## TODOs

> Implementation + Test = ONE Task. Never separate.
> EVERY task MUST have: Recommended Agent Profile + Parallelization info + QA Scenarios.

- [ ] 1. Go Module 初始化 + 项目骨架

  **What to do**:
  - 初始化 Go module：`go mod init github.com/morethan987/AutoClicks_Rod`
  - 创建目录结构：`cmd/`、`internal/config/`、`internal/browser/`、`internal/notification/`
  - 创建 `cmd/main.go` 最小 stub（package main + func main() + placeholder log）
  - 安装核心依赖：`go get github.com/go-rod/rod` + `go get gopkg.in/yaml.v3`
  - 确保 `go build ./cmd/...` 能成功编译 stub

  **Must NOT do**:
  - 不要在 stub 中写任何实际业务逻辑
  - 不要创建 internal 包下的任何 .go 文件（仅创建目录）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 纯脚手架任务，创建目录和最小文件
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (first task)
  - **Blocks**: Tasks 2, 3, 4
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References**:
  - `py_ref/config.py:1-10` — Python 版的配置常量结构，了解需要哪些配置项
  - `py_ref/main.py:1-16` — Python 版的导入结构，了解模块划分

  **External References**:
  - Rod 官方文档：`https://go-rod.github.io/`

  **WHY Each Reference Matters**:
  - config.py 的前 10 行展示配置字段（用户名/密码/URL/间隔/重试/ServerKey），指导 Go 配置结构
  - main.py 的导入展示模块划分（config/notification/browser/utils），映射到 Go internal 包结构

  **Acceptance Criteria**:
  - [ ] `go build ./cmd/...` 编译成功，exit code 0
  - [ ] 目录 `internal/config/`、`internal/browser/`、`internal/notification/` 存在
  - [ ] `go.mod` 包含 module path `github.com/morethan987/AutoClicks_Rod`
  - [ ] `go.mod` 包含 `github.com/go-rod/rod` 和 `gopkg.in/yaml.v3` 依赖

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Go module 初始化成功
    Tool: Bash
    Preconditions: 项目根目录存在
    Steps:
      1. 运行 `go build ./cmd/...`
      2. 运行 `grep 'module github.com/morethan987/AutoClicks_Rod' go.mod`
      3. 运行 `grep 'go-rod/rod' go.mod`
      4. 运行 `ls -d internal/config internal/browser internal/notification`
    Expected Result: 所有命令 exit code 0
    Failure Indicators: 编译错误、目录不存在、go.mod 缺少依赖
    Evidence: .sisyphus/evidence/task-1-go-module-init.txt
  ```

  **Commit**: YES
  - Message: `chore: initialize Go module and project skeleton`
  - Files: `go.mod`, `go.sum`, `cmd/main.go`, `internal/` dirs
  - Pre-commit: `go build ./cmd/...`

- [ ] 2. YAML 配置模块

  **What to do**:
  - 创建 `internal/config/config.go`：
    - 定义 `Config` struct（YAML tags）：Username, Password, URL, Interval(int), MaxRetry(int), Headless(bool), ServerKey(string), Courses([]TargetCourse), Selectors(SelectorConfig)
    - SelectorConfig 含 Login/Course/Sidebar/Selection 四组 sub-struct，每个字段为 CSS 选择器字符串
    - SidebarSelectors 需包含 FullFlag 和 SelectedFlag 两个新字段（Python 版内联在代码中）
    - `Load(path string) (*Config, error)` 函数：os.ReadFile → yaml.Unmarshal → 验证必填字段
    - 验证：Username/Password/URL/Courses 非空，Interval > 0，MaxRetry > 0
  - 创建 `config.yaml` 模板（项目根目录）：含所有配置项示例值、中文注释、从 Python 转换的 CSS 选择器
  
  **选择器转换对照**（Python → CSS）：
  - `{tag_name: input, name: username}` → `input[name='username']`
  - `{tag_name: input, type: password}` → `input[type='password']`
  - `{tag_name: button, type: submit, class_name: 'login-button ant-btn'}` → `button[type='submit'].login-button.ant-btn`
  - `{tag_name: span, class_name: 'ant-table-column-title'}` → `span.ant-table-column-title`
  - `{tag_name: tr, class_name: 'ant-table-row ant-table-row-level-0'}` → `tr.ant-table-row.ant-table-row-level-0`
  - sidebar_flag_css → `div.ant-drawer-body tbody.ant-table-tbody`
  - `{tag_name: input, type: checkbox}` → `input[type='checkbox']`
  - close_button_css → `div.drawer-close-wrap-right svg`
  - data_raw_css → `div.ant-drawer-body tbody.ant-table-tbody tr.ant-table-row.ant-table-row-level-0`
  - Python _is_available 内联 `span.text-error` → SidebarSelectors.FullFlag
  - Python _is_available 内联 `span.text-success` → SidebarSelectors.SelectedFlag
  - select_button_css → `div.ant-drawer-body button`
  - confirm_button_css → `.ant-modal button.ant-btn.ant-btn-primary`

  **Must NOT do**: 不创建 config 接口；不超过 2 层 YAML 嵌套；不添加环境变量覆盖

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high` — 需准确 struct 定义和选择器转换
  - **Skills**: []

  **Parallelization**: Wave 1 | Parallel with T3, T4 | Blocks: T5-T8 | Blocked By: T1

  **References**:
  - `py_ref/config.py:1-80` — 完整文件，所有配置常量和选择器类，1:1 映射源
  - `py_ref/main.py:146-154` — _is_available() 中内联选择器，需补充到 YAML

  **Acceptance Criteria**:
  - [ ] `internal/config/config.go` 存在并可编译
  - [ ] `config.yaml` 包含所有配置项示例值
  - [ ] Config struct YAML tag 与 config.yaml key 一一对应
  - [ ] Load() 对缺少必填字段返回有意义错误
  - [ ] 所有 Python 选择器已转换为 CSS 并记录在 config.yaml

  **QA Scenarios (MANDATORY):**
  ```
  Scenario: 有效配置加载成功
    Tool: Bash
    Steps: 创建临时 Go 程序调用 config.Load("config.yaml")，验证 Courses 长度 > 0
    Expected: exit 0，输出含课程名称
    Evidence: .sisyphus/evidence/task-2-config-valid.txt

  Scenario: 无效配置报错
    Tool: Bash
    Steps: 用缺少 username 的 YAML 调用 Load()
    Expected: exit 非零，stderr 含 "username" 错误
    Evidence: .sisyphus/evidence/task-2-config-invalid.txt
  ```

  **Commit**: YES (groups with T3)
  - Message: `feat(config): add YAML configuration and ServerChan notification modules`
  - Pre-commit: `go build ./cmd/...`

- [ ] 3. Server酱通知模块

  **What to do**:
  - 创建 `internal/notification/serverchan.go`：
    - `Send(serverKey, title, description string) error` 函数
    - URL 路由完整移植自 Python notification.py：
      - `sctp` 前缀 key → `https://{num}.push.ft07.com/send/{key}.send`（regexp 提取数字）
      - 标准 key → `https://sctapi.ftqq.com/{key}.send`
    - HTTP POST JSON：`{"title": ..., "desp": ...}`，Content-Type: `application/json;charset=utf-8`
    - 使用 `net/http` 标准库 + `slog` 记录发送结果
    - 返回 error（HTTP 错误或响应异常时）

  **Must NOT do**: 不创建 Notifier 接口；不支持其他通知渠道；不引入第三方 HTTP 库

  **Recommended Agent Profile**:
  - **Category**: `quick` — 单函数，逻辑简单
  - **Skills**: []

  **Parallelization**: Wave 1 | Parallel with T2, T4 | Blocks: T8 | Blocked By: T1

  **References**:
  - `py_ref/notification.py:1-21` — 完整文件，1:1 移植源。Line 8-16 是 sctp URL 路由关键逻辑

  **Acceptance Criteria**:
  - [ ] `internal/notification/serverchan.go` 存在并可编译
  - [ ] 函数签名：`Send(serverKey, title, description string) error`
  - [ ] 支持 sctp 前缀和标准两种 URL 格式
  - [ ] 使用 regexp 进行 sctp 前缀解析

  **QA Scenarios (MANDATORY):**
  ```
  Scenario: 标准 key URL 构造
    Tool: Bash
    Steps: 创建临时 Go 程序调用 notification 包，传入 key="SCT1234"，打印构造的 URL
    Expected: URL 包含 "sctapi.ftqq.com/SCT1234.send"
    Evidence: .sisyphus/evidence/task-3-url-standard.txt

  Scenario: sctp key URL 构造
    Tool: Bash
    Steps: 传入 key="sctp123t456"，打印构造的 URL
    Expected: URL 包含 "123.push.ft07.com/send/sctp123t456.send"
    Evidence: .sisyphus/evidence/task-3-url-sctp.txt
  ```

  **Commit**: YES (groups with T2)
  - Message: `feat(config): add YAML configuration and ServerChan notification modules`
  - Pre-commit: `go build ./cmd/...`

- [ ] 4. 浏览器生命周期管理

  **What to do**:
  - 创建 `internal/browser/browser.go`：
    - `LaunchBrowser(cfg *config.Config) (*rod.Browser, *launcher.Launcher, error)` 函数：
      - 使用 `launcher.New().Headless(cfg.Headless)` 配置 headless 模式
      - 添加 Chrome 启动参数（对应 Python 版的 options）：--disable-extensions, --disable-dev-shm-usage, --disable-background-networking, --disable-sync, --disable-translate, --disable-notifications, --blink-settings=imagesEnabled=false, --disable-features=NetworkPrediction, --dns-prefetch-disable
      - 返回 browser 和 launcher（调用方负责 defer l.Cleanup() 和 browser.Close()）
    - `SetupImageBlocking(page *rod.Page) error` 函数：
      - 使用 `page.HijackRequests()` 创建 router
      - 匹配所有图片请求，用 `proto.NetworkErrorReasonBlockedByClient` 拒绝
      - 返回前启动 `go router.Run()`
    - `NewPage(browser *rod.Browser) (*rod.Page, error)` 辅助函数：创建新 page 并设置图片屏蔽

  **Must NOT do**: 不创建 BrowserDriver 接口；不要在此文件中包含登录或选课逻辑

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high` — Rod API 使用需准确
  - **Skills**: []

  **Parallelization**: Wave 1 | Parallel with T2, T3 | Blocks: T5-T8 | Blocked By: T1

  **References**:
  - `py_ref/config.py:11-30` — get_chromium_options() 函数，Chrome 启动参数列表
  - Rod 文档：`launcher.New()` 自定义启动选项，`page.HijackRequests()` 屏蔽图片

  **Acceptance Criteria**:
  - [ ] `internal/browser/browser.go` 存在并可编译
  - [ ] LaunchBrowser 返回 browser + launcher
  - [ ] Chrome 启动参数与 Python 版一致
  - [ ] 图片屏蔽通过 HijackRequests 实现

  **QA Scenarios (MANDATORY):**
  ```
  Scenario: 浏览器启动和清理
    Tool: Bash
    Steps: 创建临时 Go 程序调用 LaunchBrowser()，验证 browser 非 nil，然后 Close + Cleanup
    Expected: 无 panic，无残留 chromium 进程（pgrep -f chromium 无输出）
    Evidence: .sisyphus/evidence/task-4-browser-lifecycle.txt

  Scenario: headless 配置生效
    Tool: Bash
    Steps: 分别用 Headless=true 和 Headless=false 调用 LaunchBrowser，观察 slog 输出
    Expected: 日志显示对应的 headless 状态
    Evidence: .sisyphus/evidence/task-4-headless-config.txt
  ```

  **Commit**: YES (groups with T5)
  - Message: `feat(browser): add browser lifecycle management and login flow`
  - Pre-commit: `go build ./cmd/...`

- [ ] 5. 登录流程

  **What to do**:
  - 创建 `internal/browser/login.go`：
    - `Login(page *rod.Page, cfg *config.Config) error` 函数：
      1. `page.Navigate(cfg.URL)` 导航到选课 URL
      2. 等待页面加载：链式 `page.WaitLoad()` + `page.WaitStable()` 作为初始等待
      3. 用 `page.Timeout(10s).Element(cfg.Selectors.Login.UsernameInput)` 定位用户名输入框
      4. 用同样模式定位密码输入框和登录按钮
      5. `usernameEl.Input(cfg.Username)` 输入用户名
      6. `passwordEl.Input(cfg.Password)` 输入密码
      7. `loginBtn.Click(input.MouseButtonLeft)` 点击登录
      8. slog 记录每个步骤
    - **使用 non-Must API**（error-returning）处理所有元素操作
    - 如果 WaitStable() 超时，回退到直接元素等待（带充足超时 50s）

  **Must NOT do**: 不使用 Must* API 处理元素操作；不要在登录中处理选课逻辑；不要硬编码选择器

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high` — Rod 页面交互核心逻辑
  - **Skills**: []

  **Parallelization**: Wave 2 | Parallel with T6 | Blocks: T8 | Blocked By: T2, T4

  **References**:
  - `py_ref/main.py:30-62` — login() 函数完整流程：navigate → wait_for_network_idle → find elements → type → click
  - `py_ref/utils.py:1-72` — wait_for_network_idle() — 理解 Python 版为何需要网络空闲等待（SPA 动态渲染）
  - `py_ref/config.py:42-51` — LoginSelectors 类定义

  **WHY Each Reference Matters**:
  - main.py login() 是 1:1 移植目标，步骤顺序必须一致
  - utils.py 解释了为何需要等待策略——目标网站是重度 SPA（Ant Design/React），DOM 元素在 JS 框架渲染完成前不可用
  - config.py LoginSelectors 已在 Task 2 转换为 CSS 选择器，此处通过 cfg.Selectors.Login 访问

  **Acceptance Criteria**:
  - [ ] `internal/browser/login.go` 存在并可编译
  - [ ] Login 函数使用 non-Must API
  - [ ] 选择器从 config 读取，无硬编码
  - [ ] 包含 slog 日志记录

  **QA Scenarios (MANDATORY):**
  ```
  Scenario: Login 函数编译和签名验证
    Tool: Bash
    Steps: go build ./cmd/... 确认编译通过；grep "func Login" internal/browser/login.go 确认函数存在
    Expected: 编译通过，函数签名匹配
    Evidence: .sisyphus/evidence/task-5-login-compile.txt

  Scenario: 无 Must* API 调用检查
    Tool: Bash
    Steps: grep -n "Must" internal/browser/login.go
    Expected: 无匹配结果（或仅在非关键路径如日志中出现）
    Evidence: .sisyphus/evidence/task-5-no-must-api.txt
  ```

  **Commit**: YES (groups with T4)
  - Message: `feat(browser): add browser lifecycle management and login flow`
  - Pre-commit: `go build ./cmd/...`

- [ ] 6. 课程查找与匹配

  **What to do**:
  - 创建 `internal/browser/course.go`（Part 1 — 查找匹配）：
    - `WaitForCoursePage(page *rod.Page, cfg *config.Config) error` 函数：
      - 用 `page.Timeout(40s).Element(cfg.Selectors.Course.Flag)` 等待选课页面标志元素出现
      - slog 记录页面加载完成
    - `FindTargetCourses(page *rod.Page, cfg *config.Config) ([]*rod.Element, error)` 函数：
      - 用 `page.Timeout(10s).Elements(cfg.Selectors.Course.DataRow)` 获取所有课程行
      - 遍历每行：`row.Element("a")` 获取课程链接，`row.Element("div")` 获取课程号
      - 提取课程名：`linkEl.Attribute("title")` 获取 title 属性
      - 提取课程号：`idEl.Text()` 获取文本
      - 与 cfg.Courses 列表三重匹配（课程名 + 课程号 + 教师姓名在后续 selectCourse 中匹配）
      - 此处仅匹配课程名 + 课程号，返回匹配的 link 元素列表
      - slog 记录：设定课程数 / 找到课程数

  **Must NOT do**: 不使用模糊匹配；不缓存元素引用跨 refresh；课程名和 ID 精确匹配

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high` — DOM 遍历和数据提取逻辑
  - **Skills**: []

  **Parallelization**: Wave 2 | Parallel with T5 | Blocks: T7, T8 | Blocked By: T2, T4

  **References**:
  - `py_ref/main.py:64-97` — wait_for_course_page() + find_target_courses() 完整实现
  - `py_ref/config.py:54-63` — CourseSelectors 类定义
  - `py_ref/config.py:33-38` — TargetCourses 结构（name + id + teachers）

  **WHY Each Reference Matters**:
  - main.py:73-97 是核心匹配算法：每行 tr 包含一个 a(课程名 via title 属性) 和一个 div(课程号 via text)，必须理解这个 DOM 结构才能正确提取
  - config.py TargetCourses 结构决定了 Go 的 TargetCourse struct 字段

  **Acceptance Criteria**:
  - [ ] `internal/browser/course.go` 存在并可编译
  - [ ] WaitForCoursePage 使用可配置选择器
  - [ ] FindTargetCourses 实现 name + id 双重匹配
  - [ ] 使用 non-Must API

  **QA Scenarios (MANDATORY):**
  ```
  Scenario: 函数编译和签名验证
    Tool: Bash
    Steps: go build ./cmd/... && grep -n "func WaitForCoursePage\|func FindTargetCourses" internal/browser/course.go
    Expected: 两个函数签名都存在
    Evidence: .sisyphus/evidence/task-6-course-find-compile.txt

  Scenario: 与 Python 逻辑对比
    Tool: Bash
    Steps: 阅读 course.go 中 FindTargetCourses 实现，对比 py_ref/main.py:73-97 的逻辑
    Expected: 遍历 → 提取 a[title] + div.text → 双重匹配逻辑一致
    Evidence: .sisyphus/evidence/task-6-python-comparison.txt
  ```

  **Commit**: YES (groups with T7)
  - Message: `feat(browser): add course finding, matching, and enrollment flow`
  - Pre-commit: `go build ./cmd/...`

- [ ] 7. 侧边栏操作与选课确认

  **What to do**:
  - 在 `internal/browser/course.go` 中续写（Part 2 — 选课确认）：
    - `SelectCourse(page *rod.Page, courseLink *rod.Element, cfg *config.Config) error` 函数：
      1. `courseLink.Click()` 点击课程打开侧边栏
      2. `page.Timeout(10s).Element(cfg.Selectors.Sidebar.SidebarFlag)` 等待侧边栏加载
      3. 定位关闭按钮（后续可能需要）
      4. `page.Timeout(10s).Elements(cfg.Selectors.Sidebar.DataRow)` 获取所有教师行
      5. 遍历每行：提取 td 单元格，取第 4 个（index 3）为教师姓名
      6. 与 cfg.Courses 中的 Teachers 列表匹配（遍历匹配，非模糊）
      7. 匹配成功 → 调用 isAvailable() 检查可选性
      8. 可选 → 勾选 checkbox → 调用 confirmSelection()
      9. 不可选 → slog 记录 "课程已满或已选"
      10. 选课成功侧边栏自动关闭；未选课则手动点击关闭按钮
    - `isAvailable(row *rod.Element, cfg *config.Config) (bool, error)` 函数：
      - 使用 `row.Has(cfg.Selectors.Sidebar.FullFlag)` 检查是否已满
      - 使用 `row.Has(cfg.Selectors.Sidebar.SelectedFlag)` 检查是否已选
      - 两者都不存在 → 返回 true（可选）
    - `confirmSelection(page *rod.Page, cfg *config.Config, serverKey string) error` 函数：
      - `page.Has(cfg.Selectors.Selection.SelectButton)` 检查选课按钮是否存在
      - 不存在 → slog "可能不是选课时间段"，return nil
      - 存在 → 点击选课按钮
      - `page.Has(cfg.Selectors.Selection.ConfirmButton)` 检查确认按钮
      - 不存在 → slog "选课可能未成功"，return nil
      - 存在 → 点击确认按钮
      - 如果 serverKey 非空 → 调用 notification.Send() 发送微信通知

  **Must NOT do**: 不缓存元素跨操作；教师匹配精确匹配（非子串）；不硬编码选择器

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high` — 最复杂的浏览器交互逻辑
  - **Skills**: []

  **Parallelization**: Wave 2 | Sequential after T6 | Blocks: T8 | Blocked By: T2, T4, T6

  **References**:
  - `py_ref/main.py:100-180` — select_course() + _is_available() + confirm_selection() 完整实现
  - `py_ref/config.py:65-80` — SidebarSelectors + SelectionSelectors 定义
  - `py_ref/notification.py` — sc_send() 调用点在 confirm_selection() 内（line 178）

  **WHY Each Reference Matters**:
  - main.py:100-143 是侧边栏操作核心：注意 line 141-143 的条件关闭逻辑（选课成功侧边栏自动关闭，未选课才需手动关闭）
  - main.py:146-154 的 _is_available() 使用 raise_exc=False 模式，Go 中用 page.Has() 对应
  - main.py:157-179 的 confirm_selection() 也使用 raise_exc=False 检查按钮存在性
  - notification 调用嵌入在 confirm_selection 内部

  **Acceptance Criteria**:
  - [ ] SelectCourse, isAvailable, confirmSelection 三个函数实现
  - [ ] 侧边栏关闭逻辑正确（成功选课不点关闭，未选课才关闭）
  - [ ] isAvailable 使用 Has() 而非 Must* 检查
  - [ ] confirmSelection 集成 notification.Send()
  - [ ] 所有选择器从 config 读取

  **QA Scenarios (MANDATORY):**
  ```
  Scenario: 函数签名和编译验证
    Tool: Bash
    Steps: go build ./cmd/... && grep -n "func SelectCourse\|func isAvailable\|func confirmSelection" internal/browser/course.go
    Expected: 三个函数签名都存在
    Evidence: .sisyphus/evidence/task-7-sidebar-compile.txt

  Scenario: 与 Python 逻辑对比 — 侧边栏关闭条件
    Tool: Bash
    Steps: 阅读 SelectCourse 中的关闭逻辑，对比 py_ref/main.py:140-143
    Expected: Go 版本同样只在未成功选课时关闭侧边栏
    Evidence: .sisyphus/evidence/task-7-close-logic.txt
  ```

  **Commit**: YES (groups with T6)
  - Message: `feat(browser): add course finding, matching, and enrollment flow`
  - Pre-commit: `go build ./cmd/...`

- [ ] 8. 主程序 Orchestration

  **What to do**:
  - 重写 `cmd/main.go`（替换 Task 1 的 stub）：
    - CLI 参数解析：`--config` (string, 默认 "config.yaml") + `--headless` (bool, 可选覆盖)
    - 加载配置：`config.Load(configPath)`
    - 如果 --headless 被设置，覆盖 cfg.Headless
    - 信号处理：`signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)` 获取 ctx
    - **顶层重试循环**（对应 Python 的 @retry 装饰器）：
      ```go
      for attempt := 0; attempt < cfg.MaxRetry; attempt++ {
          err := run(ctx, cfg)
          if err == nil { break }
          if ctx.Err() != nil { break } // signal received
          backoff := min(time.Duration(1<<attempt) * time.Second, 30*time.Second)
          slog.Error("运行出错，准备重试", "attempt", attempt+1, "backoff", backoff, "error", err)
          time.Sleep(backoff) // 这里的 time.Sleep 是重试间隔，非页面等待，允许使用
      }
      ```
    - `run(ctx context.Context, cfg *config.Config) error` 内部函数：
      1. LaunchBrowser(cfg) → browser, launcher
      2. defer browser.Close() + defer launcher.Cleanup()
      3. NewPage(browser) → page
      4. Login(page, cfg)
      5. **轮询循环**：
         - WaitForCoursePage(page, cfg)
         - FindTargetCourses(page, cfg)
         - for each course → SelectCourse(page, course, cfg)
         - time.Sleep(cfg.Interval) — 轮询间隔，允许 time.Sleep
         - page.Reload() 刷新页面
         - 检查 ctx.Done() 以响应信号
      6. 任何错误 → return err（触发顶层重试）
    - **两级错误区分**：
      - 基础设施错误（浏览器崩溃、网络断开、element 超时）→ return err → 顶层指数退避重试
      - 业务逻辑结果（课程已满、已选、非选课时间）→ slog 记录 → 继续轮询

  **Must NOT do**: 不创建子命令；time.Sleep 仅用于重试退避和轮询间隔，不用于等待页面状态；不创建接口

  **Recommended Agent Profile**:
  - **Category**: `deep` — 整合所有模块，重试/信号/context 逻辑复杂
  - **Skills**: []

  **Parallelization**: Wave 3 | Sequential | Blocks: T9 | Blocked By: T2-T7 (all)

  **References**:
  - `py_ref/main.py:182-210` — main() 函数：@retry 装饰器 + browser 启动 + login + while True 循环 + sleep + refresh
  - `py_ref/main.py:186-207` — 轮询循环的完整结构
  - `py_ref/config.py:5-7` — INTERVAL, MAX_RETRY 配置值

  **WHY Each Reference Matters**:
  - main.py:182-210 是整个程序的 orchestration 逻辑，Go 版 cmd/main.go 是其 1:1 对应
  - Python 的 @retry 装饰器包裹整个 main() 包括浏览器创建 — Go 版重试循环也必须在 browser 创建之外
  - 轮询循环的 sleep → refresh → re-find 模式必须在 Go 中保持

  **Acceptance Criteria**:
  - [ ] `cmd/main.go` 完整实现，可编译运行
  - [ ] CLI 支持 --config 和 --headless 参数
  - [ ] 信号处理：SIGINT/SIGTERM 触发优雅退出
  - [ ] 顶层重试 + 指数退避（上限 30s）
  - [ ] 轮询循环：sleep → refresh → re-find
  - [ ] 两级错误区分：基础设施 vs 业务逻辑
  - [ ] `go build ./cmd/...` + `go vet ./...` 通过

  **QA Scenarios (MANDATORY):**
  ```
  Scenario: 编译和基本启动
    Tool: Bash
    Steps:
      1. go build -o autoclicks ./cmd/...
      2. ./autoclicks --config config.yaml &
      3. sleep 5
      4. kill -INT $(pgrep autoclicks)
      5. wait
      6. pgrep -f chromium | wc -l
    Expected: 编译成功；启动后 slog 输出配置加载信息；SIGINT 后优雅退出；无残留 chromium 进程
    Evidence: .sisyphus/evidence/task-8-main-lifecycle.txt

  Scenario: go vet 通过
    Tool: Bash
    Steps: go vet ./...
    Expected: exit 0，无警告
    Evidence: .sisyphus/evidence/task-8-go-vet.txt
  ```

  **Commit**: YES
  - Message: `feat: complete main orchestration with retry, signal handling, and CLI`
  - Files: `cmd/main.go`
  - Pre-commit: `go build ./cmd/... && go vet ./...`

- [ ] 9. 端到端集成验证

  **What to do**:
  - 运行完整编译：`go build -o autoclicks ./cmd/...` + `go vet ./...`
  - 验证二进制启动、配置加载、浏览器启动、信号退出的完整流程
  - 检查所有 Go 文件与 Python 参考的函数对应关系
  - 修复集成中发现的任何编译或运行时问题
  - 确认 .gitignore 已更新（如需要，忽略编译产物）
  - 清理任何临时文件或调试代码

  **Must NOT do**: 不添加新功能；不重构已完成的代码（除非有 bug）

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high` — 集成验证需要全局视角
  - **Skills**: []

  **Parallelization**: Wave 3 | Sequential after T8 | Blocks: F1-F4 | Blocked By: ALL

  **References**:
  - 所有 `internal/` 目录下的 Go 文件
  - `py_ref/main.py:1-210` — 完整 Python 参考，逐函数对比
  - `config.yaml` — 验证配置模板完整性

  **Acceptance Criteria**:
  - [ ] `go build -o autoclicks ./cmd/...` 成功
  - [ ] `go vet ./...` 通过
  - [ ] 二进制启动后 slog 输出配置加载信息
  - [ ] SIGINT 后优雅退出，无残留进程
  - [ ] Python 每个核心函数在 Go 中有对应实现

  **QA Scenarios (MANDATORY):**
  ```
  Scenario: 完整编译验证
    Tool: Bash
    Steps:
      1. go build -o autoclicks ./cmd/...
      2. go vet ./...
      3. file autoclicks
    Expected: 编译成功，vet 通过，file 显示 ELF 可执行文件
    Evidence: .sisyphus/evidence/task-9-build-verify.txt

  Scenario: Python 函数映射验证
    Tool: Bash
    Steps: 对比 py_ref/ 中每个函数在 Go 代码中的对应实现
    Expected: login→Login, wait_for_course_page→WaitForCoursePage, find_target_courses→FindTargetCourses, select_course→SelectCourse, _is_available→isAvailable, confirm_selection→confirmSelection, sc_send→Send
    Evidence: .sisyphus/evidence/task-9-function-mapping.txt
  ```

  **Commit**: YES
  - Message: `chore: final integration verification and cleanup`
  - Pre-commit: `go build ./cmd/... && go vet ./...`
---

## Final Verification Wave (MANDATORY — after ALL implementation tasks)

> 4 review agents run in PARALLEL. ALL must APPROVE. Rejection → fix → re-run.

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Read the plan end-to-end. For each "Must Have": verify implementation exists (read file, run command). For each "Must NOT Have": search codebase for forbidden patterns — reject with file:line if found. Check evidence files exist in .sisyphus/evidence/. Compare deliverables against plan.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run `go build ./cmd/...` + `go vet ./...`. Review all Go files for: unnecessary interfaces, `time.Sleep()` for page state, empty error handling (`if err != nil { }`), leftover TODO/FIXME comments, unused imports. Check AI slop: excessive comments, over-abstraction, generic variable names.
  Output: `Build [PASS/FAIL] | Vet [PASS/FAIL] | Files [N clean/N issues] | VERDICT`

- [ ] F3. **Real Manual QA** — `unspecified-high`
  Start from clean state. Execute EVERY QA scenario from EVERY task — follow exact steps, capture evidence. Verify config loading (valid + invalid). Verify `go build` produces working binary. Verify SIGINT cleanup. Review slog output format. Save to `.sisyphus/evidence/final-qa/`.
  Output: `Scenarios [N/N pass] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  For each task: read "What to do", read actual code. Verify 1:1 — everything in spec was built, nothing beyond spec was built. Check every Python reference function has a Go equivalent. Check "Must NOT do" compliance (no interfaces, no per-operation retry, no `time.Sleep` for waits). Flag any file not accounted for in the plan.
  Output: `Tasks [N/N compliant] | Unaccounted [CLEAN/N files] | VERDICT`

---

## Commit Strategy

| Group | Message | Files | Pre-commit |
|-------|---------|-------|------------|
| T1 | `chore: initialize Go module and project skeleton` | go.mod, cmd/main.go (stub), internal/ dirs | `go build ./cmd/...` |
| T2+T3 | `feat(config): add YAML configuration and ServerChan notification modules` | internal/config/*, internal/notification/*, config.yaml | `go build ./cmd/...` |
| T4+T5 | `feat(browser): add browser lifecycle management and login flow` | internal/browser/browser.go, internal/browser/login.go | `go build ./cmd/...` |
| T6+T7 | `feat(browser): add course finding, matching, and enrollment flow` | internal/browser/course.go | `go build ./cmd/...` |
| T8 | `feat: complete main orchestration with retry, signal handling, and CLI` | cmd/main.go | `go build ./cmd/... && go vet ./...` |
| T9 | `chore: final integration verification and cleanup` | (any fixes) | `go build ./cmd/... && go vet ./...` |

---

## Success Criteria

### Verification Commands
```bash
go build -o autoclicks ./cmd/...           # Expected: exit 0, binary produced
go vet ./...                                # Expected: exit 0, no issues
./autoclicks --config config.yaml 2>&1      # Expected: starts, loads config, launches browser
# Send SIGINT after startup:
kill -INT $(pgrep autoclicks)               # Expected: graceful shutdown, no orphaned chromium
pgrep -f chromium                           # Expected: no output (all cleaned up)
```

### Final Checklist
- [ ] All "Must Have" present — 逐项验证
- [ ] All "Must NOT Have" absent — `grep -r "interface{" internal/` 无结果，`grep -r "time.Sleep" internal/browser/` 无结果
- [ ] `go build` + `go vet` 通过
- [ ] Python 每个核心函数在 Go 中有对应实现
- [ ] YAML 配置模板完整可用
