package daemon

import (
	"context"
	"fmt"
	"time"

	"github.com/morethan987/campus-login/internal/color"
	"github.com/morethan987/campus-login/internal/login"
	"github.com/morethan987/campus-login/internal/network"
)

// Config holds the daemon runtime configuration.
type Config struct {
	Account  string
	Password string
	Alias    string
	Interval time.Duration
}

// maxBackoff is the upper bound for exponential backoff on consecutive login failures.
const maxBackoff = 30 * time.Minute

// Run starts the daemon loop. It checks connectivity, attempts login when
// disconnected, and sleeps for the configured interval between checks.
// Login failures trigger exponential backoff; a successful connectivity
// check resets the backoff. The loop exits when ctx is canceled.
func Run(ctx context.Context, cfg Config) error {
	logf("守护进程启动 (账号: %s%s%s, 间隔: %s)",
		color.Yellow, cfg.Alias, color.NC, cfg.Interval)

	consecutiveFailures := 0

	// Run first check immediately.
	consecutiveFailures = check(cfg, consecutiveFailures)

	ticker := time.NewTicker(cfg.Interval)
	defer ticker.Stop()

	for {
		wait := nextWait(cfg.Interval, consecutiveFailures)

		select {
		case <-ctx.Done():
			logf("收到退出信号，守护进程停止。")
			return nil
		case <-ticker.C:
			// If we're in backoff, skip this tick and wait longer.
			if wait > cfg.Interval {
				ticker.Reset(wait)
			}
			consecutiveFailures = check(cfg, consecutiveFailures)
			// Reset ticker to normal interval after check (backoff is
			// recalculated each iteration).
			ticker.Reset(cfg.Interval)
		}
	}
}

// check performs one connectivity check + login attempt cycle.
// Returns the updated consecutive failure count.
func check(cfg Config, failures int) int {
	connected, err := network.CheckConnectivity()
	if err == nil && connected {
		if failures > 0 {
			logf("%s[探查] 网络已恢复连通。%s", color.Green, color.NC)
		} else {
			logf("%s[探查] 网络已连通，无需登录。%s", color.Green, color.NC)
		}
		return 0
	}

	// Not connected — either error (timeout/EOF from captive portal) or non-204 status.
	if err != nil {
		logf("%s[探查] 连通性检测失败: %s，尝试登录...%s", color.Yellow, err, color.NC)
	} else {
		logf("%s[探查] 未连通，尝试登录...%s", color.Yellow, color.NC)
	}

	localIP := network.GetLocalIP()
	success, msg, err := login.PerformLogin(cfg.Account, cfg.Password, localIP)
	if err != nil {
		logf("%s[登录] 请求失败: %s%s", color.Red, err, color.NC)
		return failures + 1
	}

	if success {
		logf("%s[登录] 成功! 服务器消息: %s%s", color.Green, msg, color.NC)
		return 0
	}

	logf("%s[登录] 失败! 服务器消息: %s%s", color.Red, msg, color.NC)
	newFailures := failures + 1
	next := nextWait(0, newFailures)
	logf("[退避] 连续失败 %d 次，下次探查将在 %s 后", newFailures, next)
	return newFailures
}

// nextWait calculates the wait duration considering exponential backoff.
// With 0 failures it returns the base interval unchanged.
func nextWait(base time.Duration, failures int) time.Duration {
	if failures <= 0 {
		return base
	}
	// Exponential: base * 2^(failures-1), capped at maxBackoff.
	backoff := base
	if backoff == 0 {
		backoff = 30 * time.Second
	}
	for i := 1; i < failures; i++ {
		backoff *= 2
		if backoff > maxBackoff {
			return maxBackoff
		}
	}
	if backoff > maxBackoff {
		return maxBackoff
	}
	return backoff
}

// logf prints a timestamped log line to stdout.
func logf(format string, args ...any) {
	ts := time.Now().Format("2006-01-02 15:04:05")
	fmt.Printf("[%s] %s\n", ts, fmt.Sprintf(format, args...))
}
