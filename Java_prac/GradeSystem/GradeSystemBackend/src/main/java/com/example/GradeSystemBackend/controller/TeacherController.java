package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.BatchGradeUpdateDTO;
import com.example.GradeSystemBackend.dto.DistributionDataDTO;
import com.example.GradeSystemBackend.dto.StudentGradeInputDTO;
import com.example.GradeSystemBackend.dto.TeacherDTO;
import com.example.GradeSystemBackend.dto.TeachingClassWithStatsDTO;
import com.example.GradeSystemBackend.service.TeacherService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    public TeacherDTO getTeacherByUserId(@PathVariable UUID userId) {
        return teacherService.getTeacherByUserId(userId);
    }

    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    @GetMapping("/{teacherId}/teaching-classes")
    public ResponseEntity<List<TeachingClassWithStatsDTO>> getTeachingClasses(
        @PathVariable UUID teacherId
    ) {
        List<TeachingClassWithStatsDTO> result =
            teacherService.getTeachingClasses(teacherId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    @GetMapping("/teaching-classes/{teachingClassId}/students")
    public ResponseEntity<List<StudentGradeInputDTO>> getStudentsInTeachingClass(
        @PathVariable UUID teachingClassId
    ) {
        List<StudentGradeInputDTO> result =
            teacherService.getStudentsInTeachingClass(teachingClassId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    @GetMapping("/teaching-classes/{teachingClassId}/grades/distribution")
    public ResponseEntity<List<DistributionDataDTO>> getGradeDistribution(
        @PathVariable UUID teachingClassId
    ) {
        List<DistributionDataDTO> result =
            teacherService.getGradeDistribution(teachingClassId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('grade:edit', 'admin:all')")
    @PostMapping("/teaching-classes/{teachingClassId}/grades/batch")
    public ResponseEntity<BatchGradeUpdateDTO.Response> batchUpdateGrades(
        @PathVariable UUID teachingClassId,
        @RequestBody BatchGradeUpdateDTO.Request request
    ) {
        BatchGradeUpdateDTO.Response result =
            teacherService.batchUpdateGrades(teachingClassId, request.getGrades());
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('teacher:view', 'admin:all')")
    @GetMapping("/teaching-classes/{teachingClassId}/grades/export")
    public ResponseEntity<byte[]> exportGrades(
        @PathVariable UUID teachingClassId
    ) throws IOException {
        byte[] excelFile = teacherService.exportGrades(teachingClassId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.setContentDispositionFormData("attachment", "grades.xlsx");

        return ResponseEntity.ok()
            .headers(headers)
            .body(excelFile);
    }

    @PreAuthorize("hasAnyAuthority('grade:edit', 'admin:all')")
    @PostMapping("/teaching-classes/{teachingClassId}/grades/import")
    public ResponseEntity<BatchGradeUpdateDTO.Response> importGrades(
        @PathVariable UUID teachingClassId,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        BatchGradeUpdateDTO.Response result =
            teacherService.importGrades(teachingClassId, file);
        return ResponseEntity.ok(result);
    }
}
