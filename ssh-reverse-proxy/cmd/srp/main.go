package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/morethan987/ssh-reverse-proxy/internal/client"
	"github.com/morethan987/ssh-reverse-proxy/internal/config"
	"github.com/morethan987/ssh-reverse-proxy/internal/daemon"
	"github.com/morethan987/ssh-reverse-proxy/internal/ipc"
	"github.com/morethan987/ssh-reverse-proxy/internal/logger"
	"github.com/morethan987/ssh-reverse-proxy/internal/xdg"
)

func main() {
	if os.Getenv("SRP_DAEMON") == "1" {
		runDaemon()
		return
	}
	runClient()
}

func runDaemon() {
	if err := xdg.EnsureDir(); err != nil {
		fatal(err)
	}

	configPath := xdg.ConfigPath()
	var cfg *config.Config
	if _, err := os.Stat(configPath); err == nil {
		loaded, err := config.Load(configPath)
		if err != nil {
			fatal(err)
		}
		cfg = loaded
	} else if errors.Is(err, os.ErrNotExist) {
		cfg = &config.Config{
			Global: config.GlobalConfig{
				RemotePort: 7890,
				LocalPort:  7890,
				SSHOptions: []string{"ServerAliveInterval=60", "ExitOnForwardFailure=yes"},
			},
			Servers: map[string]config.ServerConfig{},
		}
		if err := config.Save(configPath, cfg); err != nil {
			fatal(err)
		}
		loaded, err := config.Load(configPath)
		if err != nil {
			fatal(err)
		}
		cfg = loaded
	} else {
		fatal(err)
	}

	log, err := logger.New(xdg.LogPath())
	if err != nil {
		fatal(err)
	}
	defer func() {
		_ = log.Close()
	}()

	d := daemon.New(cfg, log)
	if err := d.Start(); err != nil {
		fatal(err)
	}
}

func runClient() {
	args := os.Args[1:]

	if len(args) == 0 {
		cfg, err := loadOrCreateConfig()
		if err != nil {
			fatal(err)
		}
		current := cfg.Global.Default
		if current == "" {
			current = "none"
		}
		fmt.Println("Current default SRP target:", current)
		fmt.Println("Starting...")
		args = []string{ipc.ActionStart}
	}

	if args[0] == "-h" || args[0] == "--help" {
		printHelp()
		return
	}

	action := args[0]
	cli := client.New(xdg.SocketPath())

	switch action {
	case ipc.ActionStart:
		cfg, err := loadOrCreateConfig()
		if err != nil {
			fatal(err)
		}
		alias := resolveAlias(args[1:], cfg.Global.Default)
		if err := ensureDaemonRunning(); err != nil {
			fatal(err)
		}
		resp, err := cli.Send(&ipc.Request{Action: ipc.ActionStart, Alias: alias})
		if err != nil {
			fatal(err)
		}
		if !resp.Success {
			fatal(errors.New(resp.Message))
		}
		fmt.Println(resp.Message)

	case ipc.ActionStop:
		cfg, err := loadOrCreateConfig()
		if err != nil {
			fatal(err)
		}
		alias := resolveAlias(args[1:], cfg.Global.Default)
		if err := ensureDaemonRunning(); err != nil {
			fatal(err)
		}
		resp, err := cli.Send(&ipc.Request{Action: ipc.ActionStop, Alias: alias})
		if err != nil {
			fatal(err)
		}
		if !resp.Success {
			fatal(errors.New(resp.Message))
		}
		fmt.Println(resp.Message)

	case ipc.ActionStatus:
		if err := ensureDaemonRunning(); err != nil {
			fatal(err)
		}
		if len(args) > 1 && args[1] != "" {
			alias := args[1]
			resp, err := cli.Send(&ipc.Request{Action: ipc.ActionStatus, Alias: alias})
			if err != nil {
				fatal(err)
			}
			if !resp.Success {
				fatal(errors.New(resp.Message))
			}
			if len(resp.Data) > 0 {
				fmt.Println(string(resp.Data))
			} else {
				fmt.Println(resp.Message)
			}
			return
		}

		resp, err := cli.Send(&ipc.Request{Action: ipc.ActionStatus})
		if err != nil {
			fatal(err)
		}
		if !resp.Success {
			fatal(errors.New(resp.Message))
		}

		statusMap := map[string]any{}
		if len(resp.Data) > 0 {
			if err := json.Unmarshal(resp.Data, &statusMap); err != nil {
				fatal(err)
			}
		}
		if len(statusMap) == 0 {
			fmt.Println("No running SRP targets")
		} else {
			for alias, raw := range statusMap {
				entry, _ := json.Marshal(raw)
				fmt.Printf("%s: %s\n", alias, string(entry))
			}
		}
		cfg, err := loadOrCreateConfig()
		if err != nil {
			fatal(err)
		}
		current := cfg.Global.Default
		if current == "" {
			current = "none"
		}
		fmt.Println("Current default SRP target:", current)

	case ipc.ActionSet:
		if len(args) < 2 || args[1] == "" {
			fmt.Fprintln(os.Stderr, "Usage: srp set <alias>")
			os.Exit(2)
		}
		if err := ensureDaemonRunning(); err != nil {
			fatal(err)
		}
		alias := args[1]
		resp, err := cli.Send(&ipc.Request{Action: ipc.ActionSet, Alias: alias})
		if err != nil {
			fatal(err)
		}
		if !resp.Success {
			fatal(errors.New(resp.Message))
		}
		fmt.Println("Default SRP target set to:", alias)

	case ipc.ActionCurrent:
		if err := ensureDaemonRunning(); err != nil {
			fatal(err)
		}
		resp, err := cli.Send(&ipc.Request{Action: ipc.ActionCurrent})
		if err != nil {
			fatal(err)
		}
		if !resp.Success {
			fatal(errors.New(resp.Message))
		}
		payload := struct {
			Default string `json:"default"`
		}{}
		if len(resp.Data) > 0 {
			if err := json.Unmarshal(resp.Data, &payload); err != nil {
				fatal(err)
			}
		}
		current := payload.Default
		if current == "" {
			current = "none"
		}
		fmt.Println("Current default SRP target:", current)

	case ipc.ActionAdd:
		alias, port := parseAddArgs(args[1:])
		if alias == "" {
			fmt.Fprintln(os.Stderr, "Usage: srp add <alias> [-p port]")
			os.Exit(2)
		}
		if err := ensureDaemonRunning(); err != nil {
			fatal(err)
		}
		resp, err := cli.Send(&ipc.Request{Action: ipc.ActionAdd, Alias: alias, Port: port})
		if err != nil {
			fatal(err)
		}
		if !resp.Success {
			fatal(errors.New(resp.Message))
		}
		finalPort := port
		if finalPort <= 0 {
			cfg, err := loadOrCreateConfig()
			if err != nil {
				fatal(err)
			}
			finalPort = cfg.Global.RemotePort
		}
		fmt.Printf("Added server '%s' with remote port %d\n", alias, finalPort)

	case ipc.ActionRemove:
		if len(args) < 2 || args[1] == "" {
			fmt.Fprintln(os.Stderr, "Usage: srp remove <alias>")
			os.Exit(2)
		}
		if err := ensureDaemonRunning(); err != nil {
			fatal(err)
		}
		alias := args[1]
		resp, err := cli.Send(&ipc.Request{Action: ipc.ActionRemove, Alias: alias})
		if err != nil {
			fatal(err)
		}
		if !resp.Success {
			fatal(errors.New(resp.Message))
		}
		fmt.Printf("Removed server '%s'\n", alias)

	case ipc.ActionList:
		if err := ensureDaemonRunning(); err != nil {
			fatal(err)
		}
		resp, err := cli.Send(&ipc.Request{Action: ipc.ActionList})
		if err != nil {
			fatal(err)
		}
		if !resp.Success {
			fatal(errors.New(resp.Message))
		}
		payload := struct {
			Servers []struct {
				Alias      string `json:"Alias"`
				RemotePort int    `json:"RemotePort"`
				LocalPort  int    `json:"LocalPort"`
			} `json:"servers"`
			Running map[string]json.RawMessage `json:"running"`
		}{}
		if err := json.Unmarshal(resp.Data, &payload); err != nil {
			fatal(err)
		}
		if len(payload.Servers) == 0 {
			fmt.Println("No configured SRP servers")
			return
		}
		for _, srv := range payload.Servers {
			status := "stopped"
			if _, ok := payload.Running[srv.Alias]; ok {
				status = "running"
			}
			fmt.Printf("%s\tport=%d\tstatus=%s\n", srv.Alias, srv.RemotePort, status)
		}

	case "kill":
		killDaemon()

	default:
		fmt.Fprintf(os.Stderr, "Unknown action: %s\n", action)
		os.Exit(2)
	}
}

func killDaemon() {
	pidBytes, err := os.ReadFile(xdg.PidPath())
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			fmt.Fprintln(os.Stderr, "Daemon is not running (no PID file found)")
			os.Exit(1)
		}
		fatal(err)
	}

	pid, err := strconv.Atoi(strings.TrimSpace(string(pidBytes)))
	if err != nil {
		fatal(fmt.Errorf("invalid PID in file: %w", err))
	}

	proc, err := os.FindProcess(pid)
	if err != nil {
		fatal(fmt.Errorf("find process %d: %w", pid, err))
	}

	if err := proc.Signal(syscall.Signal(0)); err != nil {
		fmt.Fprintf(os.Stderr, "Daemon (PID %d) is not running\n", pid)
		_ = os.Remove(xdg.PidPath())
		_ = os.Remove(xdg.SocketPath())
		os.Exit(1)
	}

	if err := proc.Signal(syscall.SIGTERM); err != nil {
		fatal(fmt.Errorf("send SIGTERM to daemon (PID %d): %w", pid, err))
	}

	deadline := time.Now().Add(5 * time.Second)
	for time.Now().Before(deadline) {
		if proc.Signal(syscall.Signal(0)) != nil {
			fmt.Println("Daemon stopped")
			return
		}
		time.Sleep(200 * time.Millisecond)
	}

	_ = proc.Signal(syscall.SIGKILL)
	_ = os.Remove(xdg.PidPath())
	_ = os.Remove(xdg.SocketPath())
	fmt.Println("Daemon killed (did not respond to SIGTERM)")
}

func ensureDaemonRunning() error {
	cli := client.New(xdg.SocketPath())
	if _, err := cli.Send(&ipc.Request{Action: ipc.ActionCurrent}); err == nil {
		return nil
	}

	if err := xdg.EnsureDir(); err != nil {
		return err
	}

	exe, err := os.Executable()
	if err != nil {
		return err
	}

	logPath := xdg.LogPath()
	if err := os.MkdirAll(filepath.Dir(logPath), 0o755); err != nil {
		return err
	}
	logFile, err := os.OpenFile(logPath, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0o644)
	if err != nil {
		return err
	}

	cmd := exec.Command(exe)
	cmd.Env = append(os.Environ(), "SRP_DAEMON=1")
	cmd.Stdout = logFile
	cmd.Stderr = logFile
	if err := cmd.Start(); err != nil {
		_ = logFile.Close()
		return err
	}
	_ = logFile.Close()

	socketPath := xdg.SocketPath()
	deadline := time.Now().Add(5 * time.Second)
	for time.Now().Before(deadline) {
		if _, err := os.Stat(socketPath); err == nil {
			return nil
		}
		time.Sleep(200 * time.Millisecond)
	}

	return fmt.Errorf("timed out waiting for daemon to start")
}

func resolveAlias(args []string, defaultAlias string) string {
	if len(args) > 0 && args[0] != "" {
		return args[0]
	}
	return defaultAlias
}

func parseAddArgs(args []string) (string, int) {
	alias := ""
	port := 0
	for i := 0; i < len(args); i++ {
		if args[i] == "-p" && i+1 < len(args) {
			parsed, err := strconv.Atoi(args[i+1])
			if err != nil {
				fatal(fmt.Errorf("invalid port: %w", err))
			}
			port = parsed
			i++
			continue
		}
		if alias == "" {
			alias = args[i]
		}
	}
	return alias, port
}

func loadOrCreateConfig() (*config.Config, error) {
	if err := xdg.EnsureDir(); err != nil {
		return nil, err
	}
	path := xdg.ConfigPath()
	if _, err := os.Stat(path); err == nil {
		return config.Load(path)
	} else if !errors.Is(err, os.ErrNotExist) {
		return nil, err
	}

	cfg := &config.Config{
		Global: config.GlobalConfig{
			RemotePort: 7890,
			LocalPort:  7890,
			SSHOptions: []string{"ServerAliveInterval=60", "ExitOnForwardFailure=yes"},
		},
		Servers: map[string]config.ServerConfig{},
	}
	if err := config.Save(path, cfg); err != nil {
		return nil, err
	}
	return config.Load(path)
}

func printHelp() {
	const help = `srp - SSH Reverse Proxy Manager

Usage:
  srp                          Show current default target and start it
  srp <command> [arguments]    Run a specific command

Commands:
  start [alias]                Start reverse proxy for alias (default: global.default)
  stop [alias]                 Stop reverse proxy for alias (default: global.default)
  status [alias]               Show status; list all running tunnels if no alias given
  set <alias>                  Set the default SSH alias target
  current                      Show the current default alias
  add <alias> [-p port]        Add a server config; -p sets remote port (default: 7890)
  remove <alias>               Remove a server config (stops it first if running)
  list                         List all configured servers with running status
  kill                         Stop the daemon and all managed SSH tunnels

Examples:
  srp add my-server -p 8080    Add server 'my-server' with remote port 8080
  srp set my-server            Set 'my-server' as the default target
  srp                          Start the default target
  srp start production         Start a specific server
  srp status                   List all running tunnels
  srp stop                     Stop the default target
  srp kill                     Stop the daemon and all tunnels

Config: ~/.config/srp/config.toml
Log:    ~/.config/srp/srp.log
`
	fmt.Print(help)
}

func fatal(err error) {
	fmt.Fprintf(os.Stderr, "Error: %v\n", err)
	os.Exit(1)
}
