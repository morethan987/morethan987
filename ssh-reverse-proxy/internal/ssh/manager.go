package ssh

import (
	"context"
	"errors"
	"fmt"
	"os"
	"os/exec"
	"sync"
	"syscall"
	"time"
)

type Status string

const (
	StatusStarting Status = "starting"
	StatusRunning  Status = "running"
	StatusStopped  Status = "stopped"
	StatusFailed   Status = "failed"
)

var (
	sshBinaryPath = "/usr/bin/ssh"
	restartDelay  = 10 * time.Second
	stopTimeout   = 5 * time.Second
)

type ProcessInfo struct {
	PID          int
	Alias        string
	RemotePort   int
	LocalPort    int
	Status       Status
	RestartCount int
	StartedAt    time.Time
}

type Manager struct {
	mu     sync.Mutex
	procs  map[string]*managedProcess
	stopCh map[string]chan struct{}
}

type managedProcess struct {
	cmd         *exec.Cmd
	cancel      context.CancelFunc
	info        ProcessInfo
	restartOnce sync.Once

	sshOptions []string
	stopReq    bool
	exitCh     chan error
}

func NewManager() *Manager {
	return &Manager{
		procs:  make(map[string]*managedProcess),
		stopCh: make(map[string]chan struct{}),
	}
}

func (m *Manager) Start(alias string, remotePort, localPort int, sshOptions []string) error {
	if alias == "" {
		return errors.New("alias is required")
	}

	m.mu.Lock()
	if existing, ok := m.procs[alias]; ok && (existing.info.Status == StatusRunning || existing.info.Status == StatusStarting) {
		m.mu.Unlock()
		return fmt.Errorf("process for alias %q is already running", alias)
	}
	m.mu.Unlock()

	return m.start(alias, remotePort, localPort, sshOptions, 0)
}

func (m *Manager) start(alias string, remotePort, localPort int, sshOptions []string, restartCount int) error {
	ctx, cancel := context.WithCancel(context.Background())
	args := buildArgs(alias, remotePort, localPort, sshOptions)
	cmd := exec.CommandContext(ctx, sshBinaryPath, args...)

	mp := &managedProcess{
		cmd:        cmd,
		cancel:     cancel,
		exitCh:     make(chan error, 1),
		stopReq:    false,
		sshOptions: append([]string(nil), sshOptions...),
		info: ProcessInfo{
			Alias:        alias,
			RemotePort:   remotePort,
			LocalPort:    localPort,
			Status:       StatusStarting,
			RestartCount: restartCount,
		},
	}

	m.mu.Lock()
	m.procs[alias] = mp
	if _, ok := m.stopCh[alias]; !ok {
		m.stopCh[alias] = make(chan struct{})
	}
	stopSignal := m.stopCh[alias]
	m.mu.Unlock()

	if err := cmd.Start(); err != nil {
		cancel()
		m.mu.Lock()
		if m.procs[alias] == mp {
			mp.info.Status = StatusFailed
			mp.info.StartedAt = time.Now()
			m.procs[alias] = mp
		}
		m.mu.Unlock()
		return fmt.Errorf("start ssh process: %w", err)
	}

	m.mu.Lock()
	if m.procs[alias] == mp {
		mp.info.PID = cmd.Process.Pid
		mp.info.Status = StatusRunning
		mp.info.StartedAt = time.Now()
	}
	m.mu.Unlock()

	go m.watch(alias, mp, stopSignal)
	return nil
}

func (m *Manager) watch(alias string, mp *managedProcess, stopSignal chan struct{}) {
	err := mp.cmd.Wait()
	mp.exitCh <- err
	close(mp.exitCh)

	m.mu.Lock()
	current, ok := m.procs[alias]
	if !ok || current != mp {
		m.mu.Unlock()
		return
	}
	if current.stopReq {
		current.info.Status = StatusStopped
		current.info.PID = 0
		m.mu.Unlock()
		return
	}
	current.info.Status = StatusFailed
	current.info.PID = 0
	restartCount := current.info.RestartCount + 1
	remotePort := current.info.RemotePort
	localPort := current.info.LocalPort
	options := append([]string(nil), current.sshOptions...)
	m.mu.Unlock()

	select {
	case <-stopSignal:
		m.mu.Lock()
		if m.procs[alias] == mp {
			m.procs[alias].info.Status = StatusStopped
		}
		m.mu.Unlock()
		return
	case <-time.After(restartDelay):
	}

	if err := m.start(alias, remotePort, localPort, options, restartCount); err != nil {
		m.mu.Lock()
		if current, ok := m.procs[alias]; ok {
			current.info.Status = StatusFailed
		}
		m.mu.Unlock()
	}
}

func (m *Manager) Stop(alias string) error {
	m.mu.Lock()
	mp, ok := m.procs[alias]
	if !ok {
		m.mu.Unlock()
		return fmt.Errorf("process for alias %q not found", alias)
	}
	stopSignal, ok := m.stopCh[alias]
	if ok {
		close(stopSignal)
		delete(m.stopCh, alias)
	}
	mp.stopReq = true
	mp.info.Status = StatusStopped
	mp.info.PID = 0
	cmd := mp.cmd
	cancel := mp.cancel
	exitCh := mp.exitCh
	m.mu.Unlock()

	cancel()

	if cmd != nil && cmd.Process != nil {
		err := cmd.Process.Signal(syscall.SIGTERM)
		if err != nil && !errors.Is(err, os.ErrProcessDone) {
			return fmt.Errorf("signal process: %w", err)
		}
	}

	select {
	case <-exitCh:
	case <-time.After(stopTimeout):
		if cmd != nil && cmd.Process != nil {
			_ = cmd.Process.Kill()
			<-exitCh
		}
	}

	m.mu.Lock()
	if current, ok := m.procs[alias]; ok {
		current.info.Status = StatusStopped
		current.info.PID = 0
	}
	m.mu.Unlock()

	return nil
}

func (m *Manager) Status(alias string) (ProcessInfo, error) {
	m.mu.Lock()
	defer m.mu.Unlock()

	mp, ok := m.procs[alias]
	if !ok {
		return ProcessInfo{}, fmt.Errorf("process for alias %q not found", alias)
	}

	return mp.info, nil
}

func (m *Manager) ListStatus() map[string]ProcessInfo {
	m.mu.Lock()
	defer m.mu.Unlock()

	out := make(map[string]ProcessInfo, len(m.procs))
	for alias, mp := range m.procs {
		out[alias] = mp.info
	}
	return out
}

func (m *Manager) StopAll() error {
	m.mu.Lock()
	aliases := make([]string, 0, len(m.procs))
	for alias := range m.procs {
		aliases = append(aliases, alias)
	}
	m.mu.Unlock()

	var errs []error
	for _, alias := range aliases {
		if err := m.Stop(alias); err != nil {
			errs = append(errs, err)
		}
	}

	return errors.Join(errs...)
}

func buildArgs(alias string, remotePort, localPort int, sshOptions []string) []string {
	args := []string{"-NT"}
	for _, opt := range sshOptions {
		args = append(args, "-o", opt)
	}
	args = append(args, "-R", fmt.Sprintf("%d:localhost:%d", remotePort, localPort), alias)
	return args
}
