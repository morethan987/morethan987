package com.example.academic.repository;

import com.example.academic.domain.Course;
import com.example.academic.domain.CourseType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByName(String name);

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    List<Course> findByNameContainingIgnoreCase(String name);

    List<Course> findByDescriptionContainingIgnoreCase(String description);

    List<Course> findByCourseType(CourseType courseType);

    List<Course> findBySemester(Integer semester);

    List<Course> findByDepartment(String department);

    List<Course> findByIsActiveTrue();

    List<Course> findByIsActiveFalse();

    @Query("SELECT c FROM Course c WHERE c.description IS NOT NULL AND c.description != ''")
    List<Course> findCoursesWithDescription();

    @Query("SELECT c FROM Course c ORDER BY c.name ASC")
    List<Course> findAllOrderByNameAsc();

    @Query("SELECT c FROM Course c ORDER BY c.courseCode ASC")
    List<Course> findAllOrderByCourseCodeAsc();

    @Query("SELECT c FROM Course c ORDER BY c.createdAt DESC")
    List<Course> findAllOrderByCreatedAtDesc();

    @Query("SELECT COUNT(c) FROM Course c")
    long countAllCourses();

    long countByCourseType(CourseType courseType);

    long countBySemester(Integer semester);

    long countByDepartment(String department);

    long countByIsActiveTrue();

    @Query("SELECT SUM(c.credit) FROM Course c WHERE c.semester = :semester")
    Double sumCreditsBySemester(@Param("semester") Integer semester);

    @Query("SELECT AVG(c.credit) FROM Course c")
    Double calculateAverageCredit();

    @Query("SELECT c FROM Course c WHERE c.credit BETWEEN :minCredit AND :maxCredit")
    List<Course> findByCreditBetween(@Param("minCredit") Double minCredit, @Param("maxCredit") Double maxCredit);

    @Query("SELECT c FROM Course c WHERE c.totalHours BETWEEN :minHours AND :maxHours")
    List<Course> findByTotalHoursBetween(@Param("minHours") Integer minHours, @Param("maxHours") Integer maxHours);

    List<Course> findByCourseTypeAndSemester(CourseType courseType, Integer semester);

    List<Course> findByDepartmentAndIsActiveTrue(String department);

    @Query("SELECT DISTINCT c.department FROM Course c WHERE c.department IS NOT NULL ORDER BY c.department")
    List<String> findAllDepartments();

    @Query("SELECT c.courseType, COUNT(c) FROM Course c GROUP BY c.courseType")
    List<Object[]> countCoursesByCourseType();

    @Query("SELECT c.semester, COUNT(c) FROM Course c GROUP BY c.semester ORDER BY c.semester")
    List<Object[]> countCoursesBySemester();

    List<Course> findByIdIn(List<UUID> ids);
}
