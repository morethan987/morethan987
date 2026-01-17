package com.example.user.client;

import java.util.UUID;

public class PasswordValidationRequest {
    
    private UUID userId;
    private String password;

    public PasswordValidationRequest() {}

    public PasswordValidationRequest(UUID userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
