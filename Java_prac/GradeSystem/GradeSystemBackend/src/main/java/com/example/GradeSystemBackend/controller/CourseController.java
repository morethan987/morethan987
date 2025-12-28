package com.example.GradeSystemBackend.controller;

import com.example.GradeSystemBackend.dto.CourseDTO;
import com.example.GradeSystemBackend.dto.TeachingClassDTO;
import com.example.GradeSystemBackend.service.CourseService;
import com.example.GradeSystemBackend.service.CourseService.BusinessException;
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
    @PreAuthorize("hasAnyAuthority('course:view')")
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
    @PreAuthorize("hasAnyAuthority('course:view')")
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
    @PreAuthorize("hasAnyAuthority('course:select')")
    @PostMapping("/select")
    public ResponseEntity<String> selectCourse(
        @RequestParam UUID studentId,
        @RequestParam UUID teachingClassId
    ) {
        try {
            courseService.selectCourse(studentId, teachingClassId);
            return ResponseEntity.ok("选课成功");
        } catch (BusinessException e) {
            // 返回具体的错误原因，状态码依然可以用 400
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 处理未预料到的系统错误
            return ResponseEntity.internalServerError().body(
                "服务器内部错误：" + e.getMessage()
            );
        }
    }

    /**
     * 学生退课
     */
    @PreAuthorize("hasAnyAuthority('course:select')")
    @DeleteMapping("/drop")
    public ResponseEntity<String> dropCourse(
        @RequestParam UUID studentId,
        @RequestParam UUID teachingClassId
    ) {
        try {
            courseService.dropCourse(studentId, teachingClassId);
            return ResponseEntity.ok("退课成功");
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取课程详情
     */
    @PreAuthorize("hasAnyAuthority('course:view')")
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
