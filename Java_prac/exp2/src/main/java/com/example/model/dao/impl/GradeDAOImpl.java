package com.example.model.dao.impl;

import com.example.model.dao.GradeDAO;
import com.example.model.entity.Grade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAOImpl extends BaseDAOImpl implements GradeDAO {

    public GradeDAOImpl() {
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
            "CREATE TABLE IF NOT EXISTS grade (" +
            "grade_id TEXT PRIMARY KEY, " +
            "student_id TEXT NOT NULL, " +
            "course_id TEXT NOT NULL, " +
            "usual_score REAL NOT NULL DEFAULT 0.0, " +
            "mid_score REAL NOT NULL DEFAULT 0.0, " +
            "experiment_score REAL NOT NULL DEFAULT 0.0, " +
            "final_exam_score REAL NOT NULL DEFAULT 0.0, " +
            "final_score REAL NOT NULL DEFAULT 0.0, " +
            "UNIQUE(student_id, course_id))";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("创建成绩表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addGrade(Grade grade) {
        String sql =
            "INSERT INTO grade(grade_id, student_id, course_id, usual_score, mid_score, experiment_score, final_exam_score, final_score) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, grade.getGradeId());
            pstmt.setString(2, grade.getStudentId());
            pstmt.setString(3, grade.getCourseId());
            pstmt.setDouble(4, grade.getUsualScore());
            pstmt.setDouble(5, grade.getMidScore());
            pstmt.setDouble(6, grade.getExperimentScore());
            pstmt.setDouble(7, grade.getFinalExamScore());
            pstmt.setDouble(8, grade.getFinalScore());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("添加成绩失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Grade getGradeById(String gradeId) {
        String sql =
            "SELECT grade_id, student_id, course_id, usual_score, mid_score, experiment_score, final_exam_score, final_score FROM grade WHERE grade_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, gradeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToGrade(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询成绩ID失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Grade> getGradesByStudentId(String studentId) {
        List<Grade> gradeList = new ArrayList<>();
        String sql =
            "SELECT grade_id, student_id, course_id, usual_score, mid_score, experiment_score, final_exam_score, final_score FROM grade WHERE student_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                gradeList.add(mapRowToGrade(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据学生ID查询成绩失败: " + e.getMessage());
        }
        return gradeList;
    }

    @Override
    public List<Grade> getGradesByCourseId(String courseId) {
        List<Grade> gradeList = new ArrayList<>();
        String sql =
            "SELECT grade_id, student_id, course_id, usual_score, mid_score, experiment_score, final_exam_score, final_score FROM grade WHERE course_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                gradeList.add(mapRowToGrade(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据课程ID查询成绩失败: " + e.getMessage());
        }
        return gradeList;
    }

    @Override
    public Grade getGradeByStudentIdAndCourseId(
        String studentId,
        String courseId
    ) {
        String sql =
            "SELECT grade_id, student_id, course_id, usual_score, mid_score, experiment_score, final_exam_score, final_score FROM grade WHERE student_id = ? AND course_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, studentId);
            pstmt.setString(2, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToGrade(rs);
            }
        } catch (SQLException e) {
            System.err.println(
                "根据学生ID和课程ID查询成绩失败: " + e.getMessage()
            );
        }
        return null;
    }

    @Override
    public List<Grade> getAllGrades() {
        List<Grade> gradeList = new ArrayList<>();
        String sql =
            "SELECT grade_id, student_id, course_id, usual_score, mid_score, experiment_score, final_exam_score, final_score FROM grade";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                gradeList.add(mapRowToGrade(rs));
            }
        } catch (SQLException e) {
            System.err.println("获取所有成绩失败: " + e.getMessage());
        }
        return gradeList;
    }

    @Override
    public boolean updateGrade(Grade grade) {
        String sql =
            "UPDATE grade SET student_id = ?, course_id = ?, usual_score = ?, mid_score = ?, experiment_score = ?, final_exam_score = ?, final_score = ? WHERE grade_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, grade.getStudentId());
            pstmt.setString(2, grade.getCourseId());
            pstmt.setDouble(3, grade.getUsualScore());
            pstmt.setDouble(4, grade.getMidScore());
            pstmt.setDouble(5, grade.getExperimentScore());
            pstmt.setDouble(6, grade.getFinalExamScore());
            pstmt.setDouble(7, grade.getFinalScore());
            pstmt.setString(8, grade.getGradeId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新成绩失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteGrade(String gradeId) {
        String sql = "DELETE FROM grade WHERE grade_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, gradeId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("删除成绩失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteGradesByStudentId(String studentId) {
        String sql = "DELETE FROM grade WHERE student_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, studentId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("根据学生ID删除成绩失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteGradesByCourseId(String courseId) {
        String sql = "DELETE FROM grade WHERE course_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, courseId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("根据课程ID删除成绩失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 辅助方法：将 ResultSet 映射为 Grade 对象
     */
    private Grade mapRowToGrade(ResultSet rs) throws SQLException {
        return new Grade(
            rs.getString("grade_id"),
            rs.getString("student_id"),
            rs.getString("course_id"),
            rs.getDouble("usual_score"),
            rs.getDouble("mid_score"),
            rs.getDouble("experiment_score"),
            rs.getDouble("final_exam_score")
        );
    }
}
