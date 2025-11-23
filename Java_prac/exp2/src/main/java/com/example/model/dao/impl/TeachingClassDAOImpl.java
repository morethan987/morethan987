package com.example.model.dao.impl;

import com.example.model.dao.TeachingClassDAO;
import com.example.model.entity.TeachingClass;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeachingClassDAOImpl
    extends BaseDAOImpl
    implements TeachingClassDAO {

    public TeachingClassDAOImpl() {
        super();
        createTable();
        createStudentTeachingClassTable();
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
            "CREATE TABLE IF NOT EXISTS teaching_class (" +
            "class_id TEXT PRIMARY KEY, " +
            "teacher_id TEXT NOT NULL, " +
            "course_id TEXT NOT NULL, " +
            "semester INTEGER, " +
            "name TEXT)";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("创建教学班表失败: " + e.getMessage());
        }
    }

    private void createStudentTeachingClassTable() {
        String sql =
            "CREATE TABLE IF NOT EXISTS student_teaching_class (" +
            "stu_id TEXT NOT NULL, " +
            "class_id TEXT NOT NULL, " +
            "PRIMARY KEY (stu_id, class_id), " +
            "FOREIGN KEY (stu_id) REFERENCES student(stu_id), " +
            "FOREIGN KEY (class_id) REFERENCES teaching_class(class_id))";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("创建学生教学班关联表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addTeachingClass(TeachingClass teachingClass) {
        String sql =
            "INSERT INTO teaching_class(class_id, teacher_id, course_id, semester, name) VALUES(?, ?, ?, ?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, teachingClass.getClassId());
            pstmt.setString(2, teachingClass.getTeacherId());
            pstmt.setString(3, teachingClass.getCourseId());
            if (teachingClass.getSemester() != null) {
                pstmt.setInt(4, teachingClass.getSemester());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setString(5, teachingClass.getName());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("添加教学班失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public TeachingClass getTeachingClassById(String classId) {
        String sql =
            "SELECT class_id, teacher_id, course_id, semester, name FROM teaching_class WHERE class_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, classId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToTeachingClass(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询教学班ID失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<TeachingClass> getTeachingClassesByTeacherId(String teacherId) {
        List<TeachingClass> classList = new ArrayList<>();
        String sql =
            "SELECT class_id, teacher_id, course_id, semester, name FROM teaching_class WHERE teacher_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                classList.add(mapRowToTeachingClass(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据教师ID查询教学班失败: " + e.getMessage());
        }
        return classList;
    }

    @Override
    public List<TeachingClass> getTeachingClassesByCourseId(String courseId) {
        List<TeachingClass> classList = new ArrayList<>();
        String sql =
            "SELECT class_id, teacher_id, course_id, semester, name FROM teaching_class WHERE course_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                classList.add(mapRowToTeachingClass(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据课程ID查询教学班失败: " + e.getMessage());
        }
        return classList;
    }

    @Override
    public List<TeachingClass> getTeachingClassesBySemester(Integer semester) {
        List<TeachingClass> classList = new ArrayList<>();
        String sql =
            "SELECT class_id, teacher_id, course_id, semester, name FROM teaching_class WHERE semester = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, semester);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                classList.add(mapRowToTeachingClass(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据学期查询教学班失败: " + e.getMessage());
        }
        return classList;
    }

    @Override
    public TeachingClass getTeachingClassByName(String name) {
        String sql =
            "SELECT class_id, teacher_id, course_id, semester, name FROM teaching_class WHERE name = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToTeachingClass(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据班级名称查询教学班失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<TeachingClass> getTeachingClassesByTeacherIdAndCourseId(
        String teacherId,
        String courseId
    ) {
        List<TeachingClass> classList = new ArrayList<>();
        String sql =
            "SELECT class_id, teacher_id, course_id, semester, name FROM teaching_class WHERE teacher_id = ? AND course_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, teacherId);
            pstmt.setString(2, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                classList.add(mapRowToTeachingClass(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "根据教师ID和课程ID查询教学班失败: " + e.getMessage()
            );
        }
        return classList;
    }

    @Override
    public List<TeachingClass> getAllTeachingClasses() {
        List<TeachingClass> classList = new ArrayList<>();
        String sql =
            "SELECT class_id, teacher_id, course_id, semester, name FROM teaching_class";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                classList.add(mapRowToTeachingClass(rs));
            }
        } catch (SQLException e) {
            System.err.println("获取所有教学班失败: " + e.getMessage());
        }
        return classList;
    }

    @Override
    public boolean updateTeachingClass(TeachingClass teachingClass) {
        String sql =
            "UPDATE teaching_class SET teacher_id = ?, course_id = ?, semester = ?, name = ? WHERE class_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, teachingClass.getTeacherId());
            pstmt.setString(2, teachingClass.getCourseId());
            if (teachingClass.getSemester() != null) {
                pstmt.setInt(3, teachingClass.getSemester());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setString(4, teachingClass.getName());
            pstmt.setString(5, teachingClass.getClassId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新教学班失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteTeachingClass(String classId) {
        String sql = "DELETE FROM teaching_class WHERE class_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, classId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除教学班失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteTeachingClassesByTeacherId(String teacherId) {
        String sql = "DELETE FROM teaching_class WHERE teacher_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, teacherId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("根据教师ID删除教学班失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteTeachingClassesByCourseId(String courseId) {
        String sql = "DELETE FROM teaching_class WHERE course_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, courseId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("根据课程ID删除教学班失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 辅助方法：将 ResultSet 映射为 TeachingClass 对象
     */
    private TeachingClass mapRowToTeachingClass(ResultSet rs)
        throws SQLException {
        TeachingClass teachingClass = new TeachingClass(
            rs.getString("class_id"),
            rs.getString("teacher_id"),
            rs.getString("course_id")
        );

        int semester = rs.getInt("semester");
        if (!rs.wasNull()) {
            teachingClass.setSemester(semester);
        }

        String name = rs.getString("name");
        if (name != null) {
            teachingClass.setName(name);
        }

        return teachingClass;
    }
}
