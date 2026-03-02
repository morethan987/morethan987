//go:build !windows

package main

import (
	"os"
	"os/exec"
	"syscall"
)

// setSysProcAttr configures the child process to run in a new process group,
// so it survives parent exit.
func setSysProcAttr(cmd *exec.Cmd) {
	cmd.SysProcAttr = &syscall.SysProcAttr{Setpgid: true}
}

// terminateProcess sends SIGTERM to allow graceful shutdown.
func terminateProcess(proc *os.Process) error {
	return proc.Signal(syscall.SIGTERM)
}
