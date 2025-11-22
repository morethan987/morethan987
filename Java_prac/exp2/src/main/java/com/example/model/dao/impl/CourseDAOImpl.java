package com.example.model.dao.impl;

import com.example.model.dao.CourseDAO;
import com.example.model.entity.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAOImpl extends BaseDAOImpl implements CourseDAO {

    public CourseDAOImpl() {
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
        // 创建表结构：id (PK), name, description
        String sql =
            "CREATE TABLE IF NOT EXISTS course (" +
            "id TEXT PRIMARY KEY, " +
            "name TEXT NOT NULL, " +
            "description TEXT)";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
            System.out.println("数据表 'course' 检查/创建成功。");
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addCourse(Course course) {
        String sql =
            "INSERT INTO course(id, name, description) VALUES(?, ?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, course.getId());
            pstmt.setString(2, course.getName());
            pstmt.setString(3, course.getDescription());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("添加课程失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Course getCourseById(String id) {
        String sql = "SELECT id, name, description FROM course WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCourse(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询课程失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT id, name, description FROM course";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有课程失败: " + e.getMessage());
        }
        return courses;
    }

    @Override
    public boolean updateCourse(Course course) {
        String sql = "UPDATE course SET name = ?, description = ? WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getDescription());
            pstmt.setString(3, course.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新课程失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteCourse(String id) {
        String sql = "DELETE FROM course WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除课程失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 辅助方法：将 ResultSet 的当前行映射为 Course 对象
     */
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String description = rs.getString("description");

        return new Course(id, name, description);
    }
}
