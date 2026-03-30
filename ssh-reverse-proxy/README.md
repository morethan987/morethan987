# srp (SSH Reverse Proxy)

`srp` 是一个用 Go 编写的 SSH 反向隧道管理工具。它通过一个单一的二进制文件同时提供 CLI 客户端和后台守护进程（Daemon），让你可以方便地创建、管理和监控多个 SSH 反向代理隧道。

## 它解决什么问题

假设你有一台远程服务器，你想把远程服务器上的某个端口映射到你的本地机器上（比如让远程服务器通过 `localhost:7890` 访问你本地的服务）。SSH 的 `-R` 参数可以做到这一点，但手动管理多个 SSH 隧道进程很快就会变得混乱：

- 连接断开后需要手动重新连接
- 多个隧道需要多个终端窗口或 tmux pane
- 难以查看哪些隧道正在运行、哪些已经挂了

`srp` 解决了这些问题。它在后台以守护进程方式运行，负责管理所有 SSH 隧道进程的生命周期。

## 快速开始

### 前置条件

- Linux 操作系统
- Go 1.21+ （如果从源码编译）
- 可用的 SSH 别名配置（在 `~/.ssh/config` 中）

### 安装

```bash
go install github.com/morethan987/ssh-reverse-proxy/cmd/srp@latest
```

或者从源码编译：

```bash
git clone https://github.com/morethan987/ssh-reverse-proxy.git
cd ssh-reverse-proxy
CGO_ENABLED=0 go build -o srp ./cmd/srp
sudo mv srp /usr/local/bin/
```

### 第一次使用

`srp` 使用 SSH 别名来标识目标服务器。确保你的 `~/.ssh/config` 中已经配置好了对应的 Host，例如：

```ssh-config
Host my-server
    HostName 192.168.1.100
    User deploy
    IdentityFile ~/.ssh/id_ed25519
```

然后就可以直接使用了——`srp` 会在首次运行时自动创建默认配置：

```bash
# 添加一个服务器配置（会使用默认端口 7890）
srp add my-server

# 设置为默认目标
srp set my-server

# 启动反向代理（使用默认目标）
srp
# 输出: Current default SRP target: my-server
# 输出: Starting...
# 输出: Started my-server

# 查看状态
srp status
# ALIAS       STATUS   PID    REMOTE  LOCAL  RESTARTS  STARTED
# my-server   running  12345  7890    7890   0         2026-03-30 10:00:00
# Current default SRP target: my-server
```

就这样。`srp` 会在后台启动一个守护进程来管理 SSH 隧道。如果 SSH 连接断开，守护进程会在 10 秒后自动重连。

## 命令详解

### 完整用法

```
srp [start|status|stop|set|current|add|remove|list|kill] [alias]
```

### 无参数运行

```bash
srp
```

显示当前默认目标，然后自动启动该目标的反向代理。这是最常用的快捷方式。

### start — 启动反向代理

```bash
srp start              # 使用默认目标启动
srp start my-server    # 启动指定别名的反向代理
```

实际上就是让守护进程在后台 spawn 一个 SSH 子进程，执行类似这样的命令：

```bash
/usr/bin/ssh -NT -o ServerAliveInterval=60 -o ExitOnForwardFailure=yes -R 7890:localhost:7890 my-server
```

如果该别名的隧道已经在运行，会提示错误。

### stop — 停止反向代理

```bash
srp stop               # 停止默认目标
srp stop my-server     # 停止指定别名的隧道
```

守护进程会先发送 `SIGTERM`，等待 5 秒后若进程仍未退出则发送 `SIGKILL`。

### status — 查看运行状态

```bash
srp status             # 列出所有运行中的隧道 + 显示默认目标
srp status my-server   # 查看指定别名的详细状态
```

无参数时会输出对齐的表格：

```
ALIAS        STATUS   PID    REMOTE  LOCAL  RESTARTS  STARTED
my-server    running  12345  7890    7890   0         2026-03-30 10:00:00
jump-campus  stopped  0      7890    7890   2         2026-03-30 09:50:00
Current default SRP target: my-server
```

### set — 设置默认目标

```bash
srp set my-server
# 输出: Default SRP target set to: my-server
```

将指定别名持久化保存为默认目标。之后所有省略别名的命令（如 `srp start`、`srp stop`）都会使用这个目标。

### current — 查看当前默认目标

```bash
srp current
# 输出: Current default SRP target: my-server
```

### add — 添加服务器配置

```bash
srp add my-server              # 使用默认端口（7890）
srp add my-server -p 8080      # 指定远程端口为 8080
srp add my-server -p 8080 -p   # 语法错误，会提示 invalid port
```

这会在配置文件中为指定别名创建一条服务器记录。添加之后可以用 `srp set` 设为默认，用 `srp start` 启动。

### remove — 移除服务器配置

```bash
srp remove my-server
# 输出: Removed server 'my-server'
```

如果该服务器正在运行，会先停止隧道，再从配置中移除。

### list — 列出所有已配置的服务器

```bash
srp list
```

输出示例：

```
another-server  port=7890   status=stopped
my-server       port=8080   status=running
```

按别名字母序排列，显示每个服务器的远程端口和当前运行状态。

### kill — 停止守护进程

```bash
srp kill
# 输出: Daemon stopped
```

停止守护进程及其管理的所有 SSH 隧道。执行流程：

1. 读取 PID 文件（`~/.config/srp/srp.pid`），找到守护进程
2. 发送 `SIGTERM`，守护进程收到后会优雅停止所有 SSH 子进程并清理 socket 和 PID 文件
3. 等待最多 5 秒，若进程已退出则输出 `Daemon stopped`
4. 超时未退出则强制 `SIGKILL`，并手动清理残留文件

> **注意**：`srp kill` 是纯客户端操作，不经过 IPC。它直接通过信号与守护进程通信，因此即使守护进程的 socket 已经异常，也能正常工作。

下次执行任意 `srp` 命令时，守护进程会自动重新启动。

### help — 帮助

```bash
srp --help
srp -h
```

### Shell 补全

`srp` 支持 bash、zsh、fish 的命令补全，包括子命令、已配置的 alias 和 `~/.ssh/config` 中的 Host。

```bash
# 安装补全（会提示确认，支持 bash/zsh/fish 自动检测）
COMP_INSTALL=1 srp

# 卸载补全
COMP_UNINSTALL=1 srp
```

安装后重启 shell 即可生效。补全内容：

- 子命令：`start`、`stop`、`status`、`set`、`current`、`add`、`remove`、`list`、`kill`
- 已配置的 alias：`srp start <Tab>` 自动补全 `config.toml` 中的服务器别名
- SSH Host：`srp add <Tab>` 自动补全 `~/.ssh/config` 中的 Host 名称

## 配置文件

配置文件位于 `~/.config/srp/config.toml`（遵循 [XDG Base Directory](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html) 规范，可通过 `XDG_CONFIG_HOME` 环境变量覆盖）。

首次运行时会自动创建带有默认值的配置文件，内容如下：

```toml
[global]
  default = ""
  remote_port = 7890
  local_port = 7890
  ssh_options = ["ServerAliveInterval=60", "ExitOnForwardFailure=yes"]

[servers]
```

### 完整配置示例

```toml
[global]
  # 省略别名参数时的默认操作目标
  default = "my-server"

  # 默认的远程端口（远程服务器上监听的端口）
  remote_port = 7890

  # 默认的本地端口（转发到本地的端口）
  local_port = 7890

  # 传递给 ssh 命令的额外 -o 参数
  ssh_options = [
    "ServerAliveInterval=60",
    "ExitOnForwardFailure=yes",
  ]

[servers]
  # 每个服务器的独立配置，端口设为 0 时继承 global 的值
  [servers.my-server]
    remote_port = 8080
    local_port = 8080

  [.servers.another-server]
    remote_port = 0    # 继承 global.remote_port (7890)
    local_port = 0     # 继承 global.local_port (7890)

  [servers.production]
    remote_port = 3000
    local_port = 3000
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `global.default` | string | `""` | 省略别名时的默认目标。空字符串表示未设置 |
| `global.remote_port` | int | `7890` | 远程服务器上监听的端口 |
| `global.local_port` | int | `7890` | 本地机器上被转发的端口 |
| `global.ssh_options` | []string | `["ServerAliveInterval=60", "ExitOnForwardFailure=yes"]` | 传递给 `ssh` 的 `-o` 参数列表 |
| `servers.<alias>.remote_port` | int | — | 该服务器的远程端口覆盖。设为 `0` 时继承 global 值 |
| `servers.<alias>.local_port` | int | — | 该服务器的本地端口覆盖 |

> **注意**：`local_port` 在 `srp add` 命令中暂不支持单独指定，目前仅通过直接编辑配置文件设置。CLI 只接受 `-p` 参数来设置 `remote_port`。

## 工作原理

### 架构概览

```
┌──────────────────────┐         Unix Socket          ┌──────────────────────────────────┐
│   srp (CLI 客户端)   │ ◄──────────────────────────► │   srp (Daemon 守护进程)          │
│                      │     JSON Request/Response    │                                  │
│  srp start my-server │                              │  ┌────────────┐  ┌────────────┐  │
│  srp status          │                              │  │ SSH 子进程 │  │ SSH 子进程 │  │
│  srp add ...         │                              │  │ (server1)  │  │ (server2)  │  │
└──────────────────────┘                              │  └────────────┘  └────────────┘  │
                                                      └──────────────────────────────────┘
```

### 运行模式

`srp` 是一个单二进制文件，根据环境变量 `SRP_DAEMON` 的值切换运行模式：

- **客户端模式**（默认）：解析命令行参数，通过 Unix Domain Socket 将指令发送给守护进程
- **守护进程模式**（`SRP_DAEMON=1`）：启动后台服务，监听 Unix Socket，管理 SSH 子进程池

### 自动启动机制

当你执行任何 `srp` 命令时，客户端会先尝试连接守护进程的 Unix Socket。如果连接失败（说明守护进程未运行），客户端会：

1. 自动 fork 出当前二进制文件，设置 `SRP_DAEMON=1` 环境变量
2. 将守护进程的 stdout/stderr 重定向到日志文件
3. 最多等待 5 秒，轮询检查 socket 文件是否创建成功
4. 超时则报错退出

你不需要手动启动或管理守护进程，它完全是自动的。

### SSH 子进程管理

守护进程为每个活跃的隧道管理一个 SSH 子进程：

- **启动**：执行 `/usr/bin/ssh -NT -o <options> -R <remote>:localhost:<local> <alias>`
- **监控**：后台 goroutine 监控进程退出，非主动停止的退出会触发自动重启
- **自动重启**：连接断开后等待 10 秒，然后自动重新启动（重启计数会递增）
- **优雅停止**：先发送 `SIGTERM`，等待 5 秒，超时后 `SIGKILL`

### IPC 协议

客户端和守护进程通过 Unix Domain Socket（`~/.config/srp/srp.sock`）通信，使用 JSON 格式：

**请求格式**：
```json
{"action": "start", "alias": "my-server", "port": 8080}
```

**响应格式**：
```json
{"success": true, "message": "Started my-server", "data": null}
```

**支持的动作（action）**：

| action | 说明 | alias 必需 | port |
|--------|------|:----------:|:----:|
| `start` | 启动隧道 | 否（使用默认） | 可选 |
| `stop` | 停止隧道 | 否（使用默认） | — |
| `status` | 查询状态 | 否（空 = 全部） | — |
| `set` | 设置默认目标 | 是 | — |
| `current` | 查看默认目标 | — | — |
| `add` | 添加服务器 | 是 | 可选 |
| `remove` | 移除服务器 | 是 | — |
| `list` | 列出所有服务器 | — | — |

> **注意**：`kill` 命令不经过 IPC，由客户端直接通过信号管理守护进程。

## 文件路径

所有文件默认存放在 `~/.config/srp/` 目录下，遵循 XDG Base Directory 规范：

| 文件 | 路径 | 说明 |
|------|------|------|
| 配置文件 | `~/.config/srp/config.toml` | TOML 格式的服务器配置和全局设置 |
| IPC Socket | `~/.config/srp/srp.sock` | Unix Domain Socket，客户端与守护进程通信 |
| PID 文件 | `~/.config/srp/srp.pid` | 守护进程的进程 ID |
| 日志文件 | `~/.config/srp/srp.log` | 守护进程运行日志 |

可通过设置 `XDG_CONFIG_HOME` 环境变量改变根目录，例如：

```bash
export XDG_CONFIG_HOME=/opt/srp
# 配置文件变为 /opt/srp/srp/config.toml
```

## 与原版本（Shell + systemd）对比

本项目从最初的 zsh 脚本 + systemd 服务模板重写而来：

| 特性 | 原版本（Shell + systemd） | Go 版本 |
|------|:-------------------------:|:-------:|
| 运行依赖 | zsh + systemd | 单二进制，无外部依赖 |
| 进程管理 | 每个隧道一个 systemd service | 守护进程内管理进程池 |
| 配置存储 | 纯文本文件存储默认别名 | TOML 结构化配置 |
| 服务器管理 | 需手动编辑配置 | `add`/`remove`/`list` 命令 |
| 多隧道 | 支持（每个一个 .service） | 支持（单一守护进程） |
| 自动重启 | systemd `Restart=always` | 内置自动重启（10 秒延迟） |
| 通信方式 | `systemctl --user` 子进程 | Unix Domain Socket IPC |
| 日志 | journald | 文件日志（`srp.log`） |

### 迁移方法

如果你之前使用的是原版 Shell 脚本：

1. 停止所有旧的 systemd 服务：`systemctl --user stop 'ssh-reverse-proxy@*.service'`
2. 安装 Go 版本
3. 用 `srp add` 添加你的服务器
4. 用 `srp set` 设置默认目标

原版中的 `default` 文件（`~/.config/srp/default`）不会被自动读取，需要重新用 `srp set` 设置。

## 常见问题

### 守护进程怎么停止？

使用 `srp kill` 命令：

```bash
srp kill
```

或者手动发送 `SIGTERM`：

```bash
kill $(cat ~/.config/srp/srp.pid)
```

守护进程收到 `SIGTERM` 或 `SIGINT` 后会自动停止所有 SSH 子进程并清理 socket 和 PID 文件。下次执行任意 `srp` 命令时，守护进程会自动重新启动。

### 修改配置后需要重启吗？

是的。守护进程在启动时读取配置文件，之后不会自动重载。修改 `config.toml` 后需要先停止守护进程，再执行任意 `srp` 命令让其自动重启：

```bash
srp kill
srp status   # 触发自动重启
```

或者通过 `srp add`/`srp remove`/`srp set` 命令修改配置，这些命令会直接更新配置文件并在下次 start 时生效。

### SSH 别名找不到？

`srp` 直接将别名传给 `/usr/bin/ssh`，SSH 客户端会读取 `~/.ssh/config` 来解析别名。如果 SSH 找不到别名，请检查：

1. `~/.ssh/config` 文件是否存在且格式正确
2. Host 名称是否与 `srp add` 的别名一致
3. `ssh my-server` 能否直接连接

### 端口 7890 已经被占用？

修改 `~/.config/srp/config.toml` 中的 `global.remote_port` 和 `global.local_port`，或为特定服务器设置不同的端口。

### 日志在哪里？

查看 `~/.config/srp/srp.log`：

```bash
tail -f ~/.config/srp/srp.log
```

日志包含所有 IPC 请求的处理记录，格式为：

```
2026/03/30 10:00:00 [INFO] daemon started on socket /home/user/.config/srp/srp.sock
2026/03/30 10:00:05 [INFO] action=start alias=my-server success=true msg=Started my-server
```

## 项目结构

```
ssh-reverse-proxy/
├── cmd/srp/
│   └── main.go              # CLI 入口，客户端/守护进程模式路由
├── internal/
│   ├── client/
│   │   └── client.go        # IPC 客户端，Unix Socket 通信
│   ├── completion/
│   │   └── completion.go    # Shell 补全定义（bash/zsh/fish）
│   ├── config/
│   │   └── config.go        # TOML 配置加载/保存/CRUD
│   ├── daemon/
│   │   └── daemon.go        # 守护进程核心，8 个 IPC 动作处理
│   ├── ipc/
│   │   └── types.go         # 请求/响应类型定义
│   ├── logger/
│   │   └── logger.go        # 文件日志记录器
│   ├── ssh/
│   │   └── manager.go       # SSH 子进程管理，自动重启
│   └── xdg/
│       └── xdg.go           # XDG 路径解析
├── integration_test.go       # 集成测试
└── README.md
```

## 开发

### 构建

```bash
CGO_ENABLED=0 go build -o srp ./cmd/srp
```

### 测试

```bash
go test ./...
```

### 依赖

- [BurntSushi/toml](https://github.com/BurntSushi/toml) — TOML 解析
- [posener/complete/v2](https://github.com/posener/complete) — Shell 补全（bash/zsh/fish）
- [stretchr/testify](https://github.com/stretchr/testify) — 测试断言（仅测试依赖）

## Shell 补全

`srp` 支持 bash、zsh 和 fish 的命令补全，包括：

- 子命令补全（`start`、`stop`、`status` 等）
- 动态 alias 补全（从配置文件读取已添加的服务器）
- SSH Host 补全（`srp add` 时从 `~/.ssh/config` 读取）

### 安装补全

```bash
COMP_INSTALL=1 srp
```

重启 shell 后生效。

### 卸载补全

```bash
COMP_UNINSTALL=1 srp
```

## License

MIT
