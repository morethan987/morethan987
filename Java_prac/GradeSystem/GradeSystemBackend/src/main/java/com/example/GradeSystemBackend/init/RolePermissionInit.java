package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.auth.Permission;
import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.domain.auth.RoleConstants;
import com.example.GradeSystemBackend.repository.PermissionRepository;
import com.example.GradeSystemBackend.repository.RoleRepository;
import com.example.GradeSystemBackend.service.PermissionDiscoveryService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class RolePermissionInit implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(
        RolePermissionInit.class
    );

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionDiscoveryService permissionDiscoveryService;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("开始初始化角色和权限...");

        try {
            // 1. 动态发现并初始化权限
            initPermissionsFromControllers();

            // 2. 初始化角色并分配权限
            initRoles();

            logger.info("角色和权限初始化完成");
        } catch (Exception e) {
            logger.error("角色和权限初始化失败", e);
            throw new RuntimeException("系统初始化失败", e);
        }
    }

    /**
     * 从Controller注解中动态发现并初始化权限
     */
    private void initPermissionsFromControllers() {
        logger.info("开始扫描Controller权限...");

        // 获取权限分组
        PermissionDiscoveryService.PermissionGroups groups =
            permissionDiscoveryService.getGroupedPermissions();

        logger.info("权限扫描结果: {}", groups);

        // 首先确保管理员全局权限存在
        createPermissionIfNotExists(
            RoleConstants.ADMIN_ALL_PERMISSION,
            "管理员全部权限，拥有系统所有操作权限"
        );

        // 创建所有发现的权限
        Set<String> allPermissions = groups.getAllPermissions();
        for (String permission : allPermissions) {
            createPermissionIfNotExists(
                permission,
                generatePermissionDescription(permission)
            );
        }

        logger.info("权限初始化完成，共创建 {} 个权限", allPermissions.size());
    }

    /**
     * 初始化系统角色并分配权限
     */
    private void initRoles() {
        logger.info("开始初始化角色...");

        // 1. 创建管理员角色
        createAdminRole();

        // 2. 创建教师角色
        createTeacherRole();

        // 3. 创建学生角色
        createStudentRole();

        logger.info("角色初始化完成");
    }

    /**
     * 创建管理员角色
     */
    private void createAdminRole() {
        Optional<Role> adminRoleOpt = roleRepository.findByName(
            RoleConstants.ROLE_ADMIN
        );
        if (adminRoleOpt.isPresent()) {
            logger.debug("管理员角色已存在，跳过创建");
            return;
        }

        // 管理员拥有全部权限
        Permission adminPermission = getPermission(
            RoleConstants.ADMIN_ALL_PERMISSION
        );

        Role adminRole = new Role();
        adminRole.setName(RoleConstants.ROLE_ADMIN);
        adminRole.setPermissions(Set.of(adminPermission));
        roleRepository.save(adminRole);

        logger.info("已创建管理员角色: {}", RoleConstants.ROLE_ADMIN);
    }

    /**
     * 创建教师角色
     */
    private void createTeacherRole() {
        Optional<Role> teacherRoleOpt = roleRepository.findByName(
            RoleConstants.ROLE_TEACHER
        );
        if (teacherRoleOpt.isPresent()) {
            logger.debug("教师角色已存在，跳过创建");
            return;
        }

        // 教师权限：基于实际Controller中的权限
        Set<Permission> teacherPermissions = new HashSet<>();

        // 动态获取教师应有的权限
        PermissionDiscoveryService.PermissionGroups groups =
            permissionDiscoveryService.getGroupedPermissions();

        // 添加用户查看权限
        addPermissionsToSet(teacherPermissions, groups.userPermissions, "view");

        // 添加学生管理权限（查看、编辑，通常不包括添加和删除）
        addPermissionsToSet(
            teacherPermissions,
            groups.studentPermissions,
            "view",
            "edit"
        );

        // 添加教师查看权限
        addPermissionsToSet(
            teacherPermissions,
            groups.teacherPermissions,
            "view"
        );

        // 添加课程管理权限（查看、添加、编辑）
        addPermissionsToSet(
            teacherPermissions,
            groups.coursePermissions,
            "view",
            "add",
            "edit"
        );

        // 添加成绩管理权限（查看、录入、编辑、导出）
        addPermissionsToSet(
            teacherPermissions,
            groups.gradePermissions,
            "view",
            "input",
            "edit",
            "export"
        );

        Role teacherRole = new Role();
        teacherRole.setName(RoleConstants.ROLE_TEACHER);
        teacherRole.setPermissions(teacherPermissions);
        roleRepository.save(teacherRole);

        logger.info(
            "已创建教师角色: {}，包含 {} 个权限",
            RoleConstants.ROLE_TEACHER,
            teacherPermissions.size()
        );
    }

    /**
     * 创建学生角色
     */
    private void createStudentRole() {
        Optional<Role> studentRoleOpt = roleRepository.findByName(
            RoleConstants.ROLE_STUDENT
        );
        if (studentRoleOpt.isPresent()) {
            logger.debug("学生角色已存在，跳过创建");
            return;
        }

        // 学生权限：只有查看权限
        Set<Permission> studentPermissions = new HashSet<>();

        // 动态获取学生应有的权限
        PermissionDiscoveryService.PermissionGroups groups =
            permissionDiscoveryService.getGroupedPermissions();

        // 添加基本查看权限
        addPermissionsToSet(studentPermissions, groups.userPermissions, "view");
        addPermissionsToSet(
            studentPermissions,
            groups.studentPermissions,
            "view"
        );
        addPermissionsToSet(
            studentPermissions,
            groups.coursePermissions,
            "view"
        );
        addPermissionsToSet(
            studentPermissions,
            groups.gradePermissions,
            "view"
        );

        Role studentRole = new Role();
        studentRole.setName(RoleConstants.ROLE_STUDENT);
        studentRole.setPermissions(studentPermissions);
        roleRepository.save(studentRole);

        logger.info(
            "已创建学生角色: {}，包含 {} 个权限",
            RoleConstants.ROLE_STUDENT,
            studentPermissions.size()
        );
    }

    /**
     * 将指定操作的权限添加到权限集合中
     */
    private void addPermissionsToSet(
        Set<Permission> targetSet,
        Set<String> sourcePermissions,
        String... allowedActions
    ) {
        for (String permission : sourcePermissions) {
            for (String action : allowedActions) {
                if (permission.endsWith(":" + action)) {
                    Optional<Permission> permObj =
                        permissionRepository.findByName(permission);
                    permObj.ifPresent(targetSet::add);
                    break;
                }
            }
        }
    }

    /**
     * 创建权限（如果不存在）
     */
    private void createPermissionIfNotExists(
        String permissionName,
        String description
    ) {
        Optional<Permission> permissionOpt = permissionRepository.findByName(
            permissionName
        );
        if (permissionOpt.isPresent()) {
            return; // 已存在，不重复创建
        }

        Permission permission = new Permission();
        permission.setName(permissionName);
        permission.setDescription(description);
        permissionRepository.save(permission);

        logger.debug("创建权限: {} - {}", permissionName, description);
    }

    /**
     * 获取权限对象
     */
    private Permission getPermission(String permissionName) {
        return permissionRepository
            .findByName(permissionName)
            .orElseThrow(() ->
                new RuntimeException("权限不存在: " + permissionName)
            );
    }

    /**
     * 根据权限名称生成描述
     */
    private String generatePermissionDescription(String permission) {
        if (permission == null || !permission.contains(":")) {
            return "未知权限";
        }

        String[] parts = permission.split(":", 2);
        String module = parts[0];
        String action = parts[1];

        String moduleDesc = getModuleDescription(module);
        String actionDesc = getActionDescription(action);

        return String.format("%s%s权限", moduleDesc, actionDesc);
    }

    /**
     * 获取模块描述
     */
    private String getModuleDescription(String module) {
        switch (module) {
            case "admin":
                return "管理员";
            case "user":
                return "用户";
            case "student":
                return "学生";
            case "teacher":
                return "教师";
            case "course":
                return "课程";
            case "grade":
                return "成绩";
            case "role":
                return "角色";
            default:
                return module;
        }
    }

    /**
     * 获取操作描述
     */
    private String getActionDescription(String action) {
        switch (action) {
            case "view":
                return "查看";
            case "add":
                return "添加";
            case "edit":
                return "编辑";
            case "update":
                return "更新";
            case "delete":
                return "删除";
            case "input":
                return "录入";
            case "export":
                return "导出";
            case "all":
                return "全部操作";
            default:
                return action;
        }
    }
}
