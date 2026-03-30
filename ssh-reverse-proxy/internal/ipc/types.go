package ipc

import "encoding/json"

const (
	ActionStart   = "start"
	ActionStop    = "stop"
	ActionStatus  = "status"
	ActionSet     = "set"
	ActionCurrent = "current"
	ActionAdd     = "add"
	ActionRemove  = "remove"
	ActionList    = "list"
)

type Request struct {
	Action string `json:"action"`
	Alias  string `json:"alias,omitempty"`
	Port   int    `json:"port,omitempty"`
}

type Response struct {
	Success bool            `json:"success"`
	Message string          `json:"message"`
	Data    json.RawMessage `json:"data,omitempty"`
}
