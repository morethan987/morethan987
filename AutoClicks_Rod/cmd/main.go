package main

import (
	"context"
	"flag"
	"log/slog"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/morethan987/AutoClicks_Rod/internal/browser"
	"github.com/morethan987/AutoClicks_Rod/internal/config"
)

func main() {
	// ── CLI flags ─────────────────────────────────────────────────────────────
	configPath := flag.String("config", "config.yaml", "path to YAML config file")
	headlessOverride := flag.Bool("headless", false, "override headless mode (sets headless=true)")
	flag.Parse()

	// ── Load configuration ────────────────────────────────────────────────────
	cfg, err := config.Load(*configPath)
	if err != nil {
		slog.Error("Failed to load config", "path", *configPath, "error", err)
		os.Exit(1)
	}
	if *headlessOverride {
		cfg.Headless = true
	}
	slog.Info("Config loaded",
		"url", cfg.URL,
		"courses", len(cfg.Courses),
		"interval", cfg.Interval,
		"headless", cfg.Headless,
	)

	// ── Signal-aware context (SIGINT / SIGTERM → graceful shutdown) ───────────
	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	// ── Top-level retry loop (infrastructure errors only) ────────────────────
	for attempt := 0; attempt < cfg.MaxRetry; attempt++ {
		if ctx.Err() != nil {
			slog.Info("Shutdown signal received, exiting")
			break
		}

		slog.Info("Starting run", "attempt", attempt+1)
		err := run(ctx, cfg)
		if err == nil {
			slog.Info("Run completed successfully")
			break
		}

		if ctx.Err() != nil {
			slog.Info("Shutdown signal received during run, exiting")
			break
		}

		// Exponential backoff capped at 30 s
		backoff := time.Duration(1<<attempt) * time.Second
		if backoff > 30*time.Second {
			backoff = 30 * time.Second
		}
		slog.Error("Run failed, retrying",
			"attempt", attempt+1,
			"max", cfg.MaxRetry,
			"backoff", backoff,
			"error", err,
		)
		// time.Sleep allowed here: retry backoff, NOT page state waiting
		select {
		case <-time.After(backoff):
		case <-ctx.Done():
			slog.Info("Shutdown signal received during backoff, exiting")
			return
		}
	}
}

// run executes one full session: launch browser → login → poll loop.
// Infrastructure errors are returned to trigger the top-level retry.
// Business-logic outcomes (course full, already selected) are logged and continue.
func run(ctx context.Context, cfg *config.Config) error {
	// Launch browser
	b, launcher, err := browser.LaunchBrowser(cfg)
	if err != nil {
		return err
	}
	defer func() {
		if err := b.Close(); err != nil {
			slog.Warn("Error closing browser", "error", err)
		}
		launcher.Cleanup()
		slog.Info("Browser closed and cleaned up")
	}()

	// Create page with image blocking
	page, err := browser.NewPage(b)
	if err != nil {
		return err
	}

	// Login
	if err := browser.Login(page, cfg); err != nil {
		return err
	}

	// Polling loop
	for {
		// Respect shutdown signal
		if ctx.Err() != nil {
			slog.Info("Shutdown signal received, stopping poll loop")
			return nil
		}

		// Wait for course list page to render
		if err := browser.WaitForCoursePage(page, cfg); err != nil {
			return err
		}

		// Find target courses on the current page
		courses, err := browser.FindTargetCourses(page, cfg)
		if err != nil {
			return err
		}

		// Attempt enrollment for each matched course link
		for _, courseLink := range courses {
			if ctx.Err() != nil {
				return nil
			}
			if err := browser.SelectCourse(page, courseLink, cfg); err != nil {
				return err
			}
		}

		// Wait for next poll interval (allowed: polling interval, not page state)
		slog.Info("Poll round complete, waiting before next refresh", "interval_s", cfg.Interval)
		select {
		case <-time.After(time.Duration(cfg.Interval) * time.Second):
		case <-ctx.Done():
			slog.Info("Shutdown signal received during poll sleep, exiting")
			return nil
		}

		// Reload page for next poll round
		if err := page.Reload(); err != nil {
			return err
		}
	}
}
