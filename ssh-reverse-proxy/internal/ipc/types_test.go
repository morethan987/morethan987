package ipc

import (
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestRequestJSONRoundTrip(t *testing.T) {
	original := Request{Action: ActionStart, Alias: "proxy", Port: 2222}

	data, err := json.Marshal(original)
	assert.NoError(t, err)

	var decoded Request
	err = json.Unmarshal(data, &decoded)
	assert.NoError(t, err)
	assert.Equal(t, original, decoded)
}

func TestResponseJSONRoundTripWithData(t *testing.T) {
	original := Response{Success: true, Message: "ok", Data: json.RawMessage(`{"pid":1234}`)}

	data, err := json.Marshal(original)
	assert.NoError(t, err)

	var decoded Response
	err = json.Unmarshal(data, &decoded)
	assert.NoError(t, err)
	assert.Equal(t, original.Success, decoded.Success)
	assert.Equal(t, original.Message, decoded.Message)
	assert.JSONEq(t, string(original.Data), string(decoded.Data))
}

func TestUnknownActionPreserved(t *testing.T) {
	data := []byte(`{"action":"custom","alias":"proxy","port":8080}`)

	var decoded Request
	err := json.Unmarshal(data, &decoded)
	assert.NoError(t, err)
	assert.Equal(t, "custom", decoded.Action)
	assert.Equal(t, "proxy", decoded.Alias)
	assert.Equal(t, 8080, decoded.Port)
}

func TestResponseJSONWithNilData(t *testing.T) {
	original := Response{Success: true, Message: "ok", Data: nil}

	data, err := json.Marshal(original)
	assert.NoError(t, err)
	assert.JSONEq(t, `{"success":true,"message":"ok"}`, string(data))

	var decoded Response
	err = json.Unmarshal(data, &decoded)
	assert.NoError(t, err)
	assert.Equal(t, original.Success, decoded.Success)
	assert.Equal(t, original.Message, decoded.Message)
	assert.Nil(t, decoded.Data)
}
