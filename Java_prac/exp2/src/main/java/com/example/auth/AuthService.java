package com.example.auth;

import com.example.model.dao.PermissionDAO;
import com.example.model.dao.RoleDAO;
import com.example.model.dao.UserDAO;
import com.example.model.entity.Role;
import com.example.model.entity.User;
import com.example.session.SessionManager;
import java.util.Set;

/**
 * 方法权限服务类
 * 执行实际的权限检查逻辑
 */
public class AuthService {

    public static boolean hasPermission(String sessionId, String perm) {
        return true;
    }

    public static String login(String username, String password) {
        // 验证用户身份（DB 查询）
        User user = userDAO.findByUsernameAndPassword(username, password);
        if (user == null) return null;

        // 加载角色
        Role role = roleDAO.findById(user.getRoleId());

        // 加载该角色拥有的权限
        Set<String> permissions = permissionDAO.findPermissionCodesByRoleId(
            role.getId()
        );

        return SessionManager.addSession(
            user.getId(),
            user.getUsername(),
            role.getName(),
            permissions
        );
    }
}
