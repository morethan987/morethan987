package config

import (
	"bufio"
	"fmt"
	"os"
	"path/filepath"
	"strings"
)

// Account represents a saved campus-login account.
type Account struct {
	Alias     string
	AccountID string
	IsDefault bool
}

// configDir returns the configuration directory path.
// Priority: $XDG_CONFIG_HOME/campus-login/ > $HOME/.config/campus-login/
func configDir() string {
	if xdg := os.Getenv("XDG_CONFIG_HOME"); xdg != "" {
		return filepath.Join(xdg, "campus-login")
	}
	home, _ := os.UserHomeDir()
	return filepath.Join(home, ".config", "campus-login")
}

// configFile returns the full path to the config file.
func configFile() string {
	return filepath.Join(configDir(), "config")
}

// Setup creates the configuration directory and file if they don't exist.
func Setup() error {
	dir := configDir()
	if err := os.MkdirAll(dir, 0o755); err != nil {
		return fmt.Errorf("failed to create config directory: %w", err)
	}
	f := configFile()
	if _, err := os.Stat(f); os.IsNotExist(err) {
		file, err := os.Create(f)
		if err != nil {
			return fmt.Errorf("failed to create config file: %w", err)
		}
		file.Close()
	}
	return nil
}

// readLines reads all lines from the config file.
func readLines() ([]string, error) {
	f, err := os.Open(configFile())
	if err != nil {
		return nil, fmt.Errorf("failed to open config file: %w", err)
	}
	defer f.Close()

	var lines []string
	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		lines = append(lines, scanner.Text())
	}
	return lines, scanner.Err()
}

// writeLines atomically writes lines to the config file using write-to-temp-then-rename.
func writeLines(lines []string) error {
	dir := configDir()
	tmp, err := os.CreateTemp(dir, "config-*")
	if err != nil {
		return fmt.Errorf("failed to create temp file: %w", err)
	}
	tmpName := tmp.Name()

	w := bufio.NewWriter(tmp)
	for _, line := range lines {
		if _, err := w.WriteString(line + "\n"); err != nil {
			tmp.Close()
			os.Remove(tmpName)
			return fmt.Errorf("failed to write temp file: %w", err)
		}
	}
	if err := w.Flush(); err != nil {
		tmp.Close()
		os.Remove(tmpName)
		return fmt.Errorf("failed to flush temp file: %w", err)
	}
	if err := tmp.Close(); err != nil {
		os.Remove(tmpName)
		return fmt.Errorf("failed to close temp file: %w", err)
	}

	if err := os.Rename(tmpName, configFile()); err != nil {
		os.Remove(tmpName)
		return fmt.Errorf("failed to rename temp file: %w", err)
	}
	return nil
}

// AddAccount adds or updates an account entry.
// Format: alias=account:password
// If the alias already exists, the old line is removed first.
func AddAccount(alias, account, password string) error {
	lines, err := readLines()
	if err != nil {
		return err
	}

	prefix := alias + "="
	var filtered []string
	for _, line := range lines {
		if !strings.HasPrefix(line, prefix) {
			filtered = append(filtered, line)
		}
	}
	filtered = append(filtered, fmt.Sprintf("%s=%s:%s", alias, account, password))
	return writeLines(filtered)
}

// RemoveAccount removes the account with the given alias.
// If default_account points to this alias, that line is also removed.
func RemoveAccount(alias string) error {
	lines, err := readLines()
	if err != nil {
		return err
	}

	aliasPrefix := alias + "="
	defaultLine := "default_account=" + alias

	var filtered []string
	for _, line := range lines {
		if strings.HasPrefix(line, aliasPrefix) {
			continue
		}
		if line == defaultLine {
			continue
		}
		filtered = append(filtered, line)
	}
	return writeLines(filtered)
}

// ListAccounts returns all saved accounts (excluding the default_account line).
func ListAccounts() ([]Account, error) {
	lines, err := readLines()
	if err != nil {
		return nil, err
	}

	defaultAlias, _ := getDefaultFromLines(lines)

	var accounts []Account
	for _, line := range lines {
		if strings.HasPrefix(line, "default_account=") {
			continue
		}
		parts := strings.SplitN(line, "=", 2)
		if len(parts) != 2 {
			continue
		}
		alias := parts[0]
		creds := parts[1]
		credParts := strings.SplitN(creds, ":", 2)
		accountID := credParts[0]

		accounts = append(accounts, Account{
			Alias:     alias,
			AccountID: accountID,
			IsDefault: alias == defaultAlias,
		})
	}
	return accounts, nil
}

// getDefaultFromLines extracts the default_account value from config lines.
func getDefaultFromLines(lines []string) (string, bool) {
	for _, line := range lines {
		if strings.HasPrefix(line, "default_account=") {
			val := strings.TrimPrefix(line, "default_account=")
			if val != "" {
				return val, true
			}
		}
	}
	return "", false
}

// GetDefault returns the default account alias. Returns empty string if not set (no error).
func GetDefault() (string, error) {
	lines, err := readLines()
	if err != nil {
		return "", err
	}
	alias, _ := getDefaultFromLines(lines)
	return alias, nil
}

// SetDefault sets the default account. Returns error if alias doesn't exist.
func SetDefault(alias string) error {
	lines, err := readLines()
	if err != nil {
		return err
	}

	// Verify alias exists
	prefix := alias + "="
	found := false
	for _, line := range lines {
		if strings.HasPrefix(line, prefix) {
			found = true
			break
		}
	}
	if !found {
		return fmt.Errorf("account alias %q not found", alias)
	}

	// Remove existing default_account line(s) and append new one
	var filtered []string
	for _, line := range lines {
		if !strings.HasPrefix(line, "default_account=") {
			filtered = append(filtered, line)
		}
	}
	filtered = append(filtered, "default_account="+alias)
	return writeLines(filtered)
}

// GetCredentials returns the account ID and password for the given alias.
// Uses strings.SplitN(creds, ":", 2) so passwords containing ":" are handled correctly.
func GetCredentials(alias string) (account, password string, err error) {
	lines, err := readLines()
	if err != nil {
		return "", "", err
	}

	prefix := alias + "="
	for _, line := range lines {
		if strings.HasPrefix(line, prefix) {
			val := strings.TrimPrefix(line, prefix)
			parts := strings.SplitN(val, ":", 2)
			if len(parts) != 2 {
				return "", "", fmt.Errorf("invalid credential format for alias %q", alias)
			}
			return parts[0], parts[1], nil
		}
	}
	return "", "", fmt.Errorf("account alias %q not found", alias)
}
