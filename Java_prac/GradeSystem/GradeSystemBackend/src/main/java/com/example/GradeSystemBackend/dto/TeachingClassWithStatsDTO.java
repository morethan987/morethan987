package com.example.GradeSystemBackend.dto;

import com.example.GradeSystemBackend.domain.course.CourseType;
import java.util.UUID;

public class TeachingClassWithStatsDTO {

    private UUID id;
    private String className;
    private String courseName;
    private CourseType courseType;
    private Double credit;
    private Integer studentCount;
    private String semester;
    private String schedule;
    private String location;

    public TeachingClassWithStatsDTO() {}

    public TeachingClassWithStatsDTO(
        UUID id,
        String className,
        String courseName,
        CourseType courseType,
        Double credit,
        Integer studentCount,
        String semester,
        String schedule,
        String location
    ) {
        this.id = id;
        this.className = className;
        this.courseName = courseName;
        this.courseType = courseType;
        this.credit = credit;
        this.studentCount = studentCount;
        this.semester = semester;
        this.schedule = schedule;
        this.location = location;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
