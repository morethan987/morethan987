//go:build windows

package main

import (
	"os"
	"os/exec"
	"syscall"
)

// setSysProcAttr configures the child process with CREATE_NEW_CONSOLE flag,
// so it runs independently of the parent's console.
func setSysProcAttr(cmd *exec.Cmd) {
	cmd.SysProcAttr = &syscall.SysProcAttr{CreationFlags: 0x00000010}
}

// terminateProcess forcefully kills the process (Windows lacks SIGTERM).
func terminateProcess(proc *os.Process) error {
	return proc.Kill()
}
