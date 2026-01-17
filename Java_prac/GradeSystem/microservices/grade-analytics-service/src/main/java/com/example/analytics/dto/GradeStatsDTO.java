package com.example.analytics.dto;

public class GradeStatsDTO {

    private Double totalCredits;
    private Double averageGPA;
    private Double averageScore;
    private Long passedCourses;
    private Long totalCourses;

    public GradeStatsDTO() {}

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
