package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.auth.Permission;
import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.repository.PermissionRepository;
import com.example.GradeSystemBackend.repository.RoleRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * 创建角色
     */
    public Role createRole(Role role) {
        // 验证角色名称不为空
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }

        // 检查角色名称是否已存在
        if (roleRepository.existsByName(role.getName().trim())) {
            throw new IllegalArgumentException(
                "角色名称 '" + role.getName() + "' 已存在"
            );
        }

        // 设置角色名称（去除首尾空格）
        role.setName(role.getName().trim());

        return roleRepository.save(role);
    }

    /**
     * 删除角色
     */
    public void deleteRole(UUID id) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("角色不存在");
        }

        Role role = roleOpt.get();

        // 防止删除系统内置角色（如ADMIN, TEACHER, STUDENT）
        if (
            "ADMIN".equals(role.getName()) ||
            "TEACHER".equals(role.getName()) ||
            "STUDENT".equals(role.getName())
        ) {
            throw new IllegalArgumentException(
                "不能删除系统内置角色: " + role.getName()
            );
        }

        roleRepository.delete(role);
    }

    /**
     * 为角色添加权限
     */
    @Transactional
    public String addPermissionToRole(UUID roleId, String permissionName) {
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new IllegalArgumentException("权限名称不能为空");
        }

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("角色不存在");
        }

        Optional<Permission> permissionOpt = permissionRepository.findByName(
            permissionName.trim()
        );
        if (permissionOpt.isEmpty()) {
            throw new IllegalArgumentException(
                "权限 '" + permissionName + "' 不存在"
            );
        }

        Role role = roleOpt.get();
        Permission permission = permissionOpt.get();

        // 检查角色是否已有该权限
        if (roleRepository.hasPermission(role, permissionName.trim())) {
            throw new IllegalArgumentException(
                "角色 '" +
                    role.getName() +
                    "' 已拥有权限 '" +
                    permissionName +
                    "'"
            );
        }

        // 添加权限
        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }
        role.getPermissions().add(permission);

        Role savedRole = roleRepository.save(role);
        return (
            "成功为角色 '" +
            savedRole.getName() +
            "' 添加权限 '" +
            permissionName +
            "'"
        );
    }

    /**
     * 从角色移除权限
     */
    @Transactional
    public String removePermissionFromRole(UUID roleId, String permissionName) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("角色不存在");
        }

        Optional<Permission> permissionOpt = permissionRepository.findByName(
            permissionName
        );
        if (permissionOpt.isEmpty()) {
            throw new IllegalArgumentException(
                "权限 '" + permissionName + "' 不存在"
            );
        }

        Role role = roleOpt.get();
        Permission permission = permissionOpt.get();

        // 检查角色是否拥有该权限
        if (!roleRepository.hasPermission(role, permissionName)) {
            throw new IllegalArgumentException(
                "角色 '" +
                    role.getName() +
                    "' 没有权限 '" +
                    permissionName +
                    "'"
            );
        }

        // 移除权限
        if (role.getPermissions() != null) {
            role.getPermissions().remove(permission);
            roleRepository.save(role);
        }

        return (
            "成功从角色 '" +
            role.getName() +
            "' 移除权限 '" +
            permissionName +
            "'"
        );
    }

    /**
     * 批量设置角色权限
     */
    @Transactional
    public Map<String, Object> setRolePermissions(
        UUID roleId,
        List<String> permissionNames
    ) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("角色不存在");
        }

        Role role = roleOpt.get();

        // 防止修改系统内置角色的权限
        if ("ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException(
                "不能修改系统内置角色 ADMIN 的权限"
            );
        }

        Set<Permission> permissions = new HashSet<>();
        List<String> notFoundPermissions = new ArrayList<>();

        for (String permissionName : permissionNames) {
            if (permissionName == null || permissionName.trim().isEmpty()) {
                continue;
            }

            Optional<Permission> permissionOpt =
                permissionRepository.findByName(permissionName.trim());
            if (permissionOpt.isPresent()) {
                permissions.add(permissionOpt.get());
            } else {
                notFoundPermissions.add(permissionName.trim());
            }
        }

        if (!notFoundPermissions.isEmpty()) {
            throw new IllegalArgumentException(
                "以下权限不存在: " + String.join(", ", notFoundPermissions)
            );
        }

        role.setPermissions(permissions);
        Role savedRole = roleRepository.save(role);

        return Map.of(
            "message",
            "成功设置角色 '" + savedRole.getName() + "' 的权限",
            "permissionsCount",
            permissions.size()
        );
    }

    /**
     * 获取所有角色列表
     */
    public Map<String, Object> getAllRoles() {
        List<Role> roles = roleRepository.findAllOrderByNameAsc();
        List<Map<String, Object>> roleList = new ArrayList<>();

        for (Role role : roles) {
            Map<String, Object> roleInfo = new HashMap<>();
            roleInfo.put("id", role.getId());
            roleInfo.put("name", role.getName());

            // 获取权限数量
            Set<Permission> permissions = role.getPermissions();
            int permissionCount = (permissions != null)
                ? permissions.size()
                : 0;
            roleInfo.put("permissionsCount", permissionCount);

            // 检查是否为系统内置角色
            boolean isSystemRole =
                "ADMIN".equals(role.getName()) ||
                "TEACHER".equals(role.getName()) ||
                "STUDENT".equals(role.getName());
            roleInfo.put("isSystemRole", isSystemRole);

            roleList.add(roleInfo);
        }

        return Map.of("roles", roleList, "total", roleList.size());
    }

    /**
     * 根据名称搜索角色
     */
    public Map<String, Object> searchRolesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        List<Role> roles = roleRepository.findByNameContainingIgnoreCase(
            name.trim()
        );
        List<Map<String, Object>> roleList = new ArrayList<>();

        for (Role role : roles) {
            Map<String, Object> roleInfo = new HashMap<>();
            roleInfo.put("id", role.getId());
            roleInfo.put("name", role.getName());

            // 获取权限数量
            Set<Permission> permissions = role.getPermissions();
            int permissionCount = (permissions != null)
                ? permissions.size()
                : 0;
            roleInfo.put("permissionsCount", permissionCount);

            roleList.add(roleInfo);
        }

        return Map.of(
            "roles",
            roleList,
            "total",
            roleList.size(),
            "searchKeyword",
            name.trim()
        );
    }

    /**
     * 查看角色的所有权限
     */
    public Map<String, Object> getRolePermissions(UUID roleId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("角色不存在");
        }

        Role role = roleOpt.get();

        // 强制加载权限（避免懒加载问题）
        Set<Permission> permissions = role.getPermissions();
        if (permissions == null) {
            permissions = new HashSet<>();
        }

        List<Map<String, Object>> permissionList = new ArrayList<>();
        for (Permission permission : permissions) {
            Map<String, Object> permissionInfo = new HashMap<>();
            permissionInfo.put("id", permission.getId());
            permissionInfo.put("name", permission.getName());
            permissionInfo.put("description", permission.getDescription());
            permissionList.add(permissionInfo);
        }

        return Map.of(
            "roleId",
            role.getId(),
            "roleName",
            role.getName(),
            "permissions",
            permissionList,
            "permissionsCount",
            permissionList.size()
        );
    }

    /**
     * 根据ID查看角色详情
     */
    public Map<String, Object> getRoleById(UUID id) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("角色不存在");
        }

        Role role = roleOpt.get();

        // 强制加载权限
        Set<Permission> permissions = role.getPermissions();
        List<String> permissionNames = new ArrayList<>();
        if (permissions != null) {
            for (Permission permission : permissions) {
                permissionNames.add(permission.getName());
            }
        }

        Map<String, Object> roleInfo = new HashMap<>();
        roleInfo.put("id", role.getId());
        roleInfo.put("name", role.getName());
        roleInfo.put("permissions", permissionNames);
        roleInfo.put("permissionsCount", permissionNames.size());

        return roleInfo;
    }

    /**
     * 获取角色统计信息
     */
    public Map<String, Object> getRoleStats() {
        long totalRoles = roleRepository.countAllRoles();
        List<Role> allRoles = roleRepository.findAll();

        int systemRolesCount = 0;
        int customRolesCount = 0;
        int rolesWithPermissionsCount = 0;

        for (Role role : allRoles) {
            if (
                "ADMIN".equals(role.getName()) ||
                "TEACHER".equals(role.getName()) ||
                "STUDENT".equals(role.getName())
            ) {
                systemRolesCount++;
            } else {
                customRolesCount++;
            }

            Set<Permission> permissions = role.getPermissions();
            if (permissions != null && !permissions.isEmpty()) {
                rolesWithPermissionsCount++;
            }
        }

        return Map.of(
            "totalRoles",
            totalRoles,
            "systemRoles",
            systemRolesCount,
            "customRoles",
            customRolesCount,
            "rolesWithPermissions",
            rolesWithPermissionsCount,
            "rolesWithoutPermissions",
            totalRoles - rolesWithPermissionsCount
        );
    }

    /**
     * 检查角色是否存在
     */
    public Map<String, Object> checkRoleExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }

        boolean exists = roleRepository.existsByName(name.trim());

        return Map.of("roleName", name.trim(), "exists", exists);
    }

    /**
     * 获取角色实体
     */
    public Optional<Role> findRoleById(UUID id) {
        return roleRepository.findById(id);
    }

    /**
     * 获取权限实体
     */
    public Optional<Permission> findPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }
}
