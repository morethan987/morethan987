package config

import (
	"os"
	"path/filepath"
	"strings"
	"testing"
)

// setupTestEnv sets HOME to a temp directory and calls Setup().
// Returns a cleanup function that restores the original HOME.
func setupTestEnv(t *testing.T) {
	t.Helper()
	tmpDir := t.TempDir()
	t.Setenv("HOME", tmpDir)
	// Clear XDG_CONFIG_HOME so configDir() falls back to $HOME/.config/campus-login
	t.Setenv("XDG_CONFIG_HOME", "")

	if err := Setup(); err != nil {
		t.Fatalf("Setup() failed: %v", err)
	}
}

func readConfigFile(t *testing.T) string {
	t.Helper()
	data, err := os.ReadFile(configFile())
	if err != nil {
		t.Fatalf("failed to read config file: %v", err)
	}
	return string(data)
}

func TestAddAccount(t *testing.T) {
	setupTestEnv(t)

	if err := AddAccount("testuser", "20230001", "testpass"); err != nil {
		t.Fatalf("AddAccount() error: %v", err)
	}

	content := readConfigFile(t)
	expected := "testuser=20230001:testpass"
	if !strings.Contains(content, expected) {
		t.Errorf("config file should contain %q, got:\n%s", expected, content)
	}

	// Adding same alias again should replace, not duplicate
	if err := AddAccount("testuser", "20230002", "newpass"); err != nil {
		t.Fatalf("AddAccount() replace error: %v", err)
	}

	content = readConfigFile(t)
	if strings.Contains(content, "20230001") {
		t.Errorf("old account should be removed after re-add, got:\n%s", content)
	}
	if !strings.Contains(content, "testuser=20230002:newpass") {
		t.Errorf("new account line missing, got:\n%s", content)
	}
}

func TestRemoveAccount(t *testing.T) {
	setupTestEnv(t)

	_ = AddAccount("alice", "10001", "pass1")
	_ = AddAccount("bob", "10002", "pass2")

	if err := RemoveAccount("alice"); err != nil {
		t.Fatalf("RemoveAccount() error: %v", err)
	}

	content := readConfigFile(t)
	if strings.Contains(content, "alice=") {
		t.Errorf("alice line should be removed, got:\n%s", content)
	}
	if !strings.Contains(content, "bob=10002:pass2") {
		t.Errorf("bob line should remain, got:\n%s", content)
	}
}

func TestRemoveDefaultAccount(t *testing.T) {
	setupTestEnv(t)

	_ = AddAccount("myacc", "20001", "pw")
	_ = SetDefault("myacc")

	// Verify default is set
	content := readConfigFile(t)
	if !strings.Contains(content, "default_account=myacc") {
		t.Fatalf("default_account line should exist before removal, got:\n%s", content)
	}

	if err := RemoveAccount("myacc"); err != nil {
		t.Fatalf("RemoveAccount() error: %v", err)
	}

	content = readConfigFile(t)
	if strings.Contains(content, "default_account") {
		t.Errorf("default_account line should be removed when its alias is deleted, got:\n%s", content)
	}
	if strings.Contains(content, "myacc=") {
		t.Errorf("myacc alias line should be removed, got:\n%s", content)
	}
}

func TestListAccounts(t *testing.T) {
	setupTestEnv(t)

	_ = AddAccount("alice", "10001", "pass1")
	_ = AddAccount("bob", "10002", "pass2")
	_ = SetDefault("alice")

	accounts, err := ListAccounts()
	if err != nil {
		t.Fatalf("ListAccounts() error: %v", err)
	}

	if len(accounts) != 2 {
		t.Fatalf("expected 2 accounts, got %d", len(accounts))
	}

	// Verify no default_account entry leaked into accounts list
	for _, a := range accounts {
		if a.Alias == "default_account" {
			t.Errorf("ListAccounts should not include the default_account line")
		}
	}

	// Check IsDefault flag
	found := false
	for _, a := range accounts {
		if a.Alias == "alice" {
			found = true
			if !a.IsDefault {
				t.Errorf("alice should be marked as default")
			}
			if a.AccountID != "10001" {
				t.Errorf("alice AccountID = %q, want %q", a.AccountID, "10001")
			}
		}
		if a.Alias == "bob" && a.IsDefault {
			t.Errorf("bob should not be marked as default")
		}
	}
	if !found {
		t.Errorf("alice not found in accounts list")
	}
}

func TestCredentialsWithColon(t *testing.T) {
	setupTestEnv(t)

	// Password contains multiple colons
	colonPassword := "my:complex:password:here"
	_ = AddAccount("colonuser", "30001", colonPassword)

	account, password, err := GetCredentials("colonuser")
	if err != nil {
		t.Fatalf("GetCredentials() error: %v", err)
	}

	if account != "30001" {
		t.Errorf("account = %q, want %q", account, "30001")
	}
	if password != colonPassword {
		t.Errorf("password = %q, want %q", password, colonPassword)
	}
}

func TestSetDefaultNotFound(t *testing.T) {
	setupTestEnv(t)

	err := SetDefault("nonexistent")
	if err == nil {
		t.Fatalf("SetDefault(nonexistent) should return error, got nil")
	}
	if !strings.Contains(err.Error(), "not found") {
		t.Errorf("error message should contain 'not found', got: %v", err)
	}
}

func TestGetDefaultEmpty(t *testing.T) {
	setupTestEnv(t)

	def, err := GetDefault()
	if err != nil {
		t.Fatalf("GetDefault() error: %v", err)
	}
	if def != "" {
		t.Errorf("GetDefault() = %q, want empty string when no default is set", def)
	}
}

func TestXDGConfigHome(t *testing.T) {
	tmpDir := t.TempDir()
	xdgDir := filepath.Join(tmpDir, "custom-xdg")
	t.Setenv("XDG_CONFIG_HOME", xdgDir)
	t.Setenv("HOME", tmpDir)

	if err := Setup(); err != nil {
		t.Fatalf("Setup() with XDG_CONFIG_HOME failed: %v", err)
	}

	expectedDir := filepath.Join(xdgDir, "campus-login")
	expectedFile := filepath.Join(expectedDir, "config")
	if _, err := os.Stat(expectedFile); os.IsNotExist(err) {
		t.Errorf("config file not created at XDG path: %s", expectedFile)
	}
}
