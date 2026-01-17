package com.example.academic.repository;

import com.example.academic.domain.Semester;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, UUID> {

    Optional<Semester> findByName(String name);

    Optional<Semester> findByAcademicYearAndSemesterNumber(String academicYear, Integer semesterNumber);

    List<Semester> findByAcademicYear(String academicYear);

    List<Semester> findByIsCurrentTrue();

    Optional<Semester> findFirstByIsCurrentTrue();

    @Query("SELECT s FROM Semester s ORDER BY s.academicYear DESC, s.semesterNumber DESC")
    List<Semester> findAllOrderByAcademicYearDescSemesterNumberDesc();

    @Query("SELECT s FROM Semester s WHERE s.startDate <= :date AND s.endDate >= :date")
    Optional<Semester> findActiveSemesterByDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM Semester s WHERE s.enrollmentStartDate <= :date AND s.enrollmentEndDate >= :date")
    List<Semester> findSemestersWithOpenEnrollment(@Param("date") LocalDate date);

    @Query("SELECT s FROM Semester s WHERE s.gradeSubmissionDeadline >= :date")
    List<Semester> findSemestersWithOpenGradeSubmission(@Param("date") LocalDate date);

    @Query("SELECT s FROM Semester s WHERE s.endDate < :date ORDER BY s.endDate DESC")
    List<Semester> findPastSemesters(@Param("date") LocalDate date);

    @Query("SELECT s FROM Semester s WHERE s.startDate > :date ORDER BY s.startDate ASC")
    List<Semester> findUpcomingSemesters(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT s.academicYear FROM Semester s ORDER BY s.academicYear DESC")
    List<String> findAllAcademicYears();

    long countByAcademicYear(String academicYear);

    @Modifying
    @Query("UPDATE Semester s SET s.isCurrent = false WHERE s.isCurrent = true")
    void clearCurrentSemester();

    @Modifying
    @Query("UPDATE Semester s SET s.isCurrent = true WHERE s.id = :id")
    void setCurrentSemester(@Param("id") UUID id);

    boolean existsByAcademicYearAndSemesterNumber(String academicYear, Integer semesterNumber);
}
