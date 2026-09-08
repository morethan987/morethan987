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

func TestParseJSONPNumericResult(t *testing.T) {
	body := `dr1004({"result":1,"msg":"登录成功"})`
	result, msg, err := ParseJSONP(body)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if result != "1" {
		t.Errorf("expected result %q, got %q", "1", result)
	}
	if msg != "登录成功" {
		t.Errorf("expected msg %q, got %q", "登录成功", msg)
	}
}

func TestParseJSONPNumericResultZero(t *testing.T) {
	body := `dr1004({"result":0,"msg":"密码错误"})`
	result, msg, err := ParseJSONP(body)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if result != "0" {
		t.Errorf("expected result %q, got %q", "0", result)
	}
	if msg != "密码错误" {
		t.Errorf("expected msg %q, got %q", "密码错误", msg)
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

func TestParseJSONPJsonpReturnCallback(t *testing.T) {
	body := `jsonpReturn({"result":1,"msg":"Radius注销成功！"})`
	result, msg, err := ParseJSONP(body)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if result != "1" {
		t.Errorf("expected result %q, got %q", "1", result)
	}
	if msg != "Radius注销成功！" {
		t.Errorf("expected msg %q, got %q", "Radius注销成功！", msg)
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

func TestPerformLoginNumericResult(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprint(w, `dr1004({"result":1,"msg":"登录成功"})`)
	}))
	defer server.Close()

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

func TestPerformLogoutSuccess(t *testing.T) {
	// Mock both unbind and logout endpoints on the same server.
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch {
		case strings.Contains(r.URL.Path, "/mac/unbind"):
			// Unbind endpoint — just return 200 OK.
			w.WriteHeader(http.StatusOK)
		case strings.Contains(r.URL.Path, "/logout"):
			fmt.Fprint(w, `jsonpReturn({"result":1,"msg":"Radius注销成功！"})`)
		default:
			w.WriteHeader(http.StatusNotFound)
		}
	}))
	defer server.Close()

	origUnbind := portalUnbindURL
	origLogout := portalLogoutURL
	portalUnbindURL = server.URL + "/eportal/portal/mac/unbind"
	portalLogoutURL = server.URL + "/eportal/portal/logout"
	defer func() {
		portalUnbindURL = origUnbind
		portalLogoutURL = origLogout
	}()

	err := PerformLogout("20230001", "10.0.0.1")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
}

func TestPerformLogoutFailure(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch {
		case strings.Contains(r.URL.Path, "/mac/unbind"):
			w.WriteHeader(http.StatusOK)
		case strings.Contains(r.URL.Path, "/logout"):
			fmt.Fprint(w, `jsonpReturn({"result":0,"msg":"注销失败"})`)
		default:
			w.WriteHeader(http.StatusNotFound)
		}
	}))
	defer server.Close()

	origUnbind := portalUnbindURL
	origLogout := portalLogoutURL
	portalUnbindURL = server.URL + "/eportal/portal/mac/unbind"
	portalLogoutURL = server.URL + "/eportal/portal/logout"
	defer func() {
		portalUnbindURL = origUnbind
		portalLogoutURL = origLogout
	}()

	err := PerformLogout("20230001", "10.0.0.1")
	if err == nil {
		t.Fatal("expected error for failed logout, got nil")
	}
}

func TestPerformLogoutUnbindParams(t *testing.T) {
	var unbindQuery url.Values
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch {
		case strings.Contains(r.URL.Path, "/mac/unbind"):
			unbindQuery = r.URL.Query()
			w.WriteHeader(http.StatusOK)
		case strings.Contains(r.URL.Path, "/logout"):
			fmt.Fprint(w, `jsonpReturn({"result":1,"msg":"ok"})`)
		default:
			w.WriteHeader(http.StatusNotFound)
		}
	}))
	defer server.Close()

	origUnbind := portalUnbindURL
	origLogout := portalLogoutURL
	portalUnbindURL = server.URL + "/eportal/portal/mac/unbind"
	portalLogoutURL = server.URL + "/eportal/portal/logout"
	defer func() {
		portalUnbindURL = origUnbind
		portalLogoutURL = origLogout
	}()

	err := PerformLogout("20230674", "192.168.1.100")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if got := unbindQuery.Get("user_account"); got != "20230674" {
		t.Errorf("expected user_account %q, got %q", "20230674", got)
	}
	if got := unbindQuery.Get("wlan_user_ip"); got != "192.168.1.100" {
		t.Errorf("expected wlan_user_ip %q, got %q", "192.168.1.100", got)
	}
	if got := unbindQuery.Get("wlan_user_mac"); got != "000000000000" {
		t.Errorf("expected wlan_user_mac %q, got %q", "000000000000", got)
	}
}
