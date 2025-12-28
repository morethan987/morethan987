package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.course.CourseType;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClassStatus;
import com.example.GradeSystemBackend.dto.CourseDTO;
import com.example.GradeSystemBackend.dto.TeachingClassDTO;
import com.example.GradeSystemBackend.repository.CourseRepository;
import com.example.GradeSystemBackend.repository.StudentRepository;
import com.example.GradeSystemBackend.repository.TeachingClassRepository;
import com.example.GradeSystemBackend.repository.UserProfileRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    /**
     * 查询某学生当前学期可选课程数量
     * @param studentId
     * @return
     */
    public long getCurrentSemesterCourseCount(UUID studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return 0;
        }

        return courseRepository.countBySemester(student.getCurrentSemester());
    }

    public long getCourseCountByCourseType(CourseType courseType) {
        return courseRepository.countByCourseType(courseType);
    }

    public Double getCurrentSemesterCreditCount(UUID studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return 0.0;
        }
        return courseRepository.sumCreditsBySemester(
            student.getCurrentSemester()
        );
    }

    /**
     * 获取学生的课程列表（包含教学班信息）
     */
    public List<TeachingClassDTO> getStudentCourses(
        UUID studentId,
        String courseType
    ) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return new ArrayList<>();
        }

        Integer semester = student.getCurrentSemester();
        List<TeachingClass> teachingClasses = new ArrayList<>();

        if (semester != null) {
            teachingClasses =
                teachingClassRepository.findByStudentIdAndSemester(
                    studentId,
                    semester
                );
        } else {
            teachingClasses = teachingClassRepository.findByStudentId(
                studentId
            );
        }

        final int enrollmentYear = student.getEnrollmentYear();
        return teachingClasses
            .stream()
            .filter(
                tc ->
                    courseType == null ||
                    tc.getCourse().getCourseType().name().equals(courseType)
            )
            .map(tc -> convertToTeachingClassDTO(tc, enrollmentYear))
            .collect(Collectors.toList());
    }

    /**
     * 获取所有可选课程
     */
    public List<TeachingClassDTO> getAvailableCourses(
        UUID studentId,
        String courseType,
        String search
    ) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return new ArrayList<>();
        }

        final int enrollmentYear = student.getEnrollmentYear();
        List<TeachingClass> teachingClasses = teachingClassRepository.findAll();

        Integer semester = student.getCurrentSemester();
        return teachingClasses
            .stream()
            .filter(
                tc ->
                    semester == null ||
                    tc.getCourse().getSemester().equals(semester)
            )
            .filter(
                tc ->
                    courseType == null ||
                    tc.getCourse().getCourseType().name().equals(courseType)
            )
            .filter(
                tc ->
                    search == null ||
                    tc.getCourse().getName().contains(search) ||
                    tc.getCourse().getDescription().contains(search)
            )
            .map(tc -> convertToTeachingClassDTO(tc, enrollmentYear))
            .collect(Collectors.toList());
    }

    /**
     * 学生选课
     */
    public void selectCourse(UUID studentId, UUID teachingClassId) {
        // 1. 查找学生和教学班
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() -> new BusinessException("找不到该学生信息"));
        TeachingClass teachingClass = teachingClassRepository
            .findById(teachingClassId)
            .orElseThrow(() -> new BusinessException("该教学班不存在"));

        // 2. 检查人数是否已满 (你原代码漏掉了这个关键逻辑，建议加上)
        if (teachingClass.getEnrolledCount() >= teachingClass.getCapacity()) {
            throw new BusinessException("该课程选课人数已满");
        }

        // 3. 检查学生是否已经选修了该教学班
        boolean alreadyEnrolled = teachingClassRepository
            .findByStudentIdDirect(studentId)
            .stream()
            .anyMatch(tc -> tc.getId().equals(teachingClassId));

        if (alreadyEnrolled) {
            throw new BusinessException("你已经选过这门课了，请勿重复选择");
        }

        // 4. 执行选课逻辑
        teachingClass.addStudent(student);
        teachingClassRepository.save(teachingClass);
    }

    /**
     * 学生退课
     */
    public void dropCourse(UUID studentId, UUID teachingClassId) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() -> new BusinessException("找不到学生信息"));
        TeachingClass teachingClass = teachingClassRepository
            .findById(teachingClassId)
            .orElseThrow(() -> new BusinessException("找不到课程信息"));

        boolean isEnrolled = teachingClassRepository
            .findByStudentIdDirect(studentId)
            .stream()
            .anyMatch(tc -> tc.getId().equals(teachingClassId));

        if (!isEnrolled) {
            throw new BusinessException("你并未选修该课程，无法退课");
        }

        teachingClass.removeStudent(student);
        teachingClassRepository.save(teachingClass);
    }

    /**
     * 根据ID获取课程
     */
    public CourseDTO getCourseById(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            return convertToCourseDTO(course);
        }
        return null;
    }

    /**
     * 将TeachingClass转换为DTO
     */
    private TeachingClassDTO convertToTeachingClassDTO(
        TeachingClass teachingClass,
        int enrollmentYear
    ) {
        TeachingClassDTO dto = new TeachingClassDTO();
        dto.setId(teachingClass.getId());
        dto.setName(teachingClass.getName());
        dto.setCourse(convertToCourseDTO(teachingClass.getCourse()));

        UserProfile teacherProfile = userProfileRepository
            .findByUser(teachingClass.getTeacher().getUser())
            .orElse(null);

        dto.setTeacherName(
            teacherProfile != null ? teacherProfile.getRealName() : "未知教师"
        );

        dto.setClassroom(teachingClass.getClassroom());
        dto.setTimeSchedule(teachingClass.getTimeSchedule());
        dto.setCapacity(teachingClass.getCapacity());
        dto.setEnrolled(teachingClass.getEnrolledCount());

        // 转换状态枚举为字符串
        if (teachingClass.getStatus() != null) {
            dto.setStatus(teachingClass.getStatus());
        } else {
            dto.setStatus(TeachingClassStatus.ACTIVE);
        }

        // 根据学期和入学年份生成学期名称
        dto.setSemesterName(
            generateSemesterName(
                teachingClass.getCourse().getSemester(),
                enrollmentYear
            )
        );

        return dto;
    }

    /**
     * 根据学期数字和入学年份生成学期名称
     */
    private String generateSemesterName(Integer semester, int enrollmentYear) {
        if (semester == null) {
            return "未知学期";
        }

        int year = enrollmentYear + (semester - 1) / 2; // 根据入学年份计算
        int semesterNum = ((semester - 1) % 2) + 1;

        if (semesterNum == 1) {
            return year + "春";
        } else {
            return year + "秋";
        }
    }

    /**
     * 将Course转换为DTO
     */
    private CourseDTO convertToCourseDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setCredit(course.getCredit());
        dto.setSemester(course.getSemester());
        dto.setCourseType(course.getCourseType());
        return dto;
    }

    // 业务异常类
    public class BusinessException extends RuntimeException {

        public BusinessException(String message) {
            super(message);
        }
    }
}
