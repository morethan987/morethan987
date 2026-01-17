package com.example.academic.service;

import com.example.academic.client.TeacherDTO;
import com.example.academic.client.UserServiceClient;
import com.example.academic.domain.Course;
import com.example.academic.domain.TeachingClass;
import com.example.academic.domain.TeachingClassStatus;
import com.example.academic.dto.TeachingClassDTO;
import com.example.academic.dto.TeachingClassWithStatsDTO;
import com.example.academic.repository.CourseRepository;
import com.example.academic.repository.TeachingClassRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeachingClassService {

    private static final Logger log = LoggerFactory.getLogger(TeachingClassService.class);

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    public TeachingClassDTO getTeachingClassById(UUID classId) {
        return teachingClassRepository.findById(classId)
            .map(TeachingClassDTO::new)
            .orElseThrow(() -> new RuntimeException("Teaching class not found: " + classId));
    }

    public TeachingClassWithStatsDTO getTeachingClassWithStats(UUID classId) {
        TeachingClass tc = teachingClassRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Teaching class not found: " + classId));

        TeachingClassWithStatsDTO dto = new TeachingClassWithStatsDTO(tc);
        enrichWithTeacherInfo(dto);
        return dto;
    }

    public List<TeachingClassDTO> getAllTeachingClasses() {
        return teachingClassRepository.findAll().stream()
            .map(TeachingClassDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeachingClassDTO> getTeachingClassesByTeacher(UUID teacherId) {
        return teachingClassRepository.findByTeacherId(teacherId).stream()
            .map(TeachingClassDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeachingClassDTO> getTeachingClassesByCourse(UUID courseId) {
        return teachingClassRepository.findByCourseId(courseId).stream()
            .map(TeachingClassDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeachingClassDTO> getTeachingClassesByStatus(TeachingClassStatus status) {
        return teachingClassRepository.findByStatus(status).stream()
            .map(TeachingClassDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeachingClassDTO> getTeachingClassesBySemester(String academicYear, Integer semesterNumber) {
        return teachingClassRepository.findByAcademicYearAndSemesterNumber(academicYear, semesterNumber).stream()
            .map(TeachingClassDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeachingClassDTO> getAvailableTeachingClasses() {
        return teachingClassRepository.findAvailableForEnrollment().stream()
            .map(TeachingClassDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeachingClassWithStatsDTO> getTeachingClassesWithStatsByCourse(UUID courseId) {
        return teachingClassRepository.findByCourseId(courseId).stream()
            .map(tc -> {
                TeachingClassWithStatsDTO dto = new TeachingClassWithStatsDTO(tc);
                enrichWithTeacherInfo(dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public TeachingClassDTO createTeachingClass(TeachingClassDTO request) {
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found: " + request.getCourseId()));

        TeachingClass tc = new TeachingClass();
        tc.setName(request.getName());
        tc.setTeacherId(request.getTeacherId());
        tc.setCourse(course);
        tc.setClassroom(request.getClassroom());
        tc.setTimeSchedule(request.getTimeSchedule());
        tc.setCapacity(request.getCapacity() != null ? request.getCapacity() : 50);
        tc.setEnrolledCount(0);
        tc.setStatus(TeachingClassStatus.PLANNED);
        tc.setAcademicYear(request.getAcademicYear());
        tc.setSemesterNumber(request.getSemesterNumber());
        tc.setStartDate(request.getStartDate());
        tc.setEndDate(request.getEndDate());

        TeachingClass saved = teachingClassRepository.save(tc);
        return new TeachingClassDTO(saved);
    }

    @Transactional
    public TeachingClassDTO updateTeachingClass(UUID classId, TeachingClassDTO request) {
        TeachingClass tc = teachingClassRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Teaching class not found: " + classId));

        if (request.getName() != null) tc.setName(request.getName());
        if (request.getTeacherId() != null) tc.setTeacherId(request.getTeacherId());
        if (request.getClassroom() != null) tc.setClassroom(request.getClassroom());
        if (request.getTimeSchedule() != null) tc.setTimeSchedule(request.getTimeSchedule());
        if (request.getCapacity() != null) tc.setCapacity(request.getCapacity());
        if (request.getStartDate() != null) tc.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) tc.setEndDate(request.getEndDate());

        TeachingClass saved = teachingClassRepository.save(tc);
        return new TeachingClassDTO(saved);
    }

    @Transactional
    public TeachingClassDTO updateTeachingClassStatus(UUID classId, TeachingClassStatus status) {
        TeachingClass tc = teachingClassRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Teaching class not found: " + classId));

        tc.setStatus(status);
        TeachingClass saved = teachingClassRepository.save(tc);
        return new TeachingClassDTO(saved);
    }

    @Transactional
    public void deleteTeachingClass(UUID classId) {
        TeachingClass tc = teachingClassRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Teaching class not found: " + classId));

        if (tc.getEnrolledCount() > 0) {
            throw new RuntimeException("Cannot delete teaching class with enrolled students");
        }

        teachingClassRepository.deleteById(classId);
    }

    public long getTotalTeachingClasses() {
        return teachingClassRepository.count();
    }

    public long countByTeacher(UUID teacherId) {
        return teachingClassRepository.countByTeacherId(teacherId);
    }

    public long countByStatus(TeachingClassStatus status) {
        return teachingClassRepository.countByStatus(status);
    }

    private void enrichWithTeacherInfo(TeachingClassWithStatsDTO dto) {
        if (dto.getTeacherId() == null) return;

        try {
            TeacherDTO teacher = userServiceClient.getTeacherById(dto.getTeacherId());
            if (teacher != null) {
                dto.setTeacherEmployeeCode(teacher.getEmployeeCode());
                dto.setTeacherDepartment(teacher.getDepartment());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch teacher info for teacherId {}: {}", dto.getTeacherId(), e.getMessage());
        }
    }
}
