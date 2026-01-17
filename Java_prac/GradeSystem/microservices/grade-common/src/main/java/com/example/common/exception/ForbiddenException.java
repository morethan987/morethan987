package com.example.common.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException() {
        super(403, "Access forbidden");
    }

    public ForbiddenException(String message) {
        super(403, message);
    }
}
