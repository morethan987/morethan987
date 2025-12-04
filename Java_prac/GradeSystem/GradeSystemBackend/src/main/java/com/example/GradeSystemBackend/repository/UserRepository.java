package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 根据用户名模糊查询
    List<User> findByUsernameContainingIgnoreCase(String username);

    // 根据启用状态查找用户
    List<User> findByEnabled(boolean enabled);

    // 查找已启用的用户
    List<User> findByEnabledTrue();

    // 查找已禁用的用户
    List<User> findByEnabledFalse();

    // 根据用户名和启用状态查找用户
    Optional<User> findByUsernameAndEnabled(String username, boolean enabled);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 统计用户总数
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    // 统计已启用的用户数量
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countEnabledUsers();

    // 统计已禁用的用户数量
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = false")
    long countDisabledUsers();

    // 按用户名排序查找所有用户
    @Query("SELECT u FROM User u ORDER BY u.username ASC")
    List<User> findAllOrderByUsernameAsc();
}
