package com.example.academic.repository;

import com.example.academic.domain.CourseEnrollment;
import com.example.academic.domain.EnrollmentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {

    List<CourseEnrollment> findByStudentId(UUID studentId);

    List<CourseEnrollment> findByTeachingClassId(UUID teachingClassId);

    Optional<CourseEnrollment> findByStudentIdAndTeachingClassId(UUID studentId, UUID teachingClassId);

    boolean existsByStudentIdAndTeachingClassId(UUID studentId, UUID teachingClassId);

    List<CourseEnrollment> findByStatus(EnrollmentStatus status);

    List<CourseEnrollment> findByStudentIdAndStatus(UUID studentId, EnrollmentStatus status);

    List<CourseEnrollment> findByTeachingClassIdAndStatus(UUID teachingClassId, EnrollmentStatus status);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.studentId = :studentId ORDER BY ce.enrolledAt DESC")
    List<CourseEnrollment> findByStudentIdOrderByEnrolledAtDesc(@Param("studentId") UUID studentId);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.teachingClass.id = :teachingClassId ORDER BY ce.enrolledAt ASC")
    List<CourseEnrollment> findByTeachingClassIdOrderByEnrolledAtAsc(@Param("teachingClassId") UUID teachingClassId);

    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.studentId = :studentId AND ce.status = 'ENROLLED'")
    long countActiveEnrollmentsByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.teachingClass.id = :teachingClassId AND ce.status = 'ENROLLED'")
    long countActiveEnrollmentsByTeachingClassId(@Param("teachingClassId") UUID teachingClassId);

    long countByStudentId(UUID studentId);

    long countByTeachingClassId(UUID teachingClassId);

    long countByStatus(EnrollmentStatus status);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.enrolledAt BETWEEN :start AND :end")
    List<CourseEnrollment> findByEnrolledAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.droppedAt BETWEEN :start AND :end")
    List<CourseEnrollment> findByDroppedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT ce.status, COUNT(ce) FROM CourseEnrollment ce WHERE ce.studentId = :studentId GROUP BY ce.status")
    List<Object[]> countEnrollmentsByStatusForStudent(@Param("studentId") UUID studentId);

    @Query("SELECT ce.status, COUNT(ce) FROM CourseEnrollment ce WHERE ce.teachingClass.id = :teachingClassId GROUP BY ce.status")
    List<Object[]> countEnrollmentsByStatusForTeachingClass(@Param("teachingClassId") UUID teachingClassId);

    @Query("SELECT ce FROM CourseEnrollment ce JOIN ce.teachingClass tc WHERE tc.course.id = :courseId AND ce.studentId = :studentId")
    List<CourseEnrollment> findByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

    @Query("SELECT ce FROM CourseEnrollment ce JOIN ce.teachingClass tc WHERE tc.teacherId = :teacherId")
    List<CourseEnrollment> findByTeacherId(@Param("teacherId") UUID teacherId);

    @Query("SELECT ce FROM CourseEnrollment ce JOIN ce.teachingClass tc WHERE tc.academicYear = :academicYear AND tc.semesterNumber = :semesterNumber")
    List<CourseEnrollment> findByAcademicYearAndSemester(@Param("academicYear") String academicYear, @Param("semesterNumber") Integer semesterNumber);

    @Query("SELECT ce FROM CourseEnrollment ce JOIN ce.teachingClass tc WHERE ce.studentId = :studentId AND tc.academicYear = :academicYear AND tc.semesterNumber = :semesterNumber")
    List<CourseEnrollment> findByStudentIdAndAcademicYearAndSemester(@Param("studentId") UUID studentId, @Param("academicYear") String academicYear, @Param("semesterNumber") Integer semesterNumber);

    void deleteByStudentIdAndTeachingClassId(UUID studentId, UUID teachingClassId);
}
