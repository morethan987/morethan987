package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据重置工具类
 * 用于开发环境中清理和重置样例数据
 *
 * 注意: 此工具仅用于开发和测试环境，生产环境请谨慎使用！
 */
@Component
public class DataResetUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataResetUtil.class);

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * 完全重置所有数据
     * 按照外键依赖关系的逆序删除所有数据
     */
    @Transactional
    public void resetAllData() {
        logger.warn("开始重置所有数据...");

        try {
            // 按照外键依赖关系逆序删除
            deleteGrades();
            deleteTeachingClasses();
            deleteStudents();
            deleteTeachers();
            deleteCourses();
            deleteUserProfiles();
            deleteUsers();
            deleteRoles();
            deletePermissions();

            logger.info("所有数据重置完成");
        } catch (Exception e) {
            logger.error("数据重置过程中发生错误", e);
            throw new RuntimeException("数据重置失败", e);
        }
    }

    /**
     * 重置业务数据（保留权限和角色数据）
     * 删除成绩、教学班、学生、教师、课程和用户数据，但保留权限系统
     */
    @Transactional
    public void resetBusinessData() {
        logger.warn("开始重置业务数据（保留权限系统）...");

        try {
            deleteGrades();
            deleteTeachingClasses();
            deleteStudents();
            deleteTeachers();
            deleteCourses();

            // 删除非管理员用户
            deleteNonAdminUsers();

            logger.info("业务数据重置完成，权限系统和管理员账号已保留");
        } catch (Exception e) {
            logger.error("业务数据重置过程中发生错误", e);
            throw new RuntimeException("业务数据重置失败", e);
        }
    }

    /**
     * 仅重置成绩数据
     */
    @Transactional
    public void resetGradesOnly() {
        logger.info("开始重置成绩数据...");
        deleteGrades();
        logger.info("成绩数据重置完成");
    }

    /**
     * 重置教学班数据（包括相关成绩）
     */
    @Transactional
    public void resetTeachingClassData() {
        logger.info("开始重置教学班数据...");
        deleteGrades();
        deleteTeachingClasses();
        logger.info("教学班数据重置完成");
    }

    private void deleteGrades() {
        long count = gradeRepository.count();
        if (count > 0) {
            gradeRepository.deleteAll();
            logger.info("已删除 {} 条成绩记录", count);
        }
    }

    private void deleteTeachingClasses() {
        long count = teachingClassRepository.count();
        if (count > 0) {
            teachingClassRepository.deleteAll();
            logger.info("已删除 {} 个教学班", count);
        }
    }

    private void deleteStudents() {
        long count = studentRepository.count();
        if (count > 0) {
            studentRepository.deleteAll();
            logger.info("已删除 {} 个学生记录", count);
        }
    }

    private void deleteTeachers() {
        long count = teacherRepository.count();
        if (count > 0) {
            teacherRepository.deleteAll();
            logger.info("已删除 {} 个教师记录", count);
        }
    }

    private void deleteCourses() {
        long count = courseRepository.count();
        if (count > 0) {
            courseRepository.deleteAll();
            logger.info("已删除 {} 门课程", count);
        }
    }

    private void deleteUserProfiles() {
        long count = userProfileRepository.count();
        if (count > 0) {
            userProfileRepository.deleteAll();
            logger.info("已删除 {} 个用户资料", count);
        }
    }

    private void deleteUsers() {
        long count = userRepository.count();
        if (count > 0) {
            userRepository.deleteAll();
            logger.info("已删除 {} 个用户账号", count);
        }
    }

    private void deleteNonAdminUsers() {
        // 删除所有非管理员用户的资料
        userProfileRepository.deleteAll();

        // 保留管理员用户，删除其他用户
        userRepository.findByUsername("admin").ifPresent(adminUser -> {
            userRepository.deleteAll();
            userRepository.save(adminUser);
            logger.info("已删除所有非管理员用户，保留管理员账号");
        });
    }

    private void deleteRoles() {
        long count = roleRepository.count();
        if (count > 0) {
            roleRepository.deleteAll();
            logger.info("已删除 {} 个角色", count);
        }
    }

    private void deletePermissions() {
        long count = permissionRepository.count();
        if (count > 0) {
            permissionRepository.deleteAll();
            logger.info("已删除 {} 个权限", count);
        }
    }

    /**
     * 获取当前数据统计信息
     */
    public void printDataStatistics() {
        logger.info("当前数据统计:");
        logger.info("  权限数量: {}", permissionRepository.count());
        logger.info("  角色数量: {}", roleRepository.count());
        logger.info("  用户数量: {}", userRepository.count());
        logger.info("  用户资料数量: {}", userProfileRepository.count());
        logger.info("  课程数量: {}", courseRepository.count());
        logger.info("  教师数量: {}", teacherRepository.count());
        logger.info("  学生数量: {}", studentRepository.count());
        logger.info("  教学班数量: {}", teachingClassRepository.count());
        logger.info("  成绩记录数量: {}", gradeRepository.count());
    }

    /**
     * 检查数据库是否为空
     */
    public boolean isDatabaseEmpty() {
        return userRepository.count() == 0 &&
               courseRepository.count() == 0 &&
               teacherRepository.count() == 0 &&
               studentRepository.count() == 0 &&
               teachingClassRepository.count() == 0 &&
               gradeRepository.count() == 0;
    }

    /**
     * 检查是否只有权限系统数据
     */
    public boolean hasOnlyPermissionData() {
        return permissionRepository.count() > 0 &&
               roleRepository.count() > 0 &&
               userRepository.count() <= 1 && // 可能有管理员账号
               courseRepository.count() == 0 &&
               teacherRepository.count() == 0 &&
               studentRepository.count() == 0;
    }
}
