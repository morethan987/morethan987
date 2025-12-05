package com.example.GradeSystemBackend.dto;

import java.util.Set;
import java.util.UUID;

public class UserDTO {

    private UUID id;
    private String username;
    private boolean enabled;
    private Set<String> roles;

    // Constructors
    public UserDTO() {}

    public UserDTO(UUID id, String username, boolean enabled) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
