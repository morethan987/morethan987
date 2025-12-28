package com.example.GradeSystemBackend.dto;

import java.util.UUID;

public class GradeDTO {
    private UUID id;
    private StudentDTO student;
    private CourseDTO course;
    private Double usualScore;
    private Double midtermScore;
    private Double finalExamScore;
    private Double experimentScore;
    private Double finalScore;
    private Double gpa;

    // Constructors
    public GradeDTO() {}

    public GradeDTO(UUID id, StudentDTO student, CourseDTO course,
                   Double usualScore, Double midtermScore, Double finalExamScore,
                   Double experimentScore, Double finalScore, Double gpa) {
        this.id = id;
        this.student = student;
        this.course = course;
        this.usualScore = usualScore;
        this.midtermScore = midtermScore;
        this.finalExamScore = finalExamScore;
        this.experimentScore = experimentScore;
        this.finalScore = finalScore;
        this.gpa = gpa;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public StudentDTO getStudent() {
        return student;
    }

    public void setStudent(StudentDTO student) {
        this.student = student;
    }

    public CourseDTO getCourse() {
        return course;
    }

    public void setCourse(CourseDTO course) {
        this.course = course;
    }

    public Double getUsualScore() {
        return usualScore;
    }

    public void setUsualScore(Double usualScore) {
        this.usualScore = usualScore;
    }

    public Double getMidtermScore() {
        return midtermScore;
    }

    public void setMidtermScore(Double midtermScore) {
        this.midtermScore = midtermScore;
    }

    public Double getFinalExamScore() {
        return finalExamScore;
    }

    public void setFinalExamScore(Double finalExamScore) {
        this.finalExamScore = finalExamScore;
    }

    public Double getExperimentScore() {
        return experimentScore;
    }

    public void setExperimentScore(Double experimentScore) {
        this.experimentScore = experimentScore;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }
}
