package com.example.GradeSystemBackend.domain.student;

import static org.junit.jupiter.api.Assertions.*;

import com.example.GradeSystemBackend.domain.auth.User;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Student实体类测试")
public class StudentTest {

    private User mockUser;
    private Student student;

    @BeforeEach
    void setUp() {
        // 创建模拟用户
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("test_student");

        // 创建学生实例
        student = new Student();
    }

    @Test
    @DisplayName("测试Student默认构造函数")
    void testDefaultConstructor() {
        Student newStudent = new Student();

        assertNotNull(newStudent);
        assertEquals(StudentStatus.ENROLLED, newStudent.getStatus());
        assertEquals(Integer.valueOf(2023), newStudent.getEnrollmentYear());
        assertEquals(Integer.valueOf(1), newStudent.getCurrentSemester());
        assertEquals(Double.valueOf(128.0), newStudent.getTotalCredits());
        assertNotNull(newStudent.getCreatedAt());
        assertNotNull(newStudent.getUpdatedAt());
    }

    @Test
    @DisplayName("测试Student带User和studentCode的构造函数")
    void testConstructorWithUserAndStudentCode() {
        String studentCode = "2023001";
        Student newStudent = new Student(mockUser, studentCode);

        assertNotNull(newStudent);
        assertEquals(mockUser, newStudent.getUser());
        assertEquals(studentCode, newStudent.getStudentCode());
        assertEquals(StudentStatus.ENROLLED, newStudent.getStatus());
    }

    @Test
    @DisplayName("测试Student完整构造函数")
    void testFullConstructor() {
        String studentCode = "2023001";
        String major = "计算机科学与技术";
        String className = "计科2023-1班";
        Integer enrollmentYear = 2023;

        Student newStudent = new Student(
            mockUser,
            studentCode,
            major,
            className,
            enrollmentYear
        );

        assertNotNull(newStudent);
        assertEquals(mockUser, newStudent.getUser());
        assertEquals(studentCode, newStudent.getStudentCode());
        assertEquals(major, newStudent.getMajor());
        assertEquals(className, newStudent.getClassName());
        assertEquals(enrollmentYear, newStudent.getEnrollmentYear());
    }

    @Test
    @DisplayName("测试设置和获取学生基本信息")
    void testSettersAndGetters() {
        UUID id = UUID.randomUUID();
        String studentCode = "2023001";
        String major = "软件工程";
        String className = "软工2023-2班";
        Integer enrollmentYear = 2023;
        Integer currentSemester = 3;
        StudentStatus status = StudentStatus.EXCHANGE;
        Double totalCredits = 120.0;
        String advisor = "张教授";
        LocalDateTime expectedGraduationDate = LocalDateTime.of(
            2027,
            6,
            30,
            0,
            0
        );
        LocalDateTime createdAt = LocalDateTime.now().minusDays(100);
        LocalDateTime updatedAt = LocalDateTime.now();

        // 设置所有属性
        student.setId(id);
        student.setUser(mockUser);
        student.setStudentCode(studentCode);
        student.setMajor(major);
        student.setClassName(className);
        student.setEnrollmentYear(enrollmentYear);
        student.setCurrentSemester(currentSemester);
        student.setStatus(status);
        student.setTotalCredits(totalCredits);
        student.setAdvisor(advisor);
        student.setExpectedGraduationDate(expectedGraduationDate);
        student.setCreatedAt(createdAt);
        student.setUpdatedAt(updatedAt);

        // 验证所有属性
        assertEquals(id, student.getId());
        assertEquals(mockUser, student.getUser());
        assertEquals(studentCode, student.getStudentCode());
        assertEquals(major, student.getMajor());
        assertEquals(className, student.getClassName());
        assertEquals(enrollmentYear, student.getEnrollmentYear());
        assertEquals(currentSemester, student.getCurrentSemester());
        assertEquals(status, student.getStatus());
        assertEquals(totalCredits, student.getTotalCredits());
        assertEquals(advisor, student.getAdvisor());
        assertEquals(
            expectedGraduationDate,
            student.getExpectedGraduationDate()
        );
        assertEquals(createdAt, student.getCreatedAt());
        assertEquals(updatedAt, student.getUpdatedAt());
    }

    @Test
    @DisplayName("测试Student toString方法")
    void testToString() {
        student.setStudentCode("2023001");
        student.setMajor("计算机科学与技术");
        student.setClassName("计科2023-1班");
        student.setEnrollmentYear(2023);
        student.setStatus(StudentStatus.ENROLLED);

        String toString = student.toString();
        System.out.println("实际toString结果: " + toString);

        assertNotNull(toString);
        assertTrue(
            toString.contains("2023001"),
            "应该包含学号2023001，实际: " + toString
        );
        assertTrue(
            toString.contains("计算机科学与技术"),
            "应该包含专业，实际: " + toString
        );
        assertTrue(
            toString.contains("计科2023-1班"),
            "应该包含班级，实际: " + toString
        );
        assertTrue(
            toString.contains("2023"),
            "应该包含年份，实际: " + toString
        );
        assertTrue(
            toString.contains("在读"),
            "应该包含状态，实际: " + toString
        );
        assertTrue(toString.startsWith("Student{"));
        assertTrue(toString.endsWith("}"));
    }

    @Test
    @DisplayName("测试Student equals方法 - 相同学号")
    void testEqualsWithSameStudentCode() {
        String studentCode = "2023001";

        Student student1 = new Student();
        student1.setStudentCode(studentCode);

        Student student2 = new Student();
        student2.setStudentCode(studentCode);

        assertEquals(student1, student2);
        assertEquals(student1.hashCode(), student2.hashCode());
    }

    @Test
    @DisplayName("测试Student equals方法 - 不同学号")
    void testEqualsWithDifferentStudentCode() {
        Student student1 = new Student();
        student1.setStudentCode("2023001");

        Student student2 = new Student();
        student2.setStudentCode("2023002");

        assertNotEquals(student1, student2);
    }

    @Test
    @DisplayName("测试Student equals方法 - null和不同类型对象")
    void testEqualsWithNullAndDifferentType() {
        student.setStudentCode("2023001");

        assertNotEquals(student, null);
        assertNotEquals(student, "不是Student对象");
        assertEquals(student, student); // 自己等于自己
    }

    @Test
    @DisplayName("测试Student hashCode方法")
    void testHashCode() {
        String studentCode = "2023001";
        student.setStudentCode(studentCode);

        assertEquals(studentCode.hashCode(), student.hashCode());
    }

    @Test
    @DisplayName("测试preUpdate方法")
    void testPreUpdate() {
        LocalDateTime beforeUpdate = LocalDateTime.now().minusMinutes(1);
        student.setUpdatedAt(beforeUpdate);

        // 模拟@PreUpdate注解的行为
        student.preUpdate();

        assertTrue(student.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    @DisplayName("测试学生状态枚举值")
    void testStudentStatusEnum() {
        // 测试所有可能的学生状态
        student.setStatus(StudentStatus.ENROLLED);
        assertEquals(StudentStatus.ENROLLED, student.getStatus());

        student.setStatus(StudentStatus.WITHDRAWN);
        assertEquals(StudentStatus.WITHDRAWN, student.getStatus());

        student.setStatus(StudentStatus.GRADUATED);
        assertEquals(StudentStatus.GRADUATED, student.getStatus());

        student.setStatus(StudentStatus.DEFERRED);
        assertEquals(StudentStatus.DEFERRED, student.getStatus());

        student.setStatus(StudentStatus.EXCHANGE);
        assertEquals(StudentStatus.EXCHANGE, student.getStatus());

        student.setStatus(StudentStatus.TRANSFERRED);
        assertEquals(StudentStatus.TRANSFERRED, student.getStatus());

        student.setStatus(StudentStatus.EXPELLED);
        assertEquals(StudentStatus.EXPELLED, student.getStatus());
    }

    @Test
    @DisplayName("测试学生信息验证")
    void testStudentValidation() {
        // 测试学号长度限制（实际应该通过Bean Validation来验证）
        String longStudentCode = "a".repeat(25); // 超过20字符限制
        student.setStudentCode(longStudentCode);
        assertEquals(longStudentCode, student.getStudentCode());

        // 测试专业长度限制
        String longMajor = "a".repeat(105); // 超过100字符限制
        student.setMajor(longMajor);
        assertEquals(longMajor, student.getMajor());

        // 测试班级名称长度限制
        String longClassName = "a".repeat(55); // 超过50字符限制
        student.setClassName(longClassName);
        assertEquals(longClassName, student.getClassName());
    }

    @Test
    @DisplayName("测试学期和年份的边界值")
    void testSemesterAndYearBoundaries() {
        // 测试学期边界值
        student.setCurrentSemester(1);
        assertEquals(Integer.valueOf(1), student.getCurrentSemester());

        student.setCurrentSemester(8);
        assertEquals(Integer.valueOf(8), student.getCurrentSemester());

        // 测试入学年份
        student.setEnrollmentYear(2020);
        assertEquals(Integer.valueOf(2020), student.getEnrollmentYear());

        student.setEnrollmentYear(2025);
        assertEquals(Integer.valueOf(2025), student.getEnrollmentYear());
    }

    @Test
    @DisplayName("测试学分设置")
    void testTotalCreditsSettings() {
        // 测试不同的学分值
        student.setTotalCredits(0.0);
        assertEquals(Double.valueOf(0.0), student.getTotalCredits());

        student.setTotalCredits(160.5);
        assertEquals(Double.valueOf(160.5), student.getTotalCredits());

        student.setTotalCredits(null);
        assertNull(student.getTotalCredits());
    }
}
