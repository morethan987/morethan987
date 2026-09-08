//go:build linux

package network

import "syscall"

// bindToDevice returns a Dialer.Control function that binds the socket
// directly to the given network interface (SO_BINDTODEVICE), so traffic
// leaves through it regardless of the routing table.
func bindToDevice(name string) func(network, address string, c syscall.RawConn) error {
	return func(network, address string, c syscall.RawConn) error {
		var opErr error
		err := c.Control(func(fd uintptr) {
			opErr = syscall.SetsockoptString(int(fd), syscall.SOL_SOCKET, syscall.SO_BINDTODEVICE, name)
		})
		if err != nil {
			return err
		}
		return opErr
	}
}
