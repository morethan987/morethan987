package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.grade.Grade;
import com.example.GradeSystemBackend.dto.GradeDTO;
import com.example.GradeSystemBackend.repository.GradeRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 并发控制服务类
 * 提供高级并发控制功能，包括乐观锁、悲观锁、重试机制等
 */
@Service
public class ConcurrencyControlService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private GradeService gradeService;

    // 内存级别的读写锁，用于应用层面的并发控制
    private final Map<UUID, ReentrantReadWriteLock> lockMap =
        new ConcurrentHashMap<>();

    // 重试配置
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;

    /**
     * 使用乐观锁更新成绩，支持自动重试
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class
    )
    public GradeDTO updateGradeWithOptimisticLock(
        UUID gradeId,
        GradeDTO gradeDTO
    ) {
        return updateGradeWithRetry(gradeId, gradeDTO, MAX_RETRY_ATTEMPTS);
    }

    /**
     * 使用悲观锁更新成绩，确保独占访问
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED
    )
    public GradeDTO updateGradeWithPessimisticLock(
        UUID gradeId,
        GradeDTO gradeDTO
    ) {
        try {
            // 使用悲观锁获取成绩记录
            Optional<Grade> gradeOpt =
                gradeRepository.findByIdWithPessimisticLock(gradeId);

            if (!gradeOpt.isPresent()) {
                return null;
            }

            Grade grade = gradeOpt.get();

            // 更新成绩字段
            updateGradeFields(grade, gradeDTO);

            // 保存更新
            Grade savedGrade = gradeRepository.save(grade);
            return gradeService.convertToGradeDTO(savedGrade);
        } catch (PessimisticLockingFailureException e) {
            throw new RuntimeException(
                "无法获取成绩锁，资源可能正被其他用户使用: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * 批量更新成绩，使用悲观锁确保一致性
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED
    )
    public Map<String, Object> batchUpdateGradesWithPessimisticLock(
        List<GradeDTO> gradeDTOs
    ) {
        Map<String, Object> result = new HashMap<>();
        List<UUID> gradeIds = gradeDTOs
            .stream()
            .map(GradeDTO::getId)
            .filter(Objects::nonNull)
            .toList();

        try {
            // 批量获取悲观锁
            List<Grade> lockedGrades = new ArrayList<>();
            for (UUID gradeId : gradeIds) {
                Optional<Grade> gradeOpt =
                    gradeRepository.findByIdWithPessimisticLock(gradeId);
                gradeOpt.ifPresent(lockedGrades::add);
            }

            // 更新成绩
            int successCount = 0;
            int failureCount = 0;
            List<String> errors = new ArrayList<>();

            for (GradeDTO gradeDTO : gradeDTOs) {
                try {
                    Grade grade = lockedGrades
                        .stream()
                        .filter(g -> g.getId().equals(gradeDTO.getId()))
                        .findFirst()
                        .orElse(null);

                    if (grade != null) {
                        updateGradeFields(grade, gradeDTO);
                        gradeRepository.save(grade);
                        successCount++;
                    } else {
                        failureCount++;
                        errors.add(
                            "成绩ID " + gradeDTO.getId() + " 不存在或无法锁定"
                        );
                    }
                } catch (Exception e) {
                    failureCount++;
                    errors.add(
                        "成绩ID " +
                            gradeDTO.getId() +
                            " 更新失败: " +
                            e.getMessage()
                    );
                }
            }

            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("errors", errors);
            result.put("lockType", "PESSIMISTIC");
        } catch (PessimisticLockingFailureException e) {
            throw new RuntimeException(
                "批量锁定成绩失败: " + e.getMessage(),
                e
            );
        }

        return result;
    }

    /**
     * 使用应用级读写锁的成绩更新
     */
    public GradeDTO updateGradeWithApplicationLock(
        UUID gradeId,
        GradeDTO gradeDTO
    ) {
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(gradeId, k ->
            new ReentrantReadWriteLock()
        );

        lock.writeLock().lock();
        try {
            return updateGradeWithOptimisticLock(gradeId, gradeDTO);
        } finally {
            lock.writeLock().unlock();
            // 清理不再需要的锁以防内存泄漏
            if (!lock.hasQueuedThreads()) {
                lockMap.remove(gradeId);
            }
        }
    }

    /**
     * 使用应用级读锁的成绩查询
     */
    public GradeDTO getGradeWithApplicationReadLock(UUID gradeId) {
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(gradeId, k ->
            new ReentrantReadWriteLock()
        );

        lock.readLock().lock();
        try {
            return gradeService.getGradeById(gradeId);
        } finally {
            lock.readLock().unlock();
            // 清理不再需要的锁以防内存泄漏
            if (!lock.hasQueuedThreads()) {
                lockMap.remove(gradeId);
            }
        }
    }

    /**
     * 检测并发冲突
     */
    @Transactional(readOnly = true)
    public Map<String, Object> detectConcurrencyConflicts(
        UUID gradeId,
        Long expectedVersion
    ) {
        Map<String, Object> result = new HashMap<>();

        Optional<Grade> currentGrade = gradeRepository.findById(gradeId);
        if (!currentGrade.isPresent()) {
            result.put("conflict", false);
            result.put("reason", "GRADE_NOT_FOUND");
            return result;
        }

        Grade grade = currentGrade.get();
        boolean hasConflict = !Objects.equals(
            grade.getVersion(),
            expectedVersion
        );

        result.put("conflict", hasConflict);
        result.put("currentVersion", grade.getVersion());
        result.put("expectedVersion", expectedVersion);
        result.put("lastModified", grade.getUpdatedAt());

        if (hasConflict) {
            result.put("reason", "VERSION_MISMATCH");
            result.put("message", "数据已被其他用户修改");
        }

        return result;
    }

    /**
     * 获取最近的成绩修改记录
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentGradeModifications(int limit) {
        List<Grade> recentGrades = gradeRepository.findRecentlyModifiedGrades();

        return recentGrades
            .stream()
            .limit(limit)
            .map(grade -> {
                Map<String, Object> info = new HashMap<>();
                info.put("gradeId", grade.getId());
                info.put("studentId", grade.getStudent().getId());
                info.put("courseId", grade.getCourse().getId());
                info.put("version", grade.getVersion());
                info.put("lastModified", grade.getUpdatedAt());
                return info;
            })
            .toList();
    }

    /**
     * 清理过期的应用锁
     */
    public void cleanupExpiredLocks() {
        lockMap
            .entrySet()
            .removeIf(
                entry ->
                    !entry.getValue().hasQueuedThreads() &&
                    entry.getValue().getReadLockCount() == 0 &&
                    !entry.getValue().isWriteLocked()
            );
    }

    /**
     * 获取当前锁状态统计
     */
    public Map<String, Object> getLockStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeLocks", lockMap.size());
        stats.put("timestamp", LocalDateTime.now());

        long readLocks = lockMap
            .values()
            .stream()
            .mapToLong(ReentrantReadWriteLock::getReadLockCount)
            .sum();
        long writeLocks = lockMap
            .values()
            .stream()
            .mapToLong(lock -> lock.isWriteLocked() ? 1 : 0)
            .sum();

        stats.put("readLocks", readLocks);
        stats.put("writeLocks", writeLocks);

        return stats;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 带重试机制的成绩更新
     */
    private GradeDTO updateGradeWithRetry(
        UUID gradeId,
        GradeDTO gradeDTO,
        int maxAttempts
    ) {
        OptimisticLockingFailureException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return gradeService.updateGrade(gradeId, gradeDTO);
            } catch (OptimisticLockingFailureException e) {
                lastException = e;

                if (attempt < maxAttempts) {
                    try {
                        // 指数退避重试
                        Thread.sleep(RETRY_DELAY_MS * attempt);

                        // 重新获取最新版本的数据
                        GradeDTO latestGrade = gradeService.getGradeById(
                            gradeId
                        );
                        if (latestGrade != null) {
                            gradeDTO.setVersion(latestGrade.getVersion());
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new OptimisticLockingFailureException(
            "经过 " +
                maxAttempts +
                " 次重试后仍然无法更新成绩，最后一次错误: " +
                (lastException != null
                    ? lastException.getMessage()
                    : "未知错误")
        );
    }

    /**
     * 更新成绩字段的通用方法
     */
    private void updateGradeFields(Grade grade, GradeDTO gradeDTO) {
        if (gradeDTO.getUsualScore() != null) {
            grade.setUsualScore(gradeDTO.getUsualScore());
        }
        if (gradeDTO.getMidtermScore() != null) {
            grade.setMidScore(gradeDTO.getMidtermScore());
        }
        if (gradeDTO.getExperimentScore() != null) {
            grade.setExperimentScore(gradeDTO.getExperimentScore());
        }
        if (gradeDTO.getFinalExamScore() != null) {
            grade.setFinalExamScore(gradeDTO.getFinalExamScore());
        }
        // finalScore 和 gpa 会通过 @PreUpdate 自动计算
    }
}
