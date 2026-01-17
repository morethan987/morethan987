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
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByName(String name);

    List<Role> findByNameContainingIgnoreCase(String name);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p = :permission")
    List<Role> findByPermission(@Param("permission") Permission permission);

    boolean existsByName(String name);

    @Query("SELECT COUNT(r) FROM Role r")
    long countAllRoles();

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findByPermissionId(@Param("permissionId") UUID permissionId);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name IN :permissionNames GROUP BY r HAVING COUNT(DISTINCT p) = :permissionCount")
    List<Role> findByAllPermissions(
        @Param("permissionNames") List<String> permissionNames,
        @Param("permissionCount") long permissionCount
    );

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name IN :permissionNames")
    List<Role> findByAnyPermission(@Param("permissionNames") List<String> permissionNames);

    @Query("SELECT r FROM Role r ORDER BY r.name ASC")
    List<Role> findAllOrderByNameAsc();

    @Query("SELECT COUNT(r) > 0 FROM Role r JOIN r.permissions p WHERE r = :role AND p.name = :permissionName")
    boolean hasPermission(@Param("role") Role role, @Param("permissionName") String permissionName);

    @Query("SELECT r FROM Role r JOIN r.permissions p GROUP BY r HAVING COUNT(p) >= :minCount")
    List<Role> findByMinPermissionCount(@Param("minCount") long minCount);
}
