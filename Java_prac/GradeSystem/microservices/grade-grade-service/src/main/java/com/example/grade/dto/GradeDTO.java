package com.example.grade.dto;

import com.example.grade.domain.Grade;
import java.time.LocalDateTime;
import java.util.UUID;

public class GradeDTO {

    private UUID id;
    private UUID courseId;
    private UUID studentId;
    private StudentDTO student;
    private CourseDTO course;
    private Double usualScore;
    private Double midScore;
    private Double experimentScore;
    private Double finalExamScore;
    private Double finalScore;
    private Double gpa;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GradeDTO() {}

    public GradeDTO(Grade grade) {
        this.id = grade.getId();
        this.courseId = grade.getCourseId();
        this.studentId = grade.getStudentId();
        this.usualScore = grade.getUsualScore();
        this.midScore = grade.getMidScore();
        this.experimentScore = grade.getExperimentScore();
        this.finalExamScore = grade.getFinalExamScore();
        this.finalScore = grade.getFinalScore();
        this.gpa = grade.getGpa();
        this.version = grade.getVersion();
        this.createdAt = grade.getCreatedAt();
        this.updatedAt = grade.getUpdatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
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

    public Double getMidScore() {
        return midScore;
    }

    public void setMidScore(Double midScore) {
        this.midScore = midScore;
    }

    public Double getExperimentScore() {
        return experimentScore;
    }

    public void setExperimentScore(Double experimentScore) {
        this.experimentScore = experimentScore;
    }

    public Double getFinalExamScore() {
        return finalExamScore;
    }

    public void setFinalExamScore(Double finalExamScore) {
        this.finalExamScore = finalExamScore;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
