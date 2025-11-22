package com.example.model.dao.impl;

import com.example.model.dao.RoleDAO;
import com.example.model.entity.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAOImpl extends BaseDAOImpl implements RoleDAO {

    public RoleDAOImpl() {
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
            "CREATE TABLE IF NOT EXISTS role (" +
            "id TEXT PRIMARY KEY, " +
            "name TEXT NOT NULL)";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
            System.out.println("数据表 'role' 检查/创建成功。");
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addRole(Role role) {
        String sql = "INSERT INTO role(id, name) VALUES(?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, role.getId());
            pstmt.setString(2, role.getName());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("添加角色失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Role getRoleById(String id) {
        String sql = "SELECT id, name FROM role WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Role(rs.getString("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("查询角色失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT id, name FROM role";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                roles.add(new Role(rs.getString("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("查询所有角色失败: " + e.getMessage());
        }
        return roles;
    }

    @Override
    public List<Role> getRolesByUserId(String userId) {
        List<Role> roles = new ArrayList<>();

        String sql =
            "SELECT r.id AS role_id, r.name AS role_name " +
            "FROM user_role ur " +
            "INNER JOIN role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(
                        new Role(
                            rs.getString("role_id"),
                            rs.getString("role_name")
                        )
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to query roles by userId = " + userId,
                e
            );
        }

        return roles;
    }

    @Override
    public boolean updateRole(Role role) {
        String sql = "UPDATE role SET name = ? WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, role.getName());
            pstmt.setString(2, role.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新角色失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteRole(String id) {
        String sql = "DELETE FROM role WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除角色失败: " + e.getMessage());
            return false;
        }
    }
}
