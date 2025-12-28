package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.grade.Grade;
import com.example.GradeSystemBackend.domain.student.Student;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    // 根据学生查找所有成绩
    List<Grade> findByStudent(Student student);

    // 根据学生ID查找所有成绩
    List<Grade> findByStudentId(UUID studentId);

    // 根据课程查找所有成绩
    List<Grade> findByCourse(Course course);

    // 根据课程ID查找所有成绩
    List<Grade> findByCourseId(UUID courseId);

    // 根据学生和课程查找成绩（唯一记录）
    Optional<Grade> findByStudentAndCourse(Student student, Course course);

    // 查找期末成绩大于等于指定值的学生
    List<Grade> findByFinalScoreGreaterThanEqual(Double finalScore);

    // 查找期末成绩小于指定值的学生
    List<Grade> findByFinalScoreLessThan(Double finalScore);

    // 查找期末成绩在指定范围内的学生
    List<Grade> findByFinalScoreBetween(Double minScore, Double maxScore);

    // 查找某课程中期末成绩大于等于指定值的学生
    List<Grade> findByCourseAndFinalScoreGreaterThanEqual(
        Course course,
        Double finalScore
    );

    // 查找某学生的所有课程的期末成绩
    @Query(
        "SELECT g FROM Grade g WHERE g.student.id = :studentId ORDER BY g.finalScore DESC"
    )
    List<Grade> findStudentGradesOrderByFinalScoreDesc(
        @Param("studentId") UUID studentId
    );

    // 查找某课程所有学生的期末成绩
    @Query(
        "SELECT g FROM Grade g WHERE g.course.id = :courseId ORDER BY g.finalScore DESC"
    )
    List<Grade> findCourseGradesOrderByFinalScoreDesc(
        @Param("courseId") UUID courseId
    );

    // 计算某课程的平均期末成绩
    @Query(
        "SELECT AVG(g.finalScore) FROM Grade g WHERE g.course.id = :courseId AND g.finalScore IS NOT NULL"
    )
    Double calculateCourseAverageFinalScore(@Param("courseId") UUID courseId);

    // 计算某学生的平均期末成绩
    @Query(
        "SELECT AVG(g.finalScore) FROM Grade g WHERE g.student.id = :studentId AND g.finalScore IS NOT NULL"
    )
    Double calculateStudentAverageFinalScore(
        @Param("studentId") UUID studentId
    );

    // 查找某课程中及格的学生
    @Query(
        "SELECT g FROM Grade g WHERE g.course.id = :courseId AND g.finalScore >= 60"
    )
    List<Grade> findPassedStudentsInCourse(@Param("courseId") UUID courseId);

    // 查找某课程中不及格的学生
    @Query(
        "SELECT g FROM Grade g WHERE g.course.id = :courseId AND (g.finalScore < 60 OR g.finalScore IS NULL)"
    )
    List<Grade> findFailedStudentsInCourse(@Param("courseId") UUID courseId);

    // 统计某课程的及格率
    @Query(
        "SELECT (SELECT COUNT(g) FROM Grade g WHERE g.course.id = :courseId AND g.finalScore >= 60) * 100.0 / " +
            "(SELECT COUNT(g) FROM Grade g WHERE g.course.id = :courseId) " +
            "FROM Grade g WHERE g.course.id = :courseId"
    )
    Double calculateCoursePassRate(@Param("courseId") UUID courseId);

    // 查找某学生的最高成绩
    @Query(
        "SELECT g FROM Grade g WHERE g.student.id = :studentId AND g.finalScore IS NOT NULL ORDER BY g.finalScore DESC"
    )
    List<Grade> findStudentHighestGrade(@Param("studentId") UUID studentId);

    // 查找某课程的最高成绩
    @Query(
        "SELECT g FROM Grade g WHERE g.course.id = :courseId AND g.finalScore IS NOT NULL ORDER BY g.finalScore DESC"
    )
    List<Grade> findCourseHighestGrade(@Param("courseId") UUID courseId);

    // 检查学生是否已有某课程的成绩记录
    boolean existsByStudentAndCourse(Student student, Course course);

    // 查找所有缺少期末成绩的记录
    List<Grade> findByFinalScoreIsNull();

    // 查找某课程中缺少期末成绩的记录
    List<Grade> findByCourseAndFinalScoreIsNull(Course course);

    // 计算某学生总学分
    @Query(
        """
            select
                sum(c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and g.finalScore is not null
        """
    )
    Double calculateStudentTotalCredits(@Param("studentId") UUID studentId);

    // 计算某学生某学期总学分
    @Query(
        """
            select
                sum(c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and c.semester = :semester
              and g.finalScore is not null
        """
    )
    Double calculateStudentTotalCreditsBySemester(
        @Param("studentId") UUID studentId,
        @Param("semester") String semester
    );

    // 计算某学生某学期之前的学期的总学分
    @Query(
        """
            select
                sum(c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and c.semester < :semester
              and g.finalScore is not null
        """
    )
    Double calculateStudentTotalCreditsBeforeSemester(
        @Param("studentId") UUID studentId,
        @Param("semester") String semester
    );

    // 计算某学生加权总分
    @Query(
        """
            select
                sum(g.finalScore * c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and g.finalScore is not null
        """
    )
    Double calculateStudentWeightedScore(@Param("studentId") UUID studentId);

    // 计算某学生某学期加权总分
    @Query(
        """
            select
                sum(g.finalScore * c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and c.semester = :semester
              and g.finalScore is not null
        """
    )
    Double calculateStudentWeightedScoreBySemester(
        @Param("studentId") UUID studentId,
        @Param("semester") String semester
    );

    // 计算某学生某学期之前的学期的加权总分
    @Query(
        """
            select
                sum(g.finalScore * c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and c.semester < :semester
              and g.finalScore is not null
        """
    )
    Double calculateStudentWeightedScoreBeforeSemester(
        @Param("studentId") UUID studentId,
        @Param("semester") String semester
    );

    // 计算某学生的加权总GPA
    @Query(
        """
            select
                sum(g.gpa * c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and g.gpa is not null
        """
    )
    Double calculateStudentWeightedGpa(@Param("studentId") UUID studentId);

    // 计算某学生某学期加权总gpa
    @Query(
        """
            select
                sum(g.gpa * c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and c.semester = :semester
              and g.gpa is not null
        """
    )
    Double calculateStudentWeightedGpaBySemester(
        @Param("studentId") UUID studentId,
        @Param("semester") String semester
    );

    // 计算某学生某学期之前的学期的加权总gpa
    @Query(
        """
            select
                sum(g.gpa * c.credit)
            from Grade g
            join g.course c
            where g.student.id = :studentId
              and c.semester < :semester
              and g.gpa is not null
        """
    )
    Double calculateStudentWeightedGpaBeforeSemester(
        @Param("studentId") UUID studentId,
        @Param("semester") String semester
    );

    // 统计某学生通过的课程数
    @Query(
        "SELECT COUNT(g) FROM Grade g WHERE g.student.id = :studentId AND g.finalScore >= 60"
    )
    Long countPassedCoursesByStudentId(@Param("studentId") UUID studentId);

    // 获取学生所有学期列表
    @Query(
        "SELECT DISTINCT CAST(c.semester AS string) FROM Grade g JOIN g.course c WHERE g.student.id = :studentId ORDER BY CAST(c.semester AS string)"
    )
    List<String> findDistinctSemestersByStudentId(
        @Param("studentId") UUID studentId
    );
}
