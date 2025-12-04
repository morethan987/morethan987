package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.auth.Permission;
import com.example.GradeSystemBackend.domain.auth.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    // 根据权限名称查找
    Optional<Permission> findByName(String name);

    // 根据权限名称模糊查询（不区分大小写）
    List<Permission> findByNameContainingIgnoreCase(String name);

    // 根据描述模糊查询（不区分大小写）
    List<Permission> findByDescriptionContainingIgnoreCase(String description);

    // 根据名称和描述查找权限
    List<
        Permission
    > findByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCase(
        String name,
        String description
    );

    // 检查权限名称是否存在
    boolean existsByName(String name);

    // 统计权限总数
    @Query("SELECT COUNT(p) FROM Permission p")
    long countAllPermissions();

    // 查找拥有特定权限的角色
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p = :permission")
    List<Role> findRolesByPermission(
        @Param("permission") Permission permission
    );

    // 查找拥有特定权限ID的角色
    @Query(
        "SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId"
    )
    List<Role> findRolesByPermissionId(
        @Param("permissionId") UUID permissionId
    );

    // 查找拥有特定权限名称的角色
    @Query(
        "SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName"
    )
    List<Role> findRolesByPermissionName(
        @Param("permissionName") String permissionName
    );

    // 查找没有被任何角色使用的权限
    @Query(
        "SELECT p FROM Permission p WHERE NOT EXISTS (SELECT r FROM Role r JOIN r.permissions p2 WHERE p2 = p)"
    )
    List<Permission> findUnusedPermissions();

    // 统计某个权限被多少个角色使用
    @Query(
        "SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p = :permission"
    )
    long countRolesUsingPermission(@Param("permission") Permission permission);

    // 按名称排序查找所有权限
    @Query("SELECT p FROM Permission p ORDER BY p.name ASC")
    List<Permission> findAllOrderByNameAsc();

    // 根据权限名称前缀查找权限（如 score:*, user:* 等）
    @Query(
        "SELECT p FROM Permission p WHERE p.name LIKE :prefix% ORDER BY p.name ASC"
    )
    List<Permission> findByNameStartingWith(@Param("prefix") String prefix);

    // 查找有描述的权限
    @Query(
        "SELECT p FROM Permission p WHERE p.description IS NOT NULL AND p.description != '' ORDER BY p.name ASC"
    )
    List<Permission> findPermissionsWithDescription();

    // 查找没有描述的权限
    @Query(
        "SELECT p FROM Permission p WHERE p.description IS NULL OR p.description = '' ORDER BY p.name ASC"
    )
    List<Permission> findPermissionsWithoutDescription();
}
