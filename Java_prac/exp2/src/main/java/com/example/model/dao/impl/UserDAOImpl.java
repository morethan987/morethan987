package com.example.model.dao.impl;

import com.example.model.dao.UserDAO;
import com.example.model.entity.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl extends BaseDAOImpl implements UserDAO {

    public UserDAOImpl() {
        super();
        createTable();
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

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addUser(User user) {
        String sql = "INSERT INTO user(id, username, password) VALUES(?, ?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword()); // 注意：实际项目中密码应当加密存储

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("添加用户失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User getUserById(String id) {
        String sql = "SELECT id, username, password FROM user WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询用户ID失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) {
        String sql =
            "SELECT id, username, password FROM user WHERE username = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询用户名失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
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
        } catch (SQLException e) {
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
