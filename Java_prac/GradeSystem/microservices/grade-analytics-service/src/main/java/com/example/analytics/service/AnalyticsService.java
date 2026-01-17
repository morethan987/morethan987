package com.example.analytics.service;

import com.example.analytics.client.AcademicServiceClient;
import com.example.analytics.client.GradeServiceClient;
import com.example.analytics.client.UserServiceClient;
import com.example.analytics.dto.CourseDTO;
import com.example.analytics.dto.DashboardStatsDTO;
import com.example.analytics.dto.GradeStatsDTO;
import com.example.analytics.dto.StudentDTO;
import com.example.analytics.dto.TeacherDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AcademicServiceClient academicServiceClient;

    @Autowired
    private GradeServiceClient gradeServiceClient;

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        try {
            List<StudentDTO> allStudents = userServiceClient.getAllStudents();
            List<StudentDTO> activeStudents = userServiceClient.getActiveStudents();
            stats.setTotalStudents((long) allStudents.size());
            stats.setActiveStudents((long) activeStudents.size());
        } catch (Exception ignored) {
            stats.setTotalStudents(0L);
            stats.setActiveStudents(0L);
        }

        try {
            List<TeacherDTO> allTeachers = userServiceClient.getAllTeachers();
            List<TeacherDTO> activeTeachers = userServiceClient.getActiveTeachers();
            stats.setTotalTeachers((long) allTeachers.size());
            stats.setActiveTeachers((long) activeTeachers.size());
        } catch (Exception ignored) {
            stats.setTotalTeachers(0L);
            stats.setActiveTeachers(0L);
        }

        try {
            List<CourseDTO> allCourses = academicServiceClient.getAllCourses();
            List<CourseDTO> activeCourses = academicServiceClient.getActiveCourses();
            stats.setTotalCourses((long) allCourses.size());
            stats.setActiveCourses((long) activeCourses.size());
        } catch (Exception ignored) {
            stats.setTotalCourses(0L);
            stats.setActiveCourses(0L);
        }

        stats.setSystemAverageGPA(0.0);
        stats.setSystemPassRate(0.0);

        return stats;
    }

    public GradeStatsDTO getStudentAnalytics(UUID studentId) {
        try {
            return gradeServiceClient.getStudentGradeStats(studentId);
        } catch (Exception ignored) {
            return new GradeStatsDTO();
        }
    }

    public Double getStudentGpa(UUID studentId) {
        try {
            return gradeServiceClient.getStudentAverageGpa(studentId);
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    public Double getCoursePassRate(UUID courseId) {
        try {
            return gradeServiceClient.getCoursePassRate(courseId);
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    public Double getCourseAverageScore(UUID courseId) {
        try {
            return gradeServiceClient.getCourseAverageScore(courseId);
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    public StudentDTO getStudentInfo(UUID studentId) {
        try {
            return userServiceClient.getStudentById(studentId);
        } catch (Exception ignored) {
            return null;
        }
    }

    public CourseDTO getCourseInfo(UUID courseId) {
        try {
            return academicServiceClient.getCourseById(courseId);
        } catch (Exception ignored) {
            return null;
        }
    }
}
