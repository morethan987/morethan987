package com.example.GradeSystemBackend.dto;

import java.util.UUID;

public class TeachingClassDTO {
    private UUID id;
    private String name;
    private CourseDTO course;
    private String teacherName;
    private String classroom;
    private String timeSchedule;
    private Integer capacity;
    private Integer enrolled;
    private String status; // "ongoing", "completed", "upcoming"
    private String semesterName;

    // Constructors
    public TeachingClassDTO() {}

    public TeachingClassDTO(UUID id, String name, CourseDTO course, String teacherName,
                           String classroom, String timeSchedule, Integer capacity,
                           Integer enrolled, String status, String semesterName) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.teacherName = teacherName;
        this.classroom = classroom;
        this.timeSchedule = timeSchedule;
        this.capacity = capacity;
        this.enrolled = enrolled;
        this.status = status;
        this.semesterName = semesterName;
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

    public CourseDTO getCourse() {
        return course;
    }

    public void setCourse(CourseDTO course) {
        this.course = course;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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

    public Integer getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Integer enrolled) {
        this.enrolled = enrolled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }
}
