package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"golang.org/x/term"

	"github.com/morethan987/cauth/internal/color"
	"github.com/morethan987/cauth/internal/config"
	"github.com/morethan987/cauth/internal/daemon"
	"github.com/morethan987/cauth/internal/login"
	"github.com/morethan987/cauth/internal/network"
)

func main() {
	color.Init()

	if err := config.Setup(); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 初始化配置失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	args := os.Args[1:]

	// Global flag: -i/--interface <name> selects the outbound network interface.
	ifaceName := ""
	if len(args) > 0 && (args[0] == "-i" || args[0] == "--interface") {
		if len(args) < 2 {
			fmt.Fprintf(os.Stderr, "%s错误: '-i' 需要一个网口名参数%s\n", color.Red, color.NC)
			fmt.Fprintln(os.Stderr, "用法: cauth -i <网口名> [命令] [参数...]  (使用 'cauth iface' 查看网口列表)")
			os.Exit(1)
		}
		ifaceName = args[1]
		args = args[2:]
	}

	// Fall back to the saved default interface (set via "cauth iface <name>").
	if ifaceName == "" {
		saved, err := config.GetDefaultIface()
		if err != nil {
			fmt.Fprintf(os.Stderr, "%s错误: 读取默认网口配置失败: %s%s\n", color.Red, err, color.NC)
			os.Exit(1)
		}
		ifaceName = saved
	}
	pendingIface = ifaceName

	if len(args) == 0 {
		handleLogin("")
		return
	}

	command := args[0]

	switch command {
	case "add":
		cmdAdd(args[1:])
	case "remove", "rm":
		cmdRemove(args[1:])
	case "list", "ls":
		cmdList()
	case "default", "def", "set":
		cmdDefault(args[1:])
	case "status":
		cmdStatus()
	case "daemon":
		cmdDaemon(args[1:])
	case "logout":
		cmdLogout(args[1:])
	case "iface", "ifaces", "interfaces":
		cmdIface(args[1:])
	case "help", "--help", "-h":
		showHelp()
	default:
		if strings.HasPrefix(command, "-") {
			fmt.Fprintf(os.Stderr, "%s错误: 未知选项 '%s'%s\n", color.Red, command, color.NC)
			fmt.Fprintf(os.Stderr, "使用 'cauth help' 查看可用命令\n")
			os.Exit(1)
		}
		handleLogin(command)
	}
}

// pendingIface holds the interface selected via -i or the saved default
// (empty = automatic OS routing).
var pendingIface string

// applyIface binds the pending interface for all outgoing traffic. It is
// called by commands that touch the network; management commands (list, add,
// iface, ...) run without it so a broken interface never blocks fixing the
// configuration.
func applyIface() {
	if pendingIface == "" {
		return
	}

	ip, err := network.GetInterfaceIP(pendingIface)
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: %s%s\n", color.Red, err, color.NC)
		fmt.Fprintln(os.Stderr, "使用 'cauth iface' 查看可用网口, 或 'cauth iface auto' 恢复自动选择")
		os.Exit(1)
	}
	network.SourceIface = pendingIface
	network.SourceIP = ip
	fmt.Printf("==> 使用网口 [%s%s%s] (%s)\n", color.Yellow, pendingIface, color.NC, ip)
}

// handleLogin performs a login using the given alias, or the default account if alias is empty.
func handleLogin(alias string) {
	applyIface()

	if alias == "" {
		defaultAlias, err := config.GetDefault()
		if err != nil {
			fmt.Fprintf(os.Stderr, "%s错误: 读取默认账号失败: %s%s\n", color.Red, err, color.NC)
			os.Exit(1)
		}
		if defaultAlias == "" {
			fmt.Fprintf(os.Stderr, "%s错误: 未设置默认账号%s\n", color.Red, color.NC)
			fmt.Fprintln(os.Stderr, "请先使用 'cauth add' 添加一个账号，然后使用 'cauth default' 设置默认账号")
			os.Exit(1)
		}
		alias = defaultAlias
	}

	account, password, err := config.GetCredentials(alias)
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 账号别名 [%s%s%s] 未找到%s\n", color.Red, color.Yellow, alias, color.Red, color.NC)
		fmt.Fprintln(os.Stderr, "使用 'cauth list' 查看所有已保存的账号")
		os.Exit(1)
	}

	localIP := network.GetLocalIPForHost(login.PortalHost)

	success, msg, err := login.PerformLogin(account, password, localIP)
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s==> 登录失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	if success {
		fmt.Printf("%s==> 登录成功! 服务器消息: %s%s\n", color.Green, msg, color.NC)
	} else if login.AlreadyOnline(msg) {
		fmt.Printf("%s==> 已在线! 服务器消息: %s%s\n", color.Green, msg, color.NC)
	} else {
		if msg == "" {
			msg = "未知错误，可能是账号密码错误或已在别处登录"
		}
		fmt.Fprintf(os.Stderr, "%s==> 登录失败! 服务器消息: %s%s\n", color.Red, msg, color.NC)
		os.Exit(1)
	}
}

// cmdAdd handles the "add" subcommand.
func cmdAdd(args []string) {
	if len(args) < 2 || len(args) > 3 {
		fmt.Fprintln(os.Stderr, "用法: cauth add <别名> <账号> [密码]")
		os.Exit(1)
	}

	alias := args[0]
	account := args[1]
	var password string

	if len(args) == 3 {
		password = args[2]
	} else {
		fmt.Fprintf(os.Stderr, "请输入账号 [%s] 的密码: ", account)
		pwBytes, err := term.ReadPassword(int(os.Stdin.Fd()))
		fmt.Fprintln(os.Stderr) // newline after password entry
		if err != nil {
			fmt.Fprintf(os.Stderr, "%s错误: 读取密码失败: %s%s\n", color.Red, err, color.NC)
			os.Exit(1)
		}
		password = string(pwBytes)
	}

	if password == "" {
		fmt.Fprintf(os.Stderr, "%s错误: 密码不能为空%s\n", color.Red, color.NC)
		os.Exit(1)
	}

	if err := config.AddAccount(alias, account, password); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 保存账号失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	fmt.Printf("账号 [%s%s%s] 已保存\n", color.Yellow, alias, color.NC)
}

// cmdRemove handles the "remove"/"rm" subcommand.
func cmdRemove(args []string) {
	if len(args) != 1 {
		fmt.Fprintln(os.Stderr, "用法: cauth remove <别名>")
		os.Exit(1)
	}

	alias := args[0]
	if err := config.RemoveAccount(alias); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 删除账号失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	fmt.Printf("账号 [%s%s%s] 已删除\n", color.Yellow, alias, color.NC)
}

// cmdList handles the "list"/"ls" subcommand.
func cmdList() {
	accounts, err := config.ListAccounts()
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 读取账号列表失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	fmt.Printf("%s已保存的账号列表:%s\n", color.Blue, color.NC)

	if len(accounts) == 0 {
		fmt.Println("  (空)")
		return
	}

	for _, acc := range accounts {
		if acc.IsDefault {
			fmt.Printf("  - %s%s%s (账号: %s) %s[默认]%s\n",
				color.Yellow, acc.Alias, color.NC, acc.AccountID, color.Green, color.NC)
		} else {
			fmt.Printf("  - %s (账号: %s)\n", acc.Alias, acc.AccountID)
		}
	}
}

// cmdDefault handles the "default" subcommand.
func cmdDefault(args []string) {
	if len(args) != 1 {
		fmt.Fprintln(os.Stderr, "用法: cauth default <别名>")
		os.Exit(1)
	}

	alias := args[0]
	if err := config.SetDefault(alias); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 账号别名 [%s%s%s] 不存在，无法设为默认%s\n",
			color.Red, color.Yellow, alias, color.Red, color.NC)
		os.Exit(1)
	}

	fmt.Printf("已将 [%s%s%s] 设置为默认登录账号\n", color.Yellow, alias, color.NC)
}

// cmdStatus handles the "status" subcommand.
func cmdStatus() {
	applyIface()

	connected, err := network.CheckConnectivity()
	if err != nil {
		fmt.Printf("%s==> 网络不可达: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	if connected {
		fmt.Printf("%s==> 网络已连通，当前可以正常访问互联网%s\n", color.Green, color.NC)
	} else {
		fmt.Printf("%s==> 检测到强制门户（未登录），请登录校园网%s\n", color.Yellow, color.NC)
	}
}

// cmdIface handles the "iface" subcommand.
// Without arguments it lists network interfaces. With a name it sets the
// default interface; "auto" restores automatic (OS routing) selection.
func cmdIface(args []string) {
	if len(args) > 1 {
		fmt.Fprintln(os.Stderr, "用法: cauth iface [网口名|auto]")
		os.Exit(1)
	}

	if len(args) == 1 {
		name := args[0]
		if name == "auto" {
			if err := config.SetDefaultIface(""); err != nil {
				fmt.Fprintf(os.Stderr, "%s错误: 清除默认网口失败: %s%s\n", color.Red, err, color.NC)
				os.Exit(1)
			}
			fmt.Printf("%s==> 已恢复自动选择网口（由系统路由决定）%s\n", color.Green, color.NC)
			return
		}
		if !network.InterfaceExists(name) {
			fmt.Fprintf(os.Stderr, "%s错误: 网口 [%s%s%s] 不存在%s\n", color.Red, color.Yellow, name, color.Red, color.NC)
			fmt.Fprintln(os.Stderr, "使用 'cauth iface' 查看可用网口")
			os.Exit(1)
		}
		if err := config.SetDefaultIface(name); err != nil {
			fmt.Fprintf(os.Stderr, "%s错误: 保存默认网口失败: %s%s\n", color.Red, err, color.NC)
			os.Exit(1)
		}
		fmt.Printf("%s==> 默认网口已设置为 [%s%s%s]%s\n", color.Green, color.Yellow, name, color.Green, color.NC)
		return
	}

	defIface, err := config.GetDefaultIface()
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 读取默认网口失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	ifaces, err := network.ListInterfaces()
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 获取网络接口失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	fmt.Printf("%s可用的网络接口:%s\n", color.Yellow, color.NC)
	for _, iface := range ifaces {
		marker := ""
		if iface.Name == defIface {
			marker = "  <- 默认"
		}
		fmt.Printf("  %-16s %s%s\n", iface.Name, strings.Join(iface.Addrs, ", "), marker)
	}
	fmt.Printf("\n使用 %scauth iface <网口名>%s 设为默认, %scauth iface auto%s 恢复自动\n", color.Yellow, color.NC, color.Yellow, color.NC)
	fmt.Printf("使用 %scauth -i <网口名> [命令]%s 临时指定出网网口\n", color.Yellow, color.NC)
}

// showHelp prints the usage information.
func showHelp() {
	fmt.Printf(`cauth - 校园网命令行登录工具

一个用于快速登录校园网、管理多个登录账号的命令行工具

%s用法:%s
  cauth [命令] [参数...]
  cauth -i <网口> [命令] [参数...]   临时指定出网网口，覆盖默认设置

%s登录操作 (默认):%s
  cauth              使用默认账号进行登录
  cauth <别名>       使用指定别名的账号进行登录

%s账号管理:%s
  %sadd <别名> <账号> [密码]%s   添加或更新账号, 若不提供密码, 将提示安全输入
  %sremove, rm <别名>%s          删除一个已保存的账号
  %slist, ls%s                   列出所有已保存的账号
  %sset, def, default <别名>%s   设置一个默认登录账号

%s其他:%s
  %slogout [别名]%s            注销校园网登录
  %sstatus%s                   检测当前网络连通性
  %sdaemon [别名] [间隔]%s     守护进程模式，周期性检测并自动登录 (默认间隔 60s)
  %shelp, -h, --help%s         显示此帮助信息
  %siface [网口名|auto]%s      列出网络接口; 设为默认网口; auto 恢复自动选择

%s示例:%s
  # 添加一个名为 myacc 的账号，并安全地输入密码
  cauth add myacc 20230001

  # 将 myacc 设置为默认账号
  cauth set myacc

  # 使用默认账号登录
  cauth

  # 使用另一个名为 otheracc 的账号登录（不改变默认设置）
  cauth otheracc

  # 启动守护进程，使用默认账号，每 60 秒检测一次
  cauth daemon

  # 启动守护进程，指定账号和检测间隔
  cauth daemon myacc 5m

  # 退出默认账号的登录
  cauth logout

  # 退出 myacc 账号的登录
  cauth logout myacc

  # 将 enp2s0 设为默认网口（写入配置，之后所有联网命令生效）
  cauth iface enp2s0

  # 恢复由系统路由自动选择网口
  cauth iface auto
`,
		color.Yellow, color.NC,
		color.Yellow, color.NC,
		color.Yellow, color.NC,
		color.Green, color.NC,
		color.Green, color.NC,
		color.Green, color.NC,
		color.Green, color.NC,
		color.Yellow, color.NC,
		color.Green, color.NC,
		color.Green, color.NC,
		color.Green, color.NC,
		color.Green, color.NC,
		color.Green, color.NC,
		color.Yellow, color.NC,
	)
}

// cmdDaemon handles the "daemon" subcommand.
// Usage: cauth daemon [alias] [interval]
// Default interval is 60s. The alias defaults to the configured default account.
func cmdDaemon(args []string) {
	applyIface()

	alias := ""
	interval := 60 * time.Second

	// Parse positional args: [alias] [interval]
	// If the first arg parses as a duration, treat it as interval (no alias).
	// Otherwise treat it as alias, and the second arg (if any) as interval.
	switch len(args) {
	case 0:
		// defaults
	case 1:
		if d, err := time.ParseDuration(args[0]); err == nil {
			interval = d
		} else {
			alias = args[0]
		}
	case 2:
		alias = args[0]
		d, err := time.ParseDuration(args[1])
		if err != nil {
			fmt.Fprintf(os.Stderr, "%s错误: 无效的时间间隔 '%s': %s%s\n", color.Red, args[1], err, color.NC)
			fmt.Fprintln(os.Stderr, "示例: 30s, 1m, 5m")
			os.Exit(1)
		}
		interval = d
	default:
		fmt.Fprintln(os.Stderr, "用法: cauth daemon [别名] [间隔]")
		os.Exit(1)
	}

	if interval <= 0 {
		fmt.Fprintf(os.Stderr, "%s错误: 间隔必须为正时长%s\n", color.Red, color.NC)
		fmt.Fprintln(os.Stderr, "示例: 30s, 1m, 5m")
		os.Exit(1)
	}

	// Resolve alias to default if not specified.
	if alias == "" {
		defaultAlias, err := config.GetDefault()
		if err != nil {
			fmt.Fprintf(os.Stderr, "%s错误: 读取默认账号失败: %s%s\n", color.Red, err, color.NC)
			os.Exit(1)
		}
		if defaultAlias == "" {
			fmt.Fprintf(os.Stderr, "%s错误: 未设置默认账号%s\n", color.Red, color.NC)
			fmt.Fprintln(os.Stderr, "请先使用 'cauth add' 添加账号并用 'cauth default' 设置默认账号，")
			fmt.Fprintln(os.Stderr, "或使用 'cauth daemon <别名>' 指定账号")
			os.Exit(1)
		}
		alias = defaultAlias
	}

	account, password, err := config.GetCredentials(alias)
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 账号别名 [%s%s%s] 未找到%s\n", color.Red, color.Yellow, alias, color.Red, color.NC)
		fmt.Fprintln(os.Stderr, "使用 'cauth list' 查看所有已保存的账号")
		os.Exit(1)
	}

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	cfg := daemon.Config{
		Account:  account,
		Password: password,
		Alias:    alias,
		Interval: interval,
	}

	if err := daemon.Run(ctx, cfg); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 守护进程异常退出: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}
}

// cmdLogout handles the "logout" subcommand.
// Usage: cauth logout [alias]
// If no alias is given, the default account is used.
func cmdLogout(args []string) {
	applyIface()

	if len(args) > 1 {
		fmt.Fprintln(os.Stderr, "用法: cauth logout [别名]")
		os.Exit(1)
	}

	alias := ""
	if len(args) == 1 {
		alias = args[0]
	}

	if alias == "" {
		defaultAlias, err := config.GetDefault()
		if err != nil {
			fmt.Fprintf(os.Stderr, "%s错误: 读取默认账号失败: %s%s\n", color.Red, err, color.NC)
			os.Exit(1)
		}
		if defaultAlias == "" {
			fmt.Fprintf(os.Stderr, "%s错误: 未设置默认账号%s\n", color.Red, color.NC)
			fmt.Fprintln(os.Stderr, "请指定要注销的账号别名: cauth logout <别名>")
			os.Exit(1)
		}
		alias = defaultAlias
	}

	account, _, err := config.GetCredentials(alias)
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 账号别名 [%s%s%s] 未找到%s\n", color.Red, color.Yellow, alias, color.Red, color.NC)
		fmt.Fprintln(os.Stderr, "使用 'cauth list' 查看所有已保存的账号")
		os.Exit(1)
	}

	localIP := network.GetLocalIPForHost(login.PortalHost)

	fmt.Printf("正在注销账号 [%s%s%s] (IP: %s)...\n", color.Yellow, alias, color.NC, localIP)

	if err := login.PerformLogout(account, localIP); err != nil {
		fmt.Fprintf(os.Stderr, "%s==> 注销失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	fmt.Printf("%s==> 注销成功!%s\n", color.Green, color.NC)
}
