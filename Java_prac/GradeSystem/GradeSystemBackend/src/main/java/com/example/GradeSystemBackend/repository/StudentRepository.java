package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.student.StudentStatus;
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
    // === 基础查询方法 ===

    // 根据User查找学生
    Optional<Student> findByUser(User user);

    // 根据User ID查找学生
    @Query("SELECT s FROM Student s WHERE s.user.id = :userId")
    Optional<Student> findByUserId(@Param("userId") UUID userId);

    // 根据用户名查找学生
    @Query("SELECT s FROM Student s WHERE s.user.username = :username")
    Optional<Student> findByUsername(@Param("username") String username);

    // 根据学号查找学生
    Optional<Student> findByStudentCode(String studentCode);

    // 检查学号是否存在
    boolean existsByStudentCode(String studentCode);

    // === 专业和班级查询 ===

    // 根据专业查找学生
    List<Student> findByMajor(String major);

    // 根据班级查找学生
    List<Student> findByClassName(String className);

    // 根据专业和班级查找学生
    List<Student> findByMajorAndClassName(String major, String className);

    // 根据专业模糊查询
    List<Student> findByMajorContainingIgnoreCase(String major);

    // 根据班级模糊查询
    List<Student> findByClassNameContainingIgnoreCase(String className);

    // === 入学年份查询 ===

    // 根据入学年份查找学生
    List<Student> findByEnrollmentYear(Integer enrollmentYear);

    // 根据入学年份范围查找学生
    List<Student> findByEnrollmentYearBetween(
        Integer startYear,
        Integer endYear
    );

    // 查找指定年份之后入学的学生
    List<Student> findByEnrollmentYearGreaterThanEqual(Integer year);

    // 查找指定年份之前入学的学生
    List<Student> findByEnrollmentYearLessThanEqual(Integer year);

    // === 状态查询 ===

    // 根据学生状态查找
    List<Student> findByStatus(StudentStatus status);

    // 查找活跃状态的学生
    @Query(
        "SELECT s FROM Student s WHERE s.status IN ('ENROLLED', 'EXCHANGE', 'DEFERRED')"
    )
    List<Student> findActiveStudents();

    // 查找已离校的学生
    @Query(
        "SELECT s FROM Student s WHERE s.status IN ('WITHDRAWN', 'GRADUATED', 'TRANSFERRED', 'EXPELLED')"
    )
    List<Student> findLeftStudents();

    // 查找在读学生
    List<Student> findByStatusOrderByStudentCodeAsc(StudentStatus status);

    // === 导师相关查询 ===

    // 根据导师查找学生
    List<Student> findByAdvisor(String advisor);

    // 根据导师模糊查询
    List<Student> findByAdvisorContainingIgnoreCase(String advisor);

    // === 统计查询 ===

    // 统计学生总数
    @Query("SELECT COUNT(s) FROM Student s")
    long countAllStudents();

    // 根据状态统计学生数量
    long countByStatus(StudentStatus status);

    // 根据专业统计学生数量
    long countByMajor(String major);

    // 根据班级统计学生数量
    long countByClassName(String className);

    // 根据入学年份统计学生数量
    long countByEnrollmentYear(Integer enrollmentYear);

    // 统计活跃学生数量
    @Query(
        "SELECT COUNT(s) FROM Student s WHERE s.status IN ('ENROLLED', 'EXCHANGE', 'DEFERRED')"
    )
    long countActiveStudents();

    // === 排序查询 ===

    // 按学号排序查找学生
    @Query("SELECT s FROM Student s ORDER BY s.studentCode ASC")
    List<Student> findAllOrderByStudentCodeAsc();

    // 按专业和学号排序查找学生
    @Query("SELECT s FROM Student s ORDER BY s.major ASC, s.studentCode ASC")
    List<Student> findAllOrderByMajorAndStudentCode();

    // 按入学年份和学号排序查找学生
    @Query(
        "SELECT s FROM Student s ORDER BY s.enrollmentYear DESC, s.studentCode ASC"
    )
    List<Student> findAllOrderByEnrollmentYearAndStudentCode();

    // 按创建时间降序排序查找学生
    @Query("SELECT s FROM Student s ORDER BY s.createdAt DESC")
    List<Student> findAllOrderByCreatedAtDesc();

    // === 复合查询 ===

    // 根据专业和状态查找学生
    List<Student> findByMajorAndStatus(String major, StudentStatus status);

    // 根据入学年份和状态查找学生
    List<Student> findByEnrollmentYearAndStatus(
        Integer enrollmentYear,
        StudentStatus status
    );

    // 根据专业、班级和状态查找学生
    List<Student> findByMajorAndClassNameAndStatus(
        String major,
        String className,
        StudentStatus status
    );

    // === 时间相关查询 ===

    // 查找在指定时间之后创建的学生记录
    List<Student> findByCreatedAtAfter(LocalDateTime dateTime);

    // 查找最近注册的学生
    @Query("SELECT s FROM Student s ORDER BY s.createdAt DESC")
    List<Student> findRecentlyCreatedStudents();

    // 查找预期毕业日期在指定范围内的学生
    List<Student> findByExpectedGraduationDateBetween(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    // === 特殊查询 ===

    // 查找没有设置导师的学生
    @Query("SELECT s FROM Student s WHERE s.advisor IS NULL OR s.advisor = ''")
    List<Student> findStudentsWithoutAdvisor();

    // 检查用户是否已经是学生
    boolean existsByUser(User user);

    // 检查用户ID是否已经是学生
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.user.id = :userId")
    boolean existsByUserId(@Param("userId") UUID userId);

    // === 教学班相关查询 ===

    // 根据教学班ID查找学生
    @Query(
        "SELECT s FROM Student s JOIN s.teachingClasses tc WHERE tc.id = :teachingClassId"
    )
    List<Student> findByTeachingClassId(
        @Param("teachingClassId") UUID teachingClassId
    );

    // 根据多个教学班ID查找学生
    @Query(
        "SELECT s FROM Student s JOIN s.teachingClasses tc WHERE tc.id IN :teachingClassIds"
    )
    List<Student> findByTeachingClassIds(
        @Param("teachingClassIds") List<UUID> teachingClassIds
    );

    // 查找选修了某课程教学班的学生
    @Query(
        "SELECT DISTINCT s FROM Student s " +
            "JOIN s.teachingClasses tc " +
            "WHERE tc.course.id = :courseId"
    )
    List<Student> findByCourseId(@Param("courseId") UUID courseId);

    // 查找选修了某教师课程的学生
    @Query(
        "SELECT DISTINCT s FROM Student s " +
            "JOIN s.teachingClasses tc " +
            "WHERE tc.teacher.id = :teacherId"
    )
    List<Student> findByTeacherId(@Param("teacherId") UUID teacherId);

    // 根据教学班ID统计学生数量
    @Query(
        "SELECT COUNT(s) FROM Student s JOIN s.teachingClasses tc WHERE tc.id = :teachingClassId"
    )
    long countByTeachingClassId(@Param("teachingClassId") UUID teachingClassId);

    // 查找没有选修任何教学班的学生
    @Query(
        "SELECT s FROM Student s WHERE SIZE(s.teachingClasses) = 0 OR s.teachingClasses IS NULL"
    )
    List<Student> findStudentsWithoutTeachingClasses();

    // 查找选修了特定数量教学班的学生
    @Query("SELECT s FROM Student s WHERE SIZE(s.teachingClasses) >= :minCount")
    List<Student> findStudentsWithMinTeachingClasses(
        @Param("minCount") int minCount
    );

    // 检查学生是否选修了某教学班
    @Query(
        "SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Student s " +
            "JOIN s.teachingClasses tc " +
            "WHERE s.id = :studentId AND tc.id = :teachingClassId"
    )
    boolean isStudentInTeachingClass(
        @Param("studentId") UUID studentId,
        @Param("teachingClassId") UUID teachingClassId
    );

    // 根据学号查找学生的教学班列表
    @Query(
        "SELECT tc FROM TeachingClass tc JOIN tc.students s WHERE s.studentCode = :studentCode"
    )
    List<
        com.example.GradeSystemBackend.domain.teachingclass.TeachingClass
    > findTeachingClassesByStudentCode(
        @Param("studentCode") String studentCode
    );
}
