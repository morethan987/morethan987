# Go 语言复刻 campus-login

## TL;DR

> **Quick Summary**: 将 267 行 bash 校园网登录工具用 Go 语言重写为多文件分包结构的 CLI 工具，1:1 复刻所有功能并新增 status 连通性检测命令。
> 
> **Deliverables**:
> - Go 项目: `go.mod` + `cmd/campus-login/main.go` + `internal/` 分包
> - 完整 CLI: login, add, remove/rm, list/ls, default, help, **status** (新增)
> - 单元测试: config 读写、URL 编码、JSONP 解析
> - 单一静态二进制: `campus-login`
> 
> **Estimated Effort**: Short
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 → Task 2 → Task 3 → Task 5 → Task 7 → Task 8

---

## Context

### Original Request
用户要求将现有的 bash 校园网自动登录工具 (`shell-bin`, 267 行) 用 Go 语言复刻，采用多文件分包结构，纯标准库实现，新增 status 命令。

### Interview Summary
**Key Discussions**:
- **项目结构**: 多文件分包 (`cmd/` + `internal/`)，代码放仓库根目录
- **CLI 框架**: 纯标准库 `os.Args`，不用 cobra
- **功能范围**: 1:1 复刻 + status 外网连通性检测
- **测试策略**: 基础单元测试 (config/URL 编码/JSONP 解析)
- **依赖策略**: 零第三方依赖 (唯一例外: `golang.org/x/term` 用于密码安全输入)

**Research Findings**:
- Portal 使用 JSONP 格式: `dr1004({"result":"1","msg":"..."})`
- 账号需要 `,0,` 前缀: `urlencode(",0," + account)`
- Go 获取本机 IP 的标准模式: `net.Dial("udp", "8.8.8.8:80")` → `conn.LocalAddr()`
- URL 中 `v=1089` 和重复的 `lang` 参数需要原样保留

### Metis Review
**Identified Gaps** (addressed):
- **密码含 `:` 的解析 bug**: 使用 `SplitN(creds, ":", 2)` 修复 (仅按第一个 `:` 分割)
- **status 命令行为未定义**: 确认为外网连通性检测 (HTTP GET `connectivitycheck.gstatic.com/generate_204`)
- **登录失败 exit code**: 修复为 exit 1 (shell 版本 exit 0 不合理)
- **HTTP 无超时**: 添加 10s 超时
- **XDG_CONFIG_HOME**: 尊重该环境变量，fallback 到 `~/.config`
- **密码安全输入**: 使用 `golang.org/x/term` (Go 官方子仓库，不算第三方)

---

## Work Objectives

### Core Objective
用 Go 语言忠实复刻 campus-login 工具的所有 CLI 功能，保持配置文件格式完全兼容，新增 status 命令检测外网连通性。

### Concrete Deliverables
```
campus-login/
├── go.mod                           # module github.com/morethan987/campus-login
├── go.sum
├── cmd/campus-login/main.go         # CLI 入口 + 命令分发
├── internal/
│   ├── config/
│   │   ├── config.go                # 配置文件 CRUD
│   │   └── config_test.go           # 配置单元测试
│   ├── login/
│   │   ├── login.go                 # HTTP 登录 + JSONP 解析
│   │   └── login_test.go            # 登录逻辑单元测试
│   ├── network/
│   │   ├── network.go               # 本机 IP 获取 + status 检测
│   │   └── network_test.go          # 网络工具单元测试
│   └── color/
│       └── color.go                 # TTY 检测 + ANSI 颜色
├── shell-bin                        # 原始 shell 脚本 (保留)
```

### Definition of Done
- [ ] `go build ./cmd/campus-login` 编译零错误
- [ ] `go vet ./...` 零问题
- [ ] `go test ./...` 所有测试通过
- [ ] `./campus-login help` 输出包含中文帮助信息
- [ ] `./campus-login add/remove/list/default` 功能与 shell 版一致
- [ ] `./campus-login status` 检测外网连通性并输出结果
- [ ] 配置文件格式与 shell 版本双向兼容

### Must Have
- 完全兼容的 CLI 接口 (login/add/remove|rm/list|ls/default/help)
- 配置文件格式兼容 (`alias=account:password`, `default_account=alias`)
- JSONP 回调解析 (`dr1004(...)`)
- 账号 `,0,` 前缀
- HTTP 请求头与 shell 版完全一致
- TTY 颜色检测
- 安全密码输入 (终端回显禁用)
- status 命令外网连通性检测
- 基础单元测试

### Must NOT Have (Guardrails)
- ❌ 第三方 HTTP/CLI 框架 (cobra, urfave, resty 等)
- ❌ logout 命令
- ❌ 守护进程 / 自动重连 / 定时任务
- ❌ `--verbose` / `--debug` / `--json` 等额外输出模式
- ❌ 日志框架 / 结构化日志
- ❌ `pkg/` 目录 (使用 `internal/`)
- ❌ 过度接口抽象 (这是 CLI 工具不是框架)
- ❌ 输入验证超出 shell 版本范围 (不验证账号格式)
- ❌ Windows/macOS 专属适配
- ❌ 配置文件版本管理 / 迁移
- ❌ 进度条 / spinner
- ❌ 重试逻辑
- ❌ Makefile

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: NO (新项目)
- **Automated tests**: YES (Tests-after, 每个 internal 包写单元测试)
- **Framework**: `go test` (标准库内置)

### QA Policy
Every task MUST include agent-executed QA scenarios.
Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

- **CLI**: Use Bash — 运行 binary、检查 stdout/stderr、验证 exit code
- **Config**: Use Bash — 写入配置后 grep 验证文件内容
- **Network**: Use `go test` with `httptest` mock server

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — 基础模块, 互相独立):
├── Task 1: 项目脚手架 + go.mod [quick]
├── Task 2: internal/config — 配置管理 + 测试 [unspecified-high]
├── Task 3: internal/network — IP 检测 + status 检测 + 测试 [unspecified-high]
├── Task 4: internal/color — 颜色输出 [quick]
└── Task 5: internal/login — HTTP 登录 + JSONP 解析 + 测试 [unspecified-high]

Wave 2 (After Wave 1 — CLI 集成):
└── Task 6: cmd/campus-login/main.go — CLI 入口 + 命令分发 [unspecified-high]

Wave 3 (After Wave 2 — 端到端验证):
└── Task 7: 端到端集成测试 (binary 级别) [unspecified-high]

Wave FINAL (After ALL tasks — independent review, 4 parallel):
├── Task F1: Plan compliance audit (oracle)
├── Task F2: Code quality review (unspecified-high)
├── Task F3: Real manual QA (unspecified-high)
└── Task F4: Scope fidelity check (deep)

Critical Path: Task 1 → Task 2,3,4,5 (parallel) → Task 6 → Task 7 → F1-F4
Parallel Speedup: ~50% faster than sequential (Wave 1 runs 5 tasks in parallel)
Max Concurrent: 5 (Wave 1)
```

### Dependency Matrix

| Task | Depends On | Blocks | Wave |
|------|-----------|--------|------|
| 1 | — | 2, 3, 4, 5 | 1 |
| 2 | 1 | 6 | 1 |
| 3 | 1 | 6 | 1 |
| 4 | 1 | 6 | 1 |
| 5 | 1 | 6 | 1 |
| 6 | 2, 3, 4, 5 | 7 | 2 |
| 7 | 6 | F1-F4 | 3 |
| F1-F4 | 7 | — | FINAL |

### Agent Dispatch Summary

- **Wave 1**: **5 tasks** — T1 → `quick`, T2 → `unspecified-high`, T3 → `unspecified-high`, T4 → `quick`, T5 → `unspecified-high`
- **Wave 2**: **1 task** — T6 → `unspecified-high`
- **Wave 3**: **1 task** — T7 → `unspecified-high`
- **FINAL**: **4 tasks** — F1 → `oracle`, F2 → `unspecified-high`, F3 → `unspecified-high`, F4 → `deep`

---

## TODOs

- [ ] 1. 项目脚手架 + go.mod 初始化

  **What to do**:
  - 创建 `go.mod`: `module github.com/morethan987/campus-login`, Go 1.26.0
  - 运行 `go get golang.org/x/term` 添加唯一外部依赖 (用于密码安全输入)
  - 创建目录结构: `cmd/campus-login/`, `internal/config/`, `internal/login/`, `internal/network/`, `internal/color/`
  - 创建 `cmd/campus-login/main.go` 占位文件: 仅包含 `package main` + `func main() {}` 确保 `go build` 通过

  **Must NOT do**:
  - 不写任何业务逻辑
  - 不添加 cobra/urfave 等第三方 CLI 框架
  - 不创建 `pkg/` 目录

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 纯脚手架任务，创建目录和空文件
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO (其他 Wave 1 任务需要 go.mod 存在)
  - **Parallel Group**: Wave 1 (先行任务)
  - **Blocks**: Tasks 2, 3, 4, 5
  - **Blocked By**: None

  **References**:
  - `shell-bin:1-20` — 项目名和版本信息，Go 项目应保持一致的命名
  - `golang.org/x/term` — Go 官方子仓库，用于终端密码输入

  **Acceptance Criteria**:
  - [ ] `go build ./cmd/campus-login` 零错误
  - [ ] `go vet ./...` 零问题
  - [ ] 目录结构存在: `cmd/campus-login/`, `internal/config/`, `internal/login/`, `internal/network/`, `internal/color/`
  - [ ] `go.mod` 包含 `golang.org/x/term` 依赖

  **QA Scenarios:**

  ```
  Scenario: go build 成功编译
    Tool: Bash
    Preconditions: go.mod 已创建
    Steps:
      1. go build ./cmd/campus-login
      2. 检查 exit code 为 0
    Expected Result: 编译成功，无错误输出
    Failure Indicators: 任何编译错误
    Evidence: .sisyphus/evidence/task-1-build.txt

  Scenario: 目录结构完整
    Tool: Bash
    Preconditions: 项目已初始化
    Steps:
      1. ls -d cmd/campus-login internal/config internal/login internal/network internal/color
      2. 检查 exit code 为 0
    Expected Result: 所有 5 个目录存在
    Failure Indicators: ls 报错某目录不存在
    Evidence: .sisyphus/evidence/task-1-dirs.txt
  ```

  **Commit**: YES (group 1)
  - Message: `chore: init Go project scaffolding`
  - Files: `go.mod`, `go.sum`, `cmd/campus-login/main.go`, `internal/*/`
  - Pre-commit: `go build ./cmd/campus-login`

---

- [ ] 2. internal/config — 配置文件管理 + 单元测试

  **What to do**:
  - 创建 `internal/config/config.go`:
    - 配置目录: 优先 `$XDG_CONFIG_HOME/campus-login/`, fallback `$HOME/.config/campus-login/`
    - `Setup()` — 创建配置目录和文件 (如不存在)
    - `AddAccount(alias, account, password string)` — 写入 `alias=account:password`，若 alias 已存在先删除旧行
    - `RemoveAccount(alias string)` — 删除 alias 行 + 如果 `default_account` 等于该 alias 也一并删除
    - `ListAccounts() []Account` — 返回所有账号 (排除 `default_account=` 行)
    - `GetDefault() string` — 返回 default_account 值
    - `SetDefault(alias string) error` — 设置 default_account (先验证 alias 存在)
    - `GetCredentials(alias string) (account, password string, err error)` — 用 `strings.SplitN(creds, ":", 2)` 分割 (修复 shell 版 `:` 分割 bug)
  - Account 结构体: `type Account struct { Alias, AccountID string, IsDefault bool }`
  - 配置文件写入使用 write-to-temp-then-rename 模式 (比 shell 的 `sed -i` 更安全)
  - 创建 `internal/config/config_test.go`:
    - 使用 `t.TempDir()` 隔离测试
    - 测试: 添加账号 → 验证文件内容
    - 测试: 删除账号 → 验证行消失
    - 测试: 删除默认账号 → 验证 default_account 行也删除
    - 测试: 列出账号 → 验证不包含 default_account 行
    - 测试: GetCredentials 密码含 `:` → 验证正确分割
    - 测试: SetDefault 不存在的 alias → 返回 error

  **Must NOT do**:
  - 不使用 JSON/YAML/TOML 格式
  - 不引入配置文件版本管理
  - 不验证账号格式 (shell 版也不验证)
  - 不在 ListAccounts 中返回密码 (仅返回 Alias + AccountID + IsDefault)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 涉及文件 I/O、原子写入、边界情况处理，需要仔细实现
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 3, 4, 5 并行)
  - **Parallel Group**: Wave 1 (with Tasks 3, 4, 5)
  - **Blocks**: Task 6
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `shell-bin:19-20` — 配置目录和文件路径定义: `CONFIG_DIR="$HOME/.config/campus-login"`, `CONFIG_FILE="$CONFIG_DIR/config"`
  - `shell-bin:78-81` — `setup_config()` 创建目录和文件: `mkdir -p` + `touch`
  - `shell-bin:159-177` — `add_account()`: `sed -i "/^${alias}=/d"` 删旧行 + `echo >> ` 追加新行
  - `shell-bin:179-188` — `remove_account()`: 同时删除 alias 行和 `default_account` 行
  - `shell-bin:190-207` — `list_accounts()`: `grep -v "^default_account="` 过滤，标记默认账号
  - `shell-bin:209-218` — `set_default()`: 先 grep 验证 alias 存在
  - `shell-bin:134-154` — `handle_login()` 中读取凭证: `grep + cut -d'=' -f2 + tr ':' ' '`

  **API/Type References**:
  - 配置文件格式: 每行 `alias=account:password`，特殊行 `default_account=alias`
  - 分隔符: `=` 分割 alias 和凭证, `:` 分割 account 和 password (Go 中用 `SplitN` 只分第一个 `:`)

  **Acceptance Criteria**:
  - [ ] `go test ./internal/config/...` 全部通过
  - [ ] 添加账号后 config 文件包含 `testuser=20230001:testpass`
  - [ ] 删除默认账号后 `default_account` 行也被删除
  - [ ] 密码含 `:` 时 `GetCredentials` 正确返回完整密码

  **QA Scenarios:**

  ```
  Scenario: 添加账号后配置文件格式正确
    Tool: Bash (go test)
    Preconditions: 空的临时配置目录
    Steps:
      1. go test ./internal/config/ -run TestAddAccount -v
      2. 检查测试输出包含 PASS
    Expected Result: 测试通过，配置文件包含 `alias=account:password` 格式行
    Failure Indicators: FAIL 或 panic
    Evidence: .sisyphus/evidence/task-2-add-account.txt

  Scenario: 删除默认账号连带清除 default_account
    Tool: Bash (go test)
    Preconditions: 配置文件中有 testuser 账号且为默认
    Steps:
      1. go test ./internal/config/ -run TestRemoveDefaultAccount -v
    Expected Result: testuser 行和 default_account=testuser 行都被删除
    Failure Indicators: 文件中仍包含 testuser 或 default_account
    Evidence: .sisyphus/evidence/task-2-remove-default.txt

  Scenario: 密码含冒号的正确解析
    Tool: Bash (go test)
    Preconditions: 配置文件包含 `user1=account1:pass:word:with:colons`
    Steps:
      1. go test ./internal/config/ -run TestCredentialsWithColon -v
    Expected Result: account=`account1`, password=`pass:word:with:colons`
    Failure Indicators: password 被截断
    Evidence: .sisyphus/evidence/task-2-colon-password.txt
  ```

  **Commit**: YES (group 2)
  - Message: `feat: implement core internal packages`
  - Files: `internal/config/config.go`, `internal/config/config_test.go`
  - Pre-commit: `go test ./internal/config/...`

---

- [ ] 3. internal/network — 本机 IP 检测 + status 连通性检测 + 测试

  **What to do**:
  - 创建 `internal/network/network.go`:
    - `GetLocalIP() string` — 用 `net.Dial("udp", "8.8.8.8:80")` + `conn.LocalAddr().(*net.UDPAddr).IP.String()` 获取本机内网 IP，失败返回 `"0.0.0.0"` (与 shell 版 fallback 一致)
    - `CheckConnectivity() (bool, error)` — HTTP GET `http://connectivitycheck.gstatic.com/generate_204`，超时 5s:
      - 返回 204 → 已联网 (true, nil)
      - 返回 302/200 → 未登录校园网 (false, nil) — portal 可能重定向
      - 超时/错误 → 网络不可用 (false, err)
  - 创建 `internal/network/network_test.go`:
    - 使用 `httptest.NewServer` mock 连通性检测端点
    - 测试: mock 返回 204 → CheckConnectivity 返回 true
    - 测试: mock 返回 302 → CheckConnectivity 返回 false
    - 测试: mock 服务器关闭 → CheckConnectivity 返回 error
    - 测试: GetLocalIP 返回非空字符串 (在有网络的环境中)

  **Must NOT do**:
  - 不用 ping/ICMP (需要 root 权限)
  - 不实现重试逻辑
  - 不实现守护进程

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 网络 I/O + httptest mock 测试，需要处理多种响应情况
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 2, 4, 5 并行)
  - **Parallel Group**: Wave 1 (with Tasks 2, 4, 5)
  - **Blocks**: Task 6
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `shell-bin:89-91` — `get_local_ip()`: `ip route get 1.1.1.1 | grep -oP 'src \K[0-9.]+'` + fallback `0.0.0.0`

  **External References**:
  - Go `net.Dial("udp", ...)` IP 检测模式: Kubernetes/Docker/etcd 等大型项目广泛使用
  - `connectivitycheck.gstatic.com/generate_204` — Google 连通性检测端点，返回 204 表示有外网访问

  **Acceptance Criteria**:
  - [ ] `go test ./internal/network/...` 全部通过
  - [ ] GetLocalIP 返回有效 IP 格式字符串
  - [ ] CheckConnectivity 在 mock 204 时返回 true
  - [ ] CheckConnectivity 在 mock 302 时返回 false

  **QA Scenarios:**

  ```
  Scenario: 连通性检测 mock 204 成功
    Tool: Bash (go test)
    Preconditions: 无
    Steps:
      1. go test ./internal/network/ -run TestCheckConnectivity204 -v
    Expected Result: 测试输出 PASS，CheckConnectivity 返回 true, nil
    Failure Indicators: FAIL
    Evidence: .sisyphus/evidence/task-3-connectivity-204.txt

  Scenario: 连通性检测 mock 302 未登录
    Tool: Bash (go test)
    Preconditions: 无
    Steps:
      1. go test ./internal/network/ -run TestCheckConnectivity302 -v
    Expected Result: 测试输出 PASS，CheckConnectivity 返回 false, nil
    Failure Indicators: FAIL
    Evidence: .sisyphus/evidence/task-3-connectivity-302.txt
  ```

  **Commit**: YES (group 2)
  - Message: `feat: implement core internal packages`
  - Files: `internal/network/network.go`, `internal/network/network_test.go`
  - Pre-commit: `go test ./internal/network/...`

---

- [ ] 4. internal/color — TTY 检测 + ANSI 颜色输出

  **What to do**:
  - 创建 `internal/color/color.go`:
    - `Init()` — 检测 `os.Stdout` 是否为 terminal (`os.Stdout.Stat()` 检查 `ModeCharDevice`)，等价于 shell 的 `[ -t 1 ]`
    - 导出颜色变量: `Red`, `Green`, `Yellow`, `Blue`, `NC` (reset)
    - 如果不是 TTY，所有颜色变量为空字符串 (与 shell 版 else 分支一致)
    - 提供 helper 函数: `Colorize(color, text string) string` 返回 `color + text + NC`
  - 这是一个非常简单的包，不需要测试文件

  **Must NOT do**:
  - 不引入 fatih/color 等第三方颜色库
  - 不支持 256 色/TrueColor
  - 不添加 `--no-color` flag

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 20-30 行代码，非常简单的常量定义 + TTY 检测
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 2, 3, 5 并行)
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 5)
  - **Blocks**: Task 6
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `shell-bin:23-35` — 颜色定义: `C_RED`, `C_GREEN`, `C_YELLOW`, `C_BLUE`, `C_NC` + TTY 检测 `[ -t 1 ]`
  - ANSI 码对应: `\033[0;31m` (Red), `\033[0;32m` (Green), `\033[0;33m` (Yellow), `\033[1;36m` (Blue/Cyan), `\033[0m` (Reset)

  **Acceptance Criteria**:
  - [ ] `go vet ./internal/color/...` 零问题
  - [ ] 导出变量 `Red`, `Green`, `Yellow`, `Blue`, `NC` 存在
  - [ ] Init() 后在 TTY 环境中颜色变量非空

  **QA Scenarios:**

  ```
  Scenario: 颜色包编译成功
    Tool: Bash
    Preconditions: 无
    Steps:
      1. go vet ./internal/color/
      2. go build ./internal/color/
    Expected Result: 零错误、零警告
    Failure Indicators: 任何 vet 或 build 错误
    Evidence: .sisyphus/evidence/task-4-color-build.txt
  ```

  **Commit**: YES (group 2)
  - Message: `feat: implement core internal packages`
  - Files: `internal/color/color.go`
  - Pre-commit: `go vet ./internal/color/...`

---

- [ ] 5. internal/login — HTTP 登录 + JSONP 解析 + 测试

  **What to do**:
  - 创建 `internal/login/login.go`:
    - 常量: `PortalHost = "login.cqu.edu.cn:801"`, `CallbackName = "dr1004"`
    - `PerformLogin(account, password, localIP string) (success bool, msg string, err error)`:
      - 账号加 `,0,` 前缀: `,0,` + account
      - URL 编码用 `net/url.QueryEscape`
      - 构造完整请求 URL (参数顺序、值完全复制 shell 版)
      - HTTP GET 请求，包含完全相同的 Headers (Accept, Accept-Language, Connection, Referer, User-Agent)
      - `http.Client` 超时 10s，`TLSClientConfig: &tls.Config{InsecureSkipVerify: true}` (对应 curl `--insecure`)
      - 禁用自动重定向: `CheckRedirect: func(req *http.Request, via []*http.Request) error { return http.ErrUseLastResponse }`
    - `ParseJSONP(body string) (result, msg string, err error)`:
      - 找 `dr1004(` 前缀和匹配的 `)` 后缀
      - 提取中间 JSON 字符串
      - 用 `encoding/json` 解析 `{"result":"...","msg":"..."}`
      - 返回 result 和 msg 字段
  - 创建 `internal/login/login_test.go`:
    - 用 `httptest.NewServer` mock portal 服务器
    - 测试 ParseJSONP:
      - 有效响应: `dr1004({"result":"1","msg":"ok"})` → result="1", msg="ok"
      - 空响应: `""` → 返回 error
      - 畲形响应: `dr1004(invalid)` → 返回 error
      - 无 callback: `{"result":"1"}` → 返回 error
    - 测试 URL 编码:
      - `,0,20230001` 编码后包含 `%2C0%2C20230001`
    - 测试 PerformLogin (mock 服务器):
      - mock 返回成功响应 → success=true
      - mock 返回失败响应 → success=false, msg 非空

  **Must NOT do**:
  - 不使用正则表达式解析 JSONP (用 strings.Index)
  - 不修改 URL 参数顺序或值 (保留重复的 `lang` 参数、`v=1089` 等)
  - 不添加重试逻辑
  - 不添加 logout 功能

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: HTTP 请求构造、JSONP 解析、TLS 配置、httptest mock，有一定复杂度
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 2, 3, 4 并行)
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: Task 6
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `shell-bin:84-86` — `urlencode()`: `jq -s -R -r @uri` — Go 中用 `url.QueryEscape`
  - `shell-bin:94-131` — `perform_login()` 完整实现:
    - 行 104: 账号前缀 `,0,$account`
    - 行 107-113: curl 完整 URL + 所有 Headers
    - 行 115: JSONP 解析 `sed -n 's/.*dr1004(\(.*\)).*/\1/p'`
    - 行 122-123: JSON 提取 result 和 msg
    - 行 125-130: 成功/失败判断逻辑
  - 注意 URL 中重复参数: `lang=zh-cn` 和 `lang=zh` 同时存在，必须保留

  **API/Type References**:
  - Portal 登录端点: `http://login.cqu.edu.cn:801/eportal/portal/login`
  - JSONP 回调格式: `dr1004({"result":"1","msg":"..."})`
  - 响应 JSON 结构: `{"result": string, "msg": string}` — result="1" 表示成功

  **Acceptance Criteria**:
  - [ ] `go test ./internal/login/...` 全部通过
  - [ ] JSONP 解析测试覆盖: 有效、空、畲形、无 callback
  - [ ] URL 编码 `,0,20230001` 结果包含 `%2C0%2C20230001`
  - [ ] mock 登录测试覆盖成功和失败场景

  **QA Scenarios:**

  ```
  Scenario: JSONP 解析有效响应
    Tool: Bash (go test)
    Preconditions: 无
    Steps:
      1. go test ./internal/login/ -run TestParseJSONP -v
    Expected Result: 解析 `dr1004({"result":"1","msg":"ok"})` 得到 result="1", msg="ok"
    Failure Indicators: FAIL
    Evidence: .sisyphus/evidence/task-5-jsonp-parse.txt

  Scenario: JSONP 解析空响应报错
    Tool: Bash (go test)
    Preconditions: 无
    Steps:
      1. go test ./internal/login/ -run TestParseJSONPEmpty -v
    Expected Result: 返回 error，不 panic
    Failure Indicators: panic 或返回 nil error
    Evidence: .sisyphus/evidence/task-5-jsonp-empty.txt

  Scenario: Mock 服务器登录成功
    Tool: Bash (go test)
    Preconditions: 无
    Steps:
      1. go test ./internal/login/ -run TestPerformLoginSuccess -v
    Expected Result: PerformLogin 返回 success=true
    Failure Indicators: FAIL
    Evidence: .sisyphus/evidence/task-5-login-success.txt
  ```

  **Commit**: YES (group 2)
  - Message: `feat: implement core internal packages`
  - Files: `internal/login/login.go`, `internal/login/login_test.go`
  - Pre-commit: `go test ./internal/login/...`

---

- [ ] 6. cmd/campus-login/main.go — CLI 入口 + 命令分发

  **What to do**:
  - 完善 `cmd/campus-login/main.go`:
    - 导入所有 internal 包
    - `main()` 函数:
      - 调用 `color.Init()` 初始化颜色
      - 调用 `config.Setup()` 初始化配置
      - 解析 `os.Args`，用 switch/case 分发命令
    - 命令分发 (完全复刻 shell 版 case 逻辑):
      - 无参数 → `handleLogin("")` (使用默认账号)
      - `add <alias> <account> [password]` → 调用 `config.AddAccount()`，如果没提供密码则用 `term.ReadPassword()` 安全输入
      - `remove`/`rm <alias>` → `config.RemoveAccount()`
      - `list`/`ls` → `config.ListAccounts()` + 格式化输出 (包含 `[默认]` 标记)
      - `default <alias>` → `config.SetDefault()`
      - `status` → `network.CheckConnectivity()` + 输出结果
      - `help`/`--help`/`-h` → 显示帮助信息 (中文，与 shell 版一致风格)
      - `-*` → 未知选项错误
      - 其他 → 视为登录别名 `handleLogin(alias)`
    - `handleLogin(alias string)` 函数:
      - 若 alias 为空，读取默认账号
      - 获取凭证 `config.GetCredentials(alias)`
      - 获取本机 IP `network.GetLocalIP()`
      - 调用 `login.PerformLogin(account, password, ip)`
      - 输出结果 (颜色化)
    - 错误退出: 所有失败 (包括登录失败) 用 `os.Exit(1)` (修复 shell 版的 exit 0 bug)
    - 帮助信息完全用中文，风格与 shell 版一致，但加上 `status` 命令说明

  **Must NOT do**:
  - 不用 cobra/urfave
  - 不添加 `--verbose`/`--debug`/`--json` 等额外 flag
  - 不添加 `--config` flag
  - 不添加 logout 命令
  - 不创建日志框架

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 这是核心集成文件，需要正确组合所有 internal 包，复刻 shell 版的完整 CLI 行为
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖所有 Wave 1 任务)
  - **Parallel Group**: Wave 2 (solo)
  - **Blocks**: Task 7
  - **Blocked By**: Tasks 2, 3, 4, 5

  **References**:

  **Pattern References**:
  - `shell-bin:40-75` — `show_help()` 帮助文本: 完整的中文 CLI 帮助信息，包括用法、命令列表、示例
  - `shell-bin:134-154` — `handle_login()` 登录入口逻辑: 读取默认账号 → 验证存在 → 获取凭证 → 调用登录
  - `shell-bin:221-264` — `main()` + case 分发: 完整的命令路由逻辑
  - `shell-bin:159-177` — `add_account()` 中的 `read -s -p` 密码输入 → Go 中用 `term.ReadPassword(int(os.Stdin.Fd()))`
  - `shell-bin:232-263` — case 分支参数验证: 每个命令的参数数量检查

  **API/Type References**:
  - 所有 internal 包的公开 API:
    - `config.Setup()`, `config.AddAccount()`, `config.RemoveAccount()`, `config.ListAccounts()`, `config.GetDefault()`, `config.SetDefault()`, `config.GetCredentials()`
    - `login.PerformLogin()`, `login.ParseJSONP()`
    - `network.GetLocalIP()`, `network.CheckConnectivity()`
    - `color.Init()`, `color.Red`, `color.Green`, etc.

  **External References**:
  - `golang.org/x/term` — `term.ReadPassword(fd int)` 用于安全密码输入

  **Acceptance Criteria**:
  - [ ] `go build -o campus-login ./cmd/campus-login` 编译成功
  - [ ] `./campus-login help` 输出包含 "campus-login" 和中文
  - [ ] `./campus-login help` 输出包含 "status" 命令说明
  - [ ] `./campus-login add testuser 20230001 testpass` 后 `./campus-login list` 包含 testuser
  - [ ] `./campus-login default testuser` 后 list 显示 `[默认]`
  - [ ] `./campus-login rm testuser` 后 list 不包含 testuser
  - [ ] `./campus-login status` 输出连通性检测结果
  - [ ] 未知命令 `./campus-login --foo` 返回 exit code 1

  **QA Scenarios:**

  ```
  Scenario: 完整 CLI 工作流 - 添加/默认/列表/删除
    Tool: Bash
    Preconditions: 干净环境 (HOME=$(mktemp -d))
    Steps:
      1. export HOME=$(mktemp -d)
      2. ./campus-login add testuser 20230001 testpass
      3. ./campus-login default testuser
      4. ./campus-login list — 检查输出包含 "testuser" 和 "默认"
      5. ./campus-login rm testuser
      6. ./campus-login list — 检查输出不包含 "testuser"
    Expected Result: 所有步骤 exit code 0，list 输出符合预期
    Failure Indicators: 任何步骤 exit code 非 0 或输出不包含预期内容
    Evidence: .sisyphus/evidence/task-6-cli-workflow.txt

  Scenario: help 输出包含 status 命令
    Tool: Bash
    Preconditions: binary 已编译
    Steps:
      1. ./campus-login help | grep -q "status"
    Expected Result: grep exit code 0 (找到 status)
    Failure Indicators: grep exit code 1
    Evidence: .sisyphus/evidence/task-6-help-status.txt

  Scenario: 未知选项报错
    Tool: Bash
    Preconditions: binary 已编译
    Steps:
      1. ./campus-login --foo 2>&1; echo "EXIT: $?"
    Expected Result: 输出包含错误信息，EXIT: 1
    Failure Indicators: exit code 0 或无错误提示
    Evidence: .sisyphus/evidence/task-6-unknown-flag.txt
  ```

  **Commit**: YES (group 3)
  - Message: `feat: implement CLI entry point with full command dispatch`
  - Files: `cmd/campus-login/main.go`
  - Pre-commit: `go build -o campus-login ./cmd/campus-login && ./campus-login help`

---

- [ ] 7. 端到端集成测试 (binary 级别)

  **What to do**:
  - 编写 shell 脚本或直接用 bash 命令进行完整的端到端验证:
    - 编译: `go build -o campus-login ./cmd/campus-login`
    - 代码质量: `go vet ./...` + `go test ./...`
    - 配置文件兼容性: Go 版写入的配置能被 `grep` 正确解析 (格式兼容 shell 版)
    - 完整 CLI 工作流: add → list → default → list → rm → list
    - 边界情况: 空 config、无效 alias、重复 add、参数不足
    - status 命令基本输出验证 (不依赖实际网络状态，只验证命令不 crash)
  - 确保所有测试用临时 HOME 目录，不影响用户真实配置

  **Must NOT do**:
  - 不连接真实的校园网 portal 服务器
  - 不修改用户真实的 `~/.config/campus-login/`

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要编写并执行多步驌证脚本，处理边界情况
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖完整的 binary)
  - **Parallel Group**: Wave 3 (solo)
  - **Blocks**: F1-F4
  - **Blocked By**: Task 6

  **References**:

  **Pattern References**:
  - 所有前置任务的 QA Scenarios — 整合运行所有场景
  - `shell-bin:40-75` — help 输出参考: Go 版应包含相同命令 + status
  - `shell-bin:232-264` — 所有命令分支的参数验证逻辑

  **Acceptance Criteria**:
  - [ ] `go build` 成功
  - [ ] `go vet ./...` 零问题
  - [ ] `go test ./...` 全部通过
  - [ ] CLI 工作流完整通过 (add/list/default/rm)
  - [ ] 边界情况不 crash、返回有意义错误信息
  - [ ] 配置文件格式 `grep "^alias=account:password"` 成功

  **QA Scenarios:**

  ```
  Scenario: 完整端到端工作流
    Tool: Bash
    Preconditions: binary 已编译，HOME=$(mktemp -d)
    Steps:
      1. export HOME=$(mktemp -d)
      2. ./campus-login help | grep -q "campus-login" && echo "PASS: help" || echo "FAIL: help"
      3. ./campus-login add myacc 20230001 testpass && echo "PASS: add" || echo "FAIL: add"
      4. grep -q "^myacc=20230001:testpass" "$HOME/.config/campus-login/config" && echo "PASS: config-format" || echo "FAIL: config-format"
      5. ./campus-login default myacc && echo "PASS: default" || echo "FAIL: default"
      6. ./campus-login list | grep -q "默认" && echo "PASS: default-marker" || echo "FAIL: default-marker"
      7. ./campus-login rm myacc && echo "PASS: remove" || echo "FAIL: remove"
      8. ! grep -q "^myacc=" "$HOME/.config/campus-login/config" && echo "PASS: removed-from-file" || echo "FAIL: removed-from-file"
    Expected Result: 所有步骤输出 PASS
    Failure Indicators: 任何 FAIL 输出
    Evidence: .sisyphus/evidence/task-7-e2e-workflow.txt

  Scenario: 边界情况 - 无效别名登录
    Tool: Bash
    Preconditions: 干净 HOME
    Steps:
      1. export HOME=$(mktemp -d)
      2. ./campus-login nonexistent 2>&1; echo "EXIT: $?"
    Expected Result: 输出错误信息，EXIT: 1
    Failure Indicators: exit code 0 或 panic
    Evidence: .sisyphus/evidence/task-7-invalid-alias.txt

  Scenario: status 命令不 crash
    Tool: Bash
    Preconditions: binary 已编译
    Steps:
      1. ./campus-login status 2>&1; echo "EXIT: $?"
    Expected Result: 输出连通性检测结果 (不管成功失败)，不 panic
    Failure Indicators: panic 或 segfault
    Evidence: .sisyphus/evidence/task-7-status-nocrash.txt
  ```

  **Commit**: YES (group 4)
  - Message: `test: verify end-to-end CLI integration`
  - Files: (no new files, verification only)
  - Pre-commit: `go test ./... && go vet ./...`
## Final Verification Wave (MANDATORY — after ALL implementation tasks)

> 4 review agents run in PARALLEL. ALL must APPROVE. Rejection → fix → re-run.

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Read the plan end-to-end. For each "Must Have": verify implementation exists (read file, run command). For each "Must NOT Have": search codebase for forbidden patterns — reject with file:line if found. Check evidence files exist in .sisyphus/evidence/. Compare deliverables against plan.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run `go vet ./...` + `go test ./...`. Review all Go files for: `any` type abuse, empty error handling (`_ = err`), `fmt.Println` in library code (should only be in main), unused imports. Check AI slop: excessive comments, over-abstraction, generic variable names (data/result/item/temp). Verify no `pkg/` directory exists. Verify no third-party imports in go.mod (except `golang.org/x/term`).
  Output: `Vet [PASS/FAIL] | Tests [N pass/N fail] | Files [N clean/N issues] | VERDICT`

- [ ] F3. **Real Manual QA** — `unspecified-high`
  Start from clean state (`HOME=$(mktemp -d)`). Execute EVERY QA scenario from EVERY task — follow exact steps, capture evidence. Test cross-task integration: add account → set default → login attempt → list shows default → remove → list empty. Test edge cases: empty config, missing config dir, invalid alias. Save to `.sisyphus/evidence/final-qa/`.
  Output: `Scenarios [N/N pass] | Integration [N/N] | Edge Cases [N tested] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  For each task: read "What to do", read actual code. Verify 1:1 — everything in spec was built (no missing), nothing beyond spec was built (no creep). Check "Must NOT do" compliance. Detect extra commands, flags, or features not in shell version (except `status`). Compare shell-bin CLI interface line-by-line with Go binary's `help` output.
  Output: `Tasks [N/N compliant] | Scope [CLEAN/N issues] | VERDICT`

---

## Commit Strategy

| Commit | Tasks | Message | Verify |
|--------|-------|---------|--------|
| 1 | 1 | `chore: init Go project scaffolding` | `go build ./...` |
| 2 | 2, 3, 4, 5 | `feat: implement core internal packages (config, login, network, color)` | `go test ./...` |
| 3 | 6 | `feat: implement CLI entry point with full command dispatch` | `go build ./cmd/campus-login && ./campus-login help` |
| 4 | 7 | `test: add e2e integration verification` | `go test ./... && go vet ./...` |

---

## Success Criteria

### Verification Commands
```bash
go build -o campus-login ./cmd/campus-login  # Expected: binary created, zero errors
go vet ./...                                   # Expected: zero issues
go test ./...                                  # Expected: all PASS
./campus-login help                            # Expected: 中文帮助信息
./campus-login status                          # Expected: 连通性检测结果
```

### Final Checklist
- [ ] All "Must Have" present
- [ ] All "Must NOT Have" absent
- [ ] All unit tests pass
- [ ] Config file format compatible with shell version
- [ ] Binary size reasonable (single static binary)
- [ ] shell-bin preserved unmodified
