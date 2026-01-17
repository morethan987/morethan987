package com.example.analytics.client;

import com.example.analytics.dto.StudentDTO;
import com.example.analytics.dto.TeacherDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "grade-user-service", path = "/api/v1")
public interface UserServiceClient {

    @GetMapping("/student/{studentId}")
    StudentDTO getStudentById(@PathVariable("studentId") UUID studentId);

    @GetMapping("/student")
    List<StudentDTO> getAllStudents();

    @GetMapping("/student/active")
    List<StudentDTO> getActiveStudents();

    @GetMapping("/teacher/{teacherId}")
    TeacherDTO getTeacherById(@PathVariable("teacherId") UUID teacherId);

    @GetMapping("/teacher")
    List<TeacherDTO> getAllTeachers();

    @GetMapping("/teacher/active")
    List<TeacherDTO> getActiveTeachers();
}
