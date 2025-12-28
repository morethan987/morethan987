package com.example.GradeSystemBackend.service;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限发现服务
 * 自动扫描Controller中的@PreAuthorize注解，提取权限字符串
 */
@Service
public class PermissionDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(
        PermissionDiscoveryService.class
    );

    @Autowired
    private ApplicationContext applicationContext;

    // 用于匹配 hasAuthority('xxx') 和 hasAnyAuthority('xxx', 'yyy') 的正则表达式
    private static final Pattern AUTHORITY_PATTERN = Pattern.compile(
        "hasA(?:ny)?Authority\\(['\"]([^'\"]+)['\"](?:[^)]*['\"]([^'\"]+)['\"])*[^)]*\\)"
    );

    // 用于提取所有权限字符串的正则表达式
    private static final Pattern PERMISSION_EXTRACT_PATTERN = Pattern.compile(
        "['\"]([^'\"]+)['\"]"
    );

    /**
     * 扫描所有Controller并提取权限
     */
    public Set<String> discoverPermissions() {
        Set<String> permissions = new HashSet<>();

        try {
            // 获取所有标注了@Controller或@RestController的类
            String[] controllerNames =
                applicationContext.getBeanNamesForAnnotation(Controller.class);
            String[] restControllerNames =
                applicationContext.getBeanNamesForAnnotation(
                    RestController.class
                );

            // 合并数组
            Set<String> allBeanNames = new HashSet<>();
            Set.of(controllerNames).forEach(allBeanNames::add);
            Set.of(restControllerNames).forEach(allBeanNames::add);

            for (String beanName : allBeanNames) {
                Object bean = applicationContext.getBean(beanName);

                // 使用 AopUtils.getTargetClass 获取原始类
                // 这样才能读取到写在源码上的 @PreAuthorize 注解
                Class<?> targetClass = AopUtils.getTargetClass(bean);

                Set<String> beanPermissions = extractPermissionsFromClass(
                    targetClass
                );
                permissions.addAll(beanPermissions);

                if (!beanPermissions.isEmpty()) {
                    logger.debug(
                        "从 Controller {} (原始类: {}) 中提取到 {} 个权限",
                        beanName,
                        targetClass.getSimpleName(),
                        beanPermissions.size()
                    );
                }
            }

            logger.info("权限扫描完成，共发现 {} 个权限", permissions.size());
            if (logger.isDebugEnabled()) {
                permissions
                    .stream()
                    .sorted()
                    .forEach(perm -> logger.debug("发现权限: {}", perm));
            }
        } catch (Exception e) {
            logger.error("扫描权限时发生错误", e);
        }

        return permissions;
    }

    /**
     * 从指定类中提取权限
     */
    private Set<String> extractPermissionsFromClass(Class<?> clazz) {
        Set<String> permissions = new HashSet<>();

        try {
            // 检查类级别的注解
            if (clazz.isAnnotationPresent(PreAuthorize.class)) {
                PreAuthorize annotation = clazz.getAnnotation(
                    PreAuthorize.class
                );
                Set<String> classPermissions = extractPermissionsFromExpression(
                    annotation.value()
                );
                permissions.addAll(classPermissions);
            }

            // 检查方法级别的注解
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(PreAuthorize.class)) {
                    PreAuthorize annotation = method.getAnnotation(
                        PreAuthorize.class
                    );
                    Set<String> methodPermissions =
                        extractPermissionsFromExpression(annotation.value());
                    permissions.addAll(methodPermissions);

                    if (!methodPermissions.isEmpty()) {
                        logger.debug(
                            "方法 {}.{} 包含权限: {}",
                            clazz.getSimpleName(),
                            method.getName(),
                            methodPermissions
                        );
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(
                "处理类 {} 时发生错误: {}",
                clazz.getName(),
                e.getMessage()
            );
        }

        return permissions;
    }

    /**
     * 从PreAuthorize表达式中提取权限字符串
     */
    private Set<String> extractPermissionsFromExpression(String expression) {
        Set<String> permissions = new HashSet<>();

        if (expression == null || expression.trim().isEmpty()) {
            return permissions;
        }

        try {
            // 查找所有hasAuthority或hasAnyAuthority的调用
            Matcher matcher = AUTHORITY_PATTERN.matcher(expression);
            while (matcher.find()) {
                String authoritySection = matcher.group();

                // 从匹配的部分中提取所有权限字符串
                Matcher permMatcher = PERMISSION_EXTRACT_PATTERN.matcher(
                    authoritySection
                );
                while (permMatcher.find()) {
                    String permission = permMatcher.group(1);
                    if (permission != null && !permission.trim().isEmpty()) {
                        permissions.add(permission.trim());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(
                "解析权限表达式时发生错误: {}, 表达式: {}",
                e.getMessage(),
                expression
            );
        }

        return permissions;
    }

    /**
     * 获取按模块分组的权限
     */
    public PermissionGroups getGroupedPermissions() {
        Set<String> allPermissions = discoverPermissions();
        return groupPermissionsByModule(allPermissions);
    }

    /**
     * 按模块对权限进行分组
     */
    private PermissionGroups groupPermissionsByModule(Set<String> permissions) {
        PermissionGroups groups = new PermissionGroups();

        for (String permission : permissions) {
            String[] parts = permission.split(":", 2);
            if (parts.length == 2) {
                String module = parts[0];

                switch (module) {
                    case "admin":
                        groups.adminPermissions.add(permission);
                        break;
                    case "user":
                        groups.userPermissions.add(permission);
                        break;
                    case "student":
                        groups.studentPermissions.add(permission);
                        break;
                    case "teacher":
                        groups.teacherPermissions.add(permission);
                        break;
                    case "course":
                        groups.coursePermissions.add(permission);
                        break;
                    case "grade":
                        groups.gradePermissions.add(permission);
                        break;
                    case "role":
                        groups.rolePermissions.add(permission);
                        break;
                    case "teaching_class":
                        groups.teachingClassPermissions.add(permission);
                        break;
                    default:
                        groups.otherPermissions.add(permission);
                        break;
                }
            } else {
                groups.otherPermissions.add(permission);
            }
        }

        return groups;
    }

    /**
     * 验证权限字符串是否有效
     */
    public boolean isValidPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }

        // 基本格式验证：module:action
        String trimmed = permission.trim();
        return trimmed.matches("^[a-zA-Z][a-zA-Z0-9_]*:[a-zA-Z][a-zA-Z0-9_]*$");
    }

    /**
     * 检查权限是否存在于Controller注解中
     */
    public boolean permissionExistsInControllers(String permission) {
        Set<String> discoveredPermissions = discoverPermissions();
        return discoveredPermissions.contains(permission);
    }

    /**
     * 权限分组结果类
     */
    public static class PermissionGroups {

        public Set<String> adminPermissions = new HashSet<>();
        public Set<String> userPermissions = new HashSet<>();
        public Set<String> studentPermissions = new HashSet<>();
        public Set<String> teacherPermissions = new HashSet<>();
        public Set<String> coursePermissions = new HashSet<>();
        public Set<String> gradePermissions = new HashSet<>();
        public Set<String> rolePermissions = new HashSet<>();
        public Set<String> teachingClassPermissions = new HashSet<>();
        public Set<String> otherPermissions = new HashSet<>();

        public Set<String> getAllPermissions() {
            Set<String> allPermissions = new HashSet<>();
            allPermissions.addAll(adminPermissions);
            allPermissions.addAll(userPermissions);
            allPermissions.addAll(studentPermissions);
            allPermissions.addAll(teacherPermissions);
            allPermissions.addAll(coursePermissions);
            allPermissions.addAll(gradePermissions);
            allPermissions.addAll(rolePermissions);
            allPermissions.addAll(teachingClassPermissions);
            allPermissions.addAll(otherPermissions);
            return allPermissions;
        }

        public int getTotalCount() {
            return getAllPermissions().size();
        }

        @Override
        public String toString() {
            return String.format(
                "PermissionGroups{总计=%d, admin=%d, user=%d, student=%d, teacher=%d, course=%d, grade=%d, role=%d, other=%d}",
                getTotalCount(),
                adminPermissions.size(),
                userPermissions.size(),
                studentPermissions.size(),
                teacherPermissions.size(),
                coursePermissions.size(),
                gradePermissions.size(),
                rolePermissions.size(),
                teachingClassPermissions.size(),
                otherPermissions.size()
            );
        }
    }
}
