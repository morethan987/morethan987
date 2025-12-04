package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.TeachingClass;
import com.example.GradeSystemBackend.domain.Teacher;
import com.example.GradeSystemBackend.domain.Course;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeachingClassRepository extends JpaRepository<TeachingClass, UUID> {
    // 根据班级名称查找
    List<TeachingClass> findByName(String name);

    // 根据班级名称模糊查询（不区分大小写）
    List<TeachingClass> findByNameContainingIgnoreCase(String name);

    // 根据学期查找教学班
    List<TeachingClass> findBySemester(Integer semester);

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

    // 根据教师和学期查找教学班
    List<TeachingClass> findByTeacherAndSemester(Teacher teacher, Integer semester);

    // 根据课程和学期查找教学班
    List<TeachingClass> findByCourseAndSemester(Course course, Integer semester);

    // 根据教师、课程和学期查找教学班
    Optional<TeachingClass> findByTeacherAndCourseAndSemester(
        Teacher teacher,
        Course course,
        Integer semester
    );

    // 根据名称和学期查找教学班
    List<TeachingClass> findByNameContainingIgnoreCaseAndSemester(
        String name,
        Integer semester
    );

    // 统计教学班总数
    @Query("SELECT COUNT(tc) FROM TeachingClass tc")
    long countAllTeachingClasses();

    // 统计某教师的教学班数量
    long countByTeacher(Teacher teacher);

    // 统计某课程的教学班数量
    long countByCourse(Course course);

    // 统计某学期的教学班数量
    long countBySemester(Integer semester);

    // 查找某教师在某学期的所有教学班
    @Query("SELECT tc FROM TeachingClass tc WHERE tc.teacher.id = :teacherId AND tc.semester = :semester ORDER BY tc.name ASC")
    List<TeachingClass> findTeacherClassesInSemester(
        @Param("teacherId") UUID teacherId,
        @Param("semester") Integer semester
    );

    // 查找某课程在某学期的所有教学班
    @Query("SELECT tc FROM TeachingClass tc WHERE tc.course.id = :courseId AND tc.semester = :semester ORDER BY tc.name ASC")
    List<TeachingClass> findCourseClassesInSemester(
        @Param("courseId") UUID courseId,
        @Param("semester") Integer semester
    );

    // 按学期排序查找所有教学班
    @Query("SELECT tc FROM TeachingClass tc ORDER BY tc.semester DESC")
    List<TeachingClass> findAllOrderBySemesterDesc();

    // 按名称排序查找所有教学班
    @Query("SELECT tc FROM TeachingClass tc ORDER BY tc.name ASC")
    List<TeachingClass> findAllOrderByNameAsc();

    // 检查教师是否在某学期已有某课程的教学班
    boolean existsByTeacherAndCourseAndSemester(
        Teacher teacher,
        Course course,
        Integer semester
    );

    // 查找最新学期的教学班
    @Query("SELECT tc FROM TeachingClass tc WHERE tc.semester = (SELECT MAX(tc2.semester) FROM TeachingClass tc2)")
    List<TeachingClass> findLatestSemesterClasses();
}
