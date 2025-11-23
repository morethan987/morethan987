package com.example.model.dao.impl;

import com.example.model.dao.PermissionDAO;
import com.example.model.entity.Permission;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionDAOImpl extends BaseDAOImpl implements PermissionDAO {

    public PermissionDAOImpl() {
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
        // 1. 创建表 SQL
        String sql =
            "CREATE TABLE IF NOT EXISTS permission (" +
            "permission_id TEXT PRIMARY KEY, " +
            "name TEXT NOT NULL)";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("创建表失败: " + e.getMessage());
        }
    }

    public void createRolePermissionTable() {
        String sql =
            "CREATE TABLE IF NOT EXISTS role_permission (" +
            "role_id TEXT NOT NULL, " +
            "permission_id TEXT NOT NULL, " +
            "PRIMARY KEY (role_id, permission_id), " +
            "FOREIGN KEY (role_id) REFERENCES role(id), " +
            "FOREIGN KEY (permission_id) REFERENCES permission(id))";

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
    public boolean addPermission(Permission permission) {
        String sql = "INSERT INTO permission(permission_id, name) VALUES(?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, permission.getPermissionId());
            pstmt.setString(2, permission.getName());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("添加权限失败: " + e.getMessage());
        }
    }

    @Override
    public Permission getPermissionById(String permissionId) {
        String sql =
            "SELECT permission_id, name FROM permission WHERE permission_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, permissionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Permission(
                    rs.getString("permission_id"),
                    rs.getString("name")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询权限失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT permission_id, name FROM permission";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                permissions.add(
                    new Permission(
                        rs.getString("permission_id"),
                        rs.getString("name")
                    )
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询所有权限失败: " + e.getMessage());
        }
        return permissions;
    }

    @Override
    public boolean updatePermission(Permission permission) {
        String sql = "UPDATE permission SET name = ? WHERE permission_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, permission.getName());
            pstmt.setString(2, permission.getPermissionId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("更新权限失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deletePermission(String permissionId) {
        String sql = "DELETE FROM permission WHERE permission_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, permissionId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("删除权限失败: " + e.getMessage());
        }
    }
}
