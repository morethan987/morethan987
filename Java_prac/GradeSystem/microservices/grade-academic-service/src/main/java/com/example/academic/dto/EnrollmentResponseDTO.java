package com.example.academic.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class EnrollmentResponseDTO {

    private UUID enrollmentId;
    private UUID studentId;
    private UUID teachingClassId;
    private String courseName;
    private String courseCode;
    private String teachingClassName;
    private String status;
    private String message;
    private LocalDateTime enrolledAt;

    public EnrollmentResponseDTO() {}

    public static EnrollmentResponseDTO success(UUID enrollmentId, UUID studentId, UUID teachingClassId,
                                                 String courseName, String courseCode, String teachingClassName,
                                                 LocalDateTime enrolledAt) {
        EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
        dto.setEnrollmentId(enrollmentId);
        dto.setStudentId(studentId);
        dto.setTeachingClassId(teachingClassId);
        dto.setCourseName(courseName);
        dto.setCourseCode(courseCode);
        dto.setTeachingClassName(teachingClassName);
        dto.setStatus("SUCCESS");
        dto.setMessage("Successfully enrolled in course");
        dto.setEnrolledAt(enrolledAt);
        return dto;
    }

    public static EnrollmentResponseDTO failure(UUID studentId, UUID teachingClassId, String reason) {
        EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
        dto.setStudentId(studentId);
        dto.setTeachingClassId(teachingClassId);
        dto.setStatus("FAILED");
        dto.setMessage(reason);
        return dto;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(UUID enrollmentId) {
        this.enrollmentId = enrollmentId;
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

    public String getTeachingClassName() {
        return teachingClassName;
    }

    public void setTeachingClassName(String teachingClassName) {
        this.teachingClassName = teachingClassName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}
