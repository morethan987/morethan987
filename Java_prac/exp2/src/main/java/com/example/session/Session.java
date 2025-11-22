package com.example.session;

import com.example.model.entity.Role;
import java.util.List;

/**
 * 用户登陆会话类
 */
class Session {

    private String sessionId; // 会话 ID
    private String userId; // 当前登录用户的 ID
    private String username; // 当前用户的用户名
    private List<Role> roleSet; // 当前用户的角色

    public Session(
        String sessionId,
        String userId,
        String username,
        List<Role> roleSet
    ) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.roleSet = roleSet;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public List<Role> getRoleSet() {
        return roleSet;
    }

    // 清除 session（用于登出）
    public void clear() {
        this.userId = null;
        this.username = null;
        this.roleSet = null;
    }

    // 判断 session 是否有效（是否已登录）
    public boolean isLoggedIn() {
        return userId != null;
    }

    public String getSessionId() {
        return sessionId;
    }
}
