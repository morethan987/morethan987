package com.example.GradeSystemBackend.domain.course;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name = "匿名课程";

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private Double credit = 3.0;

    @Column(nullable = false)
    private Integer semester = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseType courseType = CourseType.GENERAL;

    // getters / setters
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
