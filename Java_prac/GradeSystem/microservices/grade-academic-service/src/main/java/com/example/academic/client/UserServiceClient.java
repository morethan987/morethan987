package com.example.academic.client;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "grade-user-service", path = "/api/v1")
public interface UserServiceClient {

    @GetMapping("/teacher/{teacherId}")
    TeacherDTO getTeacherById(@PathVariable("teacherId") UUID teacherId);

    @GetMapping("/teacher/by-user/{userId}")
    TeacherDTO getTeacherByUserId(@PathVariable("userId") UUID userId);

    @GetMapping("/teacher")
    List<TeacherDTO> getAllTeachers();

    @GetMapping("/teacher/active")
    List<TeacherDTO> getActiveTeachers();

    @GetMapping("/student/{studentId}")
    StudentDTO getStudentById(@PathVariable("studentId") UUID studentId);

    @GetMapping("/student/by-user/{userId}")
    StudentDTO getStudentByUserId(@PathVariable("userId") UUID userId);

    @GetMapping("/student")
    List<StudentDTO> getAllStudents();

    @GetMapping("/student/active")
    List<StudentDTO> getActiveStudents();
}
