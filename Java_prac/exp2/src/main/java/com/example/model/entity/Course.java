package com.example.model.entity;

import com.example.model.dto.BinaryMessage;

/**
 * Course entity class
 */
public class Course {

    private String id;
    private String name;
    private String description;

    public Course(String id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.description = desc;
    }

    public String getId() {
        return id;
    }

    public BinaryMessage setId(String id) {
        this.id = id;
        return new BinaryMessage(true, "ID updated successfully.");
    }

    public String getName() {
        return name;
    }

    public BinaryMessage setName(String name) {
        this.name = name;
        return new BinaryMessage(true, "Name updated successfully.");
    }

    public String getDescription() {
        return description;
    }

    public BinaryMessage setDescription(String description) {
        this.description = description;
        return new BinaryMessage(true, "Description updated successfully.");
    }
}
