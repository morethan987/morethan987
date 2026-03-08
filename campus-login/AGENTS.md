# AGENTS.md — campus-login

Campus network CLI login tool for Chongqing University (`login.cqu.edu.cn`).
Go 1.25+, pure stdlib + `golang.org/x/term`. Single static binary, zero config.

## Build / Test / Lint

```bash
# Build
go build -o output/campus-login ./cmd/campus-login

# Static build (cross-compile friendly)
CGO_ENABLED=0 go build -o output/campus-login ./cmd/campus-login

# Vet (lint — the ONLY linter used)
go vet ./...

# Test all packages
go test ./...

# Test a single package
go test ./internal/config/...
go test ./internal/login/...
go test ./internal/network/...

# Test a single test function
go test ./internal/config/ -run TestCredentialsWithColon -v
go test ./internal/login/ -run TestParseJSONP -v

# Test with verbose output
go test -v ./...

# Test with race detection
go test -race ./...
```

No Makefile, no golangci-lint, no staticcheck, no goreleaser configured.
The only CI-equivalent check is: `go vet ./... && go test ./...`

## Project Structure

```
cmd/campus-login/main.go     # CLI entry point — command dispatch via os.Args + switch/case
internal/
  color/color.go             # TTY detection + ANSI color variables (no tests)
  config/config.go           # Config file CRUD (atomic write-to-temp-then-rename)
  config/config_test.go      # Tests: add, remove, list, default, colon passwords, XDG
  daemon/daemon.go           # Daemon loop: periodic connectivity check + auto-login + backoff
  daemon/daemon_test.go      # Tests: backoff calculation, cap at maxBackoff
  login/login.go             # HTTP login + JSONP parsing (dr1004 callback)
  login/login_test.go        # Tests: JSONP parse, URL encoding, mock login success/fail
  network/network.go         # Local IP detection + connectivity check
  network/network_test.go    # Tests: mock 204/302/error, GetLocalIP
```

## Dependencies

- `golang.org/x/term` — secure password input (the ONLY external dep)
- `golang.org/x/sys` — indirect (via x/term)
- Everything else is Go stdlib

**Guardrail**: No third-party CLI frameworks (cobra, urfave), HTTP clients (resty),
color libraries (fatih/color), or logging frameworks. Keep it stdlib-only.

## Code Style

### Imports

Standard Go `goimports` ordering with blank-line separation:

```go
import (
    "fmt"           // stdlib
    "os"

    "golang.org/x/term"  // external (x/ packages)

    "github.com/morethan987/campus-login/internal/color"   // internal
    "github.com/morethan987/campus-login/internal/config"
)
```

### Formatting

- `gofmt` standard — tabs, no custom formatter config
- Line length: not enforced, but keep reasonable (~100 chars)
- No trailing commas in argument lists (Go doesn't allow them inline)

### Naming Conventions

- **Packages**: short, lowercase, single-word (`config`, `login`, `network`, `color`)
- **Exported funcs**: PascalCase verbs — `Setup()`, `AddAccount()`, `GetLocalIP()`, `ParseJSONP()`
- **Unexported funcs**: camelCase — `readLines()`, `writeLines()`, `configDir()`, `configFile()`
- **Vars**: camelCase locals (`encodedAccount`, `localAddr`), PascalCase exports (`Red`, `Green`, `NC`)
- **Constants**: PascalCase (`PortalHost`)
- **Struct fields**: PascalCase (`Alias`, `AccountID`, `IsDefault`)

### Error Handling

- Always wrap errors with `fmt.Errorf("context: %w", err)` — use `%w` for wrapping
- Never ignore errors silently (no `_ = err` patterns)
- In `main.go`: print error to stderr with color, then `os.Exit(1)`
- In library code: return errors up the stack, never `os.Exit()`
- Error messages in library code are English; user-facing messages in `main.go` are Chinese

```go
// Library code (internal/*)
return fmt.Errorf("failed to create config directory: %w", err)

// CLI code (main.go)
fmt.Fprintf(os.Stderr, "%s错误: 初始化配置失败: %s%s\n", color.Red, err, color.NC)
os.Exit(1)
```

### Comments

- Exported functions: single-line GoDoc starting with function name
- Internal helpers: brief comment explaining purpose
- No excessive comments — code should be self-explanatory
- Chinese comments are acceptable for Chinese-specific context (color variable descriptions)

```go
// Setup creates the configuration directory and file if they don't exist.
func Setup() error { ... }

// configDir returns the configuration directory path.
func configDir() string { ... }
```

### Testing Patterns

- Package-level tests (same package, not `_test` suffix): `package config` not `package config_test`
- Test helpers use `t.Helper()` — always
- Isolation via `t.TempDir()` + `t.Setenv("HOME", tmpDir)` — never touch real user config
- Mock HTTP servers via `httptest.NewServer` — override package-level URL vars for testing
- Test naming: `TestFunctionName` or `TestFunctionNameVariant` (e.g. `TestParseJSONPEmpty`)
- No testify, no gomock — plain `testing` stdlib only

```go
// Pattern for mocking HTTP endpoints:
original := portalBaseURL
portalBaseURL = server.URL
defer func() { portalBaseURL = original }()
```

### File I/O

- Config writes use atomic write-to-temp-then-rename pattern (not direct file writes)
- Config directory: `$XDG_CONFIG_HOME/campus-login/` or `$HOME/.config/campus-login/`
- Config format: plain text, `alias=account:password` per line, `default_account=alias` special line
- Password parsing: `strings.SplitN(creds, ":", 2)` — only split on first colon

### HTTP

- `http.Client` with explicit timeout (10s for login, 5s for connectivity)
- TLS: `InsecureSkipVerify: true` (campus portal uses self-signed certs)
- Disable auto-redirect: `CheckRedirect` returns `http.ErrUseLastResponse`
- JSONP parsing: `strings.Index` based, not regex

## Commit Message Style

```
<type>: <short description in English>
```

Types used: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`

Examples from history:
- `feat: add 'set' and 'def' as aliases for default subcommand`
- `fix: fix login error`
- `docs: add readme file`
- `chore: init Go project scaffolding`

## Architecture Decisions

- **No CLI framework**: Pure `os.Args` + `switch/case` — this is a simple tool, not a framework
- **No `pkg/` directory**: Everything internal via `internal/`
- **No interfaces**: Concrete types only — avoid over-abstraction for a CLI tool
- **No retry logic**: Single attempt for login and connectivity check
- **No logging framework**: `fmt.Fprintf(os.Stderr, ...)` for errors, `fmt.Printf(...)` for output
- **Chinese user-facing text**: All CLI output (help, errors, success messages) is in Chinese
- **English internal text**: Error wrapping messages, code comments, GoDoc are in English
