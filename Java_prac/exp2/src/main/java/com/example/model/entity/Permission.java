package com.example.model.entity;

import com.example.model.dto.BinaryMessage;

/**
 * Permission entity class
 */
public class Permission {

    private String permissionId;
    private String name;

    public Permission(String permissionId, String name) {
        this.permissionId = permissionId;
        this.name = name;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public BinaryMessage setPermissionId(String permissionId) {
        this.permissionId = permissionId;
        return new BinaryMessage(true, "Permission ID set successfully.");
    }

    public String getName() {
        return name;
    }

    public BinaryMessage setName(String name) {
        this.name = name;
        return new BinaryMessage(true, "Permission name set successfully.");
    }
}
