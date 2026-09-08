package network

import (
	"fmt"
	"net"
	"net/http"
	"time"
)

// connectivityURL is the URL used to check internet connectivity.
// It can be overridden in tests to point to a local httptest server.
var connectivityURL = "http://connectivitycheck.gstatic.com/generate_204"

var (
	// SourceIface optionally binds outbound sockets directly to this network
	// interface (SO_BINDTODEVICE on Linux), bypassing the routing table.
	SourceIface string

	// SourceIP optionally pins the local source address of outbound
	// connections. It is derived from SourceIface when an interface is selected.
	SourceIP string
)

// NewDialer returns a net.Dialer whose outgoing connections honor
// SourceIface/SourceIP: sockets are bound to the selected device and source
// address so traffic leaves through it. With neither set, OS routing applies.
func NewDialer(timeout time.Duration) *net.Dialer {
	d := &net.Dialer{Timeout: timeout}
	if SourceIP != "" {
		d.LocalAddr = &net.TCPAddr{IP: net.ParseIP(SourceIP)}
	}
	if SourceIface != "" {
		d.Control = bindToDevice(SourceIface)
	}
	return d
}

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
	if SourceIP != "" {
		return SourceIP
	}
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

// InterfaceInfo holds a network interface name and its IPv4 addresses.
type InterfaceInfo struct {
	Name  string
	Addrs []string
}

// ListInterfaces returns every up interface that has at least one IPv4
// address. Loopback and point-to-point (VPN) interfaces are included.
func ListInterfaces() ([]InterfaceInfo, error) {
	ifaces, err := net.Interfaces()
	if err != nil {
		return nil, fmt.Errorf("failed to list interfaces: %w", err)
	}

	var result []InterfaceInfo
	for _, iface := range ifaces {
		if iface.Flags&net.FlagUp == 0 {
			continue
		}

		addrs, err := iface.Addrs()
		if err != nil {
			continue
		}

		var ips []string
		for _, addr := range addrs {
			if ipNet, ok := addr.(*net.IPNet); ok {
				if ip := ipNet.IP.To4(); ip != nil {
					ips = append(ips, ip.String())
				}
			}
		}
		if len(ips) > 0 {
			result = append(result, InterfaceInfo{Name: iface.Name, Addrs: ips})
		}
	}

	return result, nil
}

// GetInterfaceIP returns the first IPv4 address of the named network interface.
func GetInterfaceIP(name string) (string, error) {
	iface, err := net.InterfaceByName(name)
	if err != nil {
		return "", fmt.Errorf("interface %q not found: %w", name, err)
	}

	addrs, err := iface.Addrs()
	if err != nil {
		return "", fmt.Errorf("failed to get addresses of interface %q: %w", name, err)
	}

	for _, addr := range addrs {
		ipNet, ok := addr.(*net.IPNet)
		if !ok {
			continue
		}
		if ip := ipNet.IP.To4(); ip != nil {
			return ip.String(), nil
		}
	}

	return "", fmt.Errorf("interface %q has no IPv4 address", name)
}

// InterfaceExists reports whether a network interface with the given name
// exists (regardless of its up/down state or addresses).
func InterfaceExists(name string) bool {
	_, err := net.InterfaceByName(name)
	return err == nil
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
	transport := &http.Transport{
		DialContext: NewDialer(5 * time.Second).DialContext,
	}

	client := &http.Client{
		Timeout:   5 * time.Second,
		Transport: transport,
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
