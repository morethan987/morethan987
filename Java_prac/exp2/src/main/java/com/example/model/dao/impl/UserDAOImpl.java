package com.example.model.dao.impl;

import com.example.model.dao.UserDAO;
import com.example.model.entity.User;
import com.example.util.LoggerUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl extends BaseDAOImpl implements UserDAO {

    public UserDAOImpl() {
        super();
        LoggerUtil.debug("初始化UserDAOImpl");
        createTable();
        createUserRoleTable();
        LoggerUtil.debug("UserDAOImpl初始化完成");
    }

    /**
     * 获取数据库连接
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    @Override
    public void createTable() {
        String sql =
            "CREATE TABLE IF NOT EXISTS user (" +
            "id TEXT PRIMARY KEY, " +
            "username TEXT NOT NULL UNIQUE, " +
            "password TEXT NOT NULL)";

        LoggerUtil.logDatabaseOperation("创建表", "user", "初始化用户表结构");
        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
            LoggerUtil.debug("用户表创建成功");
        } catch (SQLException e) {
            LoggerUtil.error("创建用户表失败: " + e.getMessage(), e);
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    public void createUserRoleTable() {
        String sql =
            "CREATE TABLE IF NOT EXISTS user_role (" +
            "user_id TEXT NOT NULL, " +
            "role_id TEXT NOT NULL, " +
            "PRIMARY KEY (user_id, role_id), " +
            "FOREIGN KEY (user_id) REFERENCES user(id), " +
            "FOREIGN KEY (role_id) REFERENCES role(id))";

        LoggerUtil.logDatabaseOperation(
            "创建表",
            "user_role",
            "初始化用户角色关联表结构"
        );

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
            LoggerUtil.debug("用户角色关联表创建成功");
        } catch (SQLException e) {
            LoggerUtil.error("创建用户角色关联表失败: " + e.getMessage(), e);
            System.err.println("创建用户角色关联表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addUser(User user, List<String> roleList) {
        LoggerUtil.logDatabaseOperation(
            "插入",
            "user",
            String.format(
                "添加用户 - ID: %s, Username: %s, Roles: %s",
                user.getId(),
                user.getUsername(),
                roleList
            )
        );

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 开启事务
            LoggerUtil.debug(
                "开启数据库事务用于添加用户: " + user.getUsername()
            );

            // 1. 插入用户
            String userSql =
                "INSERT INTO user(id, username, password) VALUES(?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(userSql)) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getUsername());
                pstmt.setString(3, user.getPassword()); // TODO: 实际项目中密码应当加密存储

                int userRows = pstmt.executeUpdate();
                if (userRows == 0) {
                    LoggerUtil.warning(
                        "插入用户失败，影响行数为0 - Username: " +
                            user.getUsername()
                    );
                    conn.rollback();
                    return false;
                }
                LoggerUtil.debug(
                    "成功插入用户 - Username: " + user.getUsername()
                );
            }

            // 2. 插入用户角色关联（如果roleId不为空）
            for (String roleId : roleList) {
                if (roleId != null && !roleId.trim().isEmpty()) {
                    String roleSql =
                        "INSERT INTO user_role(user_id, role_id) VALUES(?, ?)";
                    try (
                        PreparedStatement pstmt = conn.prepareStatement(roleSql)
                    ) {
                        pstmt.setString(1, user.getId());
                        pstmt.setString(2, roleId);

                        int roleRows = pstmt.executeUpdate();
                        if (roleRows == 0) {
                            LoggerUtil.warning(
                                "插入用户角色关联失败 - UserID: " +
                                    user.getId() +
                                    ", RoleID: " +
                                    roleId
                            );
                            conn.rollback();
                            return false;
                        }
                        LoggerUtil.debug(
                            "成功插入用户角色关联 - UserID: " +
                                user.getId() +
                                ", RoleID: " +
                                roleId
                        );
                    }
                }
            }

            conn.commit(); // 提交事务
            LoggerUtil.info(
                "成功添加用户 - Username: " +
                    user.getUsername() +
                    ", UserID: " +
                    user.getId()
            );
            return true;
        } catch (SQLException e) {
            LoggerUtil.error(
                "添加用户失败 - Username: " + user.getUsername(),
                e
            );
            if (conn != null) {
                try {
                    conn.rollback(); // 回滚事务
                    LoggerUtil.debug(
                        "事务回滚成功 - Username: " + user.getUsername()
                    );
                } catch (SQLException rollbackEx) {
                    LoggerUtil.error(
                        "回滚事务失败 - Username: " + user.getUsername(),
                        rollbackEx
                    );
                    System.err.println(
                        "回滚事务失败: " + rollbackEx.getMessage()
                    );
                }
            }
            System.err.println("添加用户失败: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 恢复自动提交
                    conn.close();
                } catch (SQLException closeEx) {
                    LoggerUtil.error("关闭数据库连接失败", closeEx);
                    System.err.println("关闭连接失败: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public User getUserById(String id) {
        LoggerUtil.logDatabaseOperation(
            "查询",
            "user",
            "根据ID查询用户: " + id
        );
        String sql = "SELECT id, username, password FROM user WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = mapRowToUser(rs);
                LoggerUtil.debug(
                    "根据ID查询用户成功 - ID: " +
                        id +
                        ", Username: " +
                        user.getUsername()
                );
                return user;
            }
            LoggerUtil.debug("根据ID查询用户为空 - ID: " + id);
        } catch (SQLException e) {
            LoggerUtil.error("查询用户ID失败 - ID: " + id, e);
            System.err.println("查询用户ID失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) {
        LoggerUtil.logDatabaseOperation(
            "查询",
            "user",
            "根据用户名查询用户: " + username
        );
        String sql =
            "SELECT id, username, password FROM user WHERE username = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = mapRowToUser(rs);
                LoggerUtil.debug(
                    "根据用户名查询用户成功 - Username: " +
                        username +
                        ", ID: " +
                        user.getId()
                );
                return user;
            }
            LoggerUtil.debug("根据用户名查询用户为空 - Username: " + username);
        } catch (SQLException e) {
            LoggerUtil.error("查询用户名失败 - Username: " + username, e);
            throw new RuntimeException("查询用户名失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        LoggerUtil.logDatabaseOperation("查询", "user", "查询所有用户");
        List<User> userList = new ArrayList<>();
        String sql = "SELECT id, username, password FROM user";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                userList.add(mapRowToUser(rs));
            }
            LoggerUtil.debug(
                "查询所有用户完成，共查询到: " + userList.size() + " 个用户"
            );
        } catch (SQLException e) {
            LoggerUtil.error("获取所有用户失败", e);
            System.err.println("获取所有用户失败: " + e.getMessage());
        }
        return userList;
    }

    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE user SET username = ?, password = ? WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新用户失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteUser(String id) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除用户失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 辅助方法：将 ResultSet 映射为 User 对象
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("password")
        );
    }
}
