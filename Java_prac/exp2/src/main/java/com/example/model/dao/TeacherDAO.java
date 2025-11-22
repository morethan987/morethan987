package com.example.model.dao;

import com.example.model.entity.Teacher;
import java.util.List;

public interface TeacherDAO {
    // 初始化表和目录
    void createTable();

    boolean addTeacher(Teacher teacher);

    Teacher getTeacherById(String id);

    List<Teacher> getAllTeachers();

    boolean updateTeacher(Teacher teacher);

    boolean deleteTeacher(String id);
}
