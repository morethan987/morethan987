package com.example.model.dao;

import com.example.model.entity.TeachingClass;
import java.util.List;

/**
 * TeachingClass Data Access Object (DAO) Interface
 */
public interface TeachingClassDAO {
    // 初始化数据库表（可选，方便测试）
    void createTable();

    // 增加教学班
    boolean addTeachingClass(TeachingClass teachingClass);

    // 根据班级ID查询教学班
    TeachingClass getTeachingClassById(String classId);

    // 根据教师ID查询教学班列表
    List<TeachingClass> getTeachingClassesByTeacherId(String teacherId);

    // 根据课程ID查询教学班列表
    List<TeachingClass> getTeachingClassesByCourseId(String courseId);

    // 根据学期查询教学班列表
    List<TeachingClass> getTeachingClassesBySemester(Integer semester);

    // 根据班级名称查询教学班
    TeachingClass getTeachingClassByName(String name);

    // 根据教师ID和课程ID查询教学班
    List<TeachingClass> getTeachingClassesByTeacherIdAndCourseId(
        String teacherId,
        String courseId
    );

    // 查询所有教学班
    List<TeachingClass> getAllTeachingClasses();

    // 更新教学班信息
    boolean updateTeachingClass(TeachingClass teachingClass);

    // 删除教学班
    boolean deleteTeachingClass(String classId);

    // 根据教师ID删除所有相关教学班
    boolean deleteTeachingClassesByTeacherId(String teacherId);

    // 根据课程ID删除所有相关教学班
    boolean deleteTeachingClassesByCourseId(String courseId);
}
