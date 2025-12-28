package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.GradeDTO;
import com.example.GradeSystemBackend.service.GradeService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    /**
     * 获取学生成绩列表
     */
    @PreAuthorize("hasAnyAuthority('grade:view')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeDTO>> getStudentGrades(
        @PathVariable UUID studentId,
        @RequestParam(required = false) String semester,
        @RequestParam(required = false) String courseType
    ) {
        printCurrentUser("getStudentGrades");
        List<GradeDTO> grades = gradeService.getStudentGrades(
            studentId,
            semester,
            courseType
        );
        return ResponseEntity.ok(grades);
    }

    /**
     * 获取学生成绩统计信息
     */
    @PreAuthorize("hasAnyAuthority('grade:view')")
    @GetMapping("/student/{studentId}/stats")
    public ResponseEntity<Map<String, Object>> getStudentGradeStats(
        @PathVariable UUID studentId
    ) {
        printCurrentUser("getStudentGradeStats");
        Map<String, Object> stats = gradeService.getStudentGradeStats(
            studentId
        );
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取学生指定学期的成绩统计
     */
    @PreAuthorize("hasAnyAuthority('grade:view')")
    @GetMapping("/student/{studentId}/semester/{semester}/stats")
    public ResponseEntity<Map<String, Object>> getStudentSemesterStats(
        @PathVariable UUID studentId,
        @PathVariable String semester
    ) {
        printCurrentUser("getStudentSemesterStats");
        Map<String, Object> stats = gradeService.getStudentSemesterStats(
            studentId,
            semester
        );
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取学生所有学期列表
     */
    @PreAuthorize("hasAnyAuthority('grade:view')")
    @GetMapping("/student/{studentId}/semesters")
    public ResponseEntity<List<String>> getStudentSemesters(
        @PathVariable UUID studentId
    ) {
        printCurrentUser("getStudentSemesters");
        List<String> semesters = gradeService.getStudentSemesters(studentId);
        return ResponseEntity.ok(semesters);
    }

    /**
     * 获取单个成绩详情
     */
    @PreAuthorize("hasAnyAuthority('grade:view')")
    @GetMapping("/{gradeId}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable UUID gradeId) {
        printCurrentUser("getGradeById");
        GradeDTO grade = gradeService.getGradeById(gradeId);
        if (grade != null) {
            return ResponseEntity.ok(grade);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 教师录入/更新成绩 - 支持乐观锁控制
     */
    @PreAuthorize("hasAnyAuthority('grade:edit')")
    @PutMapping("/{gradeId}")
    public ResponseEntity<?> updateGrade(
        @PathVariable UUID gradeId,
        @RequestBody GradeDTO gradeDTO
    ) {
        printCurrentUser("updateGrade");
        try {
            GradeDTO updatedGrade = gradeService.updateGrade(gradeId, gradeDTO);
            if (updatedGrade != null) {
                return ResponseEntity.ok(updatedGrade);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (OptimisticLockingFailureException e) {
            // 返回409 Conflict状态码，表示并发冲突
            Map<String, String> error = new HashMap<>();
            error.put("error", "CONCURRENT_MODIFICATION");
            error.put("message", e.getMessage());
            error.put("suggestion", "请刷新数据后重新提交");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            // 返回500 Internal Server Error
            Map<String, String> error = new HashMap<>();
            error.put("error", "UPDATE_FAILED");
            error.put("message", "更新成绩时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                error
            );
        }
    }

    /**
     * 批量更新成绩
     */
    @PreAuthorize("hasAnyAuthority('grade:edit')")
    @PutMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchUpdateGrades(
        @RequestBody List<GradeDTO> gradeDTOs
    ) {
        printCurrentUser("batchUpdateGrades");
        try {
            Map<String, Object> result = gradeService.batchUpdateGrades(
                gradeDTOs
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "BATCH_UPDATE_FAILED");
            error.put("message", "批量更新成绩时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                error
            );
        }
    }

    /**
     * 打印当前访问者（用于调试）
     */
    private void printCurrentUser(String method) {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[" + method + "] 当前登录用户：" + auth.getName());
        System.out.println(
            "[" + method + "] 用户权限：" + auth.getAuthorities()
        );
    }
}
