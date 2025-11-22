package com.example.model.entity;

import com.example.model.dto.BinaryMessage;

/**
 * TeachingClass entity class
 */
public class TeachingClass {

    private String classId;
    private String teacherId;
    private String courseId;
    private Integer semester;
    private String name;

    public TeachingClass(String classId, String teacherId, String courseId) {
        this.classId = classId;
        this.teacherId = teacherId;
        this.courseId = courseId;
    }

    public String getClassId() {
        return classId;
    }

    public BinaryMessage setClassId(String classId) {
        this.classId = classId;
        return new BinaryMessage(false, "设置 classId 成功");
    }

    public String getTeacherId() {
        return teacherId;
    }

    public BinaryMessage setTeacherId(String teacherId) {
        this.teacherId = teacherId;
        return new BinaryMessage(false, "设置 teacherId 成功");
    }

    public String getCourseId() {
        return courseId;
    }

    public BinaryMessage setCourseId(String courseId) {
        this.courseId = courseId;
        return new BinaryMessage(false, "设置 courseId 成功");
    }

    public Integer getSemester() {
        return semester;
    }

    public BinaryMessage setSemester(Integer semester) {
        this.semester = semester;
        return new BinaryMessage(false, "设置 semester 成功");
    }

    public String getName() {
        return name;
    }

    public BinaryMessage setName(String name) {
        this.name = name;
        return new BinaryMessage(false, "设置 name 成功");
    }
}
