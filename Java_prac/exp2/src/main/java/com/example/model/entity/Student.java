package com.example.model.entity;

import com.example.model.dto.BinaryMessage;

/**
 * Student entity class
 */
public class Student {

    private String stu_id;
    private String stu_name;
    private String gender;
    private Integer age;

    public Student(String stu_id, String stu_name, String gender, Integer age) {
        this.stu_id = stu_id;
        this.stu_name = stu_name;
        this.gender = gender;
        this.age = age;
    }

    public BinaryMessage setID(String id) {
        this.stu_id = id;
        return new BinaryMessage(true, "学生ID设置成功");
    }

    public String getID() {
        return stu_id;
    }

    public BinaryMessage setName(String name) {
        this.stu_name = name;
        return new BinaryMessage(true, "学生姓名设置成功");
    }

    public String getName() {
        return stu_name;
    }

    public BinaryMessage setGender(String gender) {
        this.gender = gender;
        return new BinaryMessage(true, "学生性别设置成功");
    }

    public String getGender() {
        return gender;
    }

    public BinaryMessage setAge(Integer age) {
        this.age = age;
        return new BinaryMessage(true, "学生年龄设置成功");
    }

    public Integer getAge() {
        return age;
    }
}
