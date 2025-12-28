package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.CourseDTO;
import com.example.GradeSystemBackend.dto.TeachingClassDTO;
import com.example.GradeSystemBackend.service.CourseService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取学生的课程列表（包含教学班信息）
     */
    @PreAuthorize("hasAuthority('course:view')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TeachingClassDTO>> getStudentCourses(
        @PathVariable UUID studentId,
        @RequestParam(required = false) String courseType
    ) {
        List<TeachingClassDTO> courses = courseService.getStudentCourses(
            studentId,
            courseType
        );
        return ResponseEntity.ok(courses);
    }

    /**
     * 获取所有某学生当前学期的可选课程（选课用）
     */
    @PreAuthorize("hasAuthority('course:view')")
    @GetMapping("/available")
    public ResponseEntity<List<TeachingClassDTO>> getAvailableCourses(
        @RequestParam UUID studentId,
        @RequestParam(required = false) String courseType,
        @RequestParam(required = false) String search
    ) {
        List<TeachingClassDTO> courses = courseService.getAvailableCourses(
            studentId,
            courseType,
            search
        );
        return ResponseEntity.ok(courses);
    }

    /**
     * 学生选课
     */
    @PreAuthorize("hasAuthority('course:select')")
    @PostMapping("/select")
    public ResponseEntity<String> selectCourse(
        @RequestParam UUID studentId,
        @RequestParam UUID teachingClassId
    ) {
        boolean success = courseService.selectCourse(
            studentId,
            teachingClassId
        );
        if (success) {
            return ResponseEntity.ok("选课成功");
        } else {
            return ResponseEntity.badRequest().body("选课失败");
        }
    }

    /**
     * 学生退课
     */
    @PreAuthorize("hasAuthority('course:select')")
    @DeleteMapping("/drop")
    public ResponseEntity<String> dropCourse(
        @RequestParam UUID studentId,
        @RequestParam UUID teachingClassId
    ) {
        boolean success = courseService.dropCourse(studentId, teachingClassId);
        if (success) {
            return ResponseEntity.ok("退课成功");
        } else {
            return ResponseEntity.badRequest().body("退课失败");
        }
    }

    /**
     * 获取课程详情
     */
    @PreAuthorize("hasAuthority('course:view')")
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourseById(
        @PathVariable UUID courseId
    ) {
        CourseDTO course = courseService.getCourseById(courseId);
        if (course != null) {
            return ResponseEntity.ok(course);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
