package com.example.GradeSystemBackend.dto;

import com.example.GradeSystemBackend.domain.course.CourseType;
import java.util.UUID;

public class CourseDTO {
    private UUID id;
    private String name;
    private String description;
    private Double credit;
    private Integer semester;
    private CourseType courseType;

    // Constructors
    public CourseDTO() {}

    public CourseDTO(UUID id, String name, String description, Double credit, Integer semester, CourseType courseType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.credit = credit;
        this.semester = semester;
        this.courseType = courseType;
    }

    // Getters and Setters
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
}
