package com.example.academic.repository;

import com.example.academic.domain.Course;
import com.example.academic.domain.TeachingClass;
import com.example.academic.domain.TeachingClassStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeachingClassRepository extends JpaRepository<TeachingClass, UUID> {

    List<TeachingClass> findByName(String name);

    List<TeachingClass> findByNameContainingIgnoreCase(String name);

    List<TeachingClass> findByTeacherId(UUID teacherId);

    List<TeachingClass> findByCourse(Course course);

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.course.id = :courseId")
    List<TeachingClass> findByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.teacherId = :teacherId AND tc.course.id = :courseId")
    List<TeachingClass> findByTeacherIdAndCourseId(@Param("teacherId") UUID teacherId, @Param("courseId") UUID courseId);

    List<TeachingClass> findByStatus(TeachingClassStatus status);

    List<TeachingClass> findByAcademicYear(String academicYear);

    List<TeachingClass> findBySemesterNumber(Integer semesterNumber);

    List<TeachingClass> findByAcademicYearAndSemesterNumber(String academicYear, Integer semesterNumber);

    List<TeachingClass> findByClassroom(String classroom);

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.status IN ('PLANNED', 'OPEN_FOR_ENROLLMENT', 'ENROLLMENT_CLOSED', 'ACTIVE')")
    List<TeachingClass> findActiveTeachingClasses();

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.status = 'OPEN_FOR_ENROLLMENT' AND tc.enrolledCount < tc.capacity")
    List<TeachingClass> findAvailableForEnrollment();

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.enrolledCount >= tc.capacity")
    List<TeachingClass> findFullTeachingClasses();

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.enrolledCount = 0")
    List<TeachingClass> findEmptyTeachingClasses();

    @Query("SELECT tc FROM TeachingClass tc ORDER BY tc.name ASC")
    List<TeachingClass> findAllOrderByNameAsc();

    @Query("SELECT tc FROM TeachingClass tc ORDER BY tc.enrolledCount DESC")
    List<TeachingClass> findAllOrderByEnrolledCountDesc();

    @Query("SELECT tc FROM TeachingClass tc ORDER BY tc.createdAt DESC")
    List<TeachingClass> findAllOrderByCreatedAtDesc();

    @Query("SELECT COUNT(tc) FROM TeachingClass tc")
    long countAllTeachingClasses();

    long countByTeacherId(UUID teacherId);

    @Query("SELECT COUNT(tc) FROM TeachingClass tc WHERE tc.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);

    long countByStatus(TeachingClassStatus status);

    long countByAcademicYearAndSemesterNumber(String academicYear, Integer semesterNumber);

    @Query("SELECT SUM(tc.enrolledCount) FROM TeachingClass tc WHERE tc.teacherId = :teacherId")
    Long sumEnrolledCountByTeacherId(@Param("teacherId") UUID teacherId);

    @Query("SELECT AVG(tc.enrolledCount) FROM TeachingClass tc WHERE tc.course.id = :courseId")
    Double calculateAverageEnrollmentByCourseId(@Param("courseId") UUID courseId);

    List<TeachingClass> findByTeacherIdAndStatus(UUID teacherId, TeachingClassStatus status);

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.course.id = :courseId AND tc.status = :status")
    List<TeachingClass> findByCourseIdAndStatus(@Param("courseId") UUID courseId, @Param("status") TeachingClassStatus status);

    List<TeachingClass> findByTeacherIdAndAcademicYearAndSemesterNumber(UUID teacherId, String academicYear, Integer semesterNumber);

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.startDate BETWEEN :start AND :end")
    List<TeachingClass> findByStartDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT tc FROM TeachingClass tc WHERE tc.endDate < :date AND tc.status != 'COMPLETED'")
    List<TeachingClass> findPastDueTeachingClasses(@Param("date") LocalDateTime date);

    @Query("SELECT tc.status, COUNT(tc) FROM TeachingClass tc GROUP BY tc.status")
    List<Object[]> countTeachingClassesByStatus();

    @Query("SELECT tc.academicYear, tc.semesterNumber, COUNT(tc) FROM TeachingClass tc GROUP BY tc.academicYear, tc.semesterNumber ORDER BY tc.academicYear DESC, tc.semesterNumber DESC")
    List<Object[]> countTeachingClassesByAcademicYearAndSemester();

    @Query("SELECT DISTINCT tc.academicYear FROM TeachingClass tc ORDER BY tc.academicYear DESC")
    List<String> findAllAcademicYears();

    List<TeachingClass> findByIdIn(List<UUID> ids);
}
