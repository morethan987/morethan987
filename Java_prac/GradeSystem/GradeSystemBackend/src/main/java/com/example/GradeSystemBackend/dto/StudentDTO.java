package com.example.GradeSystemBackend.dto;

import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.student.StudentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class StudentDTO {

    private UUID id;
    private String studentCode;
    private String major;
    private String className;
    private Integer enrollmentYear;
    private Integer currentSemester;
    private StudentStatus status;
    private Double totalCredits;
    private String advisor;
    private LocalDateTime expectedGraduationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StudentDTO(Student student) {
        this.id = student.getId();
        this.studentCode = student.getStudentCode();
        this.major = student.getMajor();
        this.className = student.getClassName();
        this.enrollmentYear = student.getEnrollmentYear();
        this.currentSemester = student.getCurrentSemester();
        this.status = student.getStatus();
        this.totalCredits = student.getTotalCredits();
        this.advisor = student.getAdvisor();
        this.expectedGraduationDate = student.getExpectedGraduationDate();
        this.createdAt = student.getCreatedAt();
        this.updatedAt = student.getUpdatedAt();
    }

    public StudentDTO() {}

    // Getters and Setters
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

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getEnrollmentYear() {
        return enrollmentYear;
    }

    public void setEnrollmentYear(Integer enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    public Integer getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(Integer currentSemester) {
        this.currentSemester = currentSemester;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public String getAdvisor() {
        return advisor;
    }

    public void setAdvisor(String advisor) {
        this.advisor = advisor;
    }

    public LocalDateTime getExpectedGraduationDate() {
        return expectedGraduationDate;
    }

    public void setExpectedGraduationDate(
        LocalDateTime expectedGraduationDate
    ) {
        this.expectedGraduationDate = expectedGraduationDate;
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
