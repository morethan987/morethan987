package com.example.user.controller;

import com.example.user.domain.StudentStatus;
import com.example.user.dto.StudentDTO;
import com.example.user.service.StudentService;
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
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<?> getStudentByUserId(@PathVariable UUID userId) {
        try {
            StudentDTO student = studentService.getStudentByUserId(userId);
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<?> getStudentById(@PathVariable UUID studentId) {
        try {
            StudentDTO student = studentService.getStudentById(studentId);
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/by-code/{studentCode}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<?> getStudentByCode(@PathVariable String studentCode) {
        try {
            StudentDTO student = studentService.getStudentByCode(studentCode);
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<List<StudentDTO>> getStudentsByStatus(@PathVariable StudentStatus status) {
        List<StudentDTO> students = studentService.getStudentsByStatus(status);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-major")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<List<StudentDTO>> getStudentsByMajor(@RequestParam String major) {
        List<StudentDTO> students = studentService.getStudentsByMajor(major);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-class")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<List<StudentDTO>> getStudentsByClassName(@RequestParam String className) {
        List<StudentDTO> students = studentService.getStudentsByClassName(className);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-enrollment-year/{year}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<List<StudentDTO>> getStudentsByEnrollmentYear(@PathVariable Integer year) {
        List<StudentDTO> students = studentService.getStudentsByEnrollmentYear(year);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<List<StudentDTO>> getActiveStudents() {
        List<StudentDTO> students = studentService.getActiveStudents();
        return ResponseEntity.ok(students);
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('student:create', 'admin:all')")
    public ResponseEntity<?> createStudent(@PathVariable UUID userId, @RequestBody StudentDTO request) {
        try {
            StudentDTO student = studentService.createStudent(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(student);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{studentId}")
    @PreAuthorize("hasAnyAuthority('student:update', 'admin:all')")
    public ResponseEntity<?> updateStudent(@PathVariable UUID studentId, @RequestBody StudentDTO request) {
        try {
            StudentDTO student = studentService.updateStudent(studentId, request);
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{studentId}/status")
    @PreAuthorize("hasAnyAuthority('student:update', 'admin:all')")
    public ResponseEntity<?> updateStudentStatus(@PathVariable UUID studentId, @RequestParam StudentStatus status) {
        try {
            StudentDTO student = studentService.updateStudentStatus(studentId, status);
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasAnyAuthority('student:delete', 'admin:all')")
    public ResponseEntity<?> deleteStudent(@PathVariable UUID studentId) {
        try {
            studentService.deleteStudent(studentId);
            return ResponseEntity.ok(createSuccessResponse("Student deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> getTotalStudents() {
        long count = studentService.getTotalStudents();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-status/{status}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countByStatus(@PathVariable StudentStatus status) {
        long count = studentService.countByStatus(status);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-major")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countByMajor(@RequestParam String major) {
        long count = studentService.countByMajor(major);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> existsByUserId(@PathVariable UUID userId) {
        boolean exists = studentService.existsByUserId(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/by-code/{studentCode}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> existsByStudentCode(@PathVariable String studentCode) {
        boolean exists = studentService.existsByStudentCode(studentCode);
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
