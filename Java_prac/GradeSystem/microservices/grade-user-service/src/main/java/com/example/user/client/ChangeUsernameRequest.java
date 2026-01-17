package com.example.user.client;

public class ChangeUsernameRequest {
    
    private String password;
    private String newUsername;

    public ChangeUsernameRequest() {}

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
