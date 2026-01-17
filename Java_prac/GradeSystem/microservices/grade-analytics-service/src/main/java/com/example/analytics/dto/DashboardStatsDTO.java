package com.example.analytics.dto;

public class DashboardStatsDTO {

    private Long totalStudents;
    private Long activeStudents;
    private Long totalTeachers;
    private Long activeTeachers;
    private Long totalCourses;
    private Long activeCourses;
    private Double systemAverageGPA;
    private Double systemPassRate;

    public DashboardStatsDTO() {}

    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getActiveStudents() {
        return activeStudents;
    }

    public void setActiveStudents(Long activeStudents) {
        this.activeStudents = activeStudents;
    }

    public Long getTotalTeachers() {
        return totalTeachers;
    }

    public void setTotalTeachers(Long totalTeachers) {
        this.totalTeachers = totalTeachers;
    }

    public Long getActiveTeachers() {
        return activeTeachers;
    }

    public void setActiveTeachers(Long activeTeachers) {
        this.activeTeachers = activeTeachers;
    }

    public Long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(Long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public Long getActiveCourses() {
        return activeCourses;
    }

    public void setActiveCourses(Long activeCourses) {
        this.activeCourses = activeCourses;
    }

    public Double getSystemAverageGPA() {
        return systemAverageGPA;
    }

    public void setSystemAverageGPA(Double systemAverageGPA) {
        this.systemAverageGPA = systemAverageGPA;
    }

    public Double getSystemPassRate() {
        return systemPassRate;
    }

    public void setSystemPassRate(Double systemPassRate) {
        this.systemPassRate = systemPassRate;
    }
}
