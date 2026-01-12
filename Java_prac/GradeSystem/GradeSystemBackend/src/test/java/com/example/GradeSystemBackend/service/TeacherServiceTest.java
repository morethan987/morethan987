package com.example.GradeSystemBackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.course.CourseType;
import com.example.GradeSystemBackend.domain.grade.Grade;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.teacher.Teacher;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import com.example.GradeSystemBackend.dto.BatchGradeUpdateDTO;
import com.example.GradeSystemBackend.dto.DistributionDataDTO;
import com.example.GradeSystemBackend.dto.StudentGradeInputDTO;
import com.example.GradeSystemBackend.dto.TeachingClassWithStatsDTO;
import com.example.GradeSystemBackend.repository.GradeRepository;
import com.example.GradeSystemBackend.repository.StudentRepository;
import com.example.GradeSystemBackend.repository.TeacherRepository;
import com.example.GradeSystemBackend.repository.TeachingClassRepository;
import com.example.GradeSystemBackend.repository.UserProfileRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
@DisplayName("TeacherService单元测试")
public class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeachingClassRepository teachingClassRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private TeacherService teacherService;

    private UUID teacherId;
    private UUID teachingClassId;
    private UUID courseId;
    private Teacher mockTeacher;
    private TeachingClass mockTeachingClass;
    private Course mockCourse;
    private Student mockStudent;
    private Grade mockGrade;
    private User mockUser;
    private UserProfile mockUserProfile;

    @BeforeEach
    void setUp() {
        teacherId = UUID.randomUUID();
        teachingClassId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("test_student");

        mockUserProfile = new UserProfile();
        mockUserProfile.setId(UUID.randomUUID());
        mockUserProfile.setUser(mockUser);
        mockUserProfile.setRealName("张三");

        mockTeacher = new Teacher();
        mockTeacher.setId(teacherId);

        mockCourse = new Course();
        mockCourse.setId(courseId);
        mockCourse.setName("高等数学");
        mockCourse.setCourseType(CourseType.REQUIRED);
        mockCourse.setCredit(4.0);
        mockCourse.setSemester(1);

        mockTeachingClass = new TeachingClass();
        mockTeachingClass.setId(teachingClassId);
        mockTeachingClass.setName("高数01班");
        mockTeachingClass.setTeacher(mockTeacher);
        mockTeachingClass.setCourse(mockCourse);
        mockTeachingClass.setClassroom("教学楼A101");
        mockTeachingClass.setTimeSchedule("周一3-4节");
        mockTeachingClass.setStudents(new HashSet<>());

        mockStudent = new Student();
        mockStudent.setId(UUID.randomUUID());
        mockStudent.setUser(mockUser);
        mockStudent.setStudentCode("2023001");
        mockStudent.setClassName("计科2023-1班");

        mockGrade = new Grade();
        mockGrade.setId(UUID.randomUUID());
        mockGrade.setStudent(mockStudent);
        mockGrade.setCourse(mockCourse);
        mockGrade.setUsualScore(85.0);
        mockGrade.setMidScore(80.0);
        mockGrade.setExperimentScore(90.0);
        mockGrade.setFinalExamScore(75.0);
        mockGrade.setVersion(1L);
    }

    @Test
    @DisplayName("获取教师教学班列表 - 成功")
    void testGetTeachingClassesSuccess() {
        List<TeachingClass> teachingClasses = new ArrayList<>();
        teachingClasses.add(mockTeachingClass);

        when(teachingClassRepository.findByTeacherId(teacherId))
            .thenReturn(teachingClasses);

        List<TeachingClassWithStatsDTO> result =
            teacherService.getTeachingClasses(teacherId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(teachingClassId, result.get(0).getId());
        assertEquals("高数01班", result.get(0).getClassName());
        assertEquals("高等数学", result.get(0).getCourseName());
        assertEquals(CourseType.REQUIRED, result.get(0).getCourseType());
        assertEquals(4.0, result.get(0).getCredit());
        assertEquals("教学楼A101", result.get(0).getLocation());
        assertEquals("周一3-4节", result.get(0).getSchedule());

        verify(teachingClassRepository, times(1)).findByTeacherId(teacherId);
    }

    @Test
    @DisplayName("获取教师教学班列表 - 空列表")
    void testGetTeachingClassesEmpty() {
        when(teachingClassRepository.findByTeacherId(teacherId))
            .thenReturn(new ArrayList<>());

        List<TeachingClassWithStatsDTO> result =
            teacherService.getTeachingClasses(teacherId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(teachingClassRepository, times(1)).findByTeacherId(teacherId);
    }

    @Test
    @DisplayName("获取教学班学生列表 - 成功")
    void testGetStudentsInTeachingClassSuccess() {
        List<Student> students = new ArrayList<>();
        students.add(mockStudent);

        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.of(mockTeachingClass));
        when(studentRepository.findByTeachingClassId(teachingClassId))
            .thenReturn(students);
        when(gradeRepository.findByStudentAndCourse(mockStudent, mockCourse))
            .thenReturn(Optional.of(mockGrade));
        when(userProfileRepository.findByUserId(mockUser.getId()))
            .thenReturn(Optional.of(mockUserProfile));

        List<StudentGradeInputDTO> result =
            teacherService.getStudentsInTeachingClass(teachingClassId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2023001", result.get(0).getStudentCode());
        assertEquals("张三", result.get(0).getName());
        assertEquals(85.0, result.get(0).getUsualScore());
        assertEquals(80.0, result.get(0).getMidtermScore());
        assertEquals(90.0, result.get(0).getExperimentScore());
        assertEquals(75.0, result.get(0).getFinalExamScore());
        assertEquals(1L, result.get(0).getVersion());

        verify(teachingClassRepository, times(1)).findById(teachingClassId);
        verify(studentRepository, times(1)).findByTeachingClassId(teachingClassId);
    }

    @Test
    @DisplayName("获取教学班学生列表 - 教学班不存在")
    void testGetStudentsInTeachingClassNotFound() {
        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> teacherService.getStudentsInTeachingClass(teachingClassId)
        );

        assertTrue(exception.getMessage().contains("教学班不存在"));
        verify(teachingClassRepository, times(1)).findById(teachingClassId);
    }

    @Test
    @DisplayName("获取成绩分布 - 成功")
    void testGetGradeDistributionSuccess() {
        List<Student> students = new ArrayList<>();
        students.add(mockStudent);

        Grade gradeA = createGradeWithScore(95.0);
        Grade gradeB = createGradeWithScore(85.0);
        Grade gradeC = createGradeWithScore(75.0);
        Grade gradeD = createGradeWithScore(65.0);
        Grade gradeF = createGradeWithScore(55.0);

        List<Grade> grades = List.of(gradeA, gradeB, gradeC, gradeD, gradeF);

        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.of(mockTeachingClass));
        when(gradeRepository.findByCourseId(courseId)).thenReturn(grades);
        when(studentRepository.findByTeachingClassId(teachingClassId))
            .thenReturn(students);

        List<DistributionDataDTO> result =
            teacherService.getGradeDistribution(teachingClassId);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("90-100", result.get(0).getRange());
        assertEquals("80-89", result.get(1).getRange());
        assertEquals("70-79", result.get(2).getRange());
        assertEquals("60-69", result.get(3).getRange());
        assertEquals("0-59", result.get(4).getRange());

        verify(teachingClassRepository, times(1)).findById(teachingClassId);
        verify(gradeRepository, times(1)).findByCourseId(courseId);
    }

    @Test
    @DisplayName("获取成绩分布 - 教学班没有关联课程")
    void testGetGradeDistributionNoCourse() {
        TeachingClass teachingClassNoCourse = new TeachingClass();
        teachingClassNoCourse.setId(teachingClassId);
        teachingClassNoCourse.setCourse(null);

        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.of(teachingClassNoCourse));

        List<DistributionDataDTO> result =
            teacherService.getGradeDistribution(teachingClassId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("批量更新成绩 - 成功")
    void testBatchUpdateGradesSuccess() {
        StudentGradeInputDTO input = new StudentGradeInputDTO();
        input.setId(mockGrade.getId());
        input.setStudentCode("2023001");
        input.setUsualScore(90.0);
        input.setMidtermScore(85.0);
        input.setExperimentScore(95.0);
        input.setFinalExamScore(80.0);
        input.setVersion(1L);

        List<StudentGradeInputDTO> inputs = List.of(input);

        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.of(mockTeachingClass));
        when(gradeRepository.findById(mockGrade.getId()))
            .thenReturn(Optional.of(mockGrade));
        when(gradeRepository.save(any(Grade.class))).thenReturn(mockGrade);

        BatchGradeUpdateDTO.Response result =
            teacherService.batchUpdateGrades(teachingClassId, inputs);

        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(1, result.getUpdatedCount());
        assertNull(result.getErrors());

        verify(gradeRepository, times(1)).findById(mockGrade.getId());
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    @DisplayName("批量更新成绩 - 版本冲突")
    void testBatchUpdateGradesVersionConflict() {
        StudentGradeInputDTO input = new StudentGradeInputDTO();
        input.setId(mockGrade.getId());
        input.setStudentCode("2023001");
        input.setUsualScore(90.0);
        input.setVersion(999L);

        List<StudentGradeInputDTO> inputs = List.of(input);

        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.of(mockTeachingClass));
        when(gradeRepository.findById(mockGrade.getId()))
            .thenReturn(Optional.of(mockGrade));

        BatchGradeUpdateDTO.Response result =
            teacherService.batchUpdateGrades(teachingClassId, inputs);

        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals(0, result.getUpdatedCount());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().get(0).contains("已被其他用户修改"));

        verify(gradeRepository, times(1)).findById(mockGrade.getId());
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    @DisplayName("批量更新成绩 - 教学班没有关联课程")
    void testBatchUpdateGradesNoCourse() {
        TeachingClass teachingClassNoCourse = new TeachingClass();
        teachingClassNoCourse.setId(teachingClassId);
        teachingClassNoCourse.setCourse(null);

        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.of(teachingClassNoCourse));

        BatchGradeUpdateDTO.Response result =
            teacherService.batchUpdateGrades(teachingClassId, new ArrayList<>());

        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().get(0).contains("教学班没有关联课程"));
    }

    @Test
    @DisplayName("批量更新成绩 - 跳过无ID记录")
    void testBatchUpdateGradesSkipNullId() {
        StudentGradeInputDTO inputWithoutId = new StudentGradeInputDTO();
        inputWithoutId.setId(null);
        inputWithoutId.setStudentCode("2023001");
        inputWithoutId.setUsualScore(90.0);

        List<StudentGradeInputDTO> inputs = List.of(inputWithoutId);

        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.of(mockTeachingClass));

        BatchGradeUpdateDTO.Response result =
            teacherService.batchUpdateGrades(teachingClassId, inputs);

        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(0, result.getUpdatedCount());

        verify(gradeRepository, never()).findById(any());
        verify(gradeRepository, never()).save(any());
    }

    @Test
    @DisplayName("批量更新成绩 - 部分成功")
    void testBatchUpdateGradesPartialSuccess() {
        UUID existingGradeId = mockGrade.getId();
        UUID nonExistingGradeId = UUID.randomUUID();

        StudentGradeInputDTO validInput = new StudentGradeInputDTO();
        validInput.setId(existingGradeId);
        validInput.setStudentCode("2023001");
        validInput.setUsualScore(90.0);
        validInput.setVersion(1L);

        StudentGradeInputDTO invalidInput = new StudentGradeInputDTO();
        invalidInput.setId(nonExistingGradeId);
        invalidInput.setStudentCode("2023002");
        invalidInput.setUsualScore(85.0);

        List<StudentGradeInputDTO> inputs = List.of(validInput, invalidInput);

        when(teachingClassRepository.findById(teachingClassId))
            .thenReturn(Optional.of(mockTeachingClass));
        when(gradeRepository.findById(existingGradeId))
            .thenReturn(Optional.of(mockGrade));
        when(gradeRepository.findById(nonExistingGradeId))
            .thenReturn(Optional.empty());
        when(gradeRepository.save(any(Grade.class))).thenReturn(mockGrade);

        BatchGradeUpdateDTO.Response result =
            teacherService.batchUpdateGrades(teachingClassId, inputs);

        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(1, result.getUpdatedCount());
        assertNotNull(result.getErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("成绩记录不存在"));
    }

    private Grade createGradeWithScore(Double score) {
        Grade grade = new Grade();
        grade.setId(UUID.randomUUID());
        grade.setStudent(mockStudent);
        grade.setCourse(mockCourse);
        try {
            java.lang.reflect.Field finalScoreField = Grade.class.getDeclaredField("finalScore");
            finalScoreField.setAccessible(true);
            finalScoreField.set(grade, score);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return grade;
    }
}
