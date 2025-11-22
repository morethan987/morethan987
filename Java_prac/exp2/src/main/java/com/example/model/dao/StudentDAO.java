package com.example.model.dao;

import com.example.model.dto.BinaryMessage;
import com.example.model.entity.Student;
import java.util.List;

/**
 * 学生DAO类接口定义
 */
public interface StudentDAO {
    /**
     * 添加学生
     * @param student 学生对象
     * @return 操作结果消息
     */
    BinaryMessage addStudent(Student student);

    /**
     * 根据ID删除学生
     * @param stuId 学生ID
     * @return 操作结果消息
     */
    BinaryMessage deleteStudentById(String stuId);

    /**
     * 更新学生信息
     * @param student 学生对象
     * @return 操作结果消息
     */
    BinaryMessage updateStudent(Student student);

    /**
     * 根据ID查询学生
     * @param stuId 学生ID
     * @return 学生对象，如果不存在返回null
     */
    Student findStudentById(String stuId);

    /**
     * 查询所有学生
     * @return 学生列表
     */
    List<Student> findAllStudents();

    /**
     * 根据姓名查询学生
     * @param name 学生姓名
     * @return 学生列表
     */
    List<Student> findStudentsByName(String name);

    /**
     * 根据性别查询学生
     * @param gender 性别
     * @return 学生列表
     */
    List<Student> findStudentsByGender(String gender);

    /**
     * 检查学生ID是否存在
     * @param stuId 学生ID
     * @return 存在返回true，否则返回false
     */
    boolean existsById(String stuId);
}
