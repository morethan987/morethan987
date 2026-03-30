package main_test

import (
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"syscall"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestCLIFullLifecycle(t *testing.T) {
	tmpDir := t.TempDir()
	binaryDir := filepath.Join(tmpDir, "bin")
	require.NoError(t, os.MkdirAll(binaryDir, 0o755))
	binary := filepath.Join(binaryDir, "srp")

	build := exec.Command("go", "build", "-o", binary, "./cmd/srp")
	build.Dir = "/home/morethan/GitHub/morethan987/ssh-reverse-proxy"
	buildOut, err := build.CombinedOutput()
	require.NoError(t, err, string(buildOut))

	env := append(os.Environ(), "XDG_CONFIG_HOME="+tmpDir)
	t.Cleanup(func() {
		cleanupDaemon(t, tmpDir)
	})

	out := runCmd(t, env, binary, "current")
	assert.Contains(t, out, "Current default SRP target:")

	out = runCmd(t, env, binary, "add", "test-server", "-p", "9090")
	assert.Contains(t, out, "Added server 'test-server' with remote port 9090")

	out = runCmd(t, env, binary, "list")
	assert.Contains(t, out, "test-server")
	assert.Contains(t, out, "port=9090")

	out = runCmd(t, env, binary, "set", "test-server")
	assert.Contains(t, out, "Default SRP target set to: test-server")

	out = runCmd(t, env, binary, "current")
	assert.Contains(t, out, "Current default SRP target: test-server")

	_ = runCmd(t, env, binary, "status")

	stop := exec.Command(binary, "stop", "test-server")
	stop.Env = env
	stopOut, stopErr := stop.CombinedOutput()
	if stopErr == nil {
		assert.Contains(t, string(stopOut), "Stopped test-server")
	} else {
		assert.Contains(t, string(stopOut), `Error: process for alias "test-server" not found`)
	}

	out = runCmd(t, env, binary, "remove", "test-server")
	assert.Contains(t, out, "Removed server 'test-server'")

	out = runCmd(t, env, binary, "list")
	assert.Contains(t, out, "No configured SRP servers")

	out = runCmd(t, env, binary, "--help")
	assert.Contains(t, out, "srp - SSH Reverse Proxy Manager")
	assert.Contains(t, out, "Commands:")

	unknown := exec.Command(binary, "unknown")
	unknown.Env = env
	err = unknown.Run()
	require.Error(t, err)

	exitErr, ok := err.(*exec.ExitError)
	require.True(t, ok)
	status, ok := exitErr.Sys().(syscall.WaitStatus)
	require.True(t, ok)
	assert.Equal(t, 2, status.ExitStatus())
}

func runCmd(t *testing.T, env []string, binary string, args ...string) string {
	t.Helper()

	cmd := exec.Command(binary, args...)
	cmd.Env = env
	out, err := cmd.CombinedOutput()
	require.NoError(t, err, string(out))
	return string(out)
}

func cleanupDaemon(t *testing.T, xdgConfigHome string) {
	t.Helper()

	pidPath := filepath.Join(xdgConfigHome, "srp", "srp.pid")
	pidBytes, err := os.ReadFile(pidPath)
	if err != nil {
		return
	}

	pid, err := strconv.Atoi(strings.TrimSpace(string(pidBytes)))
	if err != nil || pid <= 0 {
		return
	}

	proc, err := os.FindProcess(pid)
	if err != nil {
		return
	}

	_ = proc.Signal(syscall.SIGTERM)
	deadline := time.Now().Add(3 * time.Second)
	for time.Now().Before(deadline) {
		if err := proc.Signal(syscall.Signal(0)); err != nil {
			break
		}
		time.Sleep(100 * time.Millisecond)
	}
}
