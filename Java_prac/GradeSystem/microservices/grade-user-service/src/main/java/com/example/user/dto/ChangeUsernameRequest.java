package com.example.user.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangeUsernameRequest {

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "New username is required")
    private String newUsername;

    public ChangeUsernameRequest() {}

    public ChangeUsernameRequest(String password, String newUsername) {
        this.password = password;
        this.newUsername = newUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}
