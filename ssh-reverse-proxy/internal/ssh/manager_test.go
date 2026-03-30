package ssh

import (
	"fmt"
	"os"
	"path/filepath"
	"sync"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestStartBuildsExpectedCommandArgs(t *testing.T) {
	m := NewManager()
	binary := writeScript(t, "#!/bin/sh\ntrap 'exit 0' TERM INT\nwhile true; do sleep 1; done\n")
	setTestBinary(t, binary)

	err := m.Start("a6000", 7890, 7891, []string{"ServerAliveInterval=60", "ExitOnForwardFailure=yes"})
	require.NoError(t, err)
	t.Cleanup(func() { _ = m.StopAll() })

	m.mu.Lock()
	mp := m.procs["a6000"]
	require.NotNil(t, mp)
	args := append([]string(nil), mp.cmd.Args...)
	m.mu.Unlock()

	require.GreaterOrEqual(t, len(args), 2)
	assert.Equal(t, binary, args[0])
	assert.Equal(t, []string{"-NT", "-o", "ServerAliveInterval=60", "-o", "ExitOnForwardFailure=yes", "-R", "7890:localhost:7891", "a6000"}, args[1:])
}

func TestStartEmptyAliasReturnsError(t *testing.T) {
	m := NewManager()
	err := m.Start("", 7890, 7891, nil)
	require.Error(t, err)
	assert.Contains(t, err.Error(), "alias is required")
}

func TestStatusNonExistentAliasReturnsError(t *testing.T) {
	m := NewManager()
	_, err := m.Status("missing")
	require.Error(t, err)
	assert.Contains(t, err.Error(), `process for alias "missing" not found`)
}

func TestListStatusEmptyManager(t *testing.T) {
	m := NewManager()
	statuses := m.ListStatus()
	require.NotNil(t, statuses)
	assert.Empty(t, statuses)
}

func TestStopAllWithNoProcesses(t *testing.T) {
	m := NewManager()
	require.NoError(t, m.StopAll())
}

func TestStopTerminatesProcess(t *testing.T) {
	m := NewManager()
	binary := writeScript(t, "#!/bin/sh\ntrap 'exit 0' TERM INT\nwhile true; do sleep 1; done\n")
	setTestBinary(t, binary)

	err := m.Start("runner", 9000, 9001, nil)
	require.NoError(t, err)

	status, err := m.Status("runner")
	require.NoError(t, err)
	assert.Equal(t, StatusRunning, status.Status)
	assert.NotZero(t, status.PID)

	err = m.Stop("runner")
	require.NoError(t, err)

	status, err = m.Status("runner")
	require.NoError(t, err)
	assert.Equal(t, StatusStopped, status.Status)
	assert.Zero(t, status.PID)
}

func TestAutoRestartIncrementsRestartCount(t *testing.T) {
	m := NewManager()
	binary := writeScript(t, "#!/bin/sh\nexit 1\n")
	setTestBinary(t, binary)
	t.Cleanup(func() { _ = m.StopAll() })

	err := m.Start("flaky", 9010, 9011, nil)
	require.NoError(t, err)

	require.Eventually(t, func() bool {
		status, statusErr := m.Status("flaky")
		if statusErr != nil {
			return false
		}
		return status.RestartCount > 0
	}, 15*time.Second, 100*time.Millisecond)
}

func TestStopPreventsRestart(t *testing.T) {
	m := NewManager()
	binary := writeScript(t, "#!/bin/sh\ntrap 'exit 0' TERM INT\nwhile true; do sleep 1; done\n")
	setTestBinary(t, binary)

	err := m.Start("stable", 9020, 9021, nil)
	require.NoError(t, err)

	first, err := m.Status("stable")
	require.NoError(t, err)

	err = m.Stop("stable")
	require.NoError(t, err)

	time.Sleep(400 * time.Millisecond)

	after, err := m.Status("stable")
	require.NoError(t, err)
	assert.Equal(t, StatusStopped, after.Status)
	assert.Equal(t, first.RestartCount, after.RestartCount)
}

func TestConcurrentStartStop(t *testing.T) {
	m := NewManager()
	binary := writeScript(t, "#!/bin/sh\ntrap 'exit 0' TERM INT\nwhile true; do sleep 1; done\n")
	setTestBinary(t, binary)

	aliases := []string{"a1", "a2", "a3", "a4", "a5"}
	var wg sync.WaitGroup

	for _, alias := range aliases {
		wg.Go(func() {
			for i := range 15 {
				_ = m.Start(alias, 9300+i, 9400+i, nil)
				_ = m.Stop(alias)
			}
		})
	}

	wg.Wait()
	require.NoError(t, m.StopAll())
	statuses := m.ListStatus()
	for _, alias := range aliases {
		if st, ok := statuses[alias]; ok {
			assert.Equal(t, StatusStopped, st.Status)
		}
	}
}

func setTestBinary(t *testing.T, binary string) {
	t.Helper()
	oldBinary := sshBinaryPath
	sshBinaryPath = binary
	t.Cleanup(func() {
		sshBinaryPath = oldBinary
	})
}

func writeScript(t *testing.T, content string) string {
	t.Helper()
	path := filepath.Join(t.TempDir(), fmt.Sprintf("ssh-mock-%d.sh", time.Now().UnixNano()))
	require.NoError(t, os.WriteFile(path, []byte(content), 0o755))
	return path
}
