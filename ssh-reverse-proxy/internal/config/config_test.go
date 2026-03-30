package config

import (
	"os"
	"path/filepath"
	"sync"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestLoad(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	err := os.WriteFile(path, []byte(`
[global]
default = "prod"
remote_port = 9000
local_port = 9001
ssh_options = ["A=1", "B=2"]

[servers.app]
remote_port = 10000
local_port = 10001
`), 0o644)
	require.NoError(t, err)

	cfg, err := Load(path)
	require.NoError(t, err)
	require.NotNil(t, cfg)
	assert.Equal(t, "prod", cfg.Global.Default)
	assert.Equal(t, 9000, cfg.Global.RemotePort)
	assert.Equal(t, 9001, cfg.Global.LocalPort)
	assert.Equal(t, []string{"A=1", "B=2"}, cfg.Global.SSHOptions)
	assert.Equal(t, ServerConfig{RemotePort: 10000, LocalPort: 10001}, cfg.Servers["app"])
}

func TestLoadPartialTOMLWithoutServers(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	err := os.WriteFile(path, []byte(`
[global]
default = "prod"
remote_port = 9000
local_port = 9001
`), 0o644)
	require.NoError(t, err)

	cfg, err := Load(path)
	require.NoError(t, err)
	require.NotNil(t, cfg)
	assert.Equal(t, "prod", cfg.Global.Default)
	assert.Equal(t, 9000, cfg.Global.RemotePort)
	assert.Equal(t, 9001, cfg.Global.LocalPort)
	assert.NotNil(t, cfg.Servers)
	assert.Empty(t, cfg.Servers)
}

func TestSaveCreatesParentDirectories(t *testing.T) {
	path := filepath.Join(t.TempDir(), "a", "b", "c", "config.toml")
	cfg := &Config{Servers: map[string]ServerConfig{}}

	require.NoError(t, Save(path, cfg))

	_, err := os.Stat(path)
	require.NoError(t, err)
}

func TestSaveRoundTrip(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "nested", "config.toml")
	original := &Config{
		Global: GlobalConfig{
			Default:    "prod",
			RemotePort: 8100,
			LocalPort:  8200,
			SSHOptions: []string{"ServerAliveInterval=60"},
		},
		Servers: map[string]ServerConfig{
			"app": {RemotePort: 8300, LocalPort: 8400},
		},
	}

	require.NoError(t, Save(path, original))
	loaded, err := Load(path)
	require.NoError(t, err)
	assert.Equal(t, original.Global, loaded.Global)
	assert.Equal(t, original.Servers, loaded.Servers)
}

func TestGetEffectivePort(t *testing.T) {
	cfg := &Config{
		Global: GlobalConfig{RemotePort: 7890, LocalPort: 7891},
		Servers: map[string]ServerConfig{
			"app": {RemotePort: 9000, LocalPort: 9001},
			"bad": {RemotePort: 0, LocalPort: 9101},
		},
	}

	rp, lp := cfg.GetEffectivePort("app")
	assert.Equal(t, 9000, rp)
	assert.Equal(t, 9001, lp)

	rp, lp = cfg.GetEffectivePort("bad")
	assert.Equal(t, 7890, rp)
	assert.Equal(t, 7891, lp)

	rp, lp = cfg.GetEffectivePort("missing")
	assert.Equal(t, 7890, rp)
	assert.Equal(t, 7891, lp)
}

func TestLoadMissingFile(t *testing.T) {
	_, err := Load(filepath.Join(t.TempDir(), "missing.toml"))
	require.Error(t, err)
}

func TestAddServer(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	require.NoError(t, Save(path, &Config{Servers: map[string]ServerConfig{}}))

	cfg, err := Load(path)
	require.NoError(t, err)
	require.NoError(t, cfg.AddServer("app", 10000, 10001))

	loaded, err := Load(path)
	require.NoError(t, err)
	assert.Equal(t, ServerConfig{RemotePort: 10000, LocalPort: 10001}, loaded.Servers["app"])
}

func TestAddServerOverwriteExistingAlias(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	require.NoError(t, Save(path, &Config{Servers: map[string]ServerConfig{}}))

	cfg, err := Load(path)
	require.NoError(t, err)
	require.NoError(t, cfg.AddServer("app", 10000, 10001))
	require.NoError(t, cfg.AddServer("app", 20000, 20001))

	loaded, err := Load(path)
	require.NoError(t, err)
	assert.Equal(t, ServerConfig{RemotePort: 20000, LocalPort: 20001}, loaded.Servers["app"])
}

func TestConcurrentAddServerRemoveServer(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	require.NoError(t, Save(path, &Config{Servers: map[string]ServerConfig{}}))

	cfg, err := Load(path)
	require.NoError(t, err)

	var wg sync.WaitGroup
	aliases := []string{"a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8"}
	for i, alias := range aliases {
		remote := 10000 + i
		local := 11000 + i
		wg.Go(func() {
			for range 20 {
				require.NoError(t, cfg.AddServer(alias, remote, local))
				require.NoError(t, cfg.RemoveServer(alias))
			}
		})
	}
	wg.Wait()

	loaded, err := Load(path)
	require.NoError(t, err)
	assert.Empty(t, loaded.Servers)
}

func TestRemoveServer(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	require.NoError(t, Save(path, &Config{Servers: map[string]ServerConfig{}}))

	cfg, err := Load(path)
	require.NoError(t, err)
	require.NoError(t, cfg.AddServer("app", 10000, 10001))
	require.NoError(t, cfg.RemoveServer("app"))

	loaded, err := Load(path)
	require.NoError(t, err)
	_, ok := loaded.Servers["app"]
	assert.False(t, ok)
}

func TestRemoveServerMissing(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	require.NoError(t, Save(path, &Config{Servers: map[string]ServerConfig{}}))

	cfg, err := Load(path)
	require.NoError(t, err)
	err = cfg.RemoveServer("missing")
	require.Error(t, err)
	assert.Equal(t, `server "missing" not found`, err.Error())
}

func TestSetDefault(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	require.NoError(t, Save(path, &Config{Servers: map[string]ServerConfig{}}))

	cfg, err := Load(path)
	require.NoError(t, err)
	require.NoError(t, cfg.SetDefault("app"))

	loaded, err := Load(path)
	require.NoError(t, err)
	assert.Equal(t, "app", loaded.Global.Default)
}

func TestListServers(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.toml")
	require.NoError(t, Save(path, &Config{Servers: map[string]ServerConfig{}}))

	cfg, err := Load(path)
	require.NoError(t, err)
	require.NoError(t, cfg.AddServer("zeta", 3000, 3001))
	require.NoError(t, cfg.AddServer("alpha", 1000, 1001))
	require.NoError(t, cfg.AddServer("mu", 2000, 2001))

	servers := cfg.ListServers()
	require.Len(t, servers, 3)
	assert.Equal(t, []ServerInfo{
		{Alias: "alpha", RemotePort: 1000, LocalPort: 1001},
		{Alias: "mu", RemotePort: 2000, LocalPort: 2001},
		{Alias: "zeta", RemotePort: 3000, LocalPort: 3001},
	}, servers)
}
