package com.example.academic.controller;

import com.example.academic.domain.EnrollmentStatus;
import com.example.academic.dto.CourseEnrollmentDTO;
import com.example.academic.dto.EnrollmentRequestDTO;
import com.example.academic.dto.EnrollmentResponseDTO;
import com.example.academic.service.CourseEnrollmentService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/enrollment")
public class CourseEnrollmentController {

    @Autowired
    private CourseEnrollmentService enrollmentService;

    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<?> getEnrollmentById(@PathVariable UUID enrollmentId) {
        try {
            CourseEnrollmentDTO enrollment = enrollmentService.getEnrollmentById(enrollmentId);
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/by-student/{studentId}")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<List<CourseEnrollmentDTO>> getEnrollmentsByStudent(@PathVariable UUID studentId) {
        List<CourseEnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/by-student/{studentId}/active")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<List<CourseEnrollmentDTO>> getActiveEnrollmentsByStudent(@PathVariable UUID studentId) {
        List<CourseEnrollmentDTO> enrollments = enrollmentService.getActiveEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/by-teaching-class/{teachingClassId}")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<List<CourseEnrollmentDTO>> getEnrollmentsByTeachingClass(@PathVariable UUID teachingClassId) {
        List<CourseEnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByTeachingClass(teachingClassId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<List<CourseEnrollmentDTO>> getEnrollmentsByStatus(@PathVariable EnrollmentStatus status) {
        List<CourseEnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStatus(status);
        return ResponseEntity.ok(enrollments);
    }

    @PostMapping("/enroll")
    @PreAuthorize("hasAnyAuthority('enrollment:create', 'admin:all')")
    public ResponseEntity<EnrollmentResponseDTO> enrollStudent(@RequestBody EnrollmentRequestDTO request) {
        EnrollmentResponseDTO response = enrollmentService.enrollStudent(request);
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/drop")
    @PreAuthorize("hasAnyAuthority('enrollment:update', 'admin:all')")
    public ResponseEntity<EnrollmentResponseDTO> dropCourse(
            @RequestParam UUID studentId, @RequestParam UUID teachingClassId) {
        try {
            EnrollmentResponseDTO response = enrollmentService.dropCourse(studentId, teachingClassId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                EnrollmentResponseDTO.failure(studentId, teachingClassId, e.getMessage()));
        }
    }

    @PutMapping("/{enrollmentId}/status")
    @PreAuthorize("hasAnyAuthority('enrollment:update', 'admin:all')")
    public ResponseEntity<?> updateEnrollmentStatus(
            @PathVariable UUID enrollmentId, @RequestParam EnrollmentStatus status) {
        try {
            CourseEnrollmentDTO enrollment = enrollmentService.updateEnrollmentStatus(enrollmentId, status);
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/count/by-student/{studentId}")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countEnrollmentsByStudent(@PathVariable UUID studentId) {
        long count = enrollmentService.countEnrollmentsByStudent(studentId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-student/{studentId}/active")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countActiveEnrollmentsByStudent(@PathVariable UUID studentId) {
        long count = enrollmentService.countActiveEnrollmentsByStudent(studentId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-teaching-class/{teachingClassId}")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countEnrollmentsByTeachingClass(@PathVariable UUID teachingClassId) {
        long count = enrollmentService.countEnrollmentsByTeachingClass(teachingClassId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    @PreAuthorize("hasAnyAuthority('enrollment:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> isStudentEnrolled(
            @RequestParam UUID studentId, @RequestParam UUID teachingClassId) {
        boolean enrolled = enrollmentService.isStudentEnrolled(studentId, teachingClassId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("enrolled", enrolled);
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
}
