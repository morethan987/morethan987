package com.example.model.dto;

public class BinaryMessage {

    private boolean bool_result;
    private String message;

    public BinaryMessage(boolean bool_result, String message) {
        this.bool_result = bool_result;
        this.message = message;
    }

    public boolean isBool_result() {
        return bool_result;
    }

    public String getMessage() {
        return message;
    }
}
