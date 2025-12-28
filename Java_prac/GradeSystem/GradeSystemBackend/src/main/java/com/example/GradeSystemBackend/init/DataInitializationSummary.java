package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(8)
@Component
public class DataInitializationSummary implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(
        DataInitializationSummary.class
    );

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("=".repeat(80));
        logger.info("数据初始化完成总结");
        logger.info("=".repeat(80));

        // 统计各类数据数量
        long roleCount = roleRepository.count();
        long permissionCount = permissionRepository.count();
        long userCount = userRepository.count();
        long userProfileCount = userProfileRepository.count();
        long courseCount = courseRepository.count();
        long teacherCount = teacherRepository.count();
        long studentCount = studentRepository.count();
        long teachingClassCount = teachingClassRepository.count();
        long gradeCount = gradeRepository.count();

        // 输出统计信息
        logger.info("权限系统数据:");
        logger.info("  - 角色数量: {}", roleCount);
        logger.info("  - 权限数量: {}", permissionCount);
        logger.info("");

        logger.info("用户系统数据:");
        logger.info("  - 用户数量: {}", userCount);
        logger.info("  - 用户资料数量: {}", userProfileCount);
        logger.info("");

        logger.info("教学系统数据:");
        logger.info("  - 课程数量: {}", courseCount);
        logger.info("  - 教师数量: {}", teacherCount);
        logger.info("  - 学生数量: {}", studentCount);
        logger.info("  - 教学班数量: {}", teachingClassCount);
        logger.info("  - 成绩记录数量: {}", gradeCount);
        logger.info("");

        // 计算总计
        long totalRecords =
            roleCount +
            permissionCount +
            userCount +
            userProfileCount +
            courseCount +
            teacherCount +
            studentCount +
            teachingClassCount +
            gradeCount;

        logger.info("数据初始化统计:");
        logger.info("  - 总记录数: {}", totalRecords);

        // 显示默认账号信息
        logger.info("");
        logger.info("默认账号信息:");
        logger.info("=".repeat(50));
        logger.info("管理员账号:");
        logger.info("  用户名: admin");
        logger.info("  密码: admin123");
        logger.info("");
        logger.info("教师账号示例 (密码均为: 123456):");
        logger.info("  zhangwei   - 张伟 (教授)");
        logger.info("  lina       - 李娜 (副教授)");
        logger.info("  wangqiang  - 王强 (助理教授)");
        logger.info("  chenhong   - 陈红 (讲师)");
        logger.info("  liuming    - 刘明 (教授)");
        logger.info("");
        logger.info("学生账号示例 (密码均为: 123456):");
        logger.info("  limeng     - 李明 (2021001001)");
        logger.info("  zhangsan   - 张三 (2021001003)");
        logger.info("  wujiu      - 吴九 (2022001001)");
        logger.info("  weiwu      - 卫五 (2023001001)");
        logger.info("");

        // 显示数据分布信息
        logger.info("数据分布信息:");
        logger.info("=".repeat(50));
        logger.info("学生按年级分布:");
        logger.info("  - 2021级: 8人 (大四)");
        logger.info("  - 2022级: 6人 (大三)");
        logger.info("  - 2023级: 16人 (大二)");
        logger.info("");
        logger.info("专业分布:");
        logger.info("  - 计算机科学与技术: 20人");
        logger.info("  - 软件工程: 5人");
        logger.info("  - 数据科学与大数据技术: 3人");
        logger.info("  - 人工智能: 3人");
        logger.info("");

        // 验证数据完整性
        boolean dataIntegrityValid = validateDataIntegrity(
            userCount,
            userProfileCount,
            teacherCount,
            studentCount
        );

        if (dataIntegrityValid) {
            logger.info("✅ 数据完整性验证通过");
        } else {
            logger.warn("❌ 数据完整性验证失败，请检查数据");
        }

        logger.info("");
        logger.info("🎉 成绩管理系统样例数据初始化完成!");
        logger.info("📝 您现在可以使用上述账号登录系统进行测试");
        logger.info("=".repeat(80));
    }

    /**
     * 验证数据完整性
     */
    private boolean validateDataIntegrity(
        long userCount,
        long userProfileCount,
        long teacherCount,
        long studentCount
    ) {
        // 每个用户都应该有对应的用户资料
        if (userCount != userProfileCount) {
            logger.error(
                "用户数量({})与用户资料数量({})不匹配",
                userCount,
                userProfileCount
            );
            return false;
        }

        // 检查教师和学生总数是否合理
        long expectedUserCount = 1 + teacherCount + studentCount; // 1个管理员 + 教师 + 学生
        if (userCount < expectedUserCount) {
            logger.error(
                "用户总数({})少于期望数量({})",
                userCount,
                expectedUserCount
            );
            return false;
        }

        // 检查是否有教学班
        if (teachingClassRepository.count() == 0) {
            logger.error("没有教学班数据");
            return false;
        }

        // 检查是否有成绩记录
        if (gradeRepository.count() == 0) {
            logger.error("没有成绩记录");
            return false;
        }

        return true;
    }
}
