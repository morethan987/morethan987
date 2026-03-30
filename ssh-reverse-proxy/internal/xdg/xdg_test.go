package xdg

import (
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestConfigDirWithEnv(t *testing.T) {
	orig := os.Getenv("XDG_CONFIG_HOME")
	t.Cleanup(func() { _ = os.Setenv("XDG_CONFIG_HOME", orig) })

	_ = os.Setenv("XDG_CONFIG_HOME", "/tmp/test-xdg")
	assert.True(t, strings.HasSuffix(ConfigDir(), filepath.Join("test-xdg", "srp")))
}

func TestConfigDirWithoutEnv(t *testing.T) {
	orig := os.Getenv("XDG_CONFIG_HOME")
	t.Cleanup(func() { _ = os.Setenv("XDG_CONFIG_HOME", orig) })

	_ = os.Unsetenv("XDG_CONFIG_HOME")
	assert.True(t, strings.HasSuffix(ConfigDir(), filepath.Join(".config", "srp")))
}

func TestPaths(t *testing.T) {
	orig := os.Getenv("XDG_CONFIG_HOME")
	t.Cleanup(func() { _ = os.Setenv("XDG_CONFIG_HOME", orig) })

	_ = os.Setenv("XDG_CONFIG_HOME", "/tmp/test-xdg")
	assert.True(t, strings.HasSuffix(SocketPath(), filepath.Join("srp", "srp.sock")))
	assert.True(t, strings.HasSuffix(PidPath(), filepath.Join("srp", "srp.pid")))
	assert.True(t, strings.HasSuffix(LogPath(), filepath.Join("srp", "srp.log")))
	assert.True(t, strings.HasSuffix(ConfigPath(), filepath.Join("srp", "config.toml")))
}

func TestEnsureDir(t *testing.T) {
	t.Setenv("XDG_CONFIG_HOME", t.TempDir())

	err := EnsureDir()
	assert.NoError(t, err)
	_, statErr := os.Stat(ConfigDir())
	assert.NoError(t, statErr)
}

func TestEnsureDirCreatesNestedDirectories(t *testing.T) {
	nested := filepath.Join(t.TempDir(), "lvl1", "lvl2", "lvl3")
	t.Setenv("XDG_CONFIG_HOME", nested)

	err := EnsureDir()
	assert.NoError(t, err)
	_, statErr := os.Stat(ConfigDir())
	assert.NoError(t, statErr)
	assert.True(t, strings.HasSuffix(ConfigDir(), filepath.Join("lvl1", "lvl2", "lvl3", "srp")))
}
