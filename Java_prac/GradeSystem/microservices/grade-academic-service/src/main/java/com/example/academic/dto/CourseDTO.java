package com.example.academic.dto;

import com.example.academic.domain.Course;
import com.example.academic.domain.CourseType;
import java.time.LocalDateTime;
import java.util.UUID;

public class CourseDTO {

    private UUID id;
    private String name;
    private String courseCode;
    private String description;
    private Double credit;
    private Integer semester;
    private CourseType courseType;
    private Integer totalHours;
    private Integer lectureHours;
    private Integer labHours;
    private String department;
    private String prerequisites;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CourseDTO() {}

    public CourseDTO(Course course) {
        this.id = course.getId();
        this.name = course.getName();
        this.courseCode = course.getCourseCode();
        this.description = course.getDescription();
        this.credit = course.getCredit();
        this.semester = course.getSemester();
        this.courseType = course.getCourseType();
        this.totalHours = course.getTotalHours();
        this.lectureHours = course.getLectureHours();
        this.labHours = course.getLabHours();
        this.department = course.getDepartment();
        this.prerequisites = course.getPrerequisites();
        this.isActive = course.getIsActive();
        this.createdAt = course.getCreatedAt();
        this.updatedAt = course.getUpdatedAt();
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

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public Integer getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Integer totalHours) {
        this.totalHours = totalHours;
    }

    public Integer getLectureHours() {
        return lectureHours;
    }

    public void setLectureHours(Integer lectureHours) {
        this.lectureHours = lectureHours;
    }

    public Integer getLabHours() {
        return labHours;
    }

    public void setLabHours(Integer labHours) {
        this.labHours = labHours;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
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
