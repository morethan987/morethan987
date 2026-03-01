# campus-login

校园网命令行登录工具，支持多账号管理，单一静态二进制，零配置即用。

适用于重庆大学校园网（`login.cqu.edu.cn`）认证登录。

## 安装

需要 Go 1.25+：

```bash
# 动态链接编译
go build -o output/campus-login ./cmd/campus-login

# 静态链接编译
CGO_ENABLED=0 go build -o output/campus-login ./cmd/campus-login

# 交叉编译 for windows
CGO_ENABLED=0 GOOS=windows GOARCH=amd64 go build -o output/campus-login.exe ./cmd/campus-login
```

将编译产物移动到 `$PATH` 下即可：

```bash
# 移动到系统根目录
sudo mv campus-login /usr/local/bin/

# 或者移动到用户目录
mv campus-login ~/.local/bin/
```

> 根目录中有两个已经预编译完成的文件，针对 Linux/Windows + AMD64

## 快速开始

```bash
# 1. 添加账号（密码会提示安全输入，不回显）
campus-login add myacc 20230001

# 2. 设置为默认账号
campus-login set myacc

# 3. 一键登录
campus-login
```

## 用法

```
campus-login [命令] [参数...]
```

### 登录

```bash
campus-login              # 使用默认账号登录
campus-login <别名>       # 使用指定账号登录
```

### 账号管理

```bash
campus-login add <别名> <账号> [密码]   # 添加账号，不提供密码则安全输入
campus-login remove <别名>              # 删除账号（rm 同义）
campus-login list                       # 列出所有账号（ls 同义）
campus-login set <别名>             # 设置默认账号
```

### 守护进程

适用于 systemd 等服务管理器，持续保持校园网在线：

```bash
campus-login daemon                # 默认账号，60s 检测间隔
campus-login daemon myacc          # 指定账号
campus-login daemon 5m             # 默认账号，5 分钟间隔
campus-login daemon myacc 2m       # 指定账号 + 2 分钟间隔
```

守护进程会周期性检测外网连通性，断线时自动重连。连续失败时采用指数退避策略（最长 30 分钟）。
支持 `Ctrl-C` / `SIGTERM` 优雅退出。

> 如果运行在服务器上推荐使用 systemd 进行开机自启动管理

### 其他

```bash
campus-login status       # 检测当前外网连通性
campus-login help         # 显示帮助信息
```

## 配置文件

配置文件位于 `$XDG_CONFIG_HOME/campus-login/config`，默认为 `~/.config/campus-login/config`。

格式为纯文本，每行一个账号：

```
myacc=20230001:password123
otheracc=20240002:anotherpass
default_account=myacc
```

## 项目结构

```
├── cmd/campus-login/main.go     # CLI 入口
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
