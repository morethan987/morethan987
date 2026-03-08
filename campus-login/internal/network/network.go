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
// GetLocalIPForHost returns the local IP address that should be used when
// communicating with the given host:port.
//
// It first tries to find a private IPv4 address on a real (non-virtual)
// network interface by enumerating interfaces and skipping loopback,
// point-to-point (TUN/VPN), and down interfaces. This avoids returning
// a TUN address (e.g. 198.18.0.1 from mihomo/Clash) when a proxy is active.
//
// If interface enumeration fails to find a suitable IP, it falls back to a
// UDP dial to the given host:port to determine the outbound IP.
// Returns "0.0.0.0" on failure.
func GetLocalIPForHost(hostPort string) string {
	if ip := getPrivateIPFromInterfaces(); ip != "" {
		return ip
	}

	// Fallback: UDP dial to determine outbound IP.
	conn, err := net.Dial("udp", hostPort)
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

// getPrivateIPFromInterfaces enumerates network interfaces and returns the
// first private IPv4 address found on a real (non-virtual) interface.
// It skips loopback, point-to-point (TUN/VPN), and down interfaces.
// Returns an empty string if no suitable address is found.
func getPrivateIPFromInterfaces() string {
	ifaces, err := net.Interfaces()
	if err != nil {
		return ""
	}

	for _, iface := range ifaces {
		// Skip down, loopback, and point-to-point (TUN/VPN) interfaces.
		if iface.Flags&net.FlagUp == 0 {
			continue
		}
		if iface.Flags&net.FlagLoopback != 0 {
			continue
		}
		if iface.Flags&net.FlagPointToPoint != 0 {
			continue
		}

		addrs, err := iface.Addrs()
		if err != nil {
			continue
		}

		for _, addr := range addrs {
			ipNet, ok := addr.(*net.IPNet)
			if !ok {
				continue
			}

			ip := ipNet.IP.To4()
			if ip == nil {
				continue // skip IPv6
			}

			// Only return private (RFC 1918) addresses:
			// 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
			if ip[0] == 10 ||
				(ip[0] == 172 && ip[1] >= 16 && ip[1] <= 31) ||
				(ip[0] == 192 && ip[1] == 168) {
				return ip.String()
			}
		}
	}

	return ""
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
