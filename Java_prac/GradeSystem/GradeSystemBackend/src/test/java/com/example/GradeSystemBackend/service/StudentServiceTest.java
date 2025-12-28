package com.example.GradeSystemBackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.student.StudentStatus;
import com.example.GradeSystemBackend.dto.StudentDTO;
import com.example.GradeSystemBackend.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService单元测试")
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student mockStudent;
    private User mockUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        // 创建模拟用户
        mockUser = new User();
        mockUser.setId(testUserId);
        mockUser.setUsername("test_student");

        // 创建模拟学生
        mockStudent = new Student();
        mockStudent.setId(UUID.randomUUID());
        mockStudent.setUser(mockUser);
        mockStudent.setStudentCode("2023001");
        mockStudent.setMajor("计算机科学与技术");
        mockStudent.setClassName("计科2023-1班");
        mockStudent.setEnrollmentYear(2023);
        mockStudent.setCurrentSemester(1);
        mockStudent.setStatus(StudentStatus.ENROLLED);
        mockStudent.setTotalCredits(128.0);
        mockStudent.setAdvisor("张教授");
        mockStudent.setCreatedAt(LocalDateTime.now());
        mockStudent.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试获取学生总数")
    void testGetTotalStudents() {
        // 准备测试数据
        long expectedCount = 150L;
        when(studentRepository.count()).thenReturn(expectedCount);

        // 执行测试
        long actualCount = studentService.getTotalStudents();

        // 验证结果
        assertEquals(expectedCount, actualCount);
        verify(studentRepository, times(1)).count();
    }

    @Test
    @DisplayName("测试通过用户ID获取学生信息 - 成功")
    void testGetStudentByUserIdSuccess() {
        // 准备测试数据
        when(studentRepository.findByUserId(testUserId)).thenReturn(
            Optional.of(mockStudent)
        );

        // 执行测试
        StudentDTO result = studentService.getStudentByUserId(testUserId);

        // 验证结果
        assertNotNull(result);
        assertEquals(mockStudent.getId(), result.getId());
        assertEquals(mockStudent.getStudentCode(), result.getStudentCode());
        assertEquals(mockStudent.getMajor(), result.getMajor());
        assertEquals(mockStudent.getClassName(), result.getClassName());
        assertEquals(
            mockStudent.getEnrollmentYear(),
            result.getEnrollmentYear()
        );
        assertEquals(
            mockStudent.getCurrentSemester(),
            result.getCurrentSemester()
        );
        assertEquals(mockStudent.getStatus(), result.getStatus());
        assertEquals(mockStudent.getTotalCredits(), result.getTotalCredits());
        assertEquals(mockStudent.getAdvisor(), result.getAdvisor());
        assertEquals(
            mockStudent.getExpectedGraduationDate(),
            result.getExpectedGraduationDate()
        );
        assertEquals(mockStudent.getCreatedAt(), result.getCreatedAt());
        assertEquals(mockStudent.getUpdatedAt(), result.getUpdatedAt());

        verify(studentRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("测试通过用户ID获取学生信息 - 学生不存在")
    void testGetStudentByUserIdNotFound() {
        // 准备测试数据
        UUID nonExistentUserId = UUID.randomUUID();
        when(studentRepository.findByUserId(nonExistentUserId)).thenReturn(
            Optional.empty()
        );

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> {
                studentService.getStudentByUserId(nonExistentUserId);
            }
        );

        // 验证异常信息
        assertTrue(
            exception
                .getMessage()
                .contains(
                    "Student not found with user id: " + nonExistentUserId
                )
        );
        verify(studentRepository, times(1)).findByUserId(nonExistentUserId);
    }

    @Test
    @DisplayName("测试通过用户ID获取学生信息 - 空用户ID")
    void testGetStudentByUserIdWithNullUserId() {
        // 准备测试数据
        when(studentRepository.findByUserId(null)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> {
                studentService.getStudentByUserId(null);
            }
        );

        // 验证异常信息
        assertTrue(
            exception
                .getMessage()
                .contains("Student not found with user id: null")
        );
        verify(studentRepository, times(1)).findByUserId(null);
    }

    @Test
    @DisplayName("测试StudentRepository交互次数")
    void testRepositoryInteractionCount() {
        // 准备测试数据
        when(studentRepository.count()).thenReturn(100L);
        when(studentRepository.findByUserId(testUserId)).thenReturn(
            Optional.of(mockStudent)
        );

        // 执行多次调用
        studentService.getTotalStudents();
        studentService.getTotalStudents();
        studentService.getStudentByUserId(testUserId);

        // 验证调用次数
        verify(studentRepository, times(2)).count();
        verify(studentRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("测试获取学生总数 - 边界情况")
    void testGetTotalStudentsBoundaryValues() {
        // 测试零学生
        when(studentRepository.count()).thenReturn(0L);
        assertEquals(0L, studentService.getTotalStudents());

        // 测试大量学生
        when(studentRepository.count()).thenReturn(999999L);
        assertEquals(999999L, studentService.getTotalStudents());

        verify(studentRepository, times(2)).count();
    }

    @Test
    @DisplayName("测试学生DTO转换的完整性")
    void testStudentDTOConversionCompleteness() {
        // 设置完整的学生信息
        LocalDateTime expectedGraduation = LocalDateTime.of(2027, 6, 30, 0, 0);
        mockStudent.setExpectedGraduationDate(expectedGraduation);

        when(studentRepository.findByUserId(testUserId)).thenReturn(
            Optional.of(mockStudent)
        );

        // 执行测试
        StudentDTO result = studentService.getStudentByUserId(testUserId);

        // 验证所有字段都被正确转换
        assertNotNull(result);
        assertEquals(mockStudent.getId(), result.getId());
        assertEquals(mockStudent.getStudentCode(), result.getStudentCode());
        assertEquals(mockStudent.getMajor(), result.getMajor());
        assertEquals(mockStudent.getClassName(), result.getClassName());
        assertEquals(
            mockStudent.getEnrollmentYear(),
            result.getEnrollmentYear()
        );
        assertEquals(
            mockStudent.getCurrentSemester(),
            result.getCurrentSemester()
        );
        assertEquals(mockStudent.getStatus(), result.getStatus());
        assertEquals(mockStudent.getTotalCredits(), result.getTotalCredits());
        assertEquals(mockStudent.getAdvisor(), result.getAdvisor());
        assertEquals(expectedGraduation, result.getExpectedGraduationDate());
        assertEquals(mockStudent.getCreatedAt(), result.getCreatedAt());
        assertEquals(mockStudent.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    @DisplayName("测试不同学生状态的处理")
    void testDifferentStudentStatuses() {
        // 测试不同的学生状态
        StudentStatus[] statuses = {
            StudentStatus.ENROLLED,
            StudentStatus.WITHDRAWN,
            StudentStatus.GRADUATED,
            StudentStatus.DEFERRED,
            StudentStatus.EXCHANGE,
            StudentStatus.TRANSFERRED,
            StudentStatus.EXPELLED,
        };

        for (StudentStatus status : statuses) {
            mockStudent.setStatus(status);
            when(studentRepository.findByUserId(testUserId)).thenReturn(
                Optional.of(mockStudent)
            );

            StudentDTO result = studentService.getStudentByUserId(testUserId);

            assertEquals(status, result.getStatus());
        }
    }

    @Test
    @DisplayName("测试学生信息为空值的情况")
    void testStudentWithNullValues() {
        // 创建包含null值的学生
        Student studentWithNulls = new Student();
        studentWithNulls.setId(UUID.randomUUID());
        studentWithNulls.setUser(mockUser);
        studentWithNulls.setStudentCode("2023002");
        studentWithNulls.setMajor(null);
        studentWithNulls.setClassName(null);
        studentWithNulls.setAdvisor(null);
        studentWithNulls.setExpectedGraduationDate(null);

        when(studentRepository.findByUserId(testUserId)).thenReturn(
            Optional.of(studentWithNulls)
        );

        // 执行测试
        StudentDTO result = studentService.getStudentByUserId(testUserId);

        // 验证null值被正确处理
        assertNotNull(result);
        assertEquals(
            studentWithNulls.getStudentCode(),
            result.getStudentCode()
        );
        assertNull(result.getMajor());
        assertNull(result.getClassName());
        assertNull(result.getAdvisor());
        assertNull(result.getExpectedGraduationDate());
    }
}
