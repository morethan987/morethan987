package com.example.user.controller;

import com.example.user.domain.TeacherStatus;
import com.example.user.domain.TeacherTitle;
import com.example.user.dto.TeacherDTO;
import com.example.user.service.TeacherService;
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
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<?> getTeacherByUserId(@PathVariable UUID userId) {
        try {
            TeacherDTO teacher = teacherService.getTeacherByUserId(userId);
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{teacherId}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<?> getTeacherById(@PathVariable UUID teacherId) {
        try {
            TeacherDTO teacher = teacherService.getTeacherById(teacherId);
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/by-code/{employeeCode}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<?> getTeacherByEmployeeCode(@PathVariable String employeeCode) {
        try {
            TeacherDTO teacher = teacherService.getTeacherByEmployeeCode(employeeCode);
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        List<TeacherDTO> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/by-department")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<List<TeacherDTO>> getTeachersByDepartment(@RequestParam String department) {
        List<TeacherDTO> teachers = teacherService.getTeachersByDepartment(department);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/by-title/{title}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<List<TeacherDTO>> getTeachersByTitle(@PathVariable TeacherTitle title) {
        List<TeacherDTO> teachers = teacherService.getTeachersByTitle(title);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<List<TeacherDTO>> getTeachersByStatus(@PathVariable TeacherStatus status) {
        List<TeacherDTO> teachers = teacherService.getTeachersByStatus(status);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<List<TeacherDTO>> getActiveTeachers() {
        List<TeacherDTO> teachers = teacherService.getActiveTeachers();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<List<TeacherDTO>> getAvailableTeachers() {
        List<TeacherDTO> teachers = teacherService.getAvailableTeachers();
        return ResponseEntity.ok(teachers);
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('teacher:create', 'admin:all')")
    public ResponseEntity<?> createTeacher(@PathVariable UUID userId, @RequestBody TeacherDTO request) {
        try {
            TeacherDTO teacher = teacherService.createTeacher(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{teacherId}")
    @PreAuthorize("hasAnyAuthority('teacher:update', 'admin:all')")
    public ResponseEntity<?> updateTeacher(@PathVariable UUID teacherId, @RequestBody TeacherDTO request) {
        try {
            TeacherDTO teacher = teacherService.updateTeacher(teacherId, request);
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{teacherId}/status")
    @PreAuthorize("hasAnyAuthority('teacher:update', 'admin:all')")
    public ResponseEntity<?> updateTeacherStatus(@PathVariable UUID teacherId, @RequestParam TeacherStatus status) {
        try {
            TeacherDTO teacher = teacherService.updateTeacherStatus(teacherId, status);
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{teacherId}/workload")
    @PreAuthorize("hasAnyAuthority('teacher:update', 'admin:all')")
    public ResponseEntity<?> updateTeacherWorkload(@PathVariable UUID teacherId, @RequestParam Double workload) {
        try {
            TeacherDTO teacher = teacherService.updateTeacherWorkload(teacherId, workload);
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{teacherId}")
    @PreAuthorize("hasAnyAuthority('teacher:delete', 'admin:all')")
    public ResponseEntity<?> deleteTeacher(@PathVariable UUID teacherId) {
        try {
            teacherService.deleteTeacher(teacherId);
            return ResponseEntity.ok(createSuccessResponse("Teacher deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> getTotalTeachers() {
        long count = teacherService.getTotalTeachers();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-department")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countByDepartment(@RequestParam String department) {
        long count = teacherService.countByDepartment(department);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-status/{status}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countByStatus(@PathVariable TeacherStatus status) {
        long count = teacherService.countByStatus(status);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> existsByUserId(@PathVariable UUID userId) {
        boolean exists = teacherService.existsByUserId(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/by-code/{employeeCode}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> existsByEmployeeCode(@PathVariable String employeeCode) {
        boolean exists = teacherService.existsByEmployeeCode(employeeCode);
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
