# Learnings - Go Rewrite

## Task 2: Config Package
- Go 1.25 supports `t.Setenv()` for test environment isolation — cleaner than manual save/restore
- `t.TempDir()` auto-cleans up; setting HOME to it isolates config file operations perfectly
- Shell version has a bug: `cut -d':' -f1` loses password parts after first `:`. Fixed with `strings.SplitN(val, ":", 2)`
- Atomic file write pattern: `os.CreateTemp` in same dir + `os.Rename` = safe on same filesystem
- Config file format: `alias=account:password` lines + special `default_account=alias` line
- gopls not available in this environment; use `go vet` + `go test` for verification

## Task 5: Login Package
- JSONP parsing with `strings.Index`/`strings.LastIndex` is simpler and safer than regex for known callback names
- Package-level `var portalBaseURL` pattern (same as network's `connectivityURL`) enables clean test injection via `httptest.Server`
- URL query params built with string concatenation (not `url.Values`) to preserve exact parameter order including duplicate `lang` keys
- `url.QueryEscape(",0,20230001")` produces `%2C0%2C20230001` — comma becomes `%2C`
- `http.ErrUseLastResponse` in `CheckRedirect` prevents following captive portal redirects
- All 7 tests pass: 4 JSONP parse cases, 1 URL encoding, 2 mock login scenarios (success/failure)

## Task 7: CLI Entry Point (main.go)
- Pure `os.Args` + `switch/case` dispatch — no CLI frameworks needed for simple command structure
- `color.Init()` must be called FIRST (before any colored output), then `config.Setup()` for dir/file creation
- `term.ReadPassword(int(os.Stdin.Fd()))` works for secure password input; print prompt to stderr, add newline after entry
- Error messages to stderr (`fmt.Fprintf(os.Stderr, ...)`), success output to stdout — matches Unix convention
- Help text uses raw string literal with `%s` placeholders for color variables — avoids string concatenation mess
- Unknown `-*` flags → exit 1; any other unknown arg → treat as login alias (same as shell-bin logic)
- `CheckConnectivity()` returns `(bool, error)`: error=network unreachable, false=captive portal, true=connected
- 257 lines total; all commands tested: help/-h/--help, list/ls, add, remove/rm, default, status, unknown flags
