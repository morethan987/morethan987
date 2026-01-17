package com.example.grade.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class GradeCreateDTO {

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    private Double usualScore;
    private Double midScore;
    private Double experimentScore;
    private Double finalExamScore;

    public GradeCreateDTO() {}

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
}
