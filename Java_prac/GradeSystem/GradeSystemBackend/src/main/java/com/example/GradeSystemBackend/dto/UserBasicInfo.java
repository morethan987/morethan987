package com.example.GradeSystemBackend.dto;

import com.example.GradeSystemBackend.domain.auth.UIType;
import java.util.Set;
import java.util.UUID;

public class UserBasicInfo {

    // from entity User
    private UUID id;
    private String username;
    private boolean enabled;
    private Set<String> roles;
    private UIType uiType;

    // from entity UserProfile
    private String realName;
    private String email;
    private String avatarUrl;

    // 构造函数
    public UserBasicInfo() {}

    public UserBasicInfo(
        UUID id,
        String username,
        boolean enabled,
        Set<String> roles
    ) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
        this.roles = roles;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UIType getUiType() {
        return uiType;
    }

    public void setUiType(UIType uiType) {
        this.uiType = uiType;
    }
}
