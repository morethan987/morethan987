package com.example.user.repository;

import com.example.user.domain.Student;
import com.example.user.domain.StudentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByUserId(UUID userId);

    Optional<Student> findByStudentCode(String studentCode);

    boolean existsByStudentCode(String studentCode);

    boolean existsByUserId(UUID userId);

    List<Student> findByMajor(String major);

    List<Student> findByClassName(String className);

    List<Student> findByMajorAndClassName(String major, String className);

    List<Student> findByMajorContainingIgnoreCase(String major);

    List<Student> findByClassNameContainingIgnoreCase(String className);

    List<Student> findByEnrollmentYear(Integer enrollmentYear);

    List<Student> findByEnrollmentYearBetween(Integer startYear, Integer endYear);

    List<Student> findByEnrollmentYearGreaterThanEqual(Integer year);

    List<Student> findByEnrollmentYearLessThanEqual(Integer year);

    List<Student> findByStatus(StudentStatus status);

    @Query("SELECT s FROM Student s WHERE s.status IN ('ENROLLED', 'EXCHANGE', 'DEFERRED')")
    List<Student> findActiveStudents();

    @Query("SELECT s FROM Student s WHERE s.status IN ('WITHDRAWN', 'GRADUATED', 'TRANSFERRED', 'EXPELLED')")
    List<Student> findLeftStudents();

    List<Student> findByStatusOrderByStudentCodeAsc(StudentStatus status);

    List<Student> findByAdvisor(String advisor);

    List<Student> findByAdvisorContainingIgnoreCase(String advisor);

    @Query("SELECT COUNT(s) FROM Student s")
    long countAllStudents();

    long countByStatus(StudentStatus status);

    long countByMajor(String major);

    long countByClassName(String className);

    long countByEnrollmentYear(Integer enrollmentYear);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.status IN ('ENROLLED', 'EXCHANGE', 'DEFERRED')")
    long countActiveStudents();

    @Query("SELECT s FROM Student s ORDER BY s.studentCode ASC")
    List<Student> findAllOrderByStudentCodeAsc();

    @Query("SELECT s FROM Student s ORDER BY s.major ASC, s.studentCode ASC")
    List<Student> findAllOrderByMajorAndStudentCode();

    @Query("SELECT s FROM Student s ORDER BY s.enrollmentYear DESC, s.studentCode ASC")
    List<Student> findAllOrderByEnrollmentYearAndStudentCode();

    @Query("SELECT s FROM Student s ORDER BY s.createdAt DESC")
    List<Student> findAllOrderByCreatedAtDesc();

    List<Student> findByMajorAndStatus(String major, StudentStatus status);

    List<Student> findByEnrollmentYearAndStatus(Integer enrollmentYear, StudentStatus status);

    List<Student> findByMajorAndClassNameAndStatus(String major, String className, StudentStatus status);

    List<Student> findByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT s FROM Student s ORDER BY s.createdAt DESC")
    List<Student> findRecentlyCreatedStudents();

    List<Student> findByExpectedGraduationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM Student s WHERE s.advisor IS NULL OR s.advisor = ''")
    List<Student> findStudentsWithoutAdvisor();

    List<Student> findByIdIn(List<UUID> ids);
}
