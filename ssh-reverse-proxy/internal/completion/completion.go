package completion

import (
	"os"
	"os/user"
	"path/filepath"
	"strings"

	"github.com/morethan987/ssh-reverse-proxy/internal/config"
	"github.com/morethan987/ssh-reverse-proxy/internal/xdg"
	"github.com/posener/complete/v2"
	"github.com/posener/complete/v2/predict"
)

func SRPCommand() *complete.Command {
	return &complete.Command{
		Sub: map[string]*complete.Command{
			"start": {
				Args: predictAliases(),
			},
			"stop": {
				Args: predictAliases(),
			},
			"status": {
				Args: predictAliases(),
			},
			"set": {
				Args: predictAliases(),
			},
			"current": {},
			"add": {
				Flags: map[string]complete.Predictor{
					"p": predict.Nothing,
				},
				Args: predict.Or(
					predictAliases(),
					complete.PredictFunc(func(prefix string) []string {
						return sshHosts()
					}),
				),
			},
			"remove": {
				Args: predictAliases(),
			},
			"list": {},
			"kill": {},
		},
	}
}

func predictAliases() complete.Predictor {
	return complete.PredictFunc(func(prefix string) []string {
		cfg, err := loadConfig()
		if err != nil {
			return nil
		}
		aliases := make([]string, 0, len(cfg.Servers))
		for alias := range cfg.Servers {
			aliases = append(aliases, alias)
		}
		return aliases
	})
}

func sshHosts() []string {
	u, err := user.Current()
	if err != nil {
		return nil
	}
	data, err := os.ReadFile(filepath.Join(u.HomeDir, ".ssh", "config"))
	if err != nil {
		return nil
	}
	var hosts []string
	for _, line := range strings.Split(string(data), "\n") {
		line = strings.TrimSpace(line)
		if strings.HasPrefix(strings.ToLower(line), "host ") {
			fields := strings.Fields(line)
			if len(fields) > 1 {
				name := fields[1]
				if !strings.Contains(name, "*") && !strings.Contains(name, "?") {
					hosts = append(hosts, name)
				}
			}
		}
	}
	return hosts
}

func loadConfig() (*config.Config, error) {
	path := xdg.ConfigPath()
	if _, err := os.Stat(path); err != nil {
		return nil, err
	}
	return config.Load(path)
}
