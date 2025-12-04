package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.Role;
import com.example.GradeSystemBackend.domain.Permission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    // 根据角色名称查找
    Optional<Role> findByName(String name);

    // 根据角色名称模糊查询（不区分大小写）
    List<Role> findByNameContainingIgnoreCase(String name);

    // 查找拥有特定权限的角色
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    // 查找拥有特定权限的角色（权限对象）
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p = :permission")
    List<Role> findByPermission(@Param("permission") Permission permission);

    // 检查角色名称是否存在
    boolean existsByName(String name);

    // 统计角色总数
    @Query("SELECT COUNT(r) FROM Role r")
    long countAllRoles();

    // 查找拥有特定权限ID的角色
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findByPermissionId(@Param("permissionId") UUID permissionId);

    // 查找拥有所有指定权限的角色
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name IN :permissionNames GROUP BY r HAVING COUNT(DISTINCT p) = :permissionCount")
    List<Role> findByAllPermissions(
        @Param("permissionNames") List<String> permissionNames,
        @Param("permissionCount") long permissionCount
    );

    // 查找拥有任一指定权限的角色
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name IN :permissionNames")
    List<Role> findByAnyPermission(@Param("permissionNames") List<String> permissionNames);

    // 按名称排序查找所有角色
    @Query("SELECT r FROM Role r ORDER BY r.name ASC")
    List<Role> findAllOrderByNameAsc();

    // 检查角色是否拥有特定权限
    @Query("SELECT COUNT(r) > 0 FROM Role r JOIN r.permissions p WHERE r = :role AND p.name = :permissionName")
    boolean hasPermission(@Param("role") Role role, @Param("permissionName") String permissionName);

    // 查找拥有指定权限数量的角色
    @Query("SELECT r FROM Role r JOIN r.permissions p GROUP BY r HAVING COUNT(p) >= :minCount")
    List<Role> findByMinPermissionCount(@Param("minCount") long minCount);
}
