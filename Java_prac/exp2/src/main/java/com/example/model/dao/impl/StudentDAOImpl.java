package com.example.model.dao.impl;

import com.example.model.dao.StudentDAO;
import com.example.model.dto.BinaryMessage;
import com.example.model.entity.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生DAO类具体实现
 * 使用SQLite数据库
 */
public class StudentDAOImpl extends BaseDAOImpl implements StudentDAO {

    /**
     * 构造函数，初始化数据库和表
     */
    public StudentDAOImpl() {
        super();
        createTable();
        createStudentTeachingClassTable();
    }

    /**
     * 初始化数据库，创建学生表
     */
    private void createTable() {
        String createTableSQL =
            "CREATE TABLE IF NOT EXISTS student (" +
            "stu_id TEXT PRIMARY KEY, " +
            "stu_name TEXT NOT NULL, " +
            "gender TEXT, " +
            "age INTEGER" +
            ")";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
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

    /**
     * 获取数据库连接
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    @Override
    public BinaryMessage addStudent(Student student) {
        if (student == null) {
            return new BinaryMessage(false, "学生对象不能为空");
        }

        if (student.getID() == null || student.getID().trim().isEmpty()) {
            return new BinaryMessage(false, "学生ID不能为空");
        }

        String sql =
            "INSERT INTO student" +
            " (stu_id, stu_name, gender, age) VALUES (?, ?, ?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, student.getID());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getGender());
            pstmt.setObject(4, student.getAge());

            pstmt.executeUpdate();
            return new BinaryMessage(true, "学生添加成功");
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return new BinaryMessage(false, "学生ID已存在，添加失败");
            }
            return new BinaryMessage(false, "添加失败: " + e.getMessage());
        }
    }

    @Override
    public BinaryMessage deleteStudentById(String stuId) {
        if (stuId == null || stuId.trim().isEmpty()) {
            return new BinaryMessage(false, "学生ID不能为空");
        }

        String sql = "DELETE FROM student WHERE stu_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, stuId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return new BinaryMessage(true, "学生删除成功");
            } else {
                return new BinaryMessage(false, "学生ID不存在，删除失败");
            }
        } catch (SQLException e) {
            return new BinaryMessage(false, "删除失败: " + e.getMessage());
        }
    }

    @Override
    public BinaryMessage updateStudent(Student student) {
        if (student == null) {
            return new BinaryMessage(false, "学生对象不能为空");
        }

        if (student.getID() == null || student.getID().trim().isEmpty()) {
            return new BinaryMessage(false, "学生ID不能为空");
        }

        String sql =
            "UPDATE student SET stu_name = ?, gender = ?, age = ? WHERE stu_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getGender());
            pstmt.setObject(3, student.getAge());
            pstmt.setString(4, student.getID());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return new BinaryMessage(true, "学生信息更新成功");
            } else {
                return new BinaryMessage(false, "学生ID不存在，更新失败");
            }
        } catch (SQLException e) {
            return new BinaryMessage(false, "更新失败: " + e.getMessage());
        }
    }

    @Override
    public Student findStudentById(String stuId) {
        if (stuId == null || stuId.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM student WHERE stu_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, stuId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Student> findAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM student";

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public List<Student> findStudentsByName(String name) {
        List<Student> students = new ArrayList<>();

        if (name == null || name.trim().isEmpty()) {
            return students;
        }

        String sql = "SELECT * FROM student WHERE stu_name = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public List<Student> findStudentsByGender(String gender) {
        List<Student> students = new ArrayList<>();

        if (gender == null || gender.trim().isEmpty()) {
            return students;
        }

        String sql = "SELECT * FROM student WHERE gender = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, gender);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public boolean existsById(String stuId) {
        if (stuId == null || stuId.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM student WHERE stu_id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, stuId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 从ResultSet中提取Student对象
     */
    private Student extractStudentFromResultSet(ResultSet rs)
        throws SQLException {
        String stuId = rs.getString("stu_id");
        String stuName = rs.getString("stu_name");
        String gender = rs.getString("gender");
        Integer age = rs.getObject("age", Integer.class);

        return new Student(stuId, stuName, gender, age);
    }
}
