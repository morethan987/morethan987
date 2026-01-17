package com.example.grade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "usual_score")
    private Double usualScore;

    @Column(name = "mid_score")
    private Double midScore;

    @Column(name = "experiment_score")
    private Double experimentScore;

    @Column(name = "final_exam_score")
    private Double finalExamScore;

    @Column(name = "final_score")
    private Double finalScore;

    @Column(name = "gpa")
    private Double gpa;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Grade() {}

    public Grade(UUID courseId, UUID studentId) {
        this.courseId = courseId;
        this.studentId = studentId;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        calculateFinalScoreInternal();
        calculateGPAInternal();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateFinalScoreInternal();
        calculateGPAInternal();
    }

    // Formula: usual*0.2 + mid*0.3 + experiment*0.1 + finalExam*0.4
    private void calculateFinalScoreInternal() {
        if (usualScore == null || midScore == null || 
            experimentScore == null || finalExamScore == null) {
            this.finalScore = null;
            return;
        }

        this.finalScore = usualScore * 0.2 +
                          midScore * 0.3 +
                          experimentScore * 0.1 +
                          finalExamScore * 0.4;
    }

    // GPA: 90+ -> 4.0, <60 -> 0.0, 60-90 -> 1.0 + (score-60)*0.1
    private void calculateGPAInternal() {
        if (this.finalScore == null) {
            this.gpa = null;
            return;
        }

        if (finalScore >= 90) {
            this.gpa = 4.0;
        } else if (finalScore < 60) {
            this.gpa = 0.0;
        } else {
            this.gpa = 1.0 + (finalScore - 60) * 0.1;
        }
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

    public Double getGpa() {
        return gpa;
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
