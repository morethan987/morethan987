package com.example.GradeSystemBackend.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.student.StudentStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StudentDTO单元测试")
public class StudentDTOTest {

    private Student mockStudent;
    private User mockUser;
    private UUID testId;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testDateTime = LocalDateTime.now();

        // 创建模拟用户
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("test_student");

        // 创建模拟学生
        mockStudent = new Student();
        mockStudent.setId(testId);
        mockStudent.setUser(mockUser);
        mockStudent.setStudentCode("2023001");
        mockStudent.setMajor("计算机科学与技术");
        mockStudent.setClassName("计科2023-1班");
        mockStudent.setEnrollmentYear(2023);
        mockStudent.setCurrentSemester(3);
        mockStudent.setStatus(StudentStatus.ENROLLED);
        mockStudent.setTotalCredits(120.5);
        mockStudent.setAdvisor("张教授");
        mockStudent.setExpectedGraduationDate(testDateTime.plusYears(4));
        mockStudent.setCreatedAt(testDateTime);
        mockStudent.setUpdatedAt(testDateTime);
    }

    @Test
    @DisplayName("测试StudentDTO默认构造函数")
    void testDefaultConstructor() {
        StudentDTO dto = new StudentDTO();

        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getStudentCode());
        assertNull(dto.getMajor());
        assertNull(dto.getClassName());
        assertNull(dto.getEnrollmentYear());
        assertNull(dto.getCurrentSemester());
        assertNull(dto.getStatus());
        assertNull(dto.getTotalCredits());
        assertNull(dto.getAdvisor());
        assertNull(dto.getExpectedGraduationDate());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("测试从Student实体创建StudentDTO")
    void testConstructorFromStudent() {
        StudentDTO dto = new StudentDTO(mockStudent);

        assertNotNull(dto);
        assertEquals(mockStudent.getId(), dto.getId());
        assertEquals(mockStudent.getStudentCode(), dto.getStudentCode());
        assertEquals(mockStudent.getMajor(), dto.getMajor());
        assertEquals(mockStudent.getClassName(), dto.getClassName());
        assertEquals(mockStudent.getEnrollmentYear(), dto.getEnrollmentYear());
        assertEquals(
            mockStudent.getCurrentSemester(),
            dto.getCurrentSemester()
        );
        assertEquals(mockStudent.getStatus(), dto.getStatus());
        assertEquals(mockStudent.getTotalCredits(), dto.getTotalCredits());
        assertEquals(mockStudent.getAdvisor(), dto.getAdvisor());
        assertEquals(
            mockStudent.getExpectedGraduationDate(),
            dto.getExpectedGraduationDate()
        );
        assertEquals(mockStudent.getCreatedAt(), dto.getCreatedAt());
        assertEquals(mockStudent.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    @DisplayName("测试StudentDTO的所有setter和getter方法")
    void testSettersAndGetters() {
        StudentDTO dto = new StudentDTO();

        // 设置所有属性
        UUID id = UUID.randomUUID();
        String studentCode = "2024001";
        String major = "软件工程";
        String className = "软工2024-2班";
        Integer enrollmentYear = 2024;
        Integer currentSemester = 2;
        StudentStatus status = StudentStatus.EXCHANGE;
        Double totalCredits = 100.0;
        String advisor = "李教授";
        LocalDateTime expectedGraduationDate = LocalDateTime.of(
            2028,
            6,
            30,
            0,
            0
        );
        LocalDateTime createdAt = LocalDateTime.now().minusDays(30);
        LocalDateTime updatedAt = LocalDateTime.now();

        dto.setId(id);
        dto.setStudentCode(studentCode);
        dto.setMajor(major);
        dto.setClassName(className);
        dto.setEnrollmentYear(enrollmentYear);
        dto.setCurrentSemester(currentSemester);
        dto.setStatus(status);
        dto.setTotalCredits(totalCredits);
        dto.setAdvisor(advisor);
        dto.setExpectedGraduationDate(expectedGraduationDate);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        // 验证所有属性
        assertEquals(id, dto.getId());
        assertEquals(studentCode, dto.getStudentCode());
        assertEquals(major, dto.getMajor());
        assertEquals(className, dto.getClassName());
        assertEquals(enrollmentYear, dto.getEnrollmentYear());
        assertEquals(currentSemester, dto.getCurrentSemester());
        assertEquals(status, dto.getStatus());
        assertEquals(totalCredits, dto.getTotalCredits());
        assertEquals(advisor, dto.getAdvisor());
        assertEquals(expectedGraduationDate, dto.getExpectedGraduationDate());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("测试处理null值的Student实体")
    void testConstructorFromStudentWithNullValues() {
        // 创建包含null值的学生
        Student studentWithNulls = new Student();
        studentWithNulls.setId(testId);
        studentWithNulls.setStudentCode("2023999");
        studentWithNulls.setMajor(null);
        studentWithNulls.setClassName(null);
        studentWithNulls.setEnrollmentYear(2023);
        studentWithNulls.setCurrentSemester(1);
        studentWithNulls.setStatus(StudentStatus.ENROLLED);
        studentWithNulls.setTotalCredits(null);
        studentWithNulls.setAdvisor(null);
        studentWithNulls.setExpectedGraduationDate(null);
        studentWithNulls.setCreatedAt(testDateTime);
        studentWithNulls.setUpdatedAt(testDateTime);

        StudentDTO dto = new StudentDTO(studentWithNulls);

        assertNotNull(dto);
        assertEquals(studentWithNulls.getId(), dto.getId());
        assertEquals(studentWithNulls.getStudentCode(), dto.getStudentCode());
        assertNull(dto.getMajor());
        assertNull(dto.getClassName());
        assertEquals(
            studentWithNulls.getEnrollmentYear(),
            dto.getEnrollmentYear()
        );
        assertEquals(
            studentWithNulls.getCurrentSemester(),
            dto.getCurrentSemester()
        );
        assertEquals(studentWithNulls.getStatus(), dto.getStatus());
        assertNull(dto.getTotalCredits());
        assertNull(dto.getAdvisor());
        assertNull(dto.getExpectedGraduationDate());
        assertEquals(studentWithNulls.getCreatedAt(), dto.getCreatedAt());
        assertEquals(studentWithNulls.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    @DisplayName("测试所有学生状态枚举的处理")
    void testAllStudentStatusEnums() {
        StudentStatus[] allStatuses = StudentStatus.values();

        for (StudentStatus status : allStatuses) {
            mockStudent.setStatus(status);
            StudentDTO dto = new StudentDTO(mockStudent);

            assertEquals(status, dto.getStatus());
        }
    }

    @Test
    @DisplayName("测试数值类型的边界值")
    void testNumericBoundaryValues() {
        StudentDTO dto = new StudentDTO();

        // 测试学期边界值
        dto.setCurrentSemester(1);
        assertEquals(Integer.valueOf(1), dto.getCurrentSemester());

        dto.setCurrentSemester(8);
        assertEquals(Integer.valueOf(8), dto.getCurrentSemester());

        // 测试入学年份
        dto.setEnrollmentYear(1990);
        assertEquals(Integer.valueOf(1990), dto.getEnrollmentYear());

        dto.setEnrollmentYear(2030);
        assertEquals(Integer.valueOf(2030), dto.getEnrollmentYear());

        // 测试学分
        dto.setTotalCredits(0.0);
        assertEquals(Double.valueOf(0.0), dto.getTotalCredits());

        dto.setTotalCredits(300.5);
        assertEquals(Double.valueOf(300.5), dto.getTotalCredits());
    }

    @Test
    @DisplayName("测试时间字段的处理")
    void testDateTimeFields() {
        StudentDTO dto = new StudentDTO();

        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59);

        dto.setCreatedAt(pastDate);
        dto.setUpdatedAt(futureDate);
        dto.setExpectedGraduationDate(futureDate);

        assertEquals(pastDate, dto.getCreatedAt());
        assertEquals(futureDate, dto.getUpdatedAt());
        assertEquals(futureDate, dto.getExpectedGraduationDate());
    }

    @Test
    @DisplayName("测试字符串字段的长度处理")
    void testStringFieldLengths() {
        StudentDTO dto = new StudentDTO();

        // 测试正常长度的字符串
        String normalStudentCode = "2023001";
        String normalMajor = "计算机科学与技术";
        String normalClassName = "计科2023-1班";
        String normalAdvisor = "张教授";

        dto.setStudentCode(normalStudentCode);
        dto.setMajor(normalMajor);
        dto.setClassName(normalClassName);
        dto.setAdvisor(normalAdvisor);

        assertEquals(normalStudentCode, dto.getStudentCode());
        assertEquals(normalMajor, dto.getMajor());
        assertEquals(normalClassName, dto.getClassName());
        assertEquals(normalAdvisor, dto.getAdvisor());

        // 测试长字符串
        String longMajor = "a".repeat(100);
        String longClassName = "b".repeat(50);
        String longAdvisor = "c".repeat(100);

        dto.setMajor(longMajor);
        dto.setClassName(longClassName);
        dto.setAdvisor(longAdvisor);

        assertEquals(longMajor, dto.getMajor());
        assertEquals(longClassName, dto.getClassName());
        assertEquals(longAdvisor, dto.getAdvisor());
    }

    @Test
    @DisplayName("测试DTO的不可变性（数据传输对象特性）")
    void testDTOImmutabilityCharacteristics() {
        StudentDTO dto1 = new StudentDTO(mockStudent);
        StudentDTO dto2 = new StudentDTO(mockStudent);

        // DTO应该是数据的快照，修改原始Student不应影响已创建的DTO
        String originalStudentCode = dto1.getStudentCode();
        mockStudent.setStudentCode("MODIFIED");

        assertEquals(originalStudentCode, dto1.getStudentCode());
        assertEquals(originalStudentCode, dto2.getStudentCode());
    }

    @Test
    @DisplayName("测试空字符串和空白字符串的处理")
    void testEmptyAndBlankStrings() {
        StudentDTO dto = new StudentDTO();

        // 测试空字符串
        dto.setStudentCode("");
        dto.setMajor("");
        dto.setClassName("");
        dto.setAdvisor("");

        assertEquals("", dto.getStudentCode());
        assertEquals("", dto.getMajor());
        assertEquals("", dto.getClassName());
        assertEquals("", dto.getAdvisor());

        // 测试空白字符串
        dto.setStudentCode("   ");
        dto.setMajor("\t");
        dto.setClassName("\n");
        dto.setAdvisor(" \t\n ");

        assertEquals("   ", dto.getStudentCode());
        assertEquals("\t", dto.getMajor());
        assertEquals("\n", dto.getClassName());
        assertEquals(" \t\n ", dto.getAdvisor());
    }
}
