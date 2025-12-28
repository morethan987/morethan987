package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.GradeDTO;
import com.example.GradeSystemBackend.service.ConcurrencyControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 并发控制管理Controller
 * 提供并发控制监控和管理接口
 */
@RestController
@RequestMapping("/concurrency")
public class ConcurrencyController {

    @Autowired
    private ConcurrencyControlService concurrencyControlService;

    /**
     * 使用乐观锁更新成绩（带重试机制）
     */
    @PreAuthorize("hasAnyAuthority('grade:edit')")
    @PutMapping("/grades/{gradeId}/optimistic")
    public ResponseEntity<?> updateGradeWithOptimisticLock(
            @PathVariable UUID gradeId,
            @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO updatedGrade = concurrencyControlService.updateGradeWithOptimisticLock(gradeId, gradeDTO);
            if (updatedGrade != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("grade", updatedGrade);
                response.put("lockType", "OPTIMISTIC_WITH_RETRY");
                response.put("message", "成绩更新成功");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "OPTIMISTIC_UPDATE_FAILED");
            error.put("message", e.getMessage());
            error.put("suggestion", "请刷新数据后重试");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    /**
     * 使用悲观锁更新成绩
     */
    @PreAuthorize("hasAnyAuthority('grade:edit')")
    @PutMapping("/grades/{gradeId}/pessimistic")
    public ResponseEntity<?> updateGradeWithPessimisticLock(
            @PathVariable UUID gradeId,
            @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO updatedGrade = concurrencyControlService.updateGradeWithPessimisticLock(gradeId, gradeDTO);
            if (updatedGrade != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("grade", updatedGrade);
                response.put("lockType", "PESSIMISTIC");
                response.put("message", "成绩更新成功（独占锁）");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PESSIMISTIC_UPDATE_FAILED");
            error.put("message", e.getMessage());
            error.put("suggestion", "资源正在被占用，请稍后重试");
            return ResponseEntity.status(HttpStatus.LOCKED).body(error);
        }
    }

    /**
     * 批量更新成绩（使用悲观锁）
     */
    @PreAuthorize("hasAnyAuthority('grade:edit')")
    @PutMapping("/grades/batch/pessimistic")
    public ResponseEntity<Map<String, Object>> batchUpdateGradesWithPessimisticLock(
            @RequestBody List<GradeDTO> gradeDTOs) {
        try {
            Map<String, Object> result = concurrencyControlService.batchUpdateGradesWithPessimisticLock(gradeDTOs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "BATCH_PESSIMISTIC_UPDATE_FAILED");
            error.put("message", e.getMessage());
            error.put("successCount", 0);
            error.put("failureCount", gradeDTOs.size());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }

    /**
     * 使用应用级锁更新成绩
     */
    @PreAuthorize("hasAnyAuthority('grade:edit')")
    @PutMapping("/grades/{gradeId}/application-lock")
    public ResponseEntity<?> updateGradeWithApplicationLock(
            @PathVariable UUID gradeId,
            @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO updatedGrade = concurrencyControlService.updateGradeWithApplicationLock(gradeId, gradeDTO);
            if (updatedGrade != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("grade", updatedGrade);
                response.put("lockType", "APPLICATION_LEVEL");
                response.put("message", "成绩更新成功（应用级锁）");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "APPLICATION_LOCK_UPDATE_FAILED");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 使用应用级读锁查询成绩
     */
    @PreAuthorize("hasAnyAuthority('grade:view')")
    @GetMapping("/grades/{gradeId}/read-lock")
    public ResponseEntity<?> getGradeWithReadLock(@PathVariable UUID gradeId) {
        try {
            GradeDTO grade = concurrencyControlService.getGradeWithApplicationReadLock(gradeId);
            if (grade != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("grade", grade);
                response.put("lockType", "APPLICATION_READ_LOCK");
                response.put("message", "数据获取成功（读锁保护）");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "READ_LOCK_FAILED");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 检测并发冲突
     */
    @PreAuthorize("hasAnyAuthority('grade:view')")
    @GetMapping("/grades/{gradeId}/conflict-check")
    public ResponseEntity<Map<String, Object>> checkConcurrencyConflict(
            @PathVariable UUID gradeId,
            @RequestParam Long expectedVersion) {
        Map<String, Object> result = concurrencyControlService.detectConcurrencyConflicts(gradeId, expectedVersion);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取最近的成绩修改记录
     */
    @PreAuthorize("hasAnyAuthority('admin', 'grade:view')")
    @GetMapping("/grades/recent-modifications")
    public ResponseEntity<List<Map<String, Object>>> getRecentModifications(
            @RequestParam(defaultValue = "50") int limit) {
        List<Map<String, Object>> modifications = concurrencyControlService.getRecentGradeModifications(limit);
        return ResponseEntity.ok(modifications);
    }

    /**
     * 获取锁状态统计
     */
    @PreAuthorize("hasAnyAuthority('admin')")
    @GetMapping("/lock-statistics")
    public ResponseEntity<Map<String, Object>> getLockStatistics() {
        Map<String, Object> stats = concurrencyControlService.getLockStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * 清理过期锁
     */
    @PreAuthorize("hasAnyAuthority('admin')")
    @PostMapping("/cleanup-locks")
    public ResponseEntity<Map<String, Object>> cleanupExpiredLocks() {
        Map<String, Object> beforeStats = concurrencyControlService.getLockStatistics();
        concurrencyControlService.cleanupExpiredLocks();
        Map<String, Object> afterStats = concurrencyControlService.getLockStatistics();

        Map<String, Object> result = new HashMap<>();
        result.put("message", "锁清理完成");
        result.put("beforeCleanup", beforeStats);
        result.put("afterCleanup", afterStats);
        result.put("cleanedLocks", (Integer) beforeStats.get("activeLocks") - (Integer) afterStats.get("activeLocks"));

        return ResponseEntity.ok(result);
    }

    /**
     * 并发控制健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            Map<String, Object> lockStats = concurrencyControlService.getLockStatistics();
            health.put("status", "UP");
            health.put("lockStatistics", lockStats);
            health.put("features", List.of(
                "OPTIMISTIC_LOCKING",
                "PESSIMISTIC_LOCKING",
                "APPLICATION_LEVEL_LOCKING",
                "RETRY_MECHANISM",
                "CONFLICT_DETECTION"
            ));
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    /**
     * 获取并发控制配置信息
     */
    @PreAuthorize("hasAnyAuthority('admin')")
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConcurrencyConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("supportedLockTypes", List.of(
            "OPTIMISTIC", "PESSIMISTIC", "APPLICATION_LEVEL"
        ));
        config.put("defaultIsolationLevel", "READ_COMMITTED");
        config.put("retryConfiguration", Map.of(
            "maxAttempts", 3,
            "initialDelayMs", 100,
            "backoffStrategy", "EXPONENTIAL"
        ));
        config.put("lockFeatures", Map.of(
            "pessimisticReadLock", true,
            "pessimisticWriteLock", true,
            "applicationReadWriteLock", true,
            "automaticRetry", true,
            "conflictDetection", true
        ));
        config.put("recommendations", List.of(
            "使用乐观锁处理低冲突场景",
            "使用悲观锁处理高冲突场景",
            "批量操作时考虑使用悲观锁",
            "定期清理过期的应用级锁"
        ));

        return ResponseEntity.ok(config);
    }
}
