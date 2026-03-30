package client

import (
	"encoding/json"
	"net"
	"path/filepath"
	"testing"

	"github.com/morethan987/ssh-reverse-proxy/internal/ipc"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestSendSuccess(t *testing.T) {
	sockPath := filepath.Join(t.TempDir(), "test.sock")
	ln, err := net.Listen("unix", sockPath)
	require.NoError(t, err)
	defer ln.Close()

	serverDone := make(chan struct{})
	go func() {
		defer close(serverDone)
		conn, err := ln.Accept()
		require.NoError(t, err)
		defer conn.Close()

		var req ipc.Request
		require.NoError(t, json.NewDecoder(conn).Decode(&req))
		assert.Equal(t, ipc.ActionStart, req.Action)
		assert.Equal(t, "alias", req.Alias)
		assert.Equal(t, 8080, req.Port)

		require.NoError(t, json.NewEncoder(conn).Encode(ipc.Response{
			Success: true,
			Message: "ok",
			Data:    json.RawMessage(`{"value":"done"}`),
		}))
	}()

	c := New(sockPath)
	resp, err := c.Send(&ipc.Request{Action: ipc.ActionStart, Alias: "alias", Port: 8080})
	require.NoError(t, err)
	require.NotNil(t, resp)
	assert.True(t, resp.Success)
	assert.Equal(t, "ok", resp.Message)
	assert.JSONEq(t, `{"value":"done"}`, string(resp.Data))
	<-serverDone
}

func TestSendDaemonNotRunning(t *testing.T) {
	c := New(filepath.Join(t.TempDir(), "missing.sock"))
	resp, err := c.Send(&ipc.Request{Action: ipc.ActionStatus})
	require.Error(t, err)
	assert.Nil(t, resp)
	assert.Contains(t, err.Error(), "daemon not running")
}

func TestSendServerClosesConnectionMidRead(t *testing.T) {
	sockPath := filepath.Join(t.TempDir(), "close-mid-read.sock")
	ln, err := net.Listen("unix", sockPath)
	require.NoError(t, err)
	defer ln.Close()

	serverDone := make(chan struct{})
	go func() {
		defer close(serverDone)
		conn, err := ln.Accept()
		require.NoError(t, err)
		defer conn.Close()

		var req ipc.Request
		require.NoError(t, json.NewDecoder(conn).Decode(&req))
		assert.Equal(t, ipc.ActionStatus, req.Action)

		_, _ = conn.Write([]byte(`{"success":true,"message":"`))
		_ = conn.Close()
	}()

	c := New(sockPath)
	resp, err := c.Send(&ipc.Request{Action: ipc.ActionStatus})
	require.Error(t, err)
	assert.Nil(t, resp)
	assert.Contains(t, err.Error(), "EOF")
	<-serverDone
}
