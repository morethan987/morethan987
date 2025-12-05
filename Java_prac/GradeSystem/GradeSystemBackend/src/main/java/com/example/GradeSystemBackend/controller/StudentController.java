package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.repository.StudentRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    /**
     * ✅ 教务才能创建学生
     * permission: student:add
     */
    @PreAuthorize("hasAuthority('student:add')")
    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        printCurrentUser("createStudent");
        return studentRepository.save(student);
    }

    /**
     * ✅ 学生 / 教师 / 教务都可以查看学生列表
     * permission: student:view
     */
    @PreAuthorize("hasAuthority('student:view')")
    @GetMapping
    public List<Student> getAllStudents() {
        printCurrentUser("getAllStudents");
        return studentRepository.findAll();
    }

    /**
     * ✅ 查看某个学生
     */
    @PreAuthorize("hasAuthority('student:view')")
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable String id) {
        printCurrentUser("getStudentById");
        return studentRepository.findByStudentCode(id).orElse(null);
    }

    /**
     * 打印当前访问者（用于调试你是否真的登录 & 授权生效）
     */
    private void printCurrentUser(String method) {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[" + method + "] 当前登录用户：" + auth.getName());
        System.out.println(
            "[" + method + "] 用户权限：" + auth.getAuthorities()
        );
    }
}
