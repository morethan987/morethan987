package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.StudentDTO;
import com.example.GradeSystemBackend.service.StudentService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * 根据用户id查询学生
     * @param UUID userId
     * @return StudentDTO 学生数据传输对象
     */
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public StudentDTO getStudentIdByUserId(@PathVariable UUID userId) {
        return studentService.getStudentByUserId(userId);
    }
}
