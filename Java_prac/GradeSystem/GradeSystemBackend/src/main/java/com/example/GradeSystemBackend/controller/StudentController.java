package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.domain.Student;
import com.example.GradeSystemBackend.repository.StudentRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    // 1. 创建新学生
    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        // save() 方法由 JpaRepository 提供
        return studentRepository.save(student);
    }

    // 2. 获取所有学生
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // 3. 根据ID获取学生
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    // 4. 根据自定义方法查询学生 (查找分数 >= 80 的)
    @GetMapping("/high-scores")
    public List<Student> getHighScoringStudents() {
        return studentRepository.findByScoreGreaterThanEqual(80);
    }
}
