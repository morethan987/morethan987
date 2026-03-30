package daemon

import (
	"encoding/json"
	"errors"
	"fmt"
	"net"
	"os"
	"os/signal"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/morethan987/ssh-reverse-proxy/internal/config"
	"github.com/morethan987/ssh-reverse-proxy/internal/ipc"
	"github.com/morethan987/ssh-reverse-proxy/internal/logger"
	"github.com/morethan987/ssh-reverse-proxy/internal/ssh"
	"github.com/morethan987/ssh-reverse-proxy/internal/xdg"
)

type Daemon struct {
	sshMgr     *ssh.Manager
	cfg        *config.Config
	log        *logger.Logger
	listener   net.Listener
	socketPath string
	pidPath    string
	done       chan struct{}
}

func New(cfg *config.Config, log *logger.Logger) *Daemon {
	return &Daemon{
		sshMgr:     ssh.NewManager(),
		cfg:        cfg,
		log:        log,
		socketPath: xdg.SocketPath(),
		pidPath:    xdg.PidPath(),
		done:       make(chan struct{}),
	}
}

func (d *Daemon) Start() error {
	if err := xdg.EnsureDir(); err != nil {
		return fmt.Errorf("ensure xdg dir: %w", err)
	}

	if pidBytes, err := os.ReadFile(d.pidPath); err == nil {
		pidStr := strings.TrimSpace(string(pidBytes))
		if pidStr != "" {
			pid, convErr := strconv.Atoi(pidStr)
			if convErr == nil && isProcessAlive(pid) {
				return errors.New("daemon already running")
			}
		}
		_ = os.Remove(d.pidPath)
	} else if !errors.Is(err, os.ErrNotExist) {
		return fmt.Errorf("read pid file: %w", err)
	}

	if err := os.Remove(d.socketPath); err != nil && !errors.Is(err, os.ErrNotExist) {
		return fmt.Errorf("remove stale socket: %w", err)
	}

	listener, err := net.Listen("unix", d.socketPath)
	if err != nil {
		return fmt.Errorf("listen on unix socket: %w", err)
	}
	d.listener = listener

	if err := os.WriteFile(d.pidPath, []byte(strconv.Itoa(os.Getpid())), 0o644); err != nil {
		_ = listener.Close()
		_ = os.Remove(d.socketPath)
		return fmt.Errorf("write pid file: %w", err)
	}

	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, syscall.SIGTERM, syscall.SIGINT)
	go func() {
		<-sigCh
		signal.Stop(sigCh)
		_ = d.StopAll()
		if d.listener != nil {
			_ = d.listener.Close()
		}
		_ = os.Remove(d.socketPath)
		_ = os.Remove(d.pidPath)
		if d.log != nil {
			_ = d.log.Close()
		}
		select {
		case <-d.done:
		default:
			close(d.done)
		}
	}()

	if d.log != nil {
		d.log.Infof("daemon started on socket %s", d.socketPath)
	}

	for {
		conn, err := d.listener.Accept()
		if err != nil {
			select {
			case <-d.done:
				return nil
			default:
			}
			if errors.Is(err, net.ErrClosed) {
				return nil
			}
			return fmt.Errorf("accept connection: %w", err)
		}
		go d.handleConn(conn)
	}
}

func (d *Daemon) StopAll() error {
	if d.sshMgr == nil {
		return nil
	}
	return d.sshMgr.StopAll()
}

func (d *Daemon) handleConn(conn net.Conn) {
	defer conn.Close()

	_ = conn.SetReadDeadline(time.Now().Add(10 * time.Second))

	dec := json.NewDecoder(conn)
	var req ipc.Request
	if err := dec.Decode(&req); err != nil {
		resp := ipc.Response{Success: false, Message: fmt.Sprintf("invalid request: %v", err)}
		_ = json.NewEncoder(conn).Encode(resp)
		if d.log != nil {
			d.log.Errorf("action=decode alias= success=false msg=%s", resp.Message)
		}
		return
	}

	enc := json.NewEncoder(conn)
	resp := ipc.Response{}

	switch req.Action {
	case ipc.ActionStart:
		remotePort := req.Port
		localPort := d.cfg.Global.LocalPort
		if remotePort == 0 {
			remotePort, localPort = d.cfg.GetEffectivePort(req.Alias)
		}
		if err := d.sshMgr.Start(req.Alias, remotePort, localPort, d.cfg.Global.SSHOptions); err != nil {
			resp = ipc.Response{Success: false, Message: err.Error()}
		} else {
			resp = ipc.Response{Success: true, Message: fmt.Sprintf("Started %s", req.Alias)}
		}

	case ipc.ActionStop:
		if err := d.sshMgr.Stop(req.Alias); err != nil {
			resp = ipc.Response{Success: false, Message: err.Error()}
		} else {
			resp = ipc.Response{Success: true, Message: fmt.Sprintf("Stopped %s", req.Alias)}
		}

	case ipc.ActionStatus:
		var data any
		if req.Alias != "" {
			status, err := d.sshMgr.Status(req.Alias)
			if err != nil {
				resp = ipc.Response{Success: false, Message: err.Error()}
				break
			}
			data = status
		} else {
			data = d.sshMgr.ListStatus()
		}
		resp = ipc.Response{Success: true, Message: "Status retrieved", Data: mustMarshal(data)}

	case ipc.ActionSet:
		if err := d.cfg.SetDefault(req.Alias); err != nil {
			resp = ipc.Response{Success: false, Message: err.Error()}
		} else {
			resp = ipc.Response{Success: true, Message: fmt.Sprintf("Default target set to %s", req.Alias)}
		}

	case ipc.ActionCurrent:
		resp = ipc.Response{
			Success: true,
			Message: "Current default retrieved",
			Data:    mustMarshal(map[string]string{"default": d.cfg.Global.Default}),
		}

	case ipc.ActionAdd:
		port := req.Port
		if port <= 0 {
			port = d.cfg.Global.RemotePort
		}
		if err := d.cfg.AddServer(req.Alias, port, 0); err != nil {
			resp = ipc.Response{Success: false, Message: err.Error()}
		} else {
			resp = ipc.Response{Success: true, Message: fmt.Sprintf("Added %s", req.Alias)}
		}

	case ipc.ActionRemove:
		_ = d.sshMgr.Stop(req.Alias)
		if err := d.cfg.RemoveServer(req.Alias); err != nil {
			resp = ipc.Response{Success: false, Message: err.Error()}
		} else {
			resp = ipc.Response{Success: true, Message: fmt.Sprintf("Removed %s", req.Alias)}
		}

	case ipc.ActionList:
		data := struct {
			Servers []config.ServerInfo        `json:"servers"`
			Running map[string]ssh.ProcessInfo `json:"running"`
		}{
			Servers: d.cfg.ListServers(),
			Running: d.sshMgr.ListStatus(),
		}
		resp = ipc.Response{Success: true, Message: "Servers listed", Data: mustMarshal(data)}

	default:
		resp = ipc.Response{Success: false, Message: fmt.Sprintf("unknown action: %s", req.Action)}
	}

	_ = enc.Encode(resp)
	if d.log != nil {
		d.log.Infof("action=%s alias=%s success=%t msg=%s", req.Action, req.Alias, resp.Success, resp.Message)
	}
}

func mustMarshal(v any) json.RawMessage {
	b, err := json.Marshal(v)
	if err != nil {
		return json.RawMessage(`null`)
	}
	return b
}

func isProcessAlive(pid int) bool {
	p, err := os.FindProcess(pid)
	if err != nil {
		return false
	}
	err = p.Signal(syscall.Signal(0))
	return err == nil
}
