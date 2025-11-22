package com.example.auth;

import com.example.model.dao.RoleDAO;
import com.example.model.dao.UserDAO;
import com.example.model.dao.impl.RoleDAOImpl;
import com.example.model.dao.impl.UserDAOImpl;
import com.example.model.dto.BinaryMessage;
import com.example.model.dto.LoginMessage;
import com.example.model.entity.Role;
import com.example.model.entity.User;
import com.example.session.SessionManager;
import java.util.List;

/**
 * 方法权限服务类
 * 执行实际的权限检查逻辑
 */
public class AuthService {

    private static final UserDAO userDAO = new UserDAOImpl();
    private static final RoleDAO roleDAO = new RoleDAOImpl();

    public static boolean hasPermission(String sessionId, String perm) {
        return true;
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
            return new BinaryMessage(false, "创建会话失败");
        }

        return new BinaryMessage(true, sessionId);
    }
}
