package com.example.GradeSystemBackend.domain.grade;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.student.Student;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = true)
    private Double usualScore;

    @Column(nullable = true)
    private Double midScore;

    @Column(nullable = true)
    private Double experimentScore;

    @Column(nullable = true)
    private Double finalExamScore;

    @Column(nullable = true)
    private Double finalScore;

    @Column(nullable = true)
    private Double gpa;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getters / setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
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

    private void calculateFinalScoreInternal() {
        if (
            usualScore == null ||
            midScore == null ||
            experimentScore == null ||
            finalExamScore == null
        ) {
            this.finalScore = null;
            return;
        }

        this.finalScore =
            usualScore * 0.2 +
            midScore * 0.3 +
            experimentScore * 0.1 +
            finalExamScore * 0.4;
    }

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
}
