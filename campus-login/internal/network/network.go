package network

import (
	"net"
	"net/http"
	"time"
)

// connectivityURL is the URL used to check internet connectivity.
// It can be overridden in tests to point to a local httptest server.
var connectivityURL = "http://connectivitycheck.gstatic.com/generate_204"

// GetLocalIP returns the preferred outbound IP address of the machine.
// It uses a UDP dial to 8.8.8.8:80 to determine the local IP without
// actually sending any packets. Returns "0.0.0.0" on failure.
func GetLocalIP() string {
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err != nil {
		return "0.0.0.0"
	}
	defer conn.Close()

	localAddr, ok := conn.LocalAddr().(*net.UDPAddr)
	if !ok {
		return "0.0.0.0"
	}

	return localAddr.IP.String()
}

// CheckConnectivity checks whether the machine has internet access by
// making an HTTP GET request to a known connectivity check endpoint.
//
// Returns:
//   - (true, nil)  if the endpoint returns HTTP 204 (connected to internet)
//   - (false, nil)  if the endpoint returns any other status (e.g. 302 redirect
//     from a captive portal)
//   - (false, err) if the request fails (network unreachable, timeout, etc.)
func CheckConnectivity() (bool, error) {
	client := &http.Client{
		Timeout: 5 * time.Second,
		CheckRedirect: func(req *http.Request, via []*http.Request) error {
			return http.ErrUseLastResponse
		},
	}

	resp, err := client.Get(connectivityURL)
	if err != nil {
		return false, err
	}
	defer resp.Body.Close()

	return resp.StatusCode == http.StatusNoContent, nil
}
