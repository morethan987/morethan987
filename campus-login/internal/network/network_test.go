package network

import (
	"net"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestCheckConnectivity204(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusNoContent)
	}))
	defer server.Close()

	// Override the package-level URL to point to our test server
	original := connectivityURL
	connectivityURL = server.URL
	defer func() { connectivityURL = original }()

	connected, err := CheckConnectivity()
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if !connected {
		t.Fatal("expected connected=true for 204 response, got false")
	}
}

func TestCheckConnectivity302(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.Redirect(w, r, "http://portal.example.com/login", http.StatusFound)
	}))
	defer server.Close()

	original := connectivityURL
	connectivityURL = server.URL
	defer func() { connectivityURL = original }()

	connected, err := CheckConnectivity()
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if connected {
		t.Fatal("expected connected=false for 302 response, got true")
	}
}

func TestCheckConnectivityError(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))
	// Close immediately to cause a connection error
	server.Close()

	original := connectivityURL
	connectivityURL = server.URL
	defer func() { connectivityURL = original }()

	connected, err := CheckConnectivity()
	if err == nil {
		t.Fatal("expected error for closed server, got nil")
	}
	if connected {
		t.Fatal("expected connected=false on error, got true")
	}
}

func TestGetLocalIP(t *testing.T) {
	// Check if we have network connectivity by attempting a UDP dial
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err != nil {
		t.Skip("skipping: no network available")
	}
	conn.Close()

	ip := GetLocalIP()
	if ip == "" {
		t.Fatal("expected non-empty IP string, got empty")
	}
	if ip == "0.0.0.0" {
		t.Fatal("expected valid IP, got fallback 0.0.0.0")
	}

	// Validate it parses as a valid IP
	parsed := net.ParseIP(ip)
	if parsed == nil {
		t.Fatalf("returned string %q is not a valid IP address", ip)
	}
}
