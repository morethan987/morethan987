package com.example.grade.dto;

import java.util.Map;

public class GradeStatsDTO {

    private Double totalCredits;
    private Double averageGPA;
    private Double averageScore;
    private Long passedCourses;
    private Long totalCourses;

    public GradeStatsDTO() {}

    public GradeStatsDTO(Map<String, Object> stats) {
        this.totalCredits = stats.get("totalCredits") != null ? 
            ((Number) stats.get("totalCredits")).doubleValue() : 0.0;
        this.averageGPA = stats.get("averageGPA") != null ? 
            ((Number) stats.get("averageGPA")).doubleValue() : 0.0;
        this.averageScore = stats.get("averageScore") != null ? 
            ((Number) stats.get("averageScore")).doubleValue() : 0.0;
        this.passedCourses = stats.get("passedCourses") != null ? 
            ((Number) stats.get("passedCourses")).longValue() : 0L;
        this.totalCourses = stats.get("totalCourses") != null ? 
            ((Number) stats.get("totalCourses")).longValue() : 0L;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Double getAverageGPA() {
        return averageGPA;
    }

    public void setAverageGPA(Double averageGPA) {
        this.averageGPA = averageGPA;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Long getPassedCourses() {
        return passedCourses;
    }

    public void setPassedCourses(Long passedCourses) {
        this.passedCourses = passedCourses;
    }

    public Long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(Long totalCourses) {
        this.totalCourses = totalCourses;
    }
}
