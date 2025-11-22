package com.example.model.dao;

import com.example.model.entity.Role;
import java.util.List;

/**
 * Role Data Access Object (DAO) Interface
 */
public interface RoleDAO {
    // 初始化数据库表
    void createTable();

    // 增加
    boolean addRole(Role role);

    // 根据ID查询
    Role getRoleById(String id);

    // 根据用户ID查询角色集合
    List<Role> getRolesByUserId(String userId);

    // 查询所有
    List<Role> getAllRoles();

    // 更新
    boolean updateRole(Role role);

    // 删除
    boolean deleteRole(String id);
}
