package browser

import (
	"fmt"
	"log/slog"
	"time"

	"github.com/go-rod/rod"
	"github.com/go-rod/rod/lib/proto"
	"github.com/morethan987/AutoClicks_Rod/internal/config"
)

// Login navigates to the course selection URL and performs the login flow.
// It uses non-Must (error-returning) Rod API throughout.
func Login(page *rod.Page, cfg *config.Config) error {
	slog.Info("Navigating to course selection URL", "url", cfg.URL)
	if err := page.Navigate(cfg.URL); err != nil {
		return fmt.Errorf("login: navigate: %w", err)
	}

	// Wait for initial page load — WaitLoad waits for the load event,
	// WaitStable waits for DOM to stop changing. For heavy SPAs (Ant Design/React)
	// we chain both, then fall back to direct element waiting with a generous timeout.
	if err := page.WaitLoad(); err != nil {
		slog.Warn("WaitLoad timed out, proceeding to element wait", "error", err)
	}
	if err := page.WaitStable(5 * time.Second); err != nil {
		slog.Warn("WaitStable timed out, proceeding to element wait", "error", err)
	}

	slog.Info("Page load complete, locating login elements")

	// Locate username input — 10s timeout for SPA rendering
	usernameEl, err := page.Timeout(10 * time.Second).Element(cfg.Selectors.Login.UsernameInput)
	if err != nil {
		return fmt.Errorf("login: find username input %q: %w", cfg.Selectors.Login.UsernameInput, err)
	}

	passwordEl, err := page.Timeout(10 * time.Second).Element(cfg.Selectors.Login.PasswordInput)
	if err != nil {
		return fmt.Errorf("login: find password input %q: %w", cfg.Selectors.Login.PasswordInput, err)
	}

	loginBtn, err := page.Timeout(10 * time.Second).Element(cfg.Selectors.Login.LoginButton)
	if err != nil {
		return fmt.Errorf("login: find login button %q: %w", cfg.Selectors.Login.LoginButton, err)
	}

	slog.Info("Login elements located, entering credentials")

	if err := usernameEl.Input(cfg.Username); err != nil {
		return fmt.Errorf("login: input username: %w", err)
	}

	if err := passwordEl.Input(cfg.Password); err != nil {
		return fmt.Errorf("login: input password: %w", err)
	}

	if err := loginBtn.Click(proto.InputMouseButtonLeft, 1); err != nil {
		return fmt.Errorf("login: click login button: %w", err)
	}

	slog.Info("Login credentials submitted, waiting for redirect to course page")
	return nil
}
