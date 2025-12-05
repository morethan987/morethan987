package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.service.RoleService;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 创建角色
     */
    @PreAuthorize("hasAnyAuthority('role:add', 'admin:all')")
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        printCurrentUser("createRole");

        try {
            Role savedRole = roleService.createRole(role);
            return ResponseEntity.ok(savedRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @PreAuthorize("hasAnyAuthority('role:delete', 'admin:all')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable UUID id) {
        printCurrentUser("deleteRole");

        try {
            Optional<Role> roleOpt = roleService.findRoleById(id);
            if (roleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String roleName = roleOpt.get().getName();
            roleService.deleteRole(id);
            return ResponseEntity.ok("角色 '" + roleName + "' 删除成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 为角色添加权限
     */
    @PreAuthorize("hasAnyAuthority('role:update', 'admin:all')")
    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<?> addPermissionToRole(
        @PathVariable UUID roleId,
        @RequestBody Map<String, String> request
    ) {
        printCurrentUser("addPermissionToRole");

        String permissionName = request.get("permissionName");
        try {
            String result = roleService.addPermissionToRole(
                roleId,
                permissionName
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 从角色移除权限
     */
    @PreAuthorize("hasAnyAuthority('role:update', 'admin:all')")
    @DeleteMapping("/{roleId}/permissions/{permissionName}")
    public ResponseEntity<?> removePermissionFromRole(
        @PathVariable UUID roleId,
        @PathVariable String permissionName
    ) {
        printCurrentUser("removePermissionFromRole");

        try {
            String result = roleService.removePermissionFromRole(
                roleId,
                permissionName
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 批量设置角色权限
     */
    @PreAuthorize("hasAnyAuthority('role:update', 'admin:all')")
    @PutMapping("/{roleId}/permissions")
    public ResponseEntity<?> setRolePermissions(
        @PathVariable UUID roleId,
        @RequestBody List<String> permissionNames
    ) {
        printCurrentUser("setRolePermissions");

        try {
            Map<String, Object> result = roleService.setRolePermissions(
                roleId,
                permissionNames
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取所有角色列表
     */
    @PreAuthorize("hasAnyAuthority('role:view', 'admin:all')")
    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        printCurrentUser("getAllRoles");

        Map<String, Object> result = roleService.getAllRoles();
        return ResponseEntity.ok(result);
    }

    /**
     * 根据名称搜索角色
     */
    @PreAuthorize("hasAnyAuthority('role:view', 'admin:all')")
    @GetMapping("/search")
    public ResponseEntity<?> searchRolesByName(@RequestParam String name) {
        printCurrentUser("searchRolesByName");

        try {
            Map<String, Object> result = roleService.searchRolesByName(name);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 查看角色的所有权限
     */
    @PreAuthorize("hasAnyAuthority('role:view', 'admin:all')")
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<?> getRolePermissions(@PathVariable UUID roleId) {
        printCurrentUser("getRolePermissions");

        try {
            Map<String, Object> result = roleService.getRolePermissions(roleId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                e.getMessage()
            );
        }
    }

    /**
     * 根据ID查看角色详情
     */
    @PreAuthorize("hasAnyAuthority('role:view', 'admin:all')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable UUID id) {
        printCurrentUser("getRoleById");

        try {
            Map<String, Object> result = roleService.getRoleById(id);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                e.getMessage()
            );
        }
    }

    /**
     * 获取角色统计信息
     */
    @PreAuthorize("hasAnyAuthority('role:view', 'admin:all')")
    @GetMapping("/stats")
    public ResponseEntity<?> getRoleStats() {
        printCurrentUser("getRoleStats");

        Map<String, Object> result = roleService.getRoleStats();
        return ResponseEntity.ok(result);
    }

    /**
     * 检查角色是否存在
     */
    @PreAuthorize("hasAnyAuthority('role:view', 'admin:all')")
    @GetMapping("/exists/{name}")
    public ResponseEntity<?> checkRoleExists(@PathVariable String name) {
        printCurrentUser("checkRoleExists");

        try {
            Map<String, Object> result = roleService.checkRoleExists(name);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 打印当前访问者（用于调试你是否真的登录 & 授权生效）
     */
    private void printCurrentUser(String method) {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[" + method + "] 当前登录用户：" + auth.getName());
        System.out.println(
            "[" + method + "] 用户权限：" + auth.getAuthorities()
        );
    }

    /**
     * 专门处理权限不足异常
     * 返回 403 Forbidden 而不是 500
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(
        AccessDeniedException e
    ) {
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
     * 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        System.err.println("RoleController异常: " + e.getMessage());
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of(
                "error",
                "服务器内部错误",
                "message",
                e.getMessage(),
                "timestamp",
                System.currentTimeMillis()
            )
        );
    }

    /**
     * 参数验证异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
        IllegalArgumentException e
    ) {
        return ResponseEntity.badRequest().body(
            Map.of("error", "参数错误", "message", e.getMessage())
        );
    }
}
