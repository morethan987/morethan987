package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.Student;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// JpaRepository 泛型参数：<实体类, 主键类型>
public interface StudentRepository extends JpaRepository<Student, Long> {
    // 1. Spring Data JPA 自动实现的基础 CRUD 方法 (无需编写)
    // - save(T entity)
    // - findById(ID id)
    // - findAll()
    // - delete(T entity)

    // 2. 自定义查询方法 (根据方法名自动生成 SQL)

    // 根据学生姓名查找
    List<Student> findByName(String name);

    // 查找分数大于等于指定值的学生
    List<Student> findByScoreGreaterThanEqual(Integer score);

    // 根据学号查找 (并忽略大小写)
    Student findByStudentNumberIgnoreCase(String studentNumber);
}
