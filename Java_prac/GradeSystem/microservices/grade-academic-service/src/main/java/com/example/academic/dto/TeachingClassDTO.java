package com.example.academic.dto;

import com.example.academic.domain.TeachingClass;
import com.example.academic.domain.TeachingClassStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class TeachingClassDTO {

    private UUID id;
    private String name;
    private UUID teacherId;
    private UUID courseId;
    private String courseName;
    private String courseCode;
    private String classroom;
    private String timeSchedule;
    private Integer capacity;
    private Integer enrolledCount;
    private TeachingClassStatus status;
    private String academicYear;
    private Integer semesterNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TeachingClassDTO() {}

    public TeachingClassDTO(TeachingClass teachingClass) {
        this.id = teachingClass.getId();
        this.name = teachingClass.getName();
        this.teacherId = teachingClass.getTeacherId();
        this.courseId = teachingClass.getCourse() != null ? teachingClass.getCourse().getId() : null;
        this.courseName = teachingClass.getCourse() != null ? teachingClass.getCourse().getName() : null;
        this.courseCode = teachingClass.getCourse() != null ? teachingClass.getCourse().getCourseCode() : null;
        this.classroom = teachingClass.getClassroom();
        this.timeSchedule = teachingClass.getTimeSchedule();
        this.capacity = teachingClass.getCapacity();
        this.enrolledCount = teachingClass.getEnrolledCount();
        this.status = teachingClass.getStatus();
        this.academicYear = teachingClass.getAcademicYear();
        this.semesterNumber = teachingClass.getSemesterNumber();
        this.startDate = teachingClass.getStartDate();
        this.endDate = teachingClass.getEndDate();
        this.createdAt = teachingClass.getCreatedAt();
        this.updatedAt = teachingClass.getUpdatedAt();
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

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
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

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getTimeSchedule() {
        return timeSchedule;
    }

    public void setTimeSchedule(String timeSchedule) {
        this.timeSchedule = timeSchedule;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public TeachingClassStatus getStatus() {
        return status;
    }

    public void setStatus(TeachingClassStatus status) {
        this.status = status;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
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

    public boolean hasCapacity() {
        return enrolledCount < capacity;
    }

    public int getAvailableSlots() {
        return capacity - enrolledCount;
    }
}
