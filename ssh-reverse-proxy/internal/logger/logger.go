package logger

import (
	"log"
	"os"
	"path/filepath"
)

type Logger struct {
	infoLogger  *log.Logger
	errorLogger *log.Logger
	debugLogger *log.Logger
	file        *os.File
}

func New(logPath string) (*Logger, error) {
	if err := os.MkdirAll(filepath.Dir(logPath), 0o755); err != nil {
		return nil, err
	}

	file, err := os.OpenFile(logPath, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0o644)
	if err != nil {
		return nil, err
	}

	flags := log.Ldate | log.Ltime
	return &Logger{
		infoLogger:  log.New(file, "[INFO] ", flags),
		errorLogger: log.New(file, "[ERROR] ", flags),
		debugLogger: log.New(file, "[DEBUG] ", flags),
		file:        file,
	}, nil
}

func (l *Logger) Infof(format string, args ...any) {
	l.infoLogger.Printf(format, args...)
}

func (l *Logger) Errorf(format string, args ...any) {
	l.errorLogger.Printf(format, args...)
}

func (l *Logger) Debugf(format string, args ...any) {
	l.debugLogger.Printf(format, args...)
}

func (l *Logger) Close() error {
	return l.file.Close()
}
