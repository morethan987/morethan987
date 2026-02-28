package login

import (
	"fmt"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"testing"
)

func TestParseJSONP(t *testing.T) {
	body := `dr1004({"result":"1","msg":"ok"})`
	result, msg, err := ParseJSONP(body)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if result != "1" {
		t.Errorf("expected result %q, got %q", "1", result)
	}
	if msg != "ok" {
		t.Errorf("expected msg %q, got %q", "ok", msg)
	}
}

func TestParseJSONPEmpty(t *testing.T) {
	_, _, err := ParseJSONP("")
	if err == nil {
		t.Fatal("expected error for empty input, got nil")
	}
}

func TestParseJSONPMalformed(t *testing.T) {
	_, _, err := ParseJSONP("dr1004(invalid)")
	if err == nil {
		t.Fatal("expected error for malformed JSON, got nil")
	}
}

func TestParseJSONPNoCallback(t *testing.T) {
	_, _, err := ParseJSONP(`{"result":"1"}`)
	if err == nil {
		t.Fatal("expected error for missing callback wrapper, got nil")
	}
}

func TestURLEncoding(t *testing.T) {
	encoded := url.QueryEscape(",0,20230001")
	if !strings.Contains(encoded, "%2C0%2C20230001") {
		t.Errorf("expected encoded string to contain %%2C0%%2C20230001, got %q", encoded)
	}
}

func TestPerformLoginSuccess(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprint(w, `dr1004({"result":"1","msg":"登录成功"})`)
	}))
	defer server.Close()

	// Override the portal base URL to point to the test server.
	original := portalBaseURL
	portalBaseURL = server.URL
	defer func() { portalBaseURL = original }()

	success, msg, err := PerformLogin("20230001", "testpass", "10.0.0.1")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if !success {
		t.Error("expected success=true, got false")
	}
	if msg != "登录成功" {
		t.Errorf("expected msg %q, got %q", "登录成功", msg)
	}
}

func TestPerformLoginFailure(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprint(w, `dr1004({"result":"0","msg":"密码错误"})`)
	}))
	defer server.Close()

	// Override the portal base URL to point to the test server.
	original := portalBaseURL
	portalBaseURL = server.URL
	defer func() { portalBaseURL = original }()

	success, msg, err := PerformLogin("20230001", "wrongpass", "10.0.0.1")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if success {
		t.Error("expected success=false, got true")
	}
	if msg != "密码错误" {
		t.Errorf("expected msg %q, got %q", "密码错误", msg)
	}
}
