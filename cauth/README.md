# cauth

校园网命令行登录工具，支持多账号管理，单一静态二进制，零配置即用。

适用于重庆大学校园网（`login.cqu.edu.cn`）认证登录。

## 安装

需要 Go 1.25+：

```bash
# 动态链接编译
go build -o output/cauth ./cmd/cauth

# 静态链接编译
CGO_ENABLED=0 go build -o output/cauth ./cmd/cauth

# 交叉编译 for windows
CGO_ENABLED=0 GOOS=windows GOARCH=amd64 go build -o output/cauth.exe ./cmd/cauth
```

将编译产物移动到 `$PATH` 下即可：

```bash
# 移动到系统根目录
sudo mv cauth /usr/local/bin/

# 或者移动到用户目录
mv cauth ~/.local/bin/
```

## 快速开始

```bash
# 1. 添加账号（密码会提示安全输入，不回显）
cauth add myacc 20230001

# 2. 设置为默认账号
cauth set myacc

# 3. 一键登录
cauth
```

## 用法

```
cauth [命令] [参数...]
cauth -i <网口> [命令] [参数...]   # 指定出网网口
```

### 登录

```bash
cauth              # 使用默认账号登录
cauth <别名>       # 使用指定账号登录
```

### 账号管理

```bash
cauth add <别名> <账号> [密码]   # 添加账号，不提供密码则安全输入
cauth remove <别名>              # 删除账号（rm 同义）
cauth list                       # 列出所有账号（ls 同义）
cauth set <别名>             # 设置默认账号
```

### 守护进程

适用于 systemd 等服务管理器，持续保持校园网在线：

```bash
cauth daemon                # 默认账号，60s 检测间隔
cauth daemon myacc          # 指定账号
cauth daemon 5m             # 默认账号，5 分钟间隔
cauth daemon myacc 2m       # 指定账号 + 2 分钟间隔
```

守护进程会周期性检测外网连通性，断线时自动重连。连续失败时采用指数退避策略（最长 30 分钟）。
支持 `Ctrl-C` / `SIGTERM` 优雅退出。

> daemon 前台阻塞运行。长期运行建议交给 systemd 托管，见下文「开机自启（systemd）」。

### 其他

```bash
cauth status       # 检测当前外网连通性
cauth iface        # 列出可用网络接口
cauth help         # 显示帮助信息
```

### 指定网口（多网卡环境）

当机器上有多个网络接口时，默认由系统路由选择出口。两种指定方式：

```bash
cauth iface                  # 查看可用网口和 IP
cauth iface enp2s0           # 将 enp2s0 设为默认网口（写入配置文件）
cauth iface auto             # 清除默认网口，恢复系统路由自动选择

cauth -i enp2s0 status       # -i 临时指定，仅本次生效（优先级高于默认网口）
cauth -i enp2s0 daemon 5m    # 守护进程走指定网口
```

设置默认网口后，登录、注销、状态检测、守护进程都会走该网口：登录请求、
上报给门户的 IP、连通性检测全部绑定（Linux 下使用 `SO_BINDTODEVICE`）。
`-i` 参数只影响当次运行，不会修改配置。

## 开机自启（systemd）

`cauth daemon` 阻塞前台运行，生产环境交给 systemd 托管。两种方式的核心差异：

| | 用户服务（`--user`） | 系统服务 |
|---|---|---|
| 单元文件位置 | `~/.config/systemd/user/cauth.service` | `/etc/systemd/system/cauth.service` |
| 运行身份 | 当前用户，无需 root | 默认 root，可用 `User=` 指定普通用户 |
| 开机自启 | 需 `loginctl enable-linger`，否则随登录会话退出而停止 | 天然支持，无需任何人登录 |
| 适用场景 | 桌面机、单人使用的机器 | 服务器（推荐） |

两种方式读取的都是运行账号的 `~/.config/cauth/config`，配置完全共用。

### 方式一：用户服务

```ini
# ~/.config/systemd/user/cauth.service
[Unit]
Description=cauth campus network daemon

[Service]
ExecStart=/usr/local/bin/cauth daemon
Restart=on-failure
RestartSec=10

[Install]
WantedBy=default.target
```

```bash
systemctl --user daemon-reload
systemctl --user enable --now cauth.service
sudo loginctl enable-linger $USER    # 关键：注销后继续运行，装好后执行一次
journalctl --user -u cauth -f        # 查看日志
```

两个易踩的坑：

- **不开启 linger，用户服务会随最后一次登录会话一起被停止**——SSH 登出 daemon 就退了，
  这是用户服务最常见的问题。`enable-linger` 需要 sudo 执行一次，之后重启也不需要登录。
- **`ExecStart` 必须写绝对路径**。systemd 用户管理器的 PATH 通常不包含 `~/.local/bin`，
  装在那里的写 `/home/<用户名>/.local/bin/cauth`。

### 方式二：系统服务（服务器推荐）

```ini
# /etc/systemd/system/cauth.service
[Unit]
Description=cauth campus network daemon
After=network-online.target
Wants=network-online.target

[Service]
# 必须指定 User，否则以 root 运行，读取的是 /root/.config/cauth/config（通常没有账号）
User=<用户名>
ExecStart=/usr/local/bin/cauth daemon
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now cauth.service
journalctl -u cauth -f
```

说明：

- `User=` 填实际运行账号。该账号需先在交互终端完成 `cauth add` / `cauth set`
  （密码安全输入需要 TTY，无法在 systemd 里完成）。
- 需要指定账号或间隔时直接追加参数：`ExecStart=/usr/local/bin/cauth daemon myacc 2m`。
- 多网卡环境先用 `cauth iface <网口>` 写入默认网口，daemon 启动时自动绑定，
  效果等同 `-i`。
- `network-online.target` 等网络就绪后再启动，避免开机首轮探测全部超时；
  即便如此，daemon 自身的退避重试也能容忍网络晚就绪。

## 配置文件

配置文件位于 `$XDG_CONFIG_HOME/cauth/config`，默认为 `~/.config/cauth/config`。

格式为纯文本，每行一个账号：

```
myacc=20230001:password123
otheracc=20240002:anotherpass
default_account=myacc
```

## 项目结构

```
├── cmd/cauth/main.go     # CLI 入口
└── internal/
    ├── color/                   # TTY 颜色检测
    ├── config/                  # 配置文件读写
    ├── daemon/                  # 守护进程循环 + 退避策略
    ├── login/                   # HTTP 登录 + JSONP 解析
    └── network/                 # 本机 IP 获取 + 连通性检测
```

## 依赖

仅依赖 Go 标准库和 `golang.org/x/term`（密码安全输入），无第三方框架。

## License

MIT
