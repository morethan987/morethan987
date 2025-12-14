package com.example.GradeSystemBackend.domain.grade;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.student.Student;
import jakarta.persistence.*;
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

    @PrePersist
    @PreUpdate
    public void calculateFinalScore() {
        double usual = (usualScore != null) ? usualScore : 0.0;
        double mid = (midScore != null) ? midScore : 0.0;
        double experiment = (experimentScore != null) ? experimentScore : 0.0;
        double finalExam = (finalExamScore != null) ? finalExamScore : 0.0;

        this.finalScore =
            usual * 0.2 + mid * 0.3 + experiment * 0.1 + finalExam * 0.4;
    }

    public Double getGPA() {
        if (finalScore == null) {
            return null;
        }
        if (finalScore >= 90) {
            return 4.0;
        } else if (finalScore < 60) {
            return 0.0;
        } else {
            return 1.0 + (finalScore - 60) * 0.1;
        }
    }
}
