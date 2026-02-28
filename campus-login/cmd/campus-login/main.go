package main

import (
	"fmt"
	"os"
	"strings"

	"golang.org/x/term"

	"github.com/morethan987/campus-login/internal/color"
	"github.com/morethan987/campus-login/internal/config"
	"github.com/morethan987/campus-login/internal/login"
	"github.com/morethan987/campus-login/internal/network"
)

func main() {
	color.Init()

	if err := config.Setup(); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 初始化配置失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	args := os.Args[1:]

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
	case "default":
		cmdDefault(args[1:])
	case "status":
		cmdStatus()
	case "help", "--help", "-h":
		showHelp()
	default:
		if strings.HasPrefix(command, "-") {
			fmt.Fprintf(os.Stderr, "%s错误: 未知选项 '%s'。%s\n", color.Red, command, color.NC)
			fmt.Fprintf(os.Stderr, "使用 'campus-login help' 查看可用命令。\n")
			os.Exit(1)
		}
		handleLogin(command)
	}
}

// handleLogin performs a login using the given alias, or the default account if alias is empty.
func handleLogin(alias string) {
	if alias == "" {
		defaultAlias, err := config.GetDefault()
		if err != nil {
			fmt.Fprintf(os.Stderr, "%s错误: 读取默认账号失败: %s%s\n", color.Red, err, color.NC)
			os.Exit(1)
		}
		if defaultAlias == "" {
			fmt.Fprintf(os.Stderr, "%s错误: 未设置默认账号。%s\n", color.Red, color.NC)
			fmt.Fprintln(os.Stderr, "请先使用 'campus-login add' 添加一个账号，然后使用 'campus-login default' 设置默认账号。")
			os.Exit(1)
		}
		alias = defaultAlias
	}

	account, password, err := config.GetCredentials(alias)
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 账号别名 [%s%s%s] 未找到。%s\n", color.Red, color.Yellow, alias, color.Red, color.NC)
		fmt.Fprintln(os.Stderr, "使用 'campus-login list' 查看所有已保存的账号。")
		os.Exit(1)
	}

	localIP := network.GetLocalIP()

	success, msg, err := login.PerformLogin(account, password, localIP)
	if err != nil {
		fmt.Fprintf(os.Stderr, "%s==> 登录失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	if success {
		fmt.Printf("%s==> 登录成功! 服务器消息: %s%s\n", color.Green, msg, color.NC)
	} else {
		if msg == "" {
			msg = "未知错误，可能是账号密码错误或已在别处登录。"
		}
		fmt.Fprintf(os.Stderr, "%s==> 登录失败! 服务器消息: %s%s\n", color.Red, msg, color.NC)
		os.Exit(1)
	}
}

// cmdAdd handles the "add" subcommand.
func cmdAdd(args []string) {
	if len(args) < 2 || len(args) > 3 {
		fmt.Fprintln(os.Stderr, "用法: campus-login add <别名> <账号> [密码]")
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
		fmt.Fprintf(os.Stderr, "%s错误: 密码不能为空。%s\n", color.Red, color.NC)
		os.Exit(1)
	}

	if err := config.AddAccount(alias, account, password); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 保存账号失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	fmt.Printf("账号 [%s%s%s] 已保存。\n", color.Yellow, alias, color.NC)
}

// cmdRemove handles the "remove"/"rm" subcommand.
func cmdRemove(args []string) {
	if len(args) != 1 {
		fmt.Fprintln(os.Stderr, "用法: campus-login remove <别名>")
		os.Exit(1)
	}

	alias := args[0]
	if err := config.RemoveAccount(alias); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 删除账号失败: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	fmt.Printf("账号 [%s%s%s] 已删除。\n", color.Yellow, alias, color.NC)
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
		fmt.Fprintln(os.Stderr, "用法: campus-login default <别名>")
		os.Exit(1)
	}

	alias := args[0]
	if err := config.SetDefault(alias); err != nil {
		fmt.Fprintf(os.Stderr, "%s错误: 账号别名 [%s%s%s] 不存在，无法设为默认。%s\n",
			color.Red, color.Yellow, alias, color.Red, color.NC)
		os.Exit(1)
	}

	fmt.Printf("已将 [%s%s%s] 设置为默认登录账号。\n", color.Yellow, alias, color.NC)
}

// cmdStatus handles the "status" subcommand.
func cmdStatus() {
	connected, err := network.CheckConnectivity()
	if err != nil {
		fmt.Printf("%s==> 网络不可达: %s%s\n", color.Red, err, color.NC)
		os.Exit(1)
	}

	if connected {
		fmt.Printf("%s==> 网络已连通，当前可以正常访问互联网。%s\n", color.Green, color.NC)
	} else {
		fmt.Printf("%s==> 检测到强制门户（未登录），请登录校园网。%s\n", color.Yellow, color.NC)
	}
}

// showHelp prints the usage information.
func showHelp() {
	fmt.Printf(`campus-login - 校园网命令行登录工具

一个用于快速登录校园网、管理多个登录账号的命令行工具

%s用法:%s
  campus-login [命令] [参数...]

%s登录操作 (默认):%s
  campus-login              使用默认账号进行登录
  campus-login <别名>       使用指定别名的账号进行登录

%s账号管理:%s
  %sadd <别名> <账号> [密码]%s   添加或更新账号, 若不提供密码, 将提示安全输入
  %sremove, rm <别名>%s          删除一个已保存的账号
  %slist, ls%s                   列出所有已保存的账号
  %sdefault <别名>%s             设置一个默认登录账号

%s其他:%s
  %sstatus%s                   检测当前网络连通性
  %shelp, -h, --help%s         显示此帮助信息

%s示例:%s
  # 添加一个名为 myacc 的账号，并安全地输入密码
  campus-login add myacc 20230001

  # 将 myacc 设置为默认账号
  campus-login default myacc

  # 使用默认账号登录
  campus-login

  # 使用另一个名为 otheracc 的账号登录（不改变默认设置）
  campus-login otheracc
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
		color.Yellow, color.NC,
	)
}
