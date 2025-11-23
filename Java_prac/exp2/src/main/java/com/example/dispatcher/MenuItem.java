package com.example.dispatcher;

import com.example.auth.AuthService;
import com.example.auth.annotation.MenuAction;
import com.example.model.entity.Role;
import com.example.session.SessionManager;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 菜单项数据模型
 * 包含菜单项的所有信息和权限检查逻辑
 */
public class MenuItem {

    private String menu;
    private int option;
    private String title;
    private String permission;
    private String description;
    private String[] roles;
    private boolean requireAuth;
    private int order;
    private String icon;
    private Object controller;
    private Method method;

    /**
     * 根据注解和方法信息构造 MenuItem
     */
    public MenuItem(MenuAction annotation, Object controller, Method method) {
        this.menu = annotation.menu();
        this.option = annotation.option();
        this.title = annotation.title();
        this.permission = annotation.permission();
        this.description = annotation.description();
        this.roles = annotation.roles();
        this.requireAuth = annotation.requireAuth();
        this.order = annotation.order();
        this.icon = annotation.icon();
        this.controller = controller;
        this.method = method;
    }

    /**
     * 检查用户是否有权限访问此菜单项
     * @param sessionId 会话ID
     * @return 是否有权限访问
     */
    public boolean hasAccess(String sessionId) {
        // 如果需要认证但没有会话，则拒绝访问
        if (requireAuth && sessionId == null) {
            return false;
        }

        // 如果指定了权限要求，检查权限
        if (
            !permission.isEmpty() &&
            !AuthService.hasPermission(sessionId, permission)
        ) {
            return false;
        }

        // 如果指定了角色要求，检查角色
        if (roles.length > 0 && !hasAnyRole(sessionId, roles)) {
            return false;
        }

        return true;
    }

    /**
     * 检查用户是否拥有任一指定角色
     */
    private boolean hasAnyRole(String sessionId, String[] requiredRoles) {
        if (sessionId == null || requiredRoles.length == 0) {
            return true;
        }

        try {
            List<Role> userRoles = SessionManager.getRoleBySessionId(sessionId);
            if (userRoles == null || userRoles.isEmpty()) {
                return false;
            }

            for (Role userRole : userRoles) {
                for (String requiredRole : requiredRoles) {
                    if (userRole.getId().equals(requiredRole)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // 如果获取角色失败，为了安全起见，拒绝访问
            System.err.println("Error checking roles: " + e.getMessage());
            return false;
        }

        return false;
    }

    // Getters
    public String getMenu() {
        return menu;
    }

    public int getOption() {
        return option;
    }

    public String getTitle() {
        return title;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public String[] getRoles() {
        return roles;
    }

    public boolean isRequireAuth() {
        return requireAuth;
    }

    public int getOrder() {
        return order;
    }

    public String getIcon() {
        return icon;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return String.format(
            "MenuItem{menu='%s', option=%d, title='%s', permission='%s'}",
            menu,
            option,
            title,
            permission
        );
    }
}
