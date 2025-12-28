package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.teacher.Teacher;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeachingClassRepository
    extends JpaRepository<TeachingClass, UUID> {
    // 根据班级名称查找
    List<TeachingClass> findByName(String name);

    // 根据班级名称模糊查询（不区分大小写）
    List<TeachingClass> findByNameContainingIgnoreCase(String name);

    // 根据教师查找教学班
    List<TeachingClass> findByTeacher(Teacher teacher);

    // 根据教师ID查找教学班
    List<TeachingClass> findByTeacherId(UUID teacherId);

    // 根据课程查找教学班
    List<TeachingClass> findByCourse(Course course);

    // 根据课程ID查找教学班
    List<TeachingClass> findByCourseId(UUID courseId);

    // 根据教师和课程查找教学班
    List<TeachingClass> findByTeacherAndCourse(Teacher teacher, Course course);

    // 统计教学班总数
    @Query("SELECT COUNT(tc) FROM TeachingClass tc")
    long countAllTeachingClasses();

    // 统计某教师的教学班数量
    long countByTeacher(Teacher teacher);

    // 统计某课程的教学班数量
    long countByCourse(Course course);

    // 按名称排序查找所有教学班
    @Query("SELECT tc FROM TeachingClass tc ORDER BY tc.name ASC")
    List<TeachingClass> findAllOrderByNameAsc();

    // 根据学生ID查找已选课程的教学班
    @Query(
        "SELECT tc FROM TeachingClass tc " +
            "JOIN Grade g ON g.course = tc.course " +
            "WHERE g.student.id = :studentId"
    )
    List<TeachingClass> findByStudentId(@Param("studentId") UUID studentId);

    // 根据学生ID和学期查找已选课程的教学班
    @Query(
        "SELECT tc FROM TeachingClass tc " +
            "JOIN Grade g ON g.course = tc.course " +
            "WHERE g.student.id = :studentId AND tc.course.semester = :semester"
    )
    List<TeachingClass> findByStudentIdAndSemester(
        @Param("studentId") UUID studentId,
        @Param("semester") Integer semester
    );

    // === 学生相关查询（基于多对多关系）===

    // 根据学生ID查找教学班（直接通过多对多关系）
    @Query(
        "SELECT tc FROM TeachingClass tc JOIN tc.students s WHERE s.id = :studentId"
    )
    List<TeachingClass> findByStudentIdDirect(
        @Param("studentId") UUID studentId
    );

    // 根据学号查找教学班
    @Query(
        "SELECT tc FROM TeachingClass tc JOIN tc.students s WHERE s.studentCode = :studentCode"
    )
    List<TeachingClass> findByStudentCode(
        @Param("studentCode") String studentCode
    );

    // 查找没有学生的教学班
    @Query(
        "SELECT tc FROM TeachingClass tc WHERE SIZE(tc.students) = 0 OR tc.students IS NULL"
    )
    List<TeachingClass> findTeachingClassesWithoutStudents();

    // 查找学生数量达到上限的教学班
    @Query(
        "SELECT tc FROM TeachingClass tc WHERE SIZE(tc.students) >= :maxStudents"
    )
    List<TeachingClass> findFullTeachingClasses(
        @Param("maxStudents") int maxStudents
    );

    // 统计某教学班的学生数量
    @Query(
        "SELECT COUNT(s) FROM TeachingClass tc JOIN tc.students s WHERE tc.id = :teachingClassId"
    )
    long countStudentsByTeachingClassId(
        @Param("teachingClassId") UUID teachingClassId
    );

    // 查找学生数量在指定范围内的教学班
    @Query(
        "SELECT tc FROM TeachingClass tc " +
            "WHERE SIZE(tc.students) BETWEEN :minStudents AND :maxStudents"
    )
    List<TeachingClass> findByStudentCountRange(
        @Param("minStudents") int minStudents,
        @Param("maxStudents") int maxStudents
    );

    // 按学生数量排序查找教学班
    @Query("SELECT tc FROM TeachingClass tc ORDER BY SIZE(tc.students) DESC")
    List<TeachingClass> findAllOrderByStudentCountDesc();

    // 根据学生和课程查找教学班
    @Query(
        "SELECT tc FROM TeachingClass tc " +
            "JOIN tc.students s " +
            "WHERE s.id = :studentId AND tc.course.id = :courseId"
    )
    List<TeachingClass> findByStudentIdAndCourseId(
        @Param("studentId") UUID studentId,
        @Param("courseId") UUID courseId
    );

    // 根据学生和教师查找教学班
    @Query(
        "SELECT tc FROM TeachingClass tc " +
            "JOIN tc.students s " +
            "WHERE s.id = :studentId AND tc.teacher.id = :teacherId"
    )
    List<TeachingClass> findByStudentIdAndTeacherId(
        @Param("studentId") UUID studentId,
        @Param("teacherId") UUID teacherId
    );
}
