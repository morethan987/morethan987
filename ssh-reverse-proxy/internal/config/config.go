package config

import (
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"sync"

	"github.com/BurntSushi/toml"
)

type Config struct {
	mu         sync.Mutex              `toml:"-"`
	Global     GlobalConfig            `toml:"global"`
	Servers    map[string]ServerConfig `toml:"servers"`
	configPath string                  `toml:"-"`
}

type GlobalConfig struct {
	Default    string   `toml:"default"`
	RemotePort int      `toml:"remote_port"`
	LocalPort  int      `toml:"local_port"`
	SSHOptions []string `toml:"ssh_options"`
}

type ServerConfig struct {
	RemotePort int `toml:"remote_port"`
	LocalPort  int `toml:"local_port"`
}

func Load(path string) (*Config, error) {
	cfg := &Config{
		mu: sync.Mutex{},
		Global: GlobalConfig{
			RemotePort: 7890,
			LocalPort:  7890,
			SSHOptions: []string{"ServerAliveInterval=60", "ExitOnForwardFailure=yes"},
		},
		Servers: map[string]ServerConfig{},
	}

	if _, err := toml.DecodeFile(path, cfg); err != nil {
		return nil, err
	}
	if cfg.Servers == nil {
		cfg.Servers = map[string]ServerConfig{}
	}
	if len(cfg.Global.SSHOptions) == 0 {
		cfg.Global.SSHOptions = []string{"ServerAliveInterval=60", "ExitOnForwardFailure=yes"}
	}
	if cfg.Global.RemotePort == 0 {
		cfg.Global.RemotePort = 7890
	}
	if cfg.Global.LocalPort == 0 {
		cfg.Global.LocalPort = 7890
	}
	cfg.configPath = path
	return cfg, nil
}

func Save(path string, cfg *Config) error {
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return err
	}
	f, err := os.Create(path)
	if err != nil {
		return err
	}
	defer f.Close()

	enc := toml.NewEncoder(f)
	enc.Indent = "  "
	return enc.Encode(cfg)
}

func (c *Config) GetEffectivePort(alias string) (int, int) {
	if c == nil {
		return 0, 0
	}
	if server, ok := c.Servers[alias]; ok && server.RemotePort > 0 {
		return server.RemotePort, server.LocalPort
	}
	return c.Global.RemotePort, c.Global.LocalPort
}

type ServerInfo struct {
	Alias      string
	RemotePort int
	LocalPort  int
}

func (c *Config) AddServer(alias string, remotePort, localPort int) error {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.Servers == nil {
		c.Servers = map[string]ServerConfig{}
	}
	c.Servers[alias] = ServerConfig{RemotePort: remotePort, LocalPort: localPort}
	return Save(c.configPath, c)
}

func (c *Config) RemoveServer(alias string) error {
	c.mu.Lock()
	defer c.mu.Unlock()
	if _, ok := c.Servers[alias]; !ok {
		return fmt.Errorf("server %q not found", alias)
	}
	delete(c.Servers, alias)
	return Save(c.configPath, c)
}

func (c *Config) SetDefault(alias string) error {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.Global.Default = alias
	return Save(c.configPath, c)
}

func (c *Config) ListServers() []ServerInfo {
	c.mu.Lock()
	defer c.mu.Unlock()
	infos := make([]ServerInfo, 0, len(c.Servers))
	for alias, server := range c.Servers {
		infos = append(infos, ServerInfo{Alias: alias, RemotePort: server.RemotePort, LocalPort: server.LocalPort})
	}
	sort.Slice(infos, func(i, j int) bool { return infos[i].Alias < infos[j].Alias })
	return infos
}
