package com.example.GradeSystemBackend.dto;

public class AuthResponse {

    private boolean success;
    private String message;
    private UserBasicInfo user;
    private Object data;

    // 构造函数
    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, UserBasicInfo user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    // 静态工厂方法
    public static AuthResponse success(String message) {
        return new AuthResponse(true, message);
    }

    public static AuthResponse success(String message, UserBasicInfo user) {
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

    public UserBasicInfo getUser() {
        return user;
    }

    public void setUser(UserBasicInfo user) {
        this.user = user;
    }

    public Object getData() {
        return data;
    }

    public AuthResponse setData(Object data) {
        this.data = data;
        return this;
    }
}
