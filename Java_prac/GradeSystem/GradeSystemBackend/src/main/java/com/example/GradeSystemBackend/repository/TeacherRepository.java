package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.teacher.Teacher;
import com.example.GradeSystemBackend.domain.teacher.TeacherStatus;
import com.example.GradeSystemBackend.domain.teacher.TeacherTitle;
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
    // === 基础查询方法 ===

    // 根据User查找教师
    Optional<Teacher> findByUser(User user);

    // 根据User ID查找教师
    @Query("SELECT t FROM Teacher t WHERE t.user.id = :userId")
    Optional<Teacher> findByUserId(@Param("userId") UUID userId);

    // 根据用户名查找教师
    @Query("SELECT t FROM Teacher t WHERE t.user.username = :username")
    Optional<Teacher> findByUsername(@Param("username") String username);

    // 根据工号查找教师
    Optional<Teacher> findByEmployeeCode(String employeeCode);

    // 检查工号是否存在
    boolean existsByEmployeeCode(String employeeCode);

    // === 部门查询 ===

    // 根据部门查找教师
    List<Teacher> findByDepartment(String department);

    // 根据部门模糊查询
    List<Teacher> findByDepartmentContainingIgnoreCase(String department);

    // 统计各部门教师数量
    @Query("SELECT t.department, COUNT(t) FROM Teacher t GROUP BY t.department")
    List<Object[]> countTeachersByDepartment();

    // === 职称查询 ===

    // 根据职称查找教师
    List<Teacher> findByTitle(TeacherTitle title);

    // 根据职称列表查找教师
    List<Teacher> findByTitleIn(List<TeacherTitle> titles);

    // 查找教授级别的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.title IN ('PROFESSOR', 'RESEARCH_PROFESSOR', 'CLINICAL_PROFESSOR', 'EMERITUS_PROFESSOR', 'VISITING_PROFESSOR', 'ADJUNCT_PROFESSOR', 'DISTINGUISHED_PROFESSOR')"
    )
    List<Teacher> findProfessorLevelTeachers();

    // 查找有独立授课资格的教师
    @Query("SELECT t FROM Teacher t WHERE t.title != 'TEACHING_ASSISTANT'")
    List<Teacher> findTeachersCanTeachIndependently();

    // 查找可以指导博士生的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.title IN ('PROFESSOR', 'RESEARCH_PROFESSOR', 'CLINICAL_PROFESSOR', 'EMERITUS_PROFESSOR', 'VISITING_PROFESSOR', 'DISTINGUISHED_PROFESSOR')"
    )
    List<Teacher> findTeachersCanSuperviseDoctorate();

    // === 状态查询 ===

    // 根据教师状态查找
    List<Teacher> findByStatus(TeacherStatus status);

    // 查找活跃状态的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')"
    )
    List<Teacher> findActiveTeachers();

    // 查找已离职的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.status IN ('RETIRED', 'RESIGNED', 'TERMINATED', 'TRANSFERRED')"
    )
    List<Teacher> findLeftTeachers();

    // 查找可以教学的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')"
    )
    List<Teacher> findTeachersCanTeach();

    // 查找在职教师（按工号排序）
    List<Teacher> findByStatusOrderByEmployeeCodeAsc(TeacherStatus status);

    // === 专业领域查询 ===

    // 根据专业领域查找教师
    List<Teacher> findBySpecialization(String specialization);

    // 根据专业领域模糊查询
    List<Teacher> findBySpecializationContainingIgnoreCase(
        String specialization
    );

    // === 入职时间查询 ===

    // 根据入职时间范围查找教师
    List<Teacher> findByHireDateBetween(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    // 查找指定时间之后入职的教师
    List<Teacher> findByHireDateAfter(LocalDateTime date);

    // 查找指定时间之前入职的教师
    List<Teacher> findByHireDateBefore(LocalDateTime date);

    // 查找指定年份入职的教师
    @Query("SELECT t FROM Teacher t WHERE YEAR(t.hireDate) = :year")
    List<Teacher> findTeachersHiredInYear(@Param("year") Integer year);

    // === 工作量查询 ===

    // 根据工作量范围查找教师
    List<Teacher> findByWorkloadBetween(Double minWorkload, Double maxWorkload);

    // 查找工作量大于等于指定值的教师
    List<Teacher> findByWorkloadGreaterThanEqual(Double workload);

    // 查找工作量小于指定值的教师
    List<Teacher> findByWorkloadLessThan(Double workload);

    // 查找超负荷工作的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.maxCourses IS NOT NULL AND t.workload IS NOT NULL AND t.workload > t.maxCourses"
    )
    List<Teacher> findOverloadedTeachers();

    // 查找还能承担更多课程的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.maxCourses IS NOT NULL AND t.workload IS NOT NULL AND t.workload < t.maxCourses"
    )
    List<Teacher> findTeachersCanTeachMore();

    // === 薪资查询 ===

    // 根据薪资范围查找教师
    List<Teacher> findBySalaryBetween(Double minSalary, Double maxSalary);

    // 查找薪资大于等于指定值的教师
    List<Teacher> findBySalaryGreaterThanEqual(Double salary);

    // 查找薪资小于指定值的教师
    List<Teacher> findBySalaryLessThan(Double salary);

    // === 办公信息查询 ===

    // 根据办公室查找教师
    List<Teacher> findByOffice(String office);

    // 根据办公室模糊查询
    List<Teacher> findByOfficeContainingIgnoreCase(String office);

    // 根据办公电话查找教师
    Optional<Teacher> findByOfficePhone(String officePhone);

    // === 统计查询 ===

    // 统计教师总数
    @Query("SELECT COUNT(t) FROM Teacher t")
    long countAllTeachers();

    // 根据状态统计教师数量
    long countByStatus(TeacherStatus status);

    // 根据职称统计教师数量
    long countByTitle(TeacherTitle title);

    // 根据部门统计教师数量
    long countByDepartment(String department);

    // 统计活跃教师数量
    @Query(
        "SELECT COUNT(t) FROM Teacher t WHERE t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')"
    )
    long countActiveTeachers();

    // 统计教授级别教师数量
    @Query(
        "SELECT COUNT(t) FROM Teacher t WHERE t.title IN ('PROFESSOR', 'RESEARCH_PROFESSOR', 'CLINICAL_PROFESSOR', 'EMERITUS_PROFESSOR', 'VISITING_PROFESSOR', 'ADJUNCT_PROFESSOR', 'DISTINGUISHED_PROFESSOR')"
    )
    long countProfessorLevelTeachers();

    // === 平均值计算 ===

    // 计算所有教师的平均薪资
    @Query("SELECT AVG(t.salary) FROM Teacher t WHERE t.salary IS NOT NULL")
    Double calculateAverageSalary();

    // 根据部门计算平均薪资
    @Query(
        "SELECT AVG(t.salary) FROM Teacher t WHERE t.department = :department AND t.salary IS NOT NULL"
    )
    Double calculateAverageSalaryByDepartment(
        @Param("department") String department
    );

    // 根据职称计算平均薪资
    @Query(
        "SELECT AVG(t.salary) FROM Teacher t WHERE t.title = :title AND t.salary IS NOT NULL"
    )
    Double calculateAverageSalaryByTitle(@Param("title") TeacherTitle title);

    // 计算平均工作量
    @Query("SELECT AVG(t.workload) FROM Teacher t WHERE t.workload IS NOT NULL")
    Double calculateAverageWorkload();

    // 计算平均工作年限
    @Query(
        "SELECT AVG(YEAR(CURRENT_DATE) - YEAR(t.hireDate)) FROM Teacher t WHERE t.hireDate IS NOT NULL"
    )
    Double calculateAverageYearsOfService();

    // === 排序查询 ===

    // 按工号排序查找教师
    @Query("SELECT t FROM Teacher t ORDER BY t.employeeCode ASC")
    List<Teacher> findAllOrderByEmployeeCodeAsc();

    // 按部门和工号排序查找教师
    @Query(
        "SELECT t FROM Teacher t ORDER BY t.department ASC, t.employeeCode ASC"
    )
    List<Teacher> findAllOrderByDepartmentAndEmployeeCode();

    // 按职称等级和工号排序查找教师
    @Query("SELECT t FROM Teacher t ORDER BY t.title DESC, t.employeeCode ASC")
    List<Teacher> findAllOrderByTitleAndEmployeeCode();

    // 按入职时间排序查找教师
    @Query("SELECT t FROM Teacher t ORDER BY t.hireDate ASC")
    List<Teacher> findAllOrderByHireDateAsc();

    // 按薪资降序排序查找教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.salary IS NOT NULL ORDER BY t.salary DESC"
    )
    List<Teacher> findAllOrderBySalaryDesc();

    // 按创建时间降序排序查找教师
    @Query("SELECT t FROM Teacher t ORDER BY t.createdAt DESC")
    List<Teacher> findAllOrderByCreatedAtDesc();

    // === 复合查询 ===

    // 根据部门和状态查找教师
    List<Teacher> findByDepartmentAndStatus(
        String department,
        TeacherStatus status
    );

    // 根据职称和状态查找教师
    List<Teacher> findByTitleAndStatus(
        TeacherTitle title,
        TeacherStatus status
    );

    // 根据部门和职称查找教师
    List<Teacher> findByDepartmentAndTitle(
        String department,
        TeacherTitle title
    );

    // 根据部门、职称和状态查找教师
    List<Teacher> findByDepartmentAndTitleAndStatus(
        String department,
        TeacherTitle title,
        TeacherStatus status
    );

    // 查找指定部门中的活跃教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.department = :department AND t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')"
    )
    List<Teacher> findActiveTeachersByDepartment(
        @Param("department") String department
    );

    // 查找指定职称中的活跃教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.title = :title AND t.status IN ('ACTIVE', 'TEMPORARY', 'PART_TIME', 'VISITING')"
    )
    List<Teacher> findActiveTeachersByTitle(@Param("title") TeacherTitle title);

    // === 时间相关查询 ===

    // 查找在指定时间之后创建的教师记录
    List<Teacher> findByCreatedAtAfter(LocalDateTime dateTime);

    // 查找最近入职的教师
    @Query("SELECT t FROM Teacher t ORDER BY t.createdAt DESC")
    List<Teacher> findRecentlyCreatedTeachers();

    // 查找工作年限在指定范围内的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.hireDate IS NOT NULL AND YEAR(CURRENT_DATE) - YEAR(t.hireDate) BETWEEN :minYears AND :maxYears"
    )
    List<Teacher> findByYearsOfServiceBetween(
        @Param("minYears") Integer minYears,
        @Param("maxYears") Integer maxYears
    );

    // 查找即将退休的教师（工作年限 >= 30年）
    @Query(
        "SELECT t FROM Teacher t WHERE t.hireDate IS NOT NULL AND YEAR(CURRENT_DATE) - YEAR(t.hireDate) >= 30 AND t.status = 'ACTIVE'"
    )
    List<Teacher> findTeachersNearRetirement();

    // === 特殊查询 ===

    // 查找没有设置薪资的教师
    @Query("SELECT t FROM Teacher t WHERE t.salary IS NULL")
    List<Teacher> findTeachersWithoutSalary();

    // 查找没有设置办公室的教师
    @Query("SELECT t FROM Teacher t WHERE t.office IS NULL OR t.office = ''")
    List<Teacher> findTeachersWithoutOffice();

    // 查找没有设置工作量的教师
    @Query("SELECT t FROM Teacher t WHERE t.workload IS NULL")
    List<Teacher> findTeachersWithoutWorkload();

    // 查找没有设置专业领域的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.specialization IS NULL OR t.specialization = ''"
    )
    List<Teacher> findTeachersWithoutSpecialization();

    // 查找可以晋升的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.hireDate IS NOT NULL AND t.status = 'ACTIVE' AND " +
            "((t.title = 'TEACHING_ASSISTANT' AND YEAR(CURRENT_DATE) - YEAR(t.hireDate) >= 3) OR " +
            "(t.title = 'LECTURER' AND YEAR(CURRENT_DATE) - YEAR(t.hireDate) >= 3) OR " +
            "(t.title = 'ASSISTANT_PROFESSOR' AND YEAR(CURRENT_DATE) - YEAR(t.hireDate) >= 6) OR " +
            "(t.title = 'ASSOCIATE_PROFESSOR' AND YEAR(CURRENT_DATE) - YEAR(t.hireDate) >= 12))"
    )
    List<Teacher> findTeachersEligibleForPromotion();

    // 检查用户是否已经是教师
    boolean existsByUser(User user);

    // 检查用户ID是否已经是教师
    @Query("SELECT COUNT(t) > 0 FROM Teacher t WHERE t.user.id = :userId")
    boolean existsByUserId(@Param("userId") UUID userId);

    // 查找同一部门的其他教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.department = :department AND t.id != :excludeId"
    )
    List<Teacher> findOtherTeachersInSameDepartment(
        @Param("department") String department,
        @Param("excludeId") UUID excludeId
    );

    // 查找具有相同专业领域的教师
    @Query(
        "SELECT t FROM Teacher t WHERE t.specialization = :specialization AND t.id != :excludeId"
    )
    List<Teacher> findTeachersWithSameSpecialization(
        @Param("specialization") String specialization,
        @Param("excludeId") UUID excludeId
    );
}
