package com.example.GradeSystemBackend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * 登录由 Spring Security 处理
     * 本接口只作为登录入口占位
     */
    @PostMapping("/login")
    public void login() {
        // 由 Spring Security 接管
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public void logout() {
        // Session 自动失效
    }
}
