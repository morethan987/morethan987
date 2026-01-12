package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.grade.Grade;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import com.example.GradeSystemBackend.dto.CardDataDTO;
import com.example.GradeSystemBackend.repository.GradeRepository;
import com.example.GradeSystemBackend.repository.StudentRepository;
import com.example.GradeSystemBackend.repository.TeacherRepository;
import com.example.GradeSystemBackend.repository.TeachingClassRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private GradeService gradeService;

    public List<CardDataDTO> getAdminDashboard() {
        List<CardDataDTO> cards = new ArrayList<>();

        long totalStudents = studentRepository.countActiveStudents();
        long totalTeachers = teacherRepository.countActiveTeachers();
        long totalUsers = totalStudents + totalTeachers;
        cards.add(CardDataDTO.create(
            "total-users",
            "在校师生总数",
            String.valueOf(totalUsers),
            "neutral",
            "",
            false,
            "active",
            "学生: " + totalStudents + " | 教师: " + totalTeachers
        ));

        long activeClasses = teachingClassRepository.countAllTeachingClasses();
        cards.add(CardDataDTO.create(
            "active-classes",
            "本学期开设课程",
            String.valueOf(activeClasses),
            "neutral",
            "",
            false,
            "active",
            "开课班级总数"
        ));

        long enrolledStudents = studentRepository.countActiveStudents();
        long allStudents = studentRepository.countAllStudents();
        double enrollmentRate = allStudents > 0 ? (double) enrolledStudents / allStudents * 100 : 0;
        cards.add(CardDataDTO.create(
            "avg-enrollment",
            "平均报到率",
            String.format("%.1f%%", enrollmentRate),
            "neutral",
            "",
            false,
            "active",
            "在读学生占比"
        ));

        long gradesWithScore = countGradesWithFinalScore();
        long totalGrades = gradeRepository.count();
        double gradeInputProgress = totalGrades > 0 ? (double) gradesWithScore / totalGrades * 100 : 0;
        cards.add(CardDataDTO.create(
            "system-health",
            "成绩录入进度",
            String.format("%.1f%%", gradeInputProgress),
            gradeInputProgress >= 80 ? "up" : "down",
            String.format("%.0f/%d", (double) gradesWithScore, totalGrades),
            true,
            gradeInputProgress >= 80 ? "good" : "warning",
            "已录入成绩/总记录数"
        ));

        return cards;
    }

    public List<CardDataDTO> getStudentDashboard(UUID studentId) {
        List<CardDataDTO> cards = new ArrayList<>();

        Double gpa = gradeService.getStudentGpa(studentId);
        String gpaValue = gpa != null ? String.format("%.2f", gpa) : "N/A";
        String gpaStatus = gpa != null && gpa >= 3.0 ? "good" : (gpa != null && gpa >= 2.0 ? "warning" : "critical");
        cards.add(CardDataDTO.create(
            "gpa-stats",
            "当前绩点 (GPA)",
            gpaValue,
            "neutral",
            "",
            false,
            gpaStatus,
            "满分4.0"
        ));

        Double totalCredits = gradeRepository.calculateStudentTotalCredits(studentId);
        Student student = studentRepository.findById(studentId).orElse(null);
        double requiredCredits = student != null && student.getTotalCredits() != null ? student.getTotalCredits() : 128.0;
        double earnedCredits = totalCredits != null ? totalCredits : 0.0;
        double creditProgress = requiredCredits > 0 ? earnedCredits / requiredCredits * 100 : 0;
        cards.add(CardDataDTO.create(
            "credits-progress",
            "修读学分",
            String.format("%.1f", earnedCredits),
            "neutral",
            String.format("%.0f%%", creditProgress),
            true,
            creditProgress >= 50 ? "good" : "warning",
            String.format("要求: %.1f学分", requiredCredits)
        ));

        long currentCourseCount = countStudentCurrentCourses(studentId);
        cards.add(CardDataDTO.create(
            "current-courses",
            "本学期课程",
            String.valueOf(currentCourseCount),
            "neutral",
            "",
            false,
            "active",
            "已选课程数"
        ));

        Double weightedScore = calculateWeightedAverageScore(studentId);
        String scoreValue = weightedScore != null ? String.format("%.1f", weightedScore) : "N/A";
        String scoreStatus = weightedScore != null && weightedScore >= 80 ? "good" : 
            (weightedScore != null && weightedScore >= 60 ? "warning" : "critical");
        cards.add(CardDataDTO.create(
            "weighted-score",
            "加权平均分",
            scoreValue,
            "neutral",
            "",
            false,
            scoreStatus,
            "满分100"
        ));

        return cards;
    }

    public List<CardDataDTO> getTeacherDashboard(UUID teacherId) {
        List<CardDataDTO> cards = new ArrayList<>();

        List<TeachingClass> teachingClasses = teachingClassRepository.findByTeacherId(teacherId);
        int teachingClassCount = teachingClasses.size();
        cards.add(CardDataDTO.create(
            "teaching-classes",
            "执教班级",
            String.valueOf(teachingClassCount),
            "neutral",
            "",
            false,
            "active",
            "本学期教学班"
        ));

        int totalStudents = teachingClasses.stream()
            .mapToInt(TeachingClass::getEnrolledCount)
            .sum();
        cards.add(CardDataDTO.create(
            "total-students",
            "学生总数",
            String.valueOf(totalStudents),
            "neutral",
            "",
            false,
            "active",
            "所有班级学生"
        ));

        int workloadHours = calculateTeacherWorkloadHours(teachingClasses);
        cards.add(CardDataDTO.create(
            "workload-hours",
            "本周课时",
            String.valueOf(workloadHours),
            "neutral",
            "",
            false,
            "active",
            "预估教学时数"
        ));

        double passRate = calculateTeacherPassRate(teachingClasses);
        cards.add(CardDataDTO.create(
            "avg-pass-rate",
            "所教课程及格率",
            String.format("%.1f%%", passRate),
            passRate >= 80 ? "up" : "down",
            "",
            false,
            passRate >= 80 ? "good" : "warning",
            "及格分数线60"
        ));

        return cards;
    }

    private long countGradesWithFinalScore() {
        List<Grade> allGrades = gradeRepository.findAll();
        return allGrades.stream()
            .filter(g -> g.getFinalScore() != null)
            .count();
    }

    private long countStudentCurrentCourses(UUID studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return 0;
        }
        Set<TeachingClass> teachingClasses = student.getTeachingClasses();
        return teachingClasses != null ? teachingClasses.size() : 0;
    }

    private Double calculateWeightedAverageScore(UUID studentId) {
        Double totalCredits = gradeRepository.calculateStudentTotalCredits(studentId);
        Double weightedScore = gradeRepository.calculateStudentWeightedScore(studentId);

        if (totalCredits != null && totalCredits > 0 && weightedScore != null) {
            return weightedScore / totalCredits;
        }
        return null;
    }

    private int calculateTeacherWorkloadHours(List<TeachingClass> teachingClasses) {
        return teachingClasses.size() * 4;
    }

    private double calculateTeacherPassRate(List<TeachingClass> teachingClasses) {
        if (teachingClasses.isEmpty()) {
            return 0.0;
        }

        int totalGraded = 0;
        int totalPassed = 0;

        for (TeachingClass tc : teachingClasses) {
            if (tc.getCourse() == null) continue;

            List<Grade> grades = gradeRepository.findByCourseId(tc.getCourse().getId());
            for (Grade grade : grades) {
                if (grade.getFinalScore() != null) {
                    totalGraded++;
                    if (grade.getFinalScore() >= 60) {
                        totalPassed++;
                    }
                }
            }
        }

        return totalGraded > 0 ? (double) totalPassed / totalGraded * 100 : 0.0;
    }
}
