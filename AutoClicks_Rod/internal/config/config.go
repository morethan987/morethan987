package config

import (
	"fmt"
	"os"

	"gopkg.in/yaml.v3"
)

// Config holds all application configuration loaded from YAML.
type Config struct {
	Username  string         `yaml:"username"`
	Password  string         `yaml:"password"`
	URL       string         `yaml:"url"`
	Interval  int            `yaml:"interval"`
	MaxRetry  int            `yaml:"max_retry"`
	Headless  bool           `yaml:"headless"`
	ServerKey string         `yaml:"server_key"`
	Courses   []TargetCourse `yaml:"courses"`
	Selectors SelectorConfig `yaml:"selectors"`
}

// TargetCourse describes a course to watch and enroll in.
type TargetCourse struct {
	Name     string   `yaml:"name"`
	ID       string   `yaml:"id"`
	Teachers []string `yaml:"teachers"`
}

// SelectorConfig groups all CSS selector sets.
type SelectorConfig struct {
	Login     LoginSelectors     `yaml:"login"`
	Course    CourseSelectors    `yaml:"course"`
	Sidebar   SidebarSelectors   `yaml:"sidebar"`
	Selection SelectionSelectors `yaml:"selection"`
}

// LoginSelectors contains CSS selectors for the login page.
type LoginSelectors struct {
	UsernameInput string `yaml:"username_input"`
	PasswordInput string `yaml:"password_input"`
	LoginButton   string `yaml:"login_button"`
}

// CourseSelectors contains CSS selectors for the course list page.
type CourseSelectors struct {
	Flag    string `yaml:"flag"`
	DataRow string `yaml:"data_row"`
}

// SidebarSelectors contains CSS selectors for the course detail sidebar.
type SidebarSelectors struct {
	SidebarFlag  string `yaml:"sidebar_flag"`
	Checkbox     string `yaml:"checkbox"`
	CloseButton  string `yaml:"close_button"`
	DataRow      string `yaml:"data_row"`
	FullFlag     string `yaml:"full_flag"`
	SelectedFlag string `yaml:"selected_flag"`
}

// SelectionSelectors contains CSS selectors for the enrollment confirmation flow.
type SelectionSelectors struct {
	SelectButton  string `yaml:"select_button"`
	ConfirmButton string `yaml:"confirm_button"`
}

// Load reads and parses the YAML config file at path, then validates required fields.
func Load(path string) (*Config, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("config: read file %q: %w", path, err)
	}

	var cfg Config
	if err := yaml.Unmarshal(data, &cfg); err != nil {
		return nil, fmt.Errorf("config: parse YAML: %w", err)
	}

	if err := validate(&cfg); err != nil {
		return nil, err
	}

	return &cfg, nil
}

func validate(cfg *Config) error {
	if cfg.Username == "" {
		return fmt.Errorf("config: username is required")
	}
	if cfg.Password == "" {
		return fmt.Errorf("config: password is required")
	}
	if cfg.URL == "" {
		return fmt.Errorf("config: url is required")
	}
	if len(cfg.Courses) == 0 {
		return fmt.Errorf("config: courses list must not be empty")
	}
	if cfg.Interval <= 0 {
		return fmt.Errorf("config: interval must be > 0")
	}
	if cfg.MaxRetry <= 0 {
		return fmt.Errorf("config: max_retry must be > 0")
	}
	return nil
}
