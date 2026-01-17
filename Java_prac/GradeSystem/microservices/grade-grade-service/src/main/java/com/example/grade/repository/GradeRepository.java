package com.example.grade.repository;

import com.example.grade.domain.Grade;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {

    List<Grade> findByStudentId(UUID studentId);

    List<Grade> findByCourseId(UUID courseId);

    Optional<Grade> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<Grade> findByFinalScoreGreaterThanEqual(Double finalScore);

    List<Grade> findByFinalScoreLessThan(Double finalScore);

    List<Grade> findByFinalScoreBetween(Double minScore, Double maxScore);

    List<Grade> findByCourseIdAndFinalScoreGreaterThanEqual(UUID courseId, Double finalScore);

    @Query("SELECT g FROM Grade g WHERE g.studentId = :studentId ORDER BY g.finalScore DESC")
    List<Grade> findStudentGradesOrderByFinalScoreDesc(@Param("studentId") UUID studentId);

    @Query("SELECT g FROM Grade g WHERE g.courseId = :courseId ORDER BY g.finalScore DESC")
    List<Grade> findCourseGradesOrderByFinalScoreDesc(@Param("courseId") UUID courseId);

    @Query("SELECT AVG(g.finalScore) FROM Grade g WHERE g.courseId = :courseId AND g.finalScore IS NOT NULL")
    Double calculateCourseAverageFinalScore(@Param("courseId") UUID courseId);

    @Query("SELECT AVG(g.finalScore) FROM Grade g WHERE g.studentId = :studentId AND g.finalScore IS NOT NULL")
    Double calculateStudentAverageFinalScore(@Param("studentId") UUID studentId);

    @Query("SELECT g FROM Grade g WHERE g.courseId = :courseId AND g.finalScore >= 60")
    List<Grade> findPassedStudentsInCourse(@Param("courseId") UUID courseId);

    @Query("SELECT g FROM Grade g WHERE g.courseId = :courseId AND (g.finalScore < 60 OR g.finalScore IS NULL)")
    List<Grade> findFailedStudentsInCourse(@Param("courseId") UUID courseId);

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.courseId = :courseId AND g.finalScore >= 60")
    Long countPassedInCourse(@Param("courseId") UUID courseId);

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.courseId = :courseId")
    Long countTotalInCourse(@Param("courseId") UUID courseId);

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.studentId = :studentId AND g.finalScore >= 60")
    Long countPassedCoursesByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT AVG(g.gpa) FROM Grade g WHERE g.studentId = :studentId AND g.gpa IS NOT NULL")
    Double calculateStudentAverageGpa(@Param("studentId") UUID studentId);

    @Query("SELECT SUM(g.gpa) FROM Grade g WHERE g.studentId = :studentId AND g.gpa IS NOT NULL")
    Double calculateStudentTotalGpa(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.studentId = :studentId AND g.gpa IS NOT NULL")
    Long countGradesWithGpaByStudentId(@Param("studentId") UUID studentId);

    List<Grade> findByFinalScoreIsNull();

    List<Grade> findByCourseIdAndFinalScoreIsNull(UUID courseId);

    @Query("SELECT g FROM Grade g WHERE g.updatedAt >= :startTime AND g.updatedAt <= :endTime")
    List<Grade> findByUpdatedAtBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT g FROM Grade g ORDER BY g.updatedAt DESC")
    List<Grade> findRecentlyModifiedGrades();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Grade g WHERE g.id = :gradeId")
    Optional<Grade> findByIdWithPessimisticLock(@Param("gradeId") UUID gradeId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT g FROM Grade g WHERE g.id = :gradeId")
    Optional<Grade> findByIdWithPessimisticReadLock(@Param("gradeId") UUID gradeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Grade g WHERE g.studentId = :studentId")
    List<Grade> findByStudentIdWithPessimisticLock(@Param("studentId") UUID studentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Grade g WHERE g.courseId = :courseId")
    List<Grade> findByCourseIdWithPessimisticLock(@Param("courseId") UUID courseId);

    @Query("SELECT g FROM Grade g WHERE g.id = :gradeId AND g.version = :version")
    Optional<Grade> findByIdAndVersion(@Param("gradeId") UUID gradeId, @Param("version") Long version);

    @Query("SELECT DISTINCT g.courseId FROM Grade g WHERE g.studentId = :studentId")
    List<UUID> findDistinctCourseIdsByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT DISTINCT g.studentId FROM Grade g WHERE g.courseId = :courseId")
    List<UUID> findDistinctStudentIdsByCourseId(@Param("courseId") UUID courseId);
}
