package com.example.GradeSystemBackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.grade.Grade;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.teacher.Teacher;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import com.example.GradeSystemBackend.dto.CardDataDTO;
import com.example.GradeSystemBackend.repository.GradeRepository;
import com.example.GradeSystemBackend.repository.StudentRepository;
import com.example.GradeSystemBackend.repository.TeacherRepository;
import com.example.GradeSystemBackend.repository.TeachingClassRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService单元测试")
public class DashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeachingClassRepository teachingClassRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private GradeService gradeService;

    @InjectMocks
    private DashboardService dashboardService;

    private UUID studentId;
    private UUID teacherId;
    private Student mockStudent;
    private Teacher mockTeacher;
    private TeachingClass mockTeachingClass;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        teacherId = UUID.randomUUID();

        mockStudent = new Student();
        mockStudent.setId(studentId);
        mockStudent.setStudentCode("2023001");
        mockStudent.setTotalCredits(128.0);
        mockStudent.setTeachingClasses(new HashSet<>());

        mockTeacher = new Teacher();
        mockTeacher.setId(teacherId);

        mockCourse = new Course();
        mockCourse.setId(UUID.randomUUID());
        mockCourse.setName("高等数学");

        mockTeachingClass = new TeachingClass();
        mockTeachingClass.setId(UUID.randomUUID());
        mockTeachingClass.setTeacher(mockTeacher);
        mockTeachingClass.setCourse(mockCourse);
        mockTeachingClass.setStudents(new HashSet<>());
    }

    @Test
    @DisplayName("获取管理员仪表盘 - 成功")
    void testGetAdminDashboardSuccess() {
        when(studentRepository.countActiveStudents()).thenReturn(500L);
        when(teacherRepository.countActiveTeachers()).thenReturn(50L);
        when(teachingClassRepository.countAllTeachingClasses()).thenReturn(100L);
        when(studentRepository.countAllStudents()).thenReturn(600L);
        when(gradeRepository.count()).thenReturn(1000L);
        when(gradeRepository.findAll()).thenReturn(createGradesWithScores(800));

        List<CardDataDTO> result = dashboardService.getAdminDashboard();

        assertNotNull(result);
        assertEquals(4, result.size());

        CardDataDTO usersCard = findCardById(result, "total-users");
        assertNotNull(usersCard);
        assertEquals("在校师生总数", usersCard.getTitle());
        assertEquals("550", usersCard.getValue());

        CardDataDTO classesCard = findCardById(result, "active-classes");
        assertNotNull(classesCard);
        assertEquals("本学期开设课程", classesCard.getTitle());
        assertEquals("100", classesCard.getValue());

        CardDataDTO enrollmentCard = findCardById(result, "avg-enrollment");
        assertNotNull(enrollmentCard);
        assertEquals("平均报到率", enrollmentCard.getTitle());

        CardDataDTO healthCard = findCardById(result, "system-health");
        assertNotNull(healthCard);
        assertEquals("成绩录入进度", healthCard.getTitle());

        verify(studentRepository, times(2)).countActiveStudents();
        verify(teacherRepository, times(1)).countActiveTeachers();
    }

    @Test
    @DisplayName("获取学生仪表盘 - 成功")
    void testGetStudentDashboardSuccess() {
        Set<TeachingClass> enrolledClasses = new HashSet<>();
        enrolledClasses.add(mockTeachingClass);
        mockStudent.setTeachingClasses(enrolledClasses);

        when(gradeService.getStudentGpa(studentId)).thenReturn(3.5);
        when(gradeRepository.calculateStudentTotalCredits(studentId)).thenReturn(60.0);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(gradeRepository.calculateStudentWeightedScore(studentId)).thenReturn(5100.0);

        List<CardDataDTO> result = dashboardService.getStudentDashboard(studentId);

        assertNotNull(result);
        assertEquals(4, result.size());

        CardDataDTO gpaCard = findCardById(result, "gpa-stats");
        assertNotNull(gpaCard);
        assertEquals("当前绩点 (GPA)", gpaCard.getTitle());
        assertEquals("3.50", gpaCard.getValue());

        CardDataDTO creditsCard = findCardById(result, "credits-progress");
        assertNotNull(creditsCard);
        assertEquals("修读学分", creditsCard.getTitle());
        assertEquals("60.0", creditsCard.getValue());

        CardDataDTO coursesCard = findCardById(result, "current-courses");
        assertNotNull(coursesCard);
        assertEquals("本学期课程", coursesCard.getTitle());
        assertEquals("1", coursesCard.getValue());

        CardDataDTO scoreCard = findCardById(result, "weighted-score");
        assertNotNull(scoreCard);
        assertEquals("加权平均分", scoreCard.getTitle());
        assertEquals("85.0", scoreCard.getValue());

        verify(gradeService, times(1)).getStudentGpa(studentId);
    }

    @Test
    @DisplayName("获取学生仪表盘 - GPA为空")
    void testGetStudentDashboardNullGpa() {
        when(gradeService.getStudentGpa(studentId)).thenReturn(null);
        when(gradeRepository.calculateStudentTotalCredits(studentId)).thenReturn(null);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(gradeRepository.calculateStudentWeightedScore(studentId)).thenReturn(null);

        List<CardDataDTO> result = dashboardService.getStudentDashboard(studentId);

        assertNotNull(result);
        assertEquals(4, result.size());

        CardDataDTO gpaCard = findCardById(result, "gpa-stats");
        assertNotNull(gpaCard);
        assertEquals("N/A", gpaCard.getValue());

        CardDataDTO scoreCard = findCardById(result, "weighted-score");
        assertNotNull(scoreCard);
        assertEquals("N/A", scoreCard.getValue());
    }

    @Test
    @DisplayName("获取教师仪表盘 - 成功")
    void testGetTeacherDashboardSuccess() {
        Set<Student> students = new HashSet<>();
        students.add(mockStudent);
        mockTeachingClass.setStudents(students);

        List<TeachingClass> teachingClasses = new ArrayList<>();
        teachingClasses.add(mockTeachingClass);

        Grade passedGrade = createGradeWithFinalScore(75.0);
        List<Grade> grades = List.of(passedGrade);

        when(teachingClassRepository.findByTeacherId(teacherId)).thenReturn(teachingClasses);
        when(gradeRepository.findByCourseId(mockCourse.getId())).thenReturn(grades);

        List<CardDataDTO> result = dashboardService.getTeacherDashboard(teacherId);

        assertNotNull(result);
        assertEquals(4, result.size());

        CardDataDTO classesCard = findCardById(result, "teaching-classes");
        assertNotNull(classesCard);
        assertEquals("执教班级", classesCard.getTitle());
        assertEquals("1", classesCard.getValue());

        CardDataDTO studentsCard = findCardById(result, "total-students");
        assertNotNull(studentsCard);
        assertEquals("学生总数", studentsCard.getTitle());

        CardDataDTO workloadCard = findCardById(result, "workload-hours");
        assertNotNull(workloadCard);
        assertEquals("本周课时", workloadCard.getTitle());

        CardDataDTO passRateCard = findCardById(result, "avg-pass-rate");
        assertNotNull(passRateCard);
        assertEquals("所教课程及格率", passRateCard.getTitle());

        verify(teachingClassRepository, times(1)).findByTeacherId(teacherId);
    }

    @Test
    @DisplayName("获取教师仪表盘 - 无教学班")
    void testGetTeacherDashboardNoClasses() {
        when(teachingClassRepository.findByTeacherId(teacherId)).thenReturn(new ArrayList<>());

        List<CardDataDTO> result = dashboardService.getTeacherDashboard(teacherId);

        assertNotNull(result);
        assertEquals(4, result.size());

        CardDataDTO classesCard = findCardById(result, "teaching-classes");
        assertNotNull(classesCard);
        assertEquals("0", classesCard.getValue());

        CardDataDTO studentsCard = findCardById(result, "total-students");
        assertNotNull(studentsCard);
        assertEquals("0", studentsCard.getValue());

        CardDataDTO passRateCard = findCardById(result, "avg-pass-rate");
        assertNotNull(passRateCard);
        assertEquals("0.0%", passRateCard.getValue());
    }

    @Test
    @DisplayName("获取管理员仪表盘 - 边界值测试（零数据）")
    void testGetAdminDashboardZeroValues() {
        when(studentRepository.countActiveStudents()).thenReturn(0L);
        when(teacherRepository.countActiveTeachers()).thenReturn(0L);
        when(teachingClassRepository.countAllTeachingClasses()).thenReturn(0L);
        when(studentRepository.countAllStudents()).thenReturn(0L);
        when(gradeRepository.count()).thenReturn(0L);
        when(gradeRepository.findAll()).thenReturn(new ArrayList<>());

        List<CardDataDTO> result = dashboardService.getAdminDashboard();

        assertNotNull(result);
        assertEquals(4, result.size());

        CardDataDTO usersCard = findCardById(result, "total-users");
        assertEquals("0", usersCard.getValue());

        CardDataDTO enrollmentCard = findCardById(result, "avg-enrollment");
        assertEquals("0.0%", enrollmentCard.getValue());

        CardDataDTO healthCard = findCardById(result, "system-health");
        assertEquals("0.0%", healthCard.getValue());
    }

    private CardDataDTO findCardById(List<CardDataDTO> cards, String id) {
        return cards.stream()
            .filter(card -> id.equals(card.getId()))
            .findFirst()
            .orElse(null);
    }

    private List<Grade> createGradesWithScores(int countWithScore) {
        List<Grade> grades = new ArrayList<>();
        for (int i = 0; i < countWithScore; i++) {
            Grade grade = createGradeWithFinalScore(75.0);
            grades.add(grade);
        }
        return grades;
    }

    private Grade createGradeWithFinalScore(Double score) {
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
