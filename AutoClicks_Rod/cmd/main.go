package main

import (
	"context"
	"flag"
	"fmt"
	"log/slog"
	"os"
	"os/exec"
	"os/signal"
	"path/filepath"
	"runtime"
	"strconv"
	"syscall"
	"time"

	"github.com/morethan987/AutoClicks_Rod/internal/browser"
	"github.com/morethan987/AutoClicks_Rod/internal/config"
)

const defaultConfigYAML = `# AutoClicks Rod 配置文件
# 请填写你的实际信息后使用

# ===== 账号信息 =====
username: "你的学号"
password: "你的密码"

# ===== 选课系统 URL =====
url: "https://my.cqu.edu.cn/enroll/CourseStuSelectionList"

# ===== 轮询与重试设置 =====
# 每次轮询间隔（秒）
interval: 3
# 最大重试次数（基础设施故障时触发指数退避重试）
max_retry: 9999

# ===== 浏览器模式 =====
# true = 无头模式（后台运行），false = 可见模式（调试用）
headless: true

# ===== Server酱微信通知 =====
# 填写你的 Server酱 SCKEY 以启用选课成功微信通知，留空则不发通知
# 获取地址：https://sct.ftqq.com/
server_key: ""

# ===== 目标课程列表 =====
courses:
  - name: "边缘计算"        # 课程名（精确匹配）
    id: "CST31220"         # 课程号（精确匹配）
    teachers:
      - "汪成亮"            # 目标教师（可填多个）
  # 可添加更多课程，例如：
  # - name: "机器学习"
  #   id: "CST12345"
  #   teachers:
  #     - "张三"
  #     - "李四"

# ===== CSS 选择器配置 =====
# 当目标网站改版时，只需更新此处的选择器，无需重新编译程序

selectors:
  # 登录页面选择器
  login:
    username_input: "input[name='username']"
    password_input: "input[type='password']"
    login_button: "button[type='submit'].login-button.ant-btn"

  # 选课列表页面选择器
  course:
    # 页面加载完成标志元素
    flag: "span.ant-table-column-title"
    # 课程行
    data_row: "tr.ant-table-row.ant-table-row-level-0"

  # 课程详情侧边栏选择器
  sidebar:
    # 侧边栏加载完成标志
    sidebar_flag: "div.ant-drawer-body tbody.ant-table-tbody"
    # 选课复选框
    checkbox: "input[type='checkbox']"
    # 关闭按钮
    close_button: "div.drawer-close-wrap-right svg"
    # 教师数据行
    data_row: "div.ant-drawer-body tbody.ant-table-tbody tr.ant-table-row.ant-table-row-level-0"
    # 课程已满标志
    full_flag: "span.text-error"
    # 课程已选标志
    selected_flag: "span.text-success"

  # 选课确认流程选择器
  selection:
    # 选课按钮
    select_button: "div.ant-drawer-body button"
    # 确认对话框中的确认按钮
    confirm_button: ".ant-modal button.ant-btn.ant-btn-primary"
`

// daemonEnvKey is set by the daemon parent to signal the child that it's running
// as a background process with log output already redirected.
const daemonEnvKey = "_AUTOCLICKS_DAEMON"

func main() {
	// ── Handle subcommands before flag parsing ───────────────────────────────
	if len(os.Args) > 1 {
		switch os.Args[1] {
		case "init":
			runInit()
			return
		case "clean":
			runClean(os.Args[2:])
			return
		case "daemon":
			runDaemon(os.Args[2:])
			return
		case "stop":
			runStop(os.Args[2:])
			return
		case "help", "--help", "-h":
			printUsage()
			return
		case "version", "--version", "-v":
			fmt.Println("autoclicks v1.0.0")
			return
		}
	}

	// ── CLI flags ─────────────────────────────────────────────────────────────
	fs := flag.NewFlagSet("autoclicks", flag.ExitOnError)
	configPath := fs.String("config", "config.yaml", "配置文件路径")
	headlessOverride := fs.Bool("headless", false, "强制无头模式（覆盖配置文件中的 headless 值）")
	fs.Usage = printUsage
	fs.Parse(os.Args[1:])

	// When spawned as daemon child, redirect slog to the inherited log file (stdout).
	if os.Getenv(daemonEnvKey) == "1" {
		slog.SetDefault(slog.New(slog.NewTextHandler(os.Stdout, nil)))
	}

	// ── Load configuration ────────────────────────────────────────────────────
	cfg, err := config.Load(*configPath)
	if err != nil {
		slog.Error("Failed to load config", "path", *configPath, "error", err)
		os.Exit(1)
	}
	if *headlessOverride {
		cfg.Headless = true
	}
	slog.Info("Config loaded",
		"url", cfg.URL,
		"courses", len(cfg.Courses),
		"interval", cfg.Interval,
		"headless", cfg.Headless,
	)

	// ── Signal-aware context (SIGINT / SIGTERM → graceful shutdown) ───────────
	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	// ── Top-level retry loop (infrastructure errors only) ────────────────────
	for attempt := 0; attempt < cfg.MaxRetry; attempt++ {
		if ctx.Err() != nil {
			slog.Info("Shutdown signal received, exiting")
			break
		}

		slog.Info("Starting run", "attempt", attempt+1)
		err := run(ctx, cfg)
		if err == nil {
			slog.Info("Run completed successfully")
			break
		}

		if ctx.Err() != nil {
			slog.Info("Shutdown signal received during run, exiting")
			break
		}

		// Exponential backoff capped at 30 s
		backoff := min(time.Duration(1<<attempt)*time.Second, 30*time.Second)
		slog.Error("Run failed, retrying",
			"attempt", attempt+1,
			"max", cfg.MaxRetry,
			"backoff", backoff,
			"error", err,
		)
		// time.Sleep allowed here: retry backoff, NOT page state waiting
		select {
		case <-time.After(backoff):
		case <-ctx.Done():
			slog.Info("Shutdown signal received during backoff, exiting")
			return
		}
	}
}

// ── Subcommands ──────────────────────────────────────────────────────────────

// runInit generates a default config.yaml in the current directory.
func runInit() {
	const filename = "config.yaml"

	if _, err := os.Stat(filename); err == nil {
		fmt.Fprintf(os.Stderr, "错误：%s 已存在，不会覆盖。请删除或重命名后重试。\n", filename)
		os.Exit(1)
	}

	if err := os.WriteFile(filename, []byte(defaultConfigYAML), 0644); err != nil {
		fmt.Fprintf(os.Stderr, "错误：写入 %s 失败：%v\n", filename, err)
		os.Exit(1)
	}

	fmt.Printf("已生成默认配置文件：%s\n", filename)
	fmt.Println("请编辑此文件填入你的学号、密码和目标课程，然后运行：")
	fmt.Println("  ./autoclicks")
}

// runClean removes Rod's cache directories.
// By default only session data (user-data) is removed.
// With --all, the downloaded Chromium binary is also removed.
func runClean(args []string) {
	fs := flag.NewFlagSet("clean", flag.ExitOnError)
	all := fs.Bool("all", false, "同时删除已下载的 Chromium 浏览器")
	fs.Parse(args)

	// Rod stores session user-data under os.TempDir()/rod
	userDataDir := filepath.Join(os.TempDir(), "rod")
	removeDir(userDataDir, "会话缓存")

	if *all {
		browserDir := rodBrowserDir()
		removeDir(browserDir, "Chromium 浏览器")
		fmt.Println("提示：下次运行时将自动重新下载 Chromium（约 150MB）")
	}

	fmt.Println("清理完成。")
}

// runDaemon launches the program as a detached background process with log output
// redirected to a file. The parent prints metadata and exits immediately.
func runDaemon(args []string) {
	fs := flag.NewFlagSet("daemon", flag.ExitOnError)
	configPath := fs.String("config", "config.yaml", "配置文件路径")
	logPath := fs.String("log", "autoclicks.log", "日志文件路径")
	fs.Parse(args)

	// Resolve to absolute paths so the child process is independent of cwd changes.
	absConfig, err := filepath.Abs(*configPath)
	if err != nil {
		fmt.Fprintf(os.Stderr, "错误：无法解析配置文件路径：%v\n", err)
		os.Exit(1)
	}
	absLog, err := filepath.Abs(*logPath)
	if err != nil {
		fmt.Fprintf(os.Stderr, "错误：无法解析日志文件路径：%v\n", err)
		os.Exit(1)
	}

	// Validate config before spawning to fail fast with a clear error.
	if _, err := config.Load(absConfig); err != nil {
		fmt.Fprintf(os.Stderr, "错误：%v\n", err)
		os.Exit(1)
	}

	// Open log file (create or append).
	logFile, err := os.OpenFile(absLog, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
	if err != nil {
		fmt.Fprintf(os.Stderr, "错误：无法打开日志文件 %s：%v\n", absLog, err)
		os.Exit(1)
	}

	// Write session header to log file.
	startTime := time.Now().Format("2006-01-02 15:04:05")
	header := fmt.Sprintf(
		"\n"+
			"════════════════════════════════════════════════════════════\n"+
			"  AutoClicks Rod — 后台守护进程启动\n"+
			"  启动时间：%s\n"+
			"  配置文件：%s\n"+
			"  日志文件：%s\n"+
			"════════════════════════════════════════════════════════════\n\n",
		startTime, absConfig, absLog,
	)
	logFile.WriteString(header)

	// Build child command: re-exec ourselves with --headless and --config.
	exe, err := os.Executable()
	if err != nil {
		fmt.Fprintf(os.Stderr, "错误：无法获取可执行文件路径：%v\n", err)
		os.Exit(1)
	}
	child := exec.Command(exe, "--headless", "--config", absConfig)
	child.Stdout = logFile
	child.Stderr = logFile
	child.Env = append(os.Environ(), daemonEnvKey+"=1")
	// Detach child from parent's process group so it survives parent exit.
	setSysProcAttr(child)

	if err := child.Start(); err != nil {
		fmt.Fprintf(os.Stderr, "错误：启动后台进程失败：%v\n", err)
		os.Exit(1)
	}

	pid := child.Process.Pid

	// Write PID to log for reference.
	logFile.WriteString(fmt.Sprintf("  PID：%d\n\n", pid))
	logFile.Close()

	// Release child so parent can exit without waiting.
	child.Process.Release()

	// Write PID file next to log for easy stop.
	pidFile := absLog + ".pid"
	os.WriteFile(pidFile, []byte(strconv.Itoa(pid)), 0644)

	fmt.Println("后台进程已启动：")
	fmt.Printf("  PID：%d\n", pid)
	fmt.Printf("  日志：%s\n", absLog)
	fmt.Printf("  PID 文件：%s\n", pidFile)
	fmt.Println()
	fmt.Println("终止方式：")
	fmt.Printf("  autoclicks stop                         （自动读取 %s）\n", pidFile)
	fmt.Printf("  autoclicks stop --pid %d\n", pid)
	if runtime.GOOS == "windows" {
		fmt.Printf("  taskkill /PID %d /F\n", pid)
	} else {
		fmt.Printf("  kill %d\n", pid)
	}
}

// runStop terminates a background daemon process by PID.
func runStop(args []string) {
	fs := flag.NewFlagSet("stop", flag.ExitOnError)
	pidFlag := fs.Int("pid", 0, "要终止的进程 PID")
	logPath := fs.String("log", "autoclicks.log", "日志文件路径（用于查找 .pid 文件）")
	fs.Parse(args)

	pid := *pidFlag

	// If no --pid given, try to read from .pid file.
	if pid == 0 {
		absLog, err := filepath.Abs(*logPath)
		if err != nil {
			fmt.Fprintf(os.Stderr, "错误：无法解析日志路径：%v\n", err)
			os.Exit(1)
		}
		pidFile := absLog + ".pid"
		data, err := os.ReadFile(pidFile)
		if err != nil {
			fmt.Fprintf(os.Stderr, "错误：无法读取 PID 文件 %s：%v\n", pidFile, err)
			fmt.Fprintln(os.Stderr, "请使用 --pid <PID> 手动指定进程号")
			os.Exit(1)
		}
		pid, err = strconv.Atoi(string(data))
		if err != nil {
			fmt.Fprintf(os.Stderr, "错误：PID 文件内容无效：%s\n", string(data))
			os.Exit(1)
		}
		// Clean up PID file after reading.
		os.Remove(pidFile)
	}

	proc, err := os.FindProcess(pid)
	if err != nil {
		fmt.Fprintf(os.Stderr, "错误：找不到进程 %d：%v\n", pid, err)
		os.Exit(1)
	}

	// Send SIGTERM (or TerminateProcess on Windows) for graceful shutdown.
	if err := terminateProcess(proc); err != nil {
		fmt.Fprintf(os.Stderr, "错误：终止进程 %d 失败：%v\n", pid, err)
		os.Exit(1)
	}

	fmt.Printf("已发送终止信号到进程 %d\n", pid)
}

// removeDir removes a directory and prints the result.
func removeDir(dir, label string) {
	info, err := os.Stat(dir)
	if os.IsNotExist(err) {
		fmt.Printf("跳过 %s：%s 不存在\n", label, dir)
		return
	}
	if err != nil {
		fmt.Fprintf(os.Stderr, "错误：无法读取 %s：%v\n", dir, err)
		return
	}
	if !info.IsDir() {
		fmt.Fprintf(os.Stderr, "跳过 %s：%s 不是目录\n", label, dir)
		return
	}

	if err := os.RemoveAll(dir); err != nil {
		fmt.Fprintf(os.Stderr, "错误：删除 %s 失败：%v\n", dir, err)
		return
	}
	fmt.Printf("已删除 %s：%s\n", label, dir)
}

// rodBrowserDir returns Rod's default browser download directory.
// Mirrors the logic in github.com/go-rod/rod/lib/launcher.DefaultBrowserDir.
func rodBrowserDir() string {
	var base string
	switch runtime.GOOS {
	case "windows":
		base = os.Getenv("APPDATA")
	default: // linux, darwin
		base = filepath.Join(os.Getenv("HOME"), ".cache")
	}
	return filepath.Join(base, "rod", "browser")
}

func printUsage() {
	fmt.Print(`AutoClicks Rod — 重庆大学自动轮询抢课工具

用法：
  autoclicks [flags]          启动抢课（默认读取 config.yaml）
  autoclicks daemon [flags]   后台守护进程模式（不阻塞终端）
  autoclicks stop [flags]     终止后台守护进程
  autoclicks init             在当前目录生成默认配置文件
  autoclicks clean [flags]    清理缓存
  autoclicks help             显示此帮助信息
  autoclicks version          显示版本号

Flags（启动模式）：
  --config <path>             指定配置文件路径（默认：config.yaml）
  --headless                  强制无头模式，覆盖配置文件中的 headless 值

Flags（daemon 模式）：
  --config <path>             指定配置文件路径（默认：config.yaml）
  --log <path>                指定日志文件路径（默认：autoclicks.log）

Flags（stop 模式）：
  --pid <PID>                 指定要终止的进程 PID
  --log <path>                指定日志路径以查找 .pid 文件（默认：autoclicks.log）

Flags（clean 模式）：
  --all                       同时删除已下载的 Chromium 浏览器

示例：
  autoclicks init                       生成 config.yaml 模板
  autoclicks                            前台启动（Ctrl+C 退出）
  autoclicks daemon                     后台启动，日志写入 autoclicks.log
  autoclicks daemon --log /var/log/ac.log  指定日志路径后台启动
  autoclicks stop                       终止后台进程（读取 .pid 文件）
  autoclicks stop --pid 12345           按 PID 终止后台进程
  autoclicks clean                      清理会话缓存
  autoclicks clean --all                清理全部缓存（含浏览器二进制）
  autoclicks --config /path/to/cfg.yaml 指定配置文件启动
  autoclicks --headless                 强制无头模式启动

工作流程：
  1. autoclicks init        → 生成配置文件
  2. 编辑 config.yaml       → 填入学号、密码、目标课程
  3. autoclicks daemon      → 后台启动抢课
  4. autoclicks stop        → 优雅退出

`)
}

// ── Core logic ───────────────────────────────────────────────────────────────

// run executes one full session: launch browser → login → poll loop.
// Infrastructure errors are returned to trigger the top-level retry.
// Business-logic outcomes (course full, already selected) are logged and continue.
func run(ctx context.Context, cfg *config.Config) error {
	// Launch browser
	b, launcher, err := browser.LaunchBrowser(cfg)
	if err != nil {
		return err
	}
	defer func() {
		if err := b.Close(); err != nil {
			slog.Warn("Error closing browser", "error", err)
		}
		launcher.Cleanup()
		slog.Info("Browser closed and cleaned up")
	}()

	// Create page with image blocking
	page, err := browser.NewPage(b)
	if err != nil {
		return err
	}

	// Login
	if err := browser.Login(page, cfg); err != nil {
		return err
	}

	// Polling loop
	for {
		// Respect shutdown signal
		if ctx.Err() != nil {
			slog.Info("Shutdown signal received, stopping poll loop")
			return nil
		}

		// Wait for course list page to render
		if err := browser.WaitForCoursePage(page, cfg); err != nil {
			return err
		}

		// Find target courses on the current page
		courses, err := browser.FindTargetCourses(page, cfg)
		if err != nil {
			return err
		}

		// Attempt enrollment for each matched course link
		for _, courseLink := range courses {
			if ctx.Err() != nil {
				return nil
			}
			if err := browser.SelectCourse(page, courseLink, cfg); err != nil {
				return err
			}
		}

		// Wait for next poll interval (allowed: polling interval, not page state)
		slog.Info("Poll round complete, waiting before next refresh", "interval_s", cfg.Interval)
		select {
		case <-time.After(time.Duration(cfg.Interval) * time.Second):
		case <-ctx.Done():
			slog.Info("Shutdown signal received during poll sleep, exiting")
			return nil
		}

		// Reload page for next poll round
		if err := page.Reload(); err != nil {
			return err
		}
	}
}
