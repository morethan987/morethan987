package com.example.model.dao.impl;

import com.example.model.dao.TeacherDAO;
import com.example.model.entity.Teacher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAOImpl extends BaseDAOImpl implements TeacherDAO {

    public TeacherDAOImpl() {
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
        // 创建表结构：id (PK), name, gender, age
        String sql =
            "CREATE TABLE IF NOT EXISTS teacher (" +
            "id TEXT PRIMARY KEY, " +
            "name TEXT NOT NULL, " +
            "gender TEXT, " +
            "age INTEGER)";

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
    public boolean addTeacher(Teacher teacher) {
        String sql =
            "INSERT INTO teacher(id, name, gender, age) VALUES(?, ?, ?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, teacher.getId());
            pstmt.setString(2, teacher.getName());
            pstmt.setString(3, teacher.getGender());
            // 处理 Integer age，如果为 null 则存 NULL，否则存值
            if (teacher.getAge() == null) {
                pstmt.setNull(4, Types.INTEGER);
            } else {
                pstmt.setInt(4, teacher.getAge());
            }

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("添加教师失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Teacher getTeacherById(String id) {
        String sql = "SELECT id, name, gender, age FROM teacher WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTeacher(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询教师失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT id, name, gender, age FROM teacher";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                teachers.add(mapResultSetToTeacher(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有教师失败: " + e.getMessage());
        }
        return teachers;
    }

    @Override
    public boolean updateTeacher(Teacher teacher) {
        String sql =
            "UPDATE teacher SET name = ?, gender = ?, age = ? WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, teacher.getName());
            pstmt.setString(2, teacher.getGender());

            if (teacher.getAge() == null) {
                pstmt.setNull(3, Types.INTEGER);
            } else {
                pstmt.setInt(3, teacher.getAge());
            }

            pstmt.setString(4, teacher.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新教师失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteTeacher(String id) {
        String sql = "DELETE FROM teacher WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除教师失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 辅助方法：将 ResultSet 的当前行映射为 Teacher 对象
     */
    private Teacher mapResultSetToTeacher(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String gender = rs.getString("gender");

        // getInt 如果遇到数据库 NULL，会返回 0，所以需要判断 wasNull
        int ageVal = rs.getInt("age");
        Integer age = rs.wasNull() ? null : ageVal;

        return new Teacher(id, name, gender, age);
    }
}
