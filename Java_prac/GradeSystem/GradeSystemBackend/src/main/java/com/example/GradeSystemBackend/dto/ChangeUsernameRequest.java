package com.example.GradeSystemBackend.dto;

public class ChangeUsernameRequest {

    private String password;
    private String newUsername;

    public ChangeUsernameRequest() {}

    public ChangeUsernameRequest(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
