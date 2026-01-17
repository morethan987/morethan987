package com.example.analytics.controller;

import com.example.analytics.dto.CourseDTO;
import com.example.analytics.dto.DashboardStatsDTO;
import com.example.analytics.dto.GradeStatsDTO;
import com.example.analytics.dto.StudentDTO;
import com.example.analytics.service.AnalyticsService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('analytics:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyAuthority('analytics:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<GradeStatsDTO> getStudentAnalytics(@PathVariable UUID studentId) {
        GradeStatsDTO stats = analyticsService.getStudentAnalytics(studentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/student/{studentId}/gpa")
    @PreAuthorize("hasAnyAuthority('analytics:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<Double> getStudentGpa(@PathVariable UUID studentId) {
        Double gpa = analyticsService.getStudentGpa(studentId);
        return ResponseEntity.ok(gpa);
    }

    @GetMapping("/student/{studentId}/info")
    @PreAuthorize("hasAnyAuthority('analytics:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<StudentDTO> getStudentInfo(@PathVariable UUID studentId) {
        StudentDTO student = analyticsService.getStudentInfo(studentId);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    @GetMapping("/course/{courseId}/pass-rate")
    @PreAuthorize("hasAnyAuthority('analytics:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<Double> getCoursePassRate(@PathVariable UUID courseId) {
        Double passRate = analyticsService.getCoursePassRate(courseId);
        return ResponseEntity.ok(passRate);
    }

    @GetMapping("/course/{courseId}/average")
    @PreAuthorize("hasAnyAuthority('analytics:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<Double> getCourseAverageScore(@PathVariable UUID courseId) {
        Double average = analyticsService.getCourseAverageScore(courseId);
        return ResponseEntity.ok(average);
    }

    @GetMapping("/course/{courseId}/info")
    @PreAuthorize("hasAnyAuthority('analytics:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<CourseDTO> getCourseInfo(@PathVariable UUID courseId) {
        CourseDTO course = analyticsService.getCourseInfo(courseId);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }
}
