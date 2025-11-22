package com.example.model.entity;

import com.example.model.dto.BinaryMessage;

/**
 * Role entity class
 */
public class Role {

    private String id; // 角色 ID
    private String name; // 角色名称

    public Role(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public BinaryMessage setId(String id) {
        this.id = id;
        return new BinaryMessage(true, "角色ID设置成功");
    }

    public String getName() {
        return name;
    }

    public BinaryMessage setName(String name) {
        this.name = name;
        return new BinaryMessage(true, "角色名称设置成功");
    }
}
