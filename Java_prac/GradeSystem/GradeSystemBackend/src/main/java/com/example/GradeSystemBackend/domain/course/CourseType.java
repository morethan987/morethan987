package com.example.GradeSystemBackend.domain.course;

public enum CourseType {
    REQUIRED("必修课"),
    ELECTIVE("选修课"),
    LIMITED_ELECTIVE("限选课"),
    GENERAL("公共课"),
    PROFESSIONAL("专业课");

    private final String description;

    CourseType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
