package com.example.model.entity;

import com.example.model.dto.BinaryMessage;

/**
 * User entity class
 */
public class User {

    private String id; // User ID
    private String username; // Username
    private String password; // Password

    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public BinaryMessage setUserName(String name) {
        this.username = name;
        return new BinaryMessage(true, "用户名设置成功");
    }

    public BinaryMessage setPassword(String pwd) {
        this.password = pwd;
        return new BinaryMessage(true, "密码设置成功");
    }
}
