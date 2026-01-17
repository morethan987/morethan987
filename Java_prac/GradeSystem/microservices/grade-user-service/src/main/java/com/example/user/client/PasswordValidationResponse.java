package com.example.user.client;

public class PasswordValidationResponse {
    
    private boolean valid;
    private String message;

    public PasswordValidationResponse() {}

    public PasswordValidationResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
