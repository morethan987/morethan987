package com.example.common.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(401, "Unauthorized access");
    }

    public UnauthorizedException(String message) {
        super(401, message);
    }
}
