package com.example.grade.controller;

import com.example.grade.dto.GradeCreateDTO;
import com.example.grade.dto.GradeDTO;
import com.example.grade.dto.GradeStatsDTO;
import com.example.grade.dto.GradeUpdateDTO;
import com.example.grade.service.GradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/grade")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping("/{gradeId}")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable UUID gradeId) {
        GradeDTO grade = gradeService.getGradeById(gradeId);
        if (grade == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(grade);
    }

    @GetMapping("/{gradeId}/details")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<GradeDTO> getGradeByIdWithDetails(@PathVariable UUID gradeId) {
        GradeDTO grade = gradeService.getGradeByIdWithDetails(gradeId);
        if (grade == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(grade);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyAuthority('grade:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<List<GradeDTO>> getGradesByStudentId(@PathVariable UUID studentId) {
        List<GradeDTO> grades = gradeService.getGradesByStudentId(studentId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/student/{studentId}/details")
    @PreAuthorize("hasAnyAuthority('grade:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<List<GradeDTO>> getGradesByStudentIdWithDetails(@PathVariable UUID studentId) {
        List<GradeDTO> grades = gradeService.getGradesByStudentIdWithDetails(studentId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<List<GradeDTO>> getGradesByCourseId(@PathVariable UUID courseId) {
        List<GradeDTO> grades = gradeService.getGradesByCourseId(courseId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/course/{courseId}/details")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<List<GradeDTO>> getGradesByCourseIdWithDetails(@PathVariable UUID courseId) {
        List<GradeDTO> grades = gradeService.getGradesByCourseIdWithDetails(courseId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('grade:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<GradeDTO> getGradeByStudentAndCourse(
            @PathVariable UUID studentId,
            @PathVariable UUID courseId) {
        return gradeService.getGradeByStudentAndCourse(studentId, courseId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('grade:create', 'teacher:all', 'admin:all')")
    public ResponseEntity<GradeDTO> createGrade(@Valid @RequestBody GradeCreateDTO createDTO) {
        GradeDTO created = gradeService.createGrade(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{gradeId}")
    @PreAuthorize("hasAnyAuthority('grade:update', 'teacher:all', 'admin:all')")
    public ResponseEntity<GradeDTO> updateGrade(
            @PathVariable UUID gradeId,
            @Valid @RequestBody GradeUpdateDTO updateDTO) {
        GradeDTO updated = gradeService.updateGrade(gradeId, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/batch")
    @PreAuthorize("hasAnyAuthority('grade:update', 'teacher:all', 'admin:all')")
    public ResponseEntity<Map<String, Object>> batchUpdateGrades(
            @Valid @RequestBody List<GradeUpdateDTO> updateDTOs) {
        Map<String, Object> result = gradeService.batchUpdateGrades(updateDTOs);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{gradeId}")
    @PreAuthorize("hasAnyAuthority('grade:delete', 'admin:all')")
    public ResponseEntity<Void> deleteGrade(@PathVariable UUID gradeId) {
        gradeService.deleteGrade(gradeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}/stats")
    @PreAuthorize("hasAnyAuthority('grade:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<GradeStatsDTO> getStudentGradeStats(@PathVariable UUID studentId) {
        GradeStatsDTO stats = gradeService.getStudentGradeStats(studentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/student/{studentId}/gpa")
    @PreAuthorize("hasAnyAuthority('grade:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<Double> getStudentAverageGpa(@PathVariable UUID studentId) {
        Double gpa = gradeService.getStudentAverageGpa(studentId);
        return ResponseEntity.ok(gpa != null ? gpa : 0.0);
    }

    @GetMapping("/course/{courseId}/pass-rate")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<Double> getCoursePassRate(@PathVariable UUID courseId) {
        Double passRate = gradeService.getCoursePassRate(courseId);
        return ResponseEntity.ok(passRate != null ? passRate : 0.0);
    }

    @GetMapping("/course/{courseId}/average")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<Double> getCourseAverageScore(@PathVariable UUID courseId) {
        Double average = gradeService.getCourseAverageScore(courseId);
        return ResponseEntity.ok(average != null ? average : 0.0);
    }

    @GetMapping("/course/{courseId}/passed")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<List<GradeDTO>> getPassedStudentsInCourse(@PathVariable UUID courseId) {
        List<GradeDTO> passed = gradeService.getPassedStudentsInCourse(courseId);
        return ResponseEntity.ok(passed);
    }

    @GetMapping("/course/{courseId}/failed")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<List<GradeDTO>> getFailedStudentsInCourse(@PathVariable UUID courseId) {
        List<GradeDTO> failed = gradeService.getFailedStudentsInCourse(courseId);
        return ResponseEntity.ok(failed);
    }

    @GetMapping("/course/{courseId}/ranked")
    @PreAuthorize("hasAnyAuthority('grade:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<List<GradeDTO>> getCourseGradesRanked(@PathVariable UUID courseId) {
        List<GradeDTO> ranked = gradeService.getCourseGradesRanked(courseId);
        return ResponseEntity.ok(ranked);
    }

    @GetMapping("/student/{studentId}/ranked")
    @PreAuthorize("hasAnyAuthority('grade:view', 'student:view', 'teacher:all', 'admin:all')")
    public ResponseEntity<List<GradeDTO>> getStudentGradesRanked(@PathVariable UUID studentId) {
        List<GradeDTO> ranked = gradeService.getStudentGradesRanked(studentId);
        return ResponseEntity.ok(ranked);
    }
}
