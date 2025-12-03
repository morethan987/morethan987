package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.utils.JwtUtils;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public Map<String, String> login(
        @RequestBody Map<String, String> loginData
    ) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        // TODO: 这里应该去数据库查用户表，对比密码
        // 为了演示，我先硬编码：如果用户名是 admin，密码是 123456，就通过
        if ("admin".equals(username) && "123456".equals(password)) {
            // 验证通过，生成 Token
            String token = jwtUtils.generateToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return response;
        } else {
            throw new RuntimeException("用户名或密码错误！");
        }
    }
}
