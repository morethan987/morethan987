package daemon

import (
	"testing"
	"time"
)

func TestNextWaitNoFailures(t *testing.T) {
	base := 60 * time.Second
	got := nextWait(base, 0)
	if got != base {
		t.Errorf("nextWait(60s, 0) = %v, want %v", got, base)
	}
}

func TestNextWaitExponentialBackoff(t *testing.T) {
	base := 60 * time.Second
	tests := []struct {
		failures int
		want     time.Duration
	}{
		{1, 60 * time.Second},  // base * 2^0
		{2, 120 * time.Second}, // base * 2^1
		{3, 240 * time.Second}, // base * 2^2
		{4, 480 * time.Second}, // base * 2^3
	}

	for _, tt := range tests {
		got := nextWait(base, tt.failures)
		if got != tt.want {
			t.Errorf("nextWait(60s, %d) = %v, want %v", tt.failures, got, tt.want)
		}
	}
}

func TestNextWaitCapsAtMax(t *testing.T) {
	base := 60 * time.Second
	// With enough failures, should cap at maxBackoff (30min).
	got := nextWait(base, 100)
	if got != maxBackoff {
		t.Errorf("nextWait(60s, 100) = %v, want %v (maxBackoff)", got, maxBackoff)
	}
}

func TestNextWaitZeroBase(t *testing.T) {
	// When base is 0 (used by check() for backoff display), should use 30s fallback.
	got := nextWait(0, 1)
	if got != 30*time.Second {
		t.Errorf("nextWait(0, 1) = %v, want 30s", got)
	}

	got = nextWait(0, 2)
	if got != 60*time.Second {
		t.Errorf("nextWait(0, 2) = %v, want 60s", got)
	}
}
