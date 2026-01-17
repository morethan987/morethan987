package com.example.academic.dto;

import com.example.academic.domain.Semester;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class SemesterDTO {

    private UUID id;
    private String name;
    private String academicYear;
    private Integer semesterNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;
    private LocalDate gradeSubmissionDeadline;
    private Boolean isCurrent;
    private Boolean isEnrollmentOpen;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SemesterDTO() {}

    public SemesterDTO(Semester semester) {
        this.id = semester.getId();
        this.name = semester.getName();
        this.academicYear = semester.getAcademicYear();
        this.semesterNumber = semester.getSemesterNumber();
        this.startDate = semester.getStartDate();
        this.endDate = semester.getEndDate();
        this.enrollmentStartDate = semester.getEnrollmentStartDate();
        this.enrollmentEndDate = semester.getEnrollmentEndDate();
        this.gradeSubmissionDeadline = semester.getGradeSubmissionDeadline();
        this.isCurrent = semester.getIsCurrent();
        this.isEnrollmentOpen = semester.isEnrollmentOpen();
        this.isActive = semester.isActive();
        this.createdAt = semester.getCreatedAt();
        this.updatedAt = semester.getUpdatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getEnrollmentStartDate() {
        return enrollmentStartDate;
    }

    public void setEnrollmentStartDate(LocalDate enrollmentStartDate) {
        this.enrollmentStartDate = enrollmentStartDate;
    }

    public LocalDate getEnrollmentEndDate() {
        return enrollmentEndDate;
    }

    public void setEnrollmentEndDate(LocalDate enrollmentEndDate) {
        this.enrollmentEndDate = enrollmentEndDate;
    }

    public LocalDate getGradeSubmissionDeadline() {
        return gradeSubmissionDeadline;
    }

    public void setGradeSubmissionDeadline(LocalDate gradeSubmissionDeadline) {
        this.gradeSubmissionDeadline = gradeSubmissionDeadline;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Boolean getIsEnrollmentOpen() {
        return isEnrollmentOpen;
    }

    public void setIsEnrollmentOpen(Boolean isEnrollmentOpen) {
        this.isEnrollmentOpen = isEnrollmentOpen;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
