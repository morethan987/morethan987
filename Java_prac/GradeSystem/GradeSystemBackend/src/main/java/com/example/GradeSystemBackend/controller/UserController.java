package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.*;
import com.example.GradeSystemBackend.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> getUserProfile(@PathVariable UUID id) {
        try {
            UserDTO user = userService.getUserProfile(id);
            return ResponseEntity.ok(user);
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
    public ResponseEntity<?> getUserProfileByUsername(
        @PathVariable String username
    ) {
        try {
            UserDTO user = userService.getUserProfileByUsername(username);
            return ResponseEntity.ok(user);
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
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 根据用户名搜索用户
     */
    @GetMapping("/search")
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
    public ResponseEntity<List<UserDTO>> getEnabledUsers() {
        List<UserDTO> users = userService.getEnabledUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 获取已禁用的用户
     */
    @GetMapping("/disabled")
    public ResponseEntity<List<UserDTO>> getDisabledUsers() {
        List<UserDTO> users = userService.getDisabledUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 启用用户
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable UUID id) {
        try {
            UserDTO updatedUser = userService.enableUser(id);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * 禁用用户
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable UUID id) {
        try {
            UserDTO updatedUser = userService.disableUser(id);
            return ResponseEntity.ok(updatedUser);
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
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(
        @PathVariable String username
    ) {
        boolean exists = userService.usernameExists(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
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
