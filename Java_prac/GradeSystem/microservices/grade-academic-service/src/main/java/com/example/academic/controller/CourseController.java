package com.example.academic.controller;

import com.example.academic.domain.CourseType;
import com.example.academic.dto.CourseDTO;
import com.example.academic.service.CourseService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<?> getCourseById(@PathVariable UUID courseId) {
        try {
            CourseDTO course = courseService.getCourseById(courseId);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/by-code/{courseCode}")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<?> getCourseByCourseCode(@PathVariable String courseCode) {
        try {
            CourseDTO course = courseService.getCourseByCourseCode(courseCode);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<List<CourseDTO>> getActiveCourses() {
        List<CourseDTO> courses = courseService.getActiveCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/by-type/{type}")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<List<CourseDTO>> getCoursesByType(@PathVariable CourseType type) {
        List<CourseDTO> courses = courseService.getCoursesByType(type);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/by-semester/{semester}")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<List<CourseDTO>> getCoursesBySemester(@PathVariable Integer semester) {
        List<CourseDTO> courses = courseService.getCoursesBySemester(semester);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/by-department")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<List<CourseDTO>> getCoursesByDepartment(@RequestParam String department) {
        List<CourseDTO> courses = courseService.getCoursesByDepartment(department);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<List<CourseDTO>> searchCourses(@RequestParam String keyword) {
        List<CourseDTO> courses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('course:create', 'admin:all')")
    public ResponseEntity<?> createCourse(@RequestBody CourseDTO request) {
        try {
            CourseDTO course = courseService.createCourse(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(course);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('course:update', 'admin:all')")
    public ResponseEntity<?> updateCourse(@PathVariable UUID courseId, @RequestBody CourseDTO request) {
        try {
            CourseDTO course = courseService.updateCourse(courseId, request);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{courseId}/status")
    @PreAuthorize("hasAnyAuthority('course:update', 'admin:all')")
    public ResponseEntity<?> updateCourseStatus(@PathVariable UUID courseId, @RequestParam Boolean isActive) {
        try {
            CourseDTO course = courseService.updateCourseStatus(courseId, isActive);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('course:delete', 'admin:all')")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID courseId) {
        try {
            courseService.deleteCourse(courseId);
            return ResponseEntity.ok(createSuccessResponse("Course deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> getTotalCourses() {
        long count = courseService.getTotalCourses();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countActiveCourses() {
        long count = courseService.countActiveCourses();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-type/{type}")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countByType(@PathVariable CourseType type) {
        long count = courseService.countByType(type);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/by-code/{courseCode}")
    @PreAuthorize("hasAnyAuthority('course:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> existsByCourseCode(@PathVariable String courseCode) {
        boolean exists = courseService.existsByCourseCode(courseCode);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            Map.of(
                "error", "Access denied",
                "message", "You don't have permission to access this resource: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            )
        );
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
