package browser

import (
	"fmt"
	"log/slog"

	"github.com/go-rod/rod"
	"github.com/go-rod/rod/lib/launcher"
	"github.com/go-rod/rod/lib/proto"
	"github.com/morethan987/AutoClicks_Rod/internal/config"
)

// LaunchBrowser starts a Chrome/Chromium instance with the given config.
// The caller is responsible for calling browser.Close() and launcher.Cleanup().
func LaunchBrowser(cfg *config.Config) (*rod.Browser, *launcher.Launcher, error) {
	l := launcher.New().
		Headless(cfg.Headless).
		Set("disable-extensions", "").
		Set("disable-dev-shm-usage", "").
		Set("disable-background-networking", "").
		Set("disable-sync", "").
		Set("disable-translate", "").
		Set("disable-notifications", "").
		Set("blink-settings", "imagesEnabled=false").
		Set("disable-features", "NetworkPrediction").
		Set("dns-prefetch-disable", "")

	controlURL, err := l.Launch()
	if err != nil {
		return nil, nil, fmt.Errorf("browser: launch: %w", err)
	}

	browser := rod.New().ControlURL(controlURL)
	if err := browser.Connect(); err != nil {
		l.Cleanup()
		return nil, nil, fmt.Errorf("browser: connect: %w", err)
	}

	slog.Info("Browser launched", "headless", cfg.Headless)
	return browser, l, nil
}

// SetupImageBlocking installs a hijack router on page that drops all image requests.
// The router goroutine is started automatically.
func SetupImageBlocking(page *rod.Page) error {
	router := page.HijackRequests()
	router.MustAdd("*", func(ctx *rod.Hijack) {
		if ctx.Request.Type() == proto.NetworkResourceTypeImage {
			ctx.Response.Fail(proto.NetworkErrorReasonBlockedByClient)
			return
		}
		ctx.ContinueRequest(&proto.FetchContinueRequest{})
	})
	go router.Run()
	return nil
}

// NewPage creates a new browser page with image blocking enabled.
func NewPage(browser *rod.Browser) (*rod.Page, error) {
	page, err := browser.Page(proto.TargetCreateTarget{URL: "about:blank"})
	if err != nil {
		return nil, fmt.Errorf("browser: new page: %w", err)
	}
	if err := SetupImageBlocking(page); err != nil {
		return nil, fmt.Errorf("browser: setup image blocking: %w", err)
	}
	return page, nil
}
