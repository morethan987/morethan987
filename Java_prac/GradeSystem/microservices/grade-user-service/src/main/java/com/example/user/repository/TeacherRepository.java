package com.example.user.repository;

import com.example.user.domain.Teacher;
import com.example.user.domain.TeacherStatus;
import com.example.user.domain.TeacherTitle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    Optional<Teacher> findByUserId(UUID userId);

    Optional<Teacher> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByUserId(UUID userId);

    List<Teacher> findByDepartment(String department);

    List<Teacher> findByDepartmentContainingIgnoreCase(String department);

    @Query("SELECT t.department, COUNT(t) FROM Teacher t GROUP BY t.department")
    List<Object[]> countTeachersByDepartment();

    List<Teacher> findByTitle(TeacherTitle title);

    List<Teacher> findByTitleIn(List<TeacherTitle> titles);

    @Query("SELECT t FROM Teacher t WHERE t.title IN ('PROFESSOR', 'RESEARCH_PROFESSOR', 'CLINICAL_PROFESSOR', 'EMERITUS_PROFESSOR', 'VISITING_PROFESSOR', 'ADJUNCT_PROFESSOR', 'DISTINGUISHED_PROFESSOR')")
    List<Teacher> findProfessorLevelTeachers();

    @Query("SELECT t FROM Teacher t WHERE t.title != 'TEACHING_ASSISTANT'")
    List<Teacher> findTeachersCanTeachIndependently();

    @Query("SELECT t FROM Teacher t WHERE t.title IN ('PROFESSOR', 'RESEARCH_PROFESSOR', 'CLINICAL_PROFESSOR', 'EMERITUS_PROFESSOR', 'VISITING_PROFESSOR', 'DISTINGUISHED_PROFESSOR')")
    List<Teacher> findTeachersCanSuperviseDoctorate();

    List<Teacher> findByStatus(TeacherStatus status);

    @Query("SELECT t FROM Teacher t WHERE t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')")
    List<Teacher> findActiveTeachers();

    @Query("SELECT t FROM Teacher t WHERE t.status IN ('RETIRED', 'RESIGNED', 'TERMINATED', 'TRANSFERRED')")
    List<Teacher> findLeftTeachers();

    @Query("SELECT t FROM Teacher t WHERE t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')")
    List<Teacher> findTeachersCanTeach();

    List<Teacher> findByStatusOrderByEmployeeCodeAsc(TeacherStatus status);

    List<Teacher> findBySpecialization(String specialization);

    List<Teacher> findBySpecializationContainingIgnoreCase(String specialization);

    List<Teacher> findByHireDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Teacher> findByHireDateAfter(LocalDateTime date);

    List<Teacher> findByHireDateBefore(LocalDateTime date);

    @Query("SELECT t FROM Teacher t WHERE YEAR(t.hireDate) = :year")
    List<Teacher> findTeachersHiredInYear(@Param("year") Integer year);

    List<Teacher> findByWorkloadBetween(Double minWorkload, Double maxWorkload);

    List<Teacher> findByWorkloadGreaterThanEqual(Double workload);

    List<Teacher> findByWorkloadLessThan(Double workload);

    @Query("SELECT t FROM Teacher t WHERE t.maxCourses IS NOT NULL AND t.workload IS NOT NULL AND t.workload > t.maxCourses")
    List<Teacher> findOverloadedTeachers();

    @Query("SELECT t FROM Teacher t WHERE t.maxCourses IS NOT NULL AND t.workload IS NOT NULL AND t.workload < t.maxCourses")
    List<Teacher> findTeachersCanTeachMore();

    List<Teacher> findBySalaryBetween(Double minSalary, Double maxSalary);

    List<Teacher> findBySalaryGreaterThanEqual(Double salary);

    List<Teacher> findBySalaryLessThan(Double salary);

    List<Teacher> findByOffice(String office);

    List<Teacher> findByOfficeContainingIgnoreCase(String office);

    Optional<Teacher> findByOfficePhone(String officePhone);

    @Query("SELECT COUNT(t) FROM Teacher t")
    long countAllTeachers();

    long countByStatus(TeacherStatus status);

    long countByTitle(TeacherTitle title);

    long countByDepartment(String department);

    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')")
    long countActiveTeachers();

    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.title IN ('PROFESSOR', 'RESEARCH_PROFESSOR', 'CLINICAL_PROFESSOR', 'EMERITUS_PROFESSOR', 'VISITING_PROFESSOR', 'ADJUNCT_PROFESSOR', 'DISTINGUISHED_PROFESSOR')")
    long countProfessorLevelTeachers();

    @Query("SELECT AVG(t.salary) FROM Teacher t WHERE t.salary IS NOT NULL")
    Double calculateAverageSalary();

    @Query("SELECT AVG(t.salary) FROM Teacher t WHERE t.department = :department AND t.salary IS NOT NULL")
    Double calculateAverageSalaryByDepartment(@Param("department") String department);

    @Query("SELECT AVG(t.salary) FROM Teacher t WHERE t.title = :title AND t.salary IS NOT NULL")
    Double calculateAverageSalaryByTitle(@Param("title") TeacherTitle title);

    @Query("SELECT AVG(t.workload) FROM Teacher t WHERE t.workload IS NOT NULL")
    Double calculateAverageWorkload();

    @Query("SELECT AVG(YEAR(CURRENT_DATE) - YEAR(t.hireDate)) FROM Teacher t WHERE t.hireDate IS NOT NULL")
    Double calculateAverageYearsOfService();

    @Query("SELECT t FROM Teacher t ORDER BY t.employeeCode ASC")
    List<Teacher> findAllOrderByEmployeeCodeAsc();

    @Query("SELECT t FROM Teacher t ORDER BY t.department ASC, t.employeeCode ASC")
    List<Teacher> findAllOrderByDepartmentAndEmployeeCode();

    @Query("SELECT t FROM Teacher t ORDER BY t.title DESC, t.employeeCode ASC")
    List<Teacher> findAllOrderByTitleAndEmployeeCode();

    @Query("SELECT t FROM Teacher t ORDER BY t.hireDate ASC")
    List<Teacher> findAllOrderByHireDateAsc();

    @Query("SELECT t FROM Teacher t WHERE t.salary IS NOT NULL ORDER BY t.salary DESC")
    List<Teacher> findAllOrderBySalaryDesc();

    @Query("SELECT t FROM Teacher t ORDER BY t.createdAt DESC")
    List<Teacher> findAllOrderByCreatedAtDesc();

    List<Teacher> findByDepartmentAndStatus(String department, TeacherStatus status);

    List<Teacher> findByTitleAndStatus(TeacherTitle title, TeacherStatus status);

    List<Teacher> findByDepartmentAndTitle(String department, TeacherTitle title);

    List<Teacher> findByDepartmentAndTitleAndStatus(String department, TeacherTitle title, TeacherStatus status);

    @Query("SELECT t FROM Teacher t WHERE t.department = :department AND t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')")
    List<Teacher> findActiveTeachersByDepartment(@Param("department") String department);

    @Query("SELECT t FROM Teacher t WHERE t.title = :title AND t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')")
    List<Teacher> findActiveTeachersByTitle(@Param("title") TeacherTitle title);

    List<Teacher> findByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT t FROM Teacher t ORDER BY t.createdAt DESC")
    List<Teacher> findRecentlyCreatedTeachers();

    @Query("SELECT t FROM Teacher t WHERE t.hireDate IS NOT NULL AND YEAR(CURRENT_DATE) - YEAR(t.hireDate) BETWEEN :minYears AND :maxYears")
    List<Teacher> findByYearsOfServiceBetween(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    @Query("SELECT t FROM Teacher t WHERE t.hireDate IS NOT NULL AND YEAR(CURRENT_DATE) - YEAR(t.hireDate) >= 30 AND t.status = 'ACTIVE'")
    List<Teacher> findTeachersNearRetirement();

    @Query("SELECT t FROM Teacher t WHERE t.salary IS NULL")
    List<Teacher> findTeachersWithoutSalary();

    @Query("SELECT t FROM Teacher t WHERE t.office IS NULL OR t.office = ''")
    List<Teacher> findTeachersWithoutOffice();

    @Query("SELECT t FROM Teacher t WHERE t.workload IS NULL")
    List<Teacher> findTeachersWithoutWorkload();

    @Query("SELECT t FROM Teacher t WHERE t.specialization IS NULL OR t.specialization = ''")
    List<Teacher> findTeachersWithoutSpecialization();

    @Query("SELECT t FROM Teacher t WHERE t.department = :department AND t.id != :excludeId")
    List<Teacher> findOtherTeachersInSameDepartment(@Param("department") String department, @Param("excludeId") UUID excludeId);

    @Query("SELECT t FROM Teacher t WHERE t.specialization = :specialization AND t.id != :excludeId")
    List<Teacher> findTeachersWithSameSpecialization(@Param("specialization") String specialization, @Param("excludeId") UUID excludeId);

    List<Teacher> findByIdIn(List<UUID> ids);
}
