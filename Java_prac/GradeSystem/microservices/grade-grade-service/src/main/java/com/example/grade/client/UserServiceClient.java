package com.example.grade.client;

import com.example.grade.dto.StudentDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "grade-user-service", path = "/api/v1")
public interface UserServiceClient {

    @GetMapping("/student/{studentId}")
    StudentDTO getStudentById(@PathVariable("studentId") UUID studentId);

    @GetMapping("/student/by-user/{userId}")
    StudentDTO getStudentByUserId(@PathVariable("userId") UUID userId);

    @GetMapping("/student")
    List<StudentDTO> getAllStudents();

    @GetMapping("/student/active")
    List<StudentDTO> getActiveStudents();
}
