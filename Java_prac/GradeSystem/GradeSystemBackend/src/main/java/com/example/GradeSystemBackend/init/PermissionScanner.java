package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.auth.Permission;
import com.example.GradeSystemBackend.repository.PermissionRepository;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Component
public class PermissionScanner {

    private final PermissionRepository permissionRepository;

    public PermissionScanner(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * 在 Spring Boot 完全启动后执行扫描
     */
    @PostConstruct
    public void init() throws Exception {
        scanAndInitPermissions();
    }

    private void scanAndInitPermissions() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);

        // 扫描 Controller 和 Service 的方法
        scanner.addIncludeFilter(
            new AnnotationTypeFilter(RestController.class)
        );
        scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));

        Set<String> basePackages = Set.of("com.example"); // 修改为你的根包名

        for (String basePackage : basePackages) {
            for (var bean : scanner.findCandidateComponents(basePackage)) {
                Class<?> clazz = Class.forName(bean.getBeanClassName());
                scanClass(clazz);
            }
        }
    }

    private void scanClass(Class<?> clazz) {
        // 扫描类上的注解
        if (clazz.isAnnotationPresent(PreAuthorize.class)) {
            String expr = clazz.getAnnotation(PreAuthorize.class).value();
            extractAndSavePermissions(expr);
        }

        // 扫描方法上的注解
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreAuthorize.class)) {
                String expr = method.getAnnotation(PreAuthorize.class).value();
                extractAndSavePermissions(expr);
            }
        }
    }

    private void extractAndSavePermissions(String expr) {
        // 匹配 hasAuthority('xxx')
        Pattern p1 = Pattern.compile("hasAuthority\\('([^']+)'\\)");
        Matcher m1 = p1.matcher(expr);
        while (m1.find()) {
            String perm = m1.group(1);
            savePermissionIfMissing(perm);
        }

        // 匹配 hasAnyAuthority('a', 'b', 'c')
        Pattern p2 = Pattern.compile("'([^']+)'");
        Matcher m2 = p2.matcher(expr);
        while (m2.find()) {
            String perm = m2.group(1);
            savePermissionIfMissing(perm);
        }
    }

    private void savePermissionIfMissing(String permName) {
        if (!permissionRepository.existsByName(permName)) {
            Permission p = new Permission();
            p.setName(permName);
            permissionRepository.save(p);
            System.out.println(
                "[PermissionScanner] Initialized permission: " + permName
            );
        }
    }
}
