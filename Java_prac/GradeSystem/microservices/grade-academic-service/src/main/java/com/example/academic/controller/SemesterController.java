package com.example.academic.controller;

import com.example.academic.dto.SemesterDTO;
import com.example.academic.service.SemesterService;
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
@RequestMapping("/semester")
public class SemesterController {

    @Autowired
    private SemesterService semesterService;

    @GetMapping("/{semesterId}")
    @PreAuthorize("hasAnyAuthority('semester:view', 'admin:all')")
    public ResponseEntity<?> getSemesterById(@PathVariable UUID semesterId) {
        try {
            SemesterDTO semester = semesterService.getSemesterById(semesterId);
            return ResponseEntity.ok(semester);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('semester:view', 'admin:all')")
    public ResponseEntity<?> getCurrentSemester() {
        try {
            SemesterDTO semester = semesterService.getCurrentSemester();
            return ResponseEntity.ok(semester);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('semester:view', 'admin:all')")
    public ResponseEntity<List<SemesterDTO>> getAllSemesters() {
        List<SemesterDTO> semesters = semesterService.getAllSemesters();
        return ResponseEntity.ok(semesters);
    }

    @GetMapping("/by-academic-year")
    @PreAuthorize("hasAnyAuthority('semester:view', 'admin:all')")
    public ResponseEntity<List<SemesterDTO>> getSemestersByAcademicYear(@RequestParam String academicYear) {
        List<SemesterDTO> semesters = semesterService.getSemestersByAcademicYear(academicYear);
        return ResponseEntity.ok(semesters);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('semester:view', 'admin:all')")
    public ResponseEntity<List<SemesterDTO>> getActiveSemesters() {
        List<SemesterDTO> semesters = semesterService.getActiveSemesters();
        return ResponseEntity.ok(semesters);
    }

    @GetMapping("/open-enrollment")
    @PreAuthorize("hasAnyAuthority('semester:view', 'admin:all')")
    public ResponseEntity<List<SemesterDTO>> getSemestersWithOpenEnrollment() {
        List<SemesterDTO> semesters = semesterService.getSemestersWithOpenEnrollment();
        return ResponseEntity.ok(semesters);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('semester:create', 'admin:all')")
    public ResponseEntity<?> createSemester(@RequestBody SemesterDTO request) {
        try {
            SemesterDTO semester = semesterService.createSemester(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(semester);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{semesterId}")
    @PreAuthorize("hasAnyAuthority('semester:update', 'admin:all')")
    public ResponseEntity<?> updateSemester(@PathVariable UUID semesterId, @RequestBody SemesterDTO request) {
        try {
            SemesterDTO semester = semesterService.updateSemester(semesterId, request);
            return ResponseEntity.ok(semester);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{semesterId}/set-current")
    @PreAuthorize("hasAnyAuthority('semester:update', 'admin:all')")
    public ResponseEntity<?> setCurrentSemester(@PathVariable UUID semesterId) {
        try {
            SemesterDTO semester = semesterService.setCurrentSemester(semesterId);
            return ResponseEntity.ok(semester);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{semesterId}")
    @PreAuthorize("hasAnyAuthority('semester:delete', 'admin:all')")
    public ResponseEntity<?> deleteSemester(@PathVariable UUID semesterId) {
        try {
            semesterService.deleteSemester(semesterId);
            return ResponseEntity.ok(createSuccessResponse("Semester deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('semester:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> getTotalSemesters() {
        long count = semesterService.getTotalSemesters();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/enrollment-open")
    @PreAuthorize("hasAnyAuthority('semester:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> isEnrollmentOpen() {
        boolean open = semesterService.isEnrollmentOpen();
        Map<String, Boolean> response = new HashMap<>();
        response.put("enrollmentOpen", open);
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
