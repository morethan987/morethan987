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
import com.example.util.LoggerUtil;
import java.util.*;

/**
 * 方法权限服务类
 * 执行实际的权限检查逻辑
 */
public class AuthService {

    private static final UserDAO userDAO = new UserDAOImpl();
    private static final RoleDAO roleDAO = new RoleDAOImpl();

    public static boolean hasPermission(String sessionId, String perm) {
        LoggerUtil.debug(
            "检查权限 - SessionID: %s, Permission: %s",
            sessionId,
            perm
        );

        try {
            List<Role> userRoles = SessionManager.getRoleBySessionId(sessionId);
            Set<Permission> permissions = new HashSet<Permission>() {};
            for (Role role : userRoles) {
                roleDAO
                    .getPermissionsByRoleId(role.getId())
                    .forEach(permissions::add);
            }

            for (Permission permission : permissions) {
                if (permission.getName().equals(perm)) {
                    LoggerUtil.debug(
                        "权限检查通过 - SessionID: %s, Permission: %s",
                        sessionId,
                        perm
                    );
                    return true;
                }
            }

            LoggerUtil.debug(
                "权限检查失败 - SessionID: %s, Permission: %s",
                sessionId,
                perm
            );
            return false;
        } catch (Exception e) {
            LoggerUtil.error(
                "权限检查异常 - SessionID: %s, Permission: %s",
                e,
                sessionId,
                perm
            );
            return false;
        }
    }

    /**
     * 检查用户是否拥有任一指定角色
     * @param sessionId 会话ID
     * @param roles 角色名称数组
     * @return 是否拥有任一指定角色
     */
    public static boolean hasAnyRole(String sessionId, String[] roles) {
        LoggerUtil.debug(
            "检查角色 - SessionID: %s, RequiredRoles: %s",
            sessionId,
            Arrays.toString(roles)
        );

        if (sessionId == null || roles.length == 0) {
            LoggerUtil.debug("角色检查跳过 - SessionID为空或无需角色要求");
            return true;
        }

        try {
            List<Role> userRoles = SessionManager.getRoleBySessionId(sessionId);
            if (userRoles == null || userRoles.isEmpty()) {
                LoggerUtil.debug(
                    "角色检查失败 - 用户无角色, SessionID: %s",
                    sessionId
                );
                return false;
            }

            for (Role userRole : userRoles) {
                for (String requiredRole : roles) {
                    if (userRole.getName().equals(requiredRole)) {
                        LoggerUtil.debug(
                            "角色检查通过 - SessionID: %s, MatchedRole: %s",
                            sessionId,
                            requiredRole
                        );
                        return true;
                    }
                }
            }

            LoggerUtil.debug(
                "角色检查失败 - SessionID: %s, UserRoles: %s, RequiredRoles: %s",
                sessionId,
                userRoles.stream().map(Role::getName).toArray(),
                Arrays.toString(roles)
            );
        } catch (Exception e) {
            // 如果获取角色失败，为了安全起见，拒绝访问
            LoggerUtil.error("角色检查异常 - SessionID: %s", e, sessionId);
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

        LoggerUtil.info("用户登录认证开始 - Username: %s", username);
        LoggerUtil.logDatabaseOperation(
            "查询",
            "User",
            "根据用户名查询用户: " + username
        );

        // 验证用户身份（DB 查询）
        User user = null;
        try {
            user = userDAO.getUserByUsername(username);
        } catch (Exception e) {
            LoggerUtil.error(
                "登录认证数据库查询失败 - Username: %s",
                e,
                username
            );
            return new BinaryMessage(false, "数据库查询出错");
        }

        if (user == null) {
            LoggerUtil.warning("登录认证失败 - 用户不存在: %s", username);
            return new BinaryMessage(false, "用户不存在");
        }

        if (!user.getPassword().equals(password)) {
            LoggerUtil.warning("登录认证失败 - 密码错误: %s", username);
            return new BinaryMessage(false, "密码错误");
        }

        LoggerUtil.info("用户密码验证成功 - Username: %s", username);
        LoggerUtil.logDatabaseOperation(
            "查询",
            "Role",
            "获取用户角色: " + user.getId()
        );

        // 获取用户角色
        List<Role> roleList = null;
        try {
            roleList = roleDAO.getRolesByUserId(user.getId());
        } catch (Exception e) {
            LoggerUtil.error(
                "获取用户角色失败 - Username: %s, UserID: %s",
                e,
                username,
                user.getId()
            );
            return new BinaryMessage(false, "获取用户角色失败");
        }

        LoggerUtil.info(
            "获取用户角色成功 - Username: %s, Roles: %s",
            username,
            roleList.stream().map(Role::getName).toArray()
        );

        // 创建会话
        String sessionId = null;
        try {
            sessionId = SessionManager.addSession(
                user.getId(),
                user.getUsername(),
                roleList
            );
            LoggerUtil.info(
                "创建用户会话成功 - Username: %s, SessionID: %s",
                username,
                sessionId
            );
        } catch (Exception e) {
            LoggerUtil.error("创建用户会话失败 - Username: %s", e, username);
            return new BinaryMessage(false, "创建会话失败" + e.getMessage());
        }

        LoggerUtil.info(
            "用户登录认证成功 - Username: %s, SessionID: %s",
            username,
            sessionId
        );
        return new BinaryMessage(true, sessionId);
    }

    public static BinaryMessage registerUser(RegistMessage registMessage) {
        String username = registMessage.getUsername();
        String password = registMessage.getPassword();
        List<String> role = registMessage.getRoleList();

        LoggerUtil.info(
            "用户注册开始 - Username: %s, Roles: %s",
            username,
            role
        );
        LoggerUtil.logDatabaseOperation(
            "查询",
            "User",
            "检查用户名是否存在: " + username
        );

        // 检查用户名是否已存在
        User existingUser = null;
        try {
            existingUser = userDAO.getUserByUsername(username);
        } catch (Exception e) {
            LoggerUtil.error(
                "注册时数据库查询失败 - Username: %s",
                e,
                username
            );
            return new BinaryMessage(false, "数据库查询出错");
        }

        if (existingUser != null) {
            LoggerUtil.warning("用户注册失败 - 用户名已存在: %s", username);
            return new BinaryMessage(false, "用户名已存在");
        }

        LoggerUtil.info("用户名检查通过 - Username: %s", username);

        // 创建新用户
        String userId = UUID.randomUUID().toString();
        User newUser = new User(userId, username, password);

        LoggerUtil.logDatabaseOperation(
            "插入",
            "User",
            String.format(
                "创建新用户 - ID: %s, Username: %s, Roles: %s",
                userId,
                username,
                role
            )
        );

        try {
            userDAO.addUser(newUser, role);
            LoggerUtil.info(
                "用户注册成功 - Username: %s, UserID: %s, Roles: %s",
                username,
                userId,
                role
            );
        } catch (Exception e) {
            LoggerUtil.error(
                "创建用户失败 - Username: %s, UserID: %s",
                e,
                username,
                userId
            );
            return new BinaryMessage(false, "创建用户失败");
        }

        LoggerUtil.info("用户注册完成 - Username: %s", username);
        return new BinaryMessage(true, "用户注册成功");
    }
}
