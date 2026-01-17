package com.example.analytics.client;

import com.example.analytics.dto.GradeDTO;
import com.example.analytics.dto.GradeStatsDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "grade-grade-service", path = "/api/v1/grade")
public interface GradeServiceClient {

    @GetMapping("/student/{studentId}")
    List<GradeDTO> getGradesByStudentId(@PathVariable("studentId") UUID studentId);

    @GetMapping("/course/{courseId}")
    List<GradeDTO> getGradesByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/student/{studentId}/stats")
    GradeStatsDTO getStudentGradeStats(@PathVariable("studentId") UUID studentId);

    @GetMapping("/student/{studentId}/gpa")
    Double getStudentAverageGpa(@PathVariable("studentId") UUID studentId);

    @GetMapping("/course/{courseId}/pass-rate")
    Double getCoursePassRate(@PathVariable("courseId") UUID courseId);

    @GetMapping("/course/{courseId}/average")
    Double getCourseAverageScore(@PathVariable("courseId") UUID courseId);
}
