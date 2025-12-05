package com.example.GradeSystemBackend.dto;

import java.util.Set;
import java.util.UUID;

public class AuthResponse {

    private boolean success;
    private String message;
    private UserInfo user;
    private Object data;

    // 构造函数
    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, UserInfo user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    // 静态工厂方法
    public static AuthResponse success(String message) {
        return new AuthResponse(true, message);
    }

    public static AuthResponse success(String message, UserInfo user) {
        return new AuthResponse(true, message, user);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public Object getData() {
        return data;
    }

    public AuthResponse setData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 用户信息内部类
     */
    public static class UserInfo {

        private UUID id;
        private String username;
        private boolean enabled;
        private Set<String> roles;
        private String realName;
        private String email;

        // 构造函数
        public UserInfo() {}

        public UserInfo(
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
    }
}
