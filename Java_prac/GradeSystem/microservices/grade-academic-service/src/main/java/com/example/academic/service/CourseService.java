package com.example.academic.service;

import com.example.academic.domain.Course;
import com.example.academic.domain.CourseType;
import com.example.academic.dto.CourseDTO;
import com.example.academic.repository.CourseRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public CourseDTO getCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
            .map(CourseDTO::new)
            .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
    }

    public CourseDTO getCourseByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode)
            .map(CourseDTO::new)
            .orElseThrow(() -> new RuntimeException("Course not found with code: " + courseCode));
    }

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
            .map(CourseDTO::new)
            .collect(Collectors.toList());
    }

    public List<CourseDTO> getActiveCourses() {
        return courseRepository.findByIsActiveTrue().stream()
            .map(CourseDTO::new)
            .collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByType(CourseType type) {
        return courseRepository.findByCourseType(type).stream()
            .map(CourseDTO::new)
            .collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesBySemester(Integer semester) {
        return courseRepository.findBySemester(semester).stream()
            .map(CourseDTO::new)
            .collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByDepartment(String department) {
        return courseRepository.findByDepartment(department).stream()
            .map(CourseDTO::new)
            .collect(Collectors.toList());
    }

    public List<CourseDTO> searchCourses(String keyword) {
        return courseRepository.findByNameContainingIgnoreCase(keyword).stream()
            .map(CourseDTO::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public CourseDTO createCourse(CourseDTO request) {
        if (request.getCourseCode() != null && courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new RuntimeException("Course code already exists: " + request.getCourseCode());
        }

        Course course = new Course();
        course.setName(request.getName());
        course.setCourseCode(request.getCourseCode());
        course.setDescription(request.getDescription());
        course.setCredit(request.getCredit() != null ? request.getCredit() : 3.0);
        course.setSemester(request.getSemester() != null ? request.getSemester() : 1);
        course.setCourseType(request.getCourseType() != null ? request.getCourseType() : CourseType.GENERAL);
        course.setTotalHours(request.getTotalHours());
        course.setLectureHours(request.getLectureHours());
        course.setLabHours(request.getLabHours());
        course.setDepartment(request.getDepartment());
        course.setPrerequisites(request.getPrerequisites());
        course.setIsActive(true);

        Course saved = courseRepository.save(course);
        return new CourseDTO(saved);
    }

    @Transactional
    public CourseDTO updateCourse(UUID courseId, CourseDTO request) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        if (request.getName() != null) course.setName(request.getName());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCredit() != null) course.setCredit(request.getCredit());
        if (request.getSemester() != null) course.setSemester(request.getSemester());
        if (request.getCourseType() != null) course.setCourseType(request.getCourseType());
        if (request.getTotalHours() != null) course.setTotalHours(request.getTotalHours());
        if (request.getLectureHours() != null) course.setLectureHours(request.getLectureHours());
        if (request.getLabHours() != null) course.setLabHours(request.getLabHours());
        if (request.getDepartment() != null) course.setDepartment(request.getDepartment());
        if (request.getPrerequisites() != null) course.setPrerequisites(request.getPrerequisites());

        Course saved = courseRepository.save(course);
        return new CourseDTO(saved);
    }

    @Transactional
    public CourseDTO updateCourseStatus(UUID courseId, Boolean isActive) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        course.setIsActive(isActive);
        Course saved = courseRepository.save(course);
        return new CourseDTO(saved);
    }

    @Transactional
    public void deleteCourse(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found: " + courseId);
        }
        courseRepository.deleteById(courseId);
    }

    public long getTotalCourses() {
        return courseRepository.count();
    }

    public long countActiveCourses() {
        return courseRepository.countByIsActiveTrue();
    }

    public long countByType(CourseType type) {
        return courseRepository.countByCourseType(type);
    }

    public boolean existsByCourseCode(String courseCode) {
        return courseRepository.existsByCourseCode(courseCode);
    }
}
