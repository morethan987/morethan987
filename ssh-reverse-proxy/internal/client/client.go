package client

import (
	"encoding/json"
	"fmt"
	"net"
	"time"

	"github.com/morethan987/ssh-reverse-proxy/internal/ipc"
)

type Client struct {
	socketPath  string
	dialTimeout time.Duration
	readTimeout time.Duration
}

func New(socketPath string) *Client {
	return &Client{socketPath: socketPath, dialTimeout: 5 * time.Second, readTimeout: 10 * time.Second}
}

func (c *Client) Send(req *ipc.Request) (*ipc.Response, error) {
	conn, err := net.DialTimeout("unix", c.socketPath, c.dialTimeout)
	if err != nil {
		return nil, fmt.Errorf("daemon not running: %w", err)
	}
	defer conn.Close()

	if err := conn.SetReadDeadline(time.Now().Add(c.readTimeout)); err != nil {
		return nil, err
	}

	if err := json.NewEncoder(conn).Encode(req); err != nil {
		return nil, err
	}

	var resp ipc.Response
	if err := json.NewDecoder(conn).Decode(&resp); err != nil {
		return nil, err
	}

	return &resp, nil
}
