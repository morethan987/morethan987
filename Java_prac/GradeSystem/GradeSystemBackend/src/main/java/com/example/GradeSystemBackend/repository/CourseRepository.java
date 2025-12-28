package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.course.CourseType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository
    extends JpaRepository<Course, java.util.UUID> {
    // 根据课程名称查找
    Optional<Course> findByName(String name);

    // 根据课程名称模糊查询
    List<Course> findByNameContainingIgnoreCase(String name);

    // 根据描述模糊查询
    List<Course> findByDescriptionContainingIgnoreCase(String description);

    // 查找所有有描述的课程
    @Query(
        "SELECT c FROM Course c WHERE c.description IS NOT NULL AND c.description != ''"
    )
    List<Course> findCoursesWithDescription();

    // 统计课程总数
    @Query("SELECT COUNT(c) FROM Course c")
    long countAllCourses();

    // 根据名称统计课程数量
    long countByNameContainingIgnoreCase(String name);

    // 根据学期统计课程数量
    long countBySemester(int semester);

    // 根据课程类型统计课程数量
    long countByCourseType(CourseType courseType);

    // 统计某学期的总学分
    @Query("SELECT SUM(c.credit) FROM Course c WHERE c.semester = :semester")
    Double sumCreditsBySemester(int semester);
}
