package com.example.model.dao;

import com.example.model.entity.Course;
import java.util.List;

public interface CourseDAO {
    // 初始化表和目录
    void createTable();

    boolean addCourse(Course course);

    Course getCourseById(String id);

    List<Course> getAllCourses();

    boolean updateCourse(Course course);

    boolean deleteCourse(String id);
}
