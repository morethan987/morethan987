package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.AuthResponse;
import com.example.GradeSystemBackend.dto.LoginRequest;
import com.example.GradeSystemBackend.dto.RegisterRequest;
import com.example.GradeSystemBackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // 注入 SecurityContextRepository
    @Autowired
    private SecurityContextRepository securityContextRepository;

    /**
     * 登录接口
     * 接收 JSON 格式: {"username": "...", "password": "..."}
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest req,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        try {
            // 1. 创建认证令牌
            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                    req.getUsername(),
                    req.getPassword()
                );

            // 2. 执行认证 (会调用 UserDetailsService)
            Authentication auth = authenticationManager.authenticate(token);

            // 3. 创建新的 SecurityContext
            SecurityContext context =
                SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // 4. 显式保存 Context 到 Session
            // 这会触发创建 JSESSIONID Cookie 并写回给前端
            securityContextRepository.saveContext(context, request, response);

            return ResponseEntity.ok(authService.getCurrentUser());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                AuthResponse.error("登录失败: 用户名或密码错误")
            );
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            // Spring Security 提供的注销处理器，会自动清理 Session 和 Cookie
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok(AuthResponse.success("退出登录成功"));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request,
        BindingResult bindingResult
    ) {
        try {
            // 检查基本验证错误
            if (bindingResult.hasErrors()) {
                StringBuilder errorMsg = new StringBuilder();
                bindingResult
                    .getFieldErrors()
                    .forEach(error -> {
                        errorMsg.append(error.getDefaultMessage()).append("; ");
                    });
                return ResponseEntity.badRequest().body(
                    AuthResponse.error("输入验证失败: " + errorMsg.toString())
                );
            }

            // 进行业务逻辑验证
            String validationError = authService.validateRegisterRequest(
                request
            );
            if (validationError != null) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.error(validationError)
                );
            }

            // 调用服务进行注册
            AuthResponse response = authService.register(request);

            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthResponse.error("注册过程中发生错误: " + e.getMessage())
            );
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        try {
            AuthResponse response = authService.getCurrentUser();

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    response
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthResponse.error("获取用户信息失败: " + e.getMessage())
            );
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<AuthResponse> checkUsername(
        @RequestParam String username
    ) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.error("用户名不能为空")
                );
            }

            AuthResponse response = authService.checkUsernameAvailable(
                username.trim()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthResponse.error("检查用户名失败: " + e.getMessage())
            );
        }
    }

    /**
     * 获取所有可用角色
     */
    @GetMapping("/roles")
    public ResponseEntity<AuthResponse> getAvailableRoles() {
        try {
            var roles = authService.getAvailableRoles();
            return ResponseEntity.ok(
                AuthResponse.success("获取角色列表成功").setData(roles)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthResponse.error("获取角色列表失败: " + e.getMessage())
            );
        }
    }

    /**
     * 检查角色是否存在
     */
    @GetMapping("/check-role")
    public ResponseEntity<AuthResponse> checkRole(@RequestParam String role) {
        try {
            if (role == null || role.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.error("角色不能为空")
                );
            }

            boolean exists = authService.roleExists(role.trim());
            if (exists) {
                return ResponseEntity.ok(AuthResponse.success("角色存在"));
            } else {
                return ResponseEntity.ok(
                    AuthResponse.error(
                        "角色不存在，可用角色：" +
                            String.join(", ", authService.getAvailableRoles())
                    )
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthResponse.error("检查角色失败: " + e.getMessage())
            );
        }
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public ResponseEntity<AuthResponse> checkEmail(@RequestParam String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    AuthResponse.error("邮箱不能为空")
                );
            }

            AuthResponse response = authService.checkEmailAvailable(
                email.trim()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthResponse.error("检查邮箱失败: " + e.getMessage())
            );
        }
    }

    /**
     * 检查用户是否已登录
     */
    @GetMapping("/status")
    public ResponseEntity<AuthResponse> getAuthStatus() {
        try {
            boolean isAuthenticated = authService.isCurrentUserAuthenticated();

            if (isAuthenticated) {
                return ResponseEntity.ok(AuthResponse.success("用户已登录"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthResponse.error("用户未登录")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthResponse.error("检查登录状态失败: " + e.getMessage())
            );
        }
    }

    /**
     * 测试接口 - 获取当前用户名（用于调试）
     */
    @GetMapping("/username")
    public ResponseEntity<AuthResponse> getCurrentUsername() {
        try {
            String username = authService.getCurrentUsername();

            if (username != null) {
                return ResponseEntity.ok(
                    AuthResponse.success("当前用户名: " + username)
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthResponse.error("用户未登录")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthResponse.error("获取用户名失败: " + e.getMessage())
            );
        }
    }
}
