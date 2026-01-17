package com.example.academic.dto;

import com.example.academic.domain.CourseEnrollment;
import com.example.academic.domain.EnrollmentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class CourseEnrollmentDTO {

    private UUID id;
    private UUID studentId;
    private UUID teachingClassId;
    private String teachingClassName;
    private String courseName;
    private String courseCode;
    private Double credit;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CourseEnrollmentDTO() {}

    public CourseEnrollmentDTO(CourseEnrollment enrollment) {
        this.id = enrollment.getId();
        this.studentId = enrollment.getStudentId();
        this.teachingClassId = enrollment.getTeachingClass() != null ? enrollment.getTeachingClass().getId() : null;
        this.teachingClassName = enrollment.getTeachingClass() != null ? enrollment.getTeachingClass().getName() : null;
        if (enrollment.getTeachingClass() != null && enrollment.getTeachingClass().getCourse() != null) {
            this.courseName = enrollment.getTeachingClass().getCourse().getName();
            this.courseCode = enrollment.getTeachingClass().getCourse().getCourseCode();
            this.credit = enrollment.getTeachingClass().getCourse().getCredit();
        }
        this.status = enrollment.getStatus();
        this.enrolledAt = enrollment.getEnrolledAt();
        this.droppedAt = enrollment.getDroppedAt();
        this.completedAt = enrollment.getCompletedAt();
        this.createdAt = enrollment.getCreatedAt();
        this.updatedAt = enrollment.getUpdatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getTeachingClassId() {
        return teachingClassId;
    }

    public void setTeachingClassId(UUID teachingClassId) {
        this.teachingClassId = teachingClassId;
    }

    public String getTeachingClassName() {
        return teachingClassName;
    }

    public void setTeachingClassName(String teachingClassName) {
        this.teachingClassName = teachingClassName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDateTime getDroppedAt() {
        return droppedAt;
    }

    public void setDroppedAt(LocalDateTime droppedAt) {
        this.droppedAt = droppedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
