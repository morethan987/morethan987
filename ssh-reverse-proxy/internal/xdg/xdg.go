package xdg

import (
	"os"
	"path/filepath"
)

func ConfigDir() string {
	dir := os.Getenv("XDG_CONFIG_HOME")
	if dir == "" {
		home, _ := os.UserHomeDir()
		dir = filepath.Join(home, ".config")
	}
	return filepath.Join(dir, "srp")
}

func SocketPath() string { return filepath.Join(ConfigDir(), "srp.sock") }
func PidPath() string    { return filepath.Join(ConfigDir(), "srp.pid") }
func LogPath() string    { return filepath.Join(ConfigDir(), "srp.log") }
func ConfigPath() string { return filepath.Join(ConfigDir(), "config.toml") }

func EnsureDir() error {
	return os.MkdirAll(ConfigDir(), 0o755)
}
