package com.example.model.dto;

import java.util.List;

public class RegistMessage {

    private String username;
    private String password;
    private String role; // TODO: 这里暂时只让用户输入一个角色，但系统支持多个角色

    public RegistMessage(String role, String username, String password) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getRoleList() {
        return List.of(role);
    }
}
