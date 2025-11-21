package com.example.session;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户登陆会话类
 */
class Session {

    private String sessionId; // 会话 ID
    private String userId; // 当前登录用户的 ID
    private String username; // 当前用户的用户名
    private String roleName; // 当前用户的角色名称
    private Set<String> permissions; // 当前用户拥有的权限码集合

    public Session() {
        this.permissions = new HashSet<>();
    }

    public Session(
        String sessionId,
        String userId,
        String username,
        String roleName,
        Set<String> permissions
    ) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.roleName = roleName;
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRoleName() {
        return roleName;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    // 判断用户是否拥有某个权限码
    public boolean hasPermission(String permissionCode) {
        return permissions.contains(permissionCode);
    }

    // 添加一个权限码（用于角色权限更新后）
    public void addPermission(String permissionCode) {
        permissions.add(permissionCode);
    }

    // 清除 session（用于登出）
    public void clear() {
        this.userId = null;
        this.username = null;
        this.roleName = null;
        this.permissions.clear();
    }

    // 判断 session 是否有效（是否已登录）
    public boolean isLoggedIn() {
        return userId != null;
    }
}
