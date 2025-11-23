package com.example.model.dao;

import com.example.model.entity.User;
import java.util.List;

/**
 * User Data Access Object (DAO) Interface
 */
public interface UserDAO {
    // 初始化数据库表（可选，方便测试）
    void createTable();

    // 增加用户
    boolean addUser(User user, List<String> roleId);

    // 根据ID查询用户
    User getUserById(String id);

    // 根据用户名查询用户 (用于登录等)
    User getUserByUsername(String username);

    // 查询所有用户
    List<User> getAllUsers();

    // 更新用户信息
    boolean updateUser(User user);

    // 删除用户
    boolean deleteUser(String id);
}
