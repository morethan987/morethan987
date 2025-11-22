package com.example.model.dao;

import com.example.model.entity.Permission;
import java.util.List;

/**
 * Permission Data Access Object (DAO) Interface
 */
public interface PermissionDAO {
    /**
     * 初始化数据库表（并在必要时创建数据目录）
     */
    void createTable();

    boolean addPermission(Permission permission);

    Permission getPermissionById(String permissionId);

    List<Permission> getAllPermissions();

    boolean updatePermission(Permission permission);

    boolean deletePermission(String permissionId);
}
