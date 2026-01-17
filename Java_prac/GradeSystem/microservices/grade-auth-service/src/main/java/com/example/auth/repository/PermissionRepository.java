package com.example.auth.repository;

import com.example.auth.domain.Permission;
import com.example.auth.domain.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    Optional<Permission> findByName(String name);

    List<Permission> findByNameContainingIgnoreCase(String name);

    List<Permission> findByDescriptionContainingIgnoreCase(String description);

    List<Permission> findByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCase(
        String name,
        String description
    );

    boolean existsByName(String name);

    @Query("SELECT COUNT(p) FROM Permission p")
    long countAllPermissions();

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p = :permission")
    List<Role> findRolesByPermission(@Param("permission") Permission permission);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findRolesByPermissionId(@Param("permissionId") UUID permissionId);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findRolesByPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT p FROM Permission p WHERE NOT EXISTS (SELECT r FROM Role r JOIN r.permissions p2 WHERE p2 = p)")
    List<Permission> findUnusedPermissions();

    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p = :permission")
    long countRolesUsingPermission(@Param("permission") Permission permission);

    @Query("SELECT p FROM Permission p ORDER BY p.name ASC")
    List<Permission> findAllOrderByNameAsc();

    @Query("SELECT p FROM Permission p WHERE p.name LIKE :prefix% ORDER BY p.name ASC")
    List<Permission> findByNameStartingWith(@Param("prefix") String prefix);

    @Query("SELECT p FROM Permission p WHERE p.description IS NOT NULL AND p.description != '' ORDER BY p.name ASC")
    List<Permission> findPermissionsWithDescription();

    @Query("SELECT p FROM Permission p WHERE p.description IS NULL OR p.description = '' ORDER BY p.name ASC")
    List<Permission> findPermissionsWithoutDescription();
}
