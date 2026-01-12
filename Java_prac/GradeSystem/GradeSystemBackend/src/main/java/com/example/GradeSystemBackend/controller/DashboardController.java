package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.CardDataDTO;
import com.example.GradeSystemBackend.service.DashboardService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @PreAuthorize("hasAnyAuthority('admin:all')")
    @GetMapping("/admin")
    public ResponseEntity<List<CardDataDTO>> getAdminDashboard() {
        List<CardDataDTO> result = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CardDataDTO>> getStudentDashboard(
        @PathVariable UUID studentId
    ) {
        List<CardDataDTO> result = dashboardService.getStudentDashboard(studentId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CardDataDTO>> getTeacherDashboard(
        @PathVariable UUID teacherId
    ) {
        List<CardDataDTO> result = dashboardService.getTeacherDashboard(teacherId);
        return ResponseEntity.ok(result);
    }
}
