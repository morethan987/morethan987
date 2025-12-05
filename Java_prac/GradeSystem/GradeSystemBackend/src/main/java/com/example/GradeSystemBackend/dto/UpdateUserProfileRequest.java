package com.example.GradeSystemBackend.dto;

public class UpdateUserProfileRequest {

    private String username;
    private Boolean enabled;

    // Constructors
    public UpdateUserProfileRequest() {}

    public UpdateUserProfileRequest(String username, Boolean enabled) {
        this.username = username;
        this.enabled = enabled;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
