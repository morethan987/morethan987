package logger

import (
	"os"
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestNewCreatesLogFile(t *testing.T) {
	logPath := filepath.Join(t.TempDir(), "app.log")

	l, err := New(logPath)
	require.NoError(t, err)
	require.NotNil(t, l)
	require.NoError(t, l.Close())

	_, err = os.Stat(logPath)
	require.NoError(t, err)
}

func TestInfofWritesToFile(t *testing.T) {
	logPath := filepath.Join(t.TempDir(), "info.log")

	l, err := New(logPath)
	require.NoError(t, err)

	l.Infof("hello %s", "world")
	require.NoError(t, l.Close())

	data, err := os.ReadFile(logPath)
	require.NoError(t, err)
	assert.Contains(t, string(data), "[INFO]")
	assert.Contains(t, string(data), "hello world")
}

func TestErrorfWritesToFile(t *testing.T) {
	logPath := filepath.Join(t.TempDir(), "error.log")

	l, err := New(logPath)
	require.NoError(t, err)

	l.Errorf("boom %d", 7)
	require.NoError(t, l.Close())

	data, err := os.ReadFile(logPath)
	require.NoError(t, err)
	assert.Contains(t, string(data), "[ERROR]")
	assert.Contains(t, string(data), "boom 7")
}

func TestDebugfWritesToFile(t *testing.T) {
	logPath := filepath.Join(t.TempDir(), "debug.log")

	l, err := New(logPath)
	require.NoError(t, err)

	l.Debugf("debug %s", "value")
	require.NoError(t, l.Close())

	data, err := os.ReadFile(logPath)
	require.NoError(t, err)
	assert.Contains(t, string(data), "[DEBUG]")
	assert.Contains(t, string(data), "debug value")
}

func TestNewWithInvalidParentReturnsError(t *testing.T) {
	parentFile := filepath.Join(t.TempDir(), "parent-file")
	require.NoError(t, os.WriteFile(parentFile, []byte("x"), 0o644))

	_, err := New(filepath.Join(parentFile, "app.log"))
	require.Error(t, err)
}

func TestCloseWorks(t *testing.T) {
	logPath := filepath.Join(t.TempDir(), "close.log")

	l, err := New(logPath)
	require.NoError(t, err)
	require.NoError(t, l.Close())
}

func TestNewCreatesNestedParentDirectories(t *testing.T) {
	logPath := filepath.Join(t.TempDir(), "a", "b", "c", "nested.log")

	l, err := New(logPath)
	require.NoError(t, err)
	require.NoError(t, l.Close())

	_, err = os.Stat(logPath)
	require.NoError(t, err)
}
