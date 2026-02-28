package login

import (
	"crypto/tls"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"
)

// PortalHost is the hostname and port of the campus login portal.
const PortalHost = "login.cqu.edu.cn:801"

// portalBaseURL is the base URL for the login endpoint.
// It can be overridden in tests to point to a local httptest server.
var portalBaseURL = "http://login.cqu.edu.cn:801/eportal/portal/login"

// jsonpResponse represents the JSON payload inside a JSONP callback.
// Result is json.RawMessage to handle both string ("1") and number (1) from the server.
type jsonpResponse struct {
	Result json.RawMessage `json:"result"`
	Msg    string          `json:"msg"`
}

// ParseJSONP extracts the result and msg fields from a JSONP response
// with a "dr1004" callback wrapper. It uses strings.Index (not regex).
//
// Expected format: dr1004({"result":"...","msg":"..."}) or dr1004({"result":1,"msg":"..."})
func ParseJSONP(body string) (result, msg string, err error) {
	prefix := "dr1004("
	start := strings.Index(body, prefix)
	if start == -1 {
		return "", "", fmt.Errorf("JSONP callback 'dr1004(' not found in response")
	}

	end := strings.LastIndex(body, ")")
	if end == -1 || end <= start+len(prefix) {
		return "", "", fmt.Errorf("closing ')' not found in JSONP response")
	}

	jsonStr := body[start+len(prefix) : end]

	var resp jsonpResponse
	if err := json.Unmarshal([]byte(jsonStr), &resp); err != nil {
		return "", "", fmt.Errorf("failed to parse JSON from JSONP: %w", err)
	}

	// Normalize result: strip surrounding quotes if it's a JSON string,
	// otherwise use the raw value (e.g. number).
	var resultStr string
	if err := json.Unmarshal(resp.Result, &resultStr); err != nil {
		// Not a JSON string — use the raw text (e.g. "1" from number literal).
		resultStr = strings.TrimSpace(string(resp.Result))
	}

	return resultStr, resp.Msg, nil
}

// PerformLogin sends a login request to the campus portal and returns the result.
//
// The account is prefixed with ",0," before URL encoding, matching the shell
// version's behavior. Returns success=true when the portal responds with result="1".
func PerformLogin(account, password, localIP string) (success bool, msg string, err error) {
	encodedAccount := url.QueryEscape(",0," + account)
	encodedPassword := url.QueryEscape(password)

	loginURL := portalBaseURL + "?" +
		"callback=dr1004" +
		"&login_method=1" +
		"&user_account=" + encodedAccount +
		"&user_password=" + encodedPassword +
		"&wlan_user_ip=" + localIP +
		"&wlan_user_ipv6=" +
		"&wlan_user_mac=000000000000" +
		"&wlan_ac_ip=" +
		"&wlan_ac_name=" +
		"&term_ua=Mozilla%2F5.0%20(X11%3B%20Linux%20x86_64)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Chrome%2F145.0.0.0%20Safari%2F537.36" +
		"&term_type=1" +
		"&jsVersion=4.2.2" +
		"&terminal_type=1" +
		"&lang=zh-cn" +
		"&v=1089" +
		"&lang=zh"

	req, err := http.NewRequest("GET", loginURL, nil)
	if err != nil {
		return false, "", fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Accept", "*/*")
	req.Header.Set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
	req.Header.Set("Connection", "keep-alive")
	req.Header.Set("Referer", "http://login.cqu.edu.cn/")
	req.Header.Set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")

	client := &http.Client{
		Timeout: 10 * time.Second,
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
		CheckRedirect: func(req *http.Request, via []*http.Request) error {
			return http.ErrUseLastResponse
		},
	}

	resp, err := client.Do(req)
	if err != nil {
		return false, "", fmt.Errorf("login request failed: %w", err)
	}
	defer resp.Body.Close()

	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		return false, "", fmt.Errorf("failed to read response body: %w", err)
	}

	result, respMsg, err := ParseJSONP(string(bodyBytes))
	if err != nil {
		return false, "", fmt.Errorf("failed to parse login response: %w", err)
	}

	return result == "1", respMsg, nil
}
