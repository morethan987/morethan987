package com.example.model.entity;

import com.example.model.dto.BinaryMessage;

/**
 * Grade entity class
 */
public class Grade {

    private String gradeId;
    private String studentId;
    private String courseId;
    private Double usualScore;
    private Double midScore;
    private Double experimentScore;
    private Double finalExamScore;
    private Double finalScore;

    public Grade() {
        this.usualScore = 0.0;
        this.midScore = 0.0;
        this.experimentScore = 0.0;
        this.finalExamScore = 0.0;
        this.finalScore = 0.0;
    }

    public Grade(
        String gradeId,
        String studentId,
        String courseId,
        Double usualScore,
        Double midScore,
        Double experimentScore,
        Double finalExamScore
    ) {
        this.gradeId = gradeId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.usualScore = usualScore;
        this.midScore = midScore;
        this.experimentScore = experimentScore;
        this.finalExamScore = finalExamScore;
        this.finalScore =
            0.1 * usualScore +
            0.3 * midScore +
            0.1 * experimentScore +
            0.5 * finalExamScore;
    }

    public String getGradeId() {
        return gradeId;
    }

    public BinaryMessage setGradeId(String gradeId) {
        this.gradeId = gradeId;
        return new BinaryMessage(true, "设置成绩ID成功");
    }

    public String getStudentId() {
        return studentId;
    }

    public BinaryMessage setStudentId(String studentId) {
        this.studentId = studentId;
        return new BinaryMessage(true, "设置学生ID成功");
    }

    public String getCourseId() {
        return courseId;
    }

    public BinaryMessage setCourseId(String courseId) {
        this.courseId = courseId;
        return new BinaryMessage(true, "设置课程ID成功");
    }

    public Double getUsualScore() {
        return usualScore;
    }

    public BinaryMessage setUsualScore(Double usualScore) {
        this.usualScore = usualScore;
        return new BinaryMessage(true, "设置平时成绩成功");
    }

    public Double getMidScore() {
        return midScore;
    }

    public BinaryMessage setMidScore(Double midScore) {
        this.midScore = midScore;
        return new BinaryMessage(true, "设置期中成绩成功");
    }

    public Double getExperimentScore() {
        return experimentScore;
    }

    public BinaryMessage setExperimentScore(Double experimentScore) {
        this.experimentScore = experimentScore;
        return new BinaryMessage(true, "设置实验成绩成功");
    }

    public Double getFinalExamScore() {
        return finalExamScore;
    }

    public BinaryMessage setFinalExamScore(Double finalExamScore) {
        this.finalExamScore = finalExamScore;
        return new BinaryMessage(true, "设置期末成绩成功");
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public BinaryMessage setFinalScore() {
        this.finalScore =
            0.1 * usualScore +
            0.3 * midScore +
            0.1 * experimentScore +
            0.5 * finalExamScore;
        return new BinaryMessage(true, "设置最终成绩成功");
    }
}
