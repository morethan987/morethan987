package com.example.model.entity;

import com.example.model.dto.BinaryMessage;

/**
 * Teacher entity class
 */
public class Teacher {

    private String id;
    private String name;
    private String gender;
    private Integer age;

    public Teacher() {}

    public Teacher(String id, String name, String gender, Integer age) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public BinaryMessage setId(String id) {
        this.id = id;
        return new BinaryMessage(true, "Set id successfully");
    }

    public String getName() {
        return name;
    }

    public BinaryMessage setName(String name) {
        this.name = name;
        return new BinaryMessage(true, "Set name successfully");
    }

    public String getGender() {
        return gender;
    }

    public BinaryMessage setGender(String gender) {
        this.gender = gender;
        return new BinaryMessage(true, "Set gender successfully");
    }

    public Integer getAge() {
        return age;
    }

    public BinaryMessage setAge(Integer age) {
        this.age = age;
        return new BinaryMessage(true, "Set age successfully");
    }
}
