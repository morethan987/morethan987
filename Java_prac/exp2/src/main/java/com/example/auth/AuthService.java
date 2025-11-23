package com.example.auth;

import com.example.model.dao.RoleDAO;
import com.example.model.dao.UserDAO;
import com.example.model.dao.impl.RoleDAOImpl;
import com.example.model.dao.impl.UserDAOImpl;
import com.example.model.dto.BinaryMessage;
import com.example.model.dto.LoginMessage;
import com.example.model.dto.RegistMessage;
import com.example.model.entity.Permission;
import com.example.model.entity.Role;
import com.example.model.entity.User;
import com.example.session.SessionManager;
import java.util.*;

/**
 * 方法权限服务类
 * 执行实际的权限检查逻辑
 */
public class AuthService {

    private static final UserDAO userDAO = new UserDAOImpl();
    private static final RoleDAO roleDAO = new RoleDAOImpl();

    public static boolean hasPermission(String sessionId, String perm) {
        List<Role> userRoles = SessionManager.getRoleBySessionId(sessionId);
        Set<Permission> permissions = new HashSet<Permission>() {};
        for (Role role : userRoles) {
            roleDAO
                .getPermissionsByRoleId(role.getId())
                .forEach(permissions::add);
        }

        for (Permission permission : permissions) {
            if (permission.getName().equals(perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查用户是否拥有任一指定角色
     * @param sessionId 会话ID
     * @param roles 角色名称数组
     * @return 是否拥有任一指定角色
     */
    public static boolean hasAnyRole(String sessionId, String[] roles) {
        if (sessionId == null || roles.length == 0) {
            return true;
        }

        try {
            List<Role> userRoles = SessionManager.getRoleBySessionId(sessionId);
            if (userRoles == null || userRoles.isEmpty()) {
                return false;
            }

            for (Role userRole : userRoles) {
                for (String requiredRole : roles) {
                    if (userRole.getName().equals(requiredRole)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // 如果获取角色失败，为了安全起见，拒绝访问
            return false;
        }

        return false;
    }

    /**
     * 检查用户是否拥有指定角色
     * @param sessionId 会话ID
     * @param roleName 角色名称
     * @return 是否拥有指定角色
     */
    public static boolean hasRole(String sessionId, String roleName) {
        return hasAnyRole(sessionId, new String[] { roleName });
    }

    public static BinaryMessage authLogin(LoginMessage loginMessage) {
        String username = loginMessage.getUsername();
        String password = loginMessage.getPassword();

        // 验证用户身份（DB 查询）
        User user = null;
        try {
            user = userDAO.getUserByUsername(username);
        } catch (Exception e) {
            return new BinaryMessage(false, "数据库查询出错");
        }

        if (user == null) {
            return new BinaryMessage(false, "用户不存在");
        }

        if (!user.getPassword().equals(password)) {
            return new BinaryMessage(false, "密码错误");
        }

        // 获取用户角色
        List<Role> roleList = null;
        try {
            roleList = roleDAO.getRolesByUserId(user.getId());
        } catch (Exception e) {
            return new BinaryMessage(false, "获取用户角色失败");
        }

        // 创建会话
        String sessionId = null;
        try {
            sessionId = SessionManager.addSession(
                user.getId(),
                user.getUsername(),
                roleList
            );
        } catch (Exception e) {
            return new BinaryMessage(false, "创建会话失败" + e.getMessage());
        }

        return new BinaryMessage(true, sessionId);
    }

    public static BinaryMessage registerUser(RegistMessage registMessage) {
        String username = registMessage.getUsername();
        String password = registMessage.getPassword();
        List<String> role = registMessage.getRoleList();

        // 检查用户名是否已存在
        User existingUser = null;
        try {
            existingUser = userDAO.getUserByUsername(username);
        } catch (Exception e) {
            return new BinaryMessage(false, "数据库查询出错");
        }

        if (existingUser != null) {
            return new BinaryMessage(false, "用户名已存在");
        }

        // 创建新用户
        User newUser = new User(
            UUID.randomUUID().toString(),
            username,
            password
        );
        try {
            userDAO.addUser(newUser, role);
        } catch (Exception e) {
            return new BinaryMessage(false, "创建用户失败");
        }

        return new BinaryMessage(true, "用户注册成功");
    }
}
