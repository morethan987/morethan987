package com.example.auth.service;

import com.example.auth.domain.Permission;
import com.example.auth.domain.Role;
import com.example.auth.repository.PermissionRepository;
import com.example.auth.repository.RoleRepository;
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

    public Role createRole(Role role) {
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        if (roleRepository.existsByName(role.getName().trim())) {
            throw new IllegalArgumentException("Role name '" + role.getName() + "' already exists");
        }

        role.setName(role.getName().trim());
        return roleRepository.save(role);
    }

    public void deleteRole(UUID id) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }

        Role role = roleOpt.get();

        if ("ADMIN".equals(role.getName()) ||
            "TEACHER".equals(role.getName()) ||
            "STUDENT".equals(role.getName())) {
            throw new IllegalArgumentException("Cannot delete system built-in role: " + role.getName());
        }

        roleRepository.delete(role);
    }

    @Transactional
    public String addPermissionToRole(UUID roleId, String permissionName) {
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be empty");
        }

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }

        Optional<Permission> permissionOpt = permissionRepository.findByName(permissionName.trim());
        if (permissionOpt.isEmpty()) {
            throw new IllegalArgumentException("Permission '" + permissionName + "' not found");
        }

        Role role = roleOpt.get();
        Permission permission = permissionOpt.get();

        if (roleRepository.hasPermission(role, permissionName.trim())) {
            throw new IllegalArgumentException("Role '" + role.getName() + "' already has permission '" + permissionName + "'");
        }

        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }
        role.getPermissions().add(permission);

        Role savedRole = roleRepository.save(role);
        return "Successfully added permission '" + permissionName + "' to role '" + savedRole.getName() + "'";
    }

    @Transactional
    public String removePermissionFromRole(UUID roleId, String permissionName) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }

        Optional<Permission> permissionOpt = permissionRepository.findByName(permissionName);
        if (permissionOpt.isEmpty()) {
            throw new IllegalArgumentException("Permission '" + permissionName + "' not found");
        }

        Role role = roleOpt.get();
        Permission permission = permissionOpt.get();

        if (!roleRepository.hasPermission(role, permissionName)) {
            throw new IllegalArgumentException("Role '" + role.getName() + "' does not have permission '" + permissionName + "'");
        }

        if (role.getPermissions() != null) {
            role.getPermissions().remove(permission);
            roleRepository.save(role);
        }

        return "Successfully removed permission '" + permissionName + "' from role '" + role.getName() + "'";
    }

    @Transactional
    public Map<String, Object> setRolePermissions(UUID roleId, List<String> permissionNames) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }

        Role role = roleOpt.get();

        if ("ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException("Cannot modify permissions of system built-in role ADMIN");
        }

        Set<Permission> permissions = new HashSet<>();
        List<String> notFoundPermissions = new ArrayList<>();

        for (String permissionName : permissionNames) {
            if (permissionName == null || permissionName.trim().isEmpty()) {
                continue;
            }

            Optional<Permission> permissionOpt = permissionRepository.findByName(permissionName.trim());
            if (permissionOpt.isPresent()) {
                permissions.add(permissionOpt.get());
            } else {
                notFoundPermissions.add(permissionName.trim());
            }
        }

        if (!notFoundPermissions.isEmpty()) {
            throw new IllegalArgumentException("The following permissions do not exist: " + String.join(", ", notFoundPermissions));
        }

        role.setPermissions(permissions);
        Role savedRole = roleRepository.save(role);

        return Map.of(
            "message", "Successfully set permissions for role '" + savedRole.getName() + "'",
            "permissionsCount", permissions.size()
        );
    }

    public Map<String, Object> getAllRoles() {
        List<Role> roles = roleRepository.findAllOrderByNameAsc();
        List<Map<String, Object>> roleList = new ArrayList<>();

        for (Role role : roles) {
            Map<String, Object> roleInfo = new HashMap<>();
            roleInfo.put("id", role.getId());
            roleInfo.put("name", role.getName());

            Set<Permission> permissions = role.getPermissions();
            int permissionCount = (permissions != null) ? permissions.size() : 0;
            roleInfo.put("permissionsCount", permissionCount);

            boolean isSystemRole = "ADMIN".equals(role.getName()) ||
                                   "TEACHER".equals(role.getName()) ||
                                   "STUDENT".equals(role.getName());
            roleInfo.put("isSystemRole", isSystemRole);

            roleList.add(roleInfo);
        }

        return Map.of("roles", roleList, "total", roleList.size());
    }

    public Map<String, Object> searchRolesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }

        List<Role> roles = roleRepository.findByNameContainingIgnoreCase(name.trim());
        List<Map<String, Object>> roleList = new ArrayList<>();

        for (Role role : roles) {
            Map<String, Object> roleInfo = new HashMap<>();
            roleInfo.put("id", role.getId());
            roleInfo.put("name", role.getName());

            Set<Permission> permissions = role.getPermissions();
            int permissionCount = (permissions != null) ? permissions.size() : 0;
            roleInfo.put("permissionsCount", permissionCount);

            roleList.add(roleInfo);
        }

        return Map.of(
            "roles", roleList,
            "total", roleList.size(),
            "searchKeyword", name.trim()
        );
    }

    public Map<String, Object> getRolePermissions(UUID roleId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }

        Role role = roleOpt.get();

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
            "roleId", role.getId(),
            "roleName", role.getName(),
            "permissions", permissionList,
            "permissionsCount", permissionList.size()
        );
    }

    public Map<String, Object> getRoleById(UUID id) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }

        Role role = roleOpt.get();

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

    public Map<String, Object> getRoleStats() {
        long totalRoles = roleRepository.countAllRoles();
        List<Role> allRoles = roleRepository.findAll();

        int systemRolesCount = 0;
        int customRolesCount = 0;
        int rolesWithPermissionsCount = 0;

        for (Role role : allRoles) {
            if ("ADMIN".equals(role.getName()) ||
                "TEACHER".equals(role.getName()) ||
                "STUDENT".equals(role.getName())) {
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
            "totalRoles", totalRoles,
            "systemRoles", systemRolesCount,
            "customRoles", customRolesCount,
            "rolesWithPermissions", rolesWithPermissionsCount,
            "rolesWithoutPermissions", totalRoles - rolesWithPermissionsCount
        );
    }

    public Map<String, Object> checkRoleExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        boolean exists = roleRepository.existsByName(name.trim());
        return Map.of("roleName", name.trim(), "exists", exists);
    }

    public Optional<Role> findRoleById(UUID id) {
        return roleRepository.findById(id);
    }

    public Optional<Permission> findPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }
}
