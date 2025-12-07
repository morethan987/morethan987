package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.dto.ChangePasswordRequest;
import com.example.GradeSystemBackend.dto.ChangeUsernameRequest;
import com.example.GradeSystemBackend.dto.UpdateUserProfileRequest;
import com.example.GradeSystemBackend.dto.UserDTO;
import com.example.GradeSystemBackend.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据ID获取用户资料
     */
    @GetMapping("/profile/{id}")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<?> getUserProfile(@PathVariable UUID id) {
        try {
            UserProfile profile = userService.getUserProfile(id);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * 根据用户名获取用户资料
     */
    @GetMapping("/profile/by-username/{username}")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<?> getUserProfileByUsername(
        @PathVariable String username
    ) {
        try {
            UserProfile profile = userService.getUserProfileByUsername(
                username
            );
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile/{id}")
    @PreAuthorize("hasAnyAuthority('user_profile:update', 'admin:all')")
    public ResponseEntity<?> updateUserProfile(
        @PathVariable UUID id,
        @RequestBody UpdateUserProfileRequest request
    ) {
        try {
            UserDTO updatedUser = userService.updateUserProfile(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * 修改用户名
     */
    @PutMapping("/{id}/username")
    @PreAuthorize("hasAnyAuthority('user:update', 'admin:all')")
    public ResponseEntity<?> changeUsername(
        @PathVariable UUID id,
        @RequestBody ChangeUsernameRequest request
    ) {
        try {
            UserDTO updatedUser = userService.changeUsername(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAnyAuthority('user:update', 'admin:all')")
    public ResponseEntity<?> changePassword(
        @PathVariable UUID id,
        @RequestBody ChangePasswordRequest request
    ) {
        try {
            userService.changePassword(id, request);
            return ResponseEntity.ok(
                createSuccessResponse("Password changed successfully")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('user:view', 'admin:all')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 根据用户名搜索用户
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('user:view', 'admin:all')")
    public ResponseEntity<List<UserDTO>> searchUsers(
        @RequestParam String username
    ) {
        List<UserDTO> users = userService.searchUsersByUsername(username);
        return ResponseEntity.ok(users);
    }

    /**
     * 获取已启用的用户
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAnyAuthority('user:view', 'admin:all')")
    public ResponseEntity<List<UserDTO>> getEnabledUsers() {
        List<UserDTO> users = userService.getEnabledUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 获取已禁用的用户
     */
    @GetMapping("/disabled")
    @PreAuthorize("hasAnyAuthority('user:view', 'admin:all')")
    public ResponseEntity<List<UserDTO>> getDisabledUsers() {
        List<UserDTO> users = userService.getDisabledUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('user:update', 'admin:all')")
    public ResponseEntity<?> changeUserStatus(
        @PathVariable UUID id,
        boolean isEnabled
    ) {
        try {
            if (isEnabled) {
                UserDTO updatedUser = userService.enableUser(id);
                return ResponseEntity.ok(updatedUser);
            } else {
                UserDTO updatedUser = userService.disableUser(id);
                return ResponseEntity.ok(updatedUser);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/exists/{username}")
    @PreAuthorize("hasAnyAuthority('user:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(
        @PathVariable String username
    ) {
        boolean exists = userService.usernameExists(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    /**
     * 专门处理权限不足异常
     * 返回 403 Forbidden 而不是 500
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(
        AccessDeniedException e
    ) {
        // 这里不需要 printStackTrace，因为权限不足是很正常的业务逻辑
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            Map.of(
                "error",
                "权限不足",
                "message",
                "您没有操作该资源的权限: " + e.getMessage(),
                "timestamp",
                System.currentTimeMillis()
            )
        );
    }

    /**
     * 创建错误响应
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    /**
     * 创建成功响应
     */
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
