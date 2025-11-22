package com.example.model.dao;

import com.example.model.entity.Grade;
import java.util.List;

/**
 * Grade Data Access Object (DAO) Interface
 */
public interface GradeDAO {
    // 初始化数据库表（可选，方便测试）
    void createTable();

    // 增加成绩
    boolean addGrade(Grade grade);

    // 根据成绩ID查询成绩
    Grade getGradeById(String gradeId);

    // 根据学生ID查询成绩列表
    List<Grade> getGradesByStudentId(String studentId);

    // 根据课程ID查询成绩列表
    List<Grade> getGradesByCourseId(String courseId);

    // 根据学生ID和课程ID查询特定成绩
    Grade getGradeByStudentIdAndCourseId(String studentId, String courseId);

    // 查询所有成绩
    List<Grade> getAllGrades();

    // 更新成绩信息
    boolean updateGrade(Grade grade);

    // 删除成绩
    boolean deleteGrade(String gradeId);

    // 根据学生ID删除所有相关成绩
    boolean deleteGradesByStudentId(String studentId);

    // 根据课程ID删除所有相关成绩
    boolean deleteGradesByCourseId(String courseId);
}
