package com.example.academic.controller;

import com.example.academic.domain.TeachingClassStatus;
import com.example.academic.dto.TeachingClassDTO;
import com.example.academic.dto.TeachingClassWithStatsDTO;
import com.example.academic.service.TeachingClassService;
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
@RequestMapping("/teaching-class")
public class TeachingClassController {

    @Autowired
    private TeachingClassService teachingClassService;

    @GetMapping("/{classId}")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<?> getTeachingClassById(@PathVariable UUID classId) {
        try {
            TeachingClassDTO tc = teachingClassService.getTeachingClassById(classId);
            return ResponseEntity.ok(tc);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{classId}/with-stats")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<?> getTeachingClassWithStats(@PathVariable UUID classId) {
        try {
            TeachingClassWithStatsDTO tc = teachingClassService.getTeachingClassWithStats(classId);
            return ResponseEntity.ok(tc);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<List<TeachingClassDTO>> getAllTeachingClasses() {
        List<TeachingClassDTO> classes = teachingClassService.getAllTeachingClasses();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/by-teacher/{teacherId}")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<List<TeachingClassDTO>> getTeachingClassesByTeacher(@PathVariable UUID teacherId) {
        List<TeachingClassDTO> classes = teachingClassService.getTeachingClassesByTeacher(teacherId);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/by-course/{courseId}")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<List<TeachingClassDTO>> getTeachingClassesByCourse(@PathVariable UUID courseId) {
        List<TeachingClassDTO> classes = teachingClassService.getTeachingClassesByCourse(courseId);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/by-course/{courseId}/with-stats")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<List<TeachingClassWithStatsDTO>> getTeachingClassesWithStatsByCourse(@PathVariable UUID courseId) {
        List<TeachingClassWithStatsDTO> classes = teachingClassService.getTeachingClassesWithStatsByCourse(courseId);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<List<TeachingClassDTO>> getTeachingClassesByStatus(@PathVariable TeachingClassStatus status) {
        List<TeachingClassDTO> classes = teachingClassService.getTeachingClassesByStatus(status);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/by-semester")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<List<TeachingClassDTO>> getTeachingClassesBySemester(
            @RequestParam String academicYear, @RequestParam Integer semesterNumber) {
        List<TeachingClassDTO> classes = teachingClassService.getTeachingClassesBySemester(academicYear, semesterNumber);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<List<TeachingClassDTO>> getAvailableTeachingClasses() {
        List<TeachingClassDTO> classes = teachingClassService.getAvailableTeachingClasses();
        return ResponseEntity.ok(classes);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('teaching-class:create', 'admin:all')")
    public ResponseEntity<?> createTeachingClass(@RequestBody TeachingClassDTO request) {
        try {
            TeachingClassDTO tc = teachingClassService.createTeachingClass(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(tc);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{classId}")
    @PreAuthorize("hasAnyAuthority('teaching-class:update', 'admin:all')")
    public ResponseEntity<?> updateTeachingClass(@PathVariable UUID classId, @RequestBody TeachingClassDTO request) {
        try {
            TeachingClassDTO tc = teachingClassService.updateTeachingClass(classId, request);
            return ResponseEntity.ok(tc);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{classId}/status")
    @PreAuthorize("hasAnyAuthority('teaching-class:update', 'admin:all')")
    public ResponseEntity<?> updateTeachingClassStatus(@PathVariable UUID classId, @RequestParam TeachingClassStatus status) {
        try {
            TeachingClassDTO tc = teachingClassService.updateTeachingClassStatus(classId, status);
            return ResponseEntity.ok(tc);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{classId}")
    @PreAuthorize("hasAnyAuthority('teaching-class:delete', 'admin:all')")
    public ResponseEntity<?> deleteTeachingClass(@PathVariable UUID classId) {
        try {
            teachingClassService.deleteTeachingClass(classId);
            return ResponseEntity.ok(createSuccessResponse("Teaching class deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> getTotalTeachingClasses() {
        long count = teachingClassService.getTotalTeachingClasses();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-teacher/{teacherId}")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countByTeacher(@PathVariable UUID teacherId) {
        long count = teachingClassService.countByTeacher(teacherId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/by-status/{status}")
    @PreAuthorize("hasAnyAuthority('teaching-class:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> countByStatus(@PathVariable TeachingClassStatus status) {
        long count = teachingClassService.countByStatus(status);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
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
