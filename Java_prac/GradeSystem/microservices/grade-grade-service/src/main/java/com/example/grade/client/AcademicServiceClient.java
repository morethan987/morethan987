package com.example.grade.client;

import com.example.grade.dto.CourseDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "grade-academic-service", path = "/api/v1")
public interface AcademicServiceClient {

    @GetMapping("/course/{courseId}")
    CourseDTO getCourseById(@PathVariable("courseId") UUID courseId);

    @GetMapping("/course")
    List<CourseDTO> getAllCourses();

    @GetMapping("/course/active")
    List<CourseDTO> getActiveCourses();

    @GetMapping("/course/by-semester/{semester}")
    List<CourseDTO> getCoursesBySemester(@PathVariable("semester") Integer semester);
}
