package com.example.GradeSystemBackend.dto;

import java.util.UUID;

public class StudentGradeInputDTO {

    private UUID id;
    private String studentCode;
    private String name;
    private String className;
    private Double usualScore;
    private Double midtermScore;
    private Double finalExamScore;
    private Double experimentScore;
    private Double finalScore;
    private Double gpa;
    private Boolean isModified;
    private Long version;

    public StudentGradeInputDTO() {}

    public StudentGradeInputDTO(
        UUID id,
        String studentCode,
        String name,
        String className,
        Double usualScore,
        Double midtermScore,
        Double finalExamScore,
        Double experimentScore,
        Double finalScore,
        Double gpa,
        Boolean isModified,
        Long version
    ) {
        this.id = id;
        this.studentCode = studentCode;
        this.name = name;
        this.className = className;
        this.usualScore = usualScore;
        this.midtermScore = midtermScore;
        this.finalExamScore = finalExamScore;
        this.experimentScore = experimentScore;
        this.finalScore = finalScore;
        this.gpa = gpa;
        this.isModified = isModified;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public Boolean getIsModified() {
        return isModified;
    }

    public void setIsModified(Boolean isModified) {
        this.isModified = isModified;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
