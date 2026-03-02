package notification

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/http"
	"regexp"
)

// sctp pattern: extract digits between "sctp" and "t"
var sctpPattern = regexp.MustCompile(`sctp(\d+)t`)

// Send pushes a notification via ServerChan (Server酱).
// It supports both standard keys (sctapi.ftqq.com) and sctp-prefixed keys (push.ft07.com).
func Send(serverKey, title, description string) error {
	url, err := buildURL(serverKey)
	if err != nil {
		return fmt.Errorf("notification: build URL: %w", err)
	}

	payload := map[string]string{
		"title": title,
		"desp":  description,
	}
	body, err := json.Marshal(payload)
	if err != nil {
		return fmt.Errorf("notification: marshal payload: %w", err)
	}

	resp, err := http.Post(url, "application/json;charset=utf-8", bytes.NewReader(body)) //nolint:noctx
	if err != nil {
		return fmt.Errorf("notification: HTTP POST: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return fmt.Errorf("notification: unexpected HTTP status %d", resp.StatusCode)
	}

	slog.Info("ServerChan notification sent", "title", title, "status", resp.StatusCode)
	return nil
}

func buildURL(serverKey string) (string, error) {
	if len(serverKey) >= 4 && serverKey[:4] == "sctp" {
		match := sctpPattern.FindStringSubmatch(serverKey)
		if match == nil {
			return "", fmt.Errorf("invalid sctp key format: %q", serverKey)
		}
		num := match[1]
		return fmt.Sprintf("https://%s.push.ft07.com/send/%s.send", num, serverKey), nil
	}
	return fmt.Sprintf("https://sctapi.ftqq.com/%s.send", serverKey), nil
}
