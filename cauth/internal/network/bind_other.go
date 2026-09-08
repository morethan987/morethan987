//go:build !linux

package network

import "syscall"

// bindToDevice is a no-op on platforms without SO_BINDTODEVICE; interface
// selection then relies on source-address binding only.
func bindToDevice(name string) func(network, address string, c syscall.RawConn) error {
	return nil
}
