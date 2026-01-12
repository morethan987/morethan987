package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.domain.auth.RoleConstants;
import com.example.GradeSystemBackend.domain.auth.UIType;
import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.info.Gender;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.domain.teacher.Teacher;
import com.example.GradeSystemBackend.domain.teacher.TeacherStatus;
import com.example.GradeSystemBackend.domain.teacher.TeacherTitle;
import com.example.GradeSystemBackend.repository.RoleRepository;
import com.example.GradeSystemBackend.repository.TeacherRepository;
import com.example.GradeSystemBackend.repository.UserProfileRepository;
import com.example.GradeSystemBackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Order(4)
@Component
public class TeacherInit implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TeacherInit.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private RoleRepository roleRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        logger.info("开始初始化教师数据...");

        try {
            initSampleTeachers();
            logger.info("教师数据初始化完成");
        } catch (Exception e) {
            logger.error("教师数据初始化失败", e);
            throw new RuntimeException("教师数据初始化失败", e);
        }
    }

    private void initSampleTeachers() {
        // 如果已有教师数据，跳过初始化
        if (teacherRepository.count() > 0) {
            logger.info("教师数据已存在，跳过初始化");
            return;
        }

        // 获取教师角色
        Optional<Role> teacherRoleOpt = roleRepository.findByName(RoleConstants.ROLE_TEACHER);
        if (!teacherRoleOpt.isPresent()) {
            logger.error("教师角色不存在，无法创建教师数据");
            return;
        }
        Role teacherRole = teacherRoleOpt.get();

        // 创建计算机科学系教师
        createTeacher("张伟", "T001001", "zhangwei", "计算机科学与技术系",
                TeacherTitle.PROFESSOR, "算法与数据结构", Gender.MALE,
                "zhangwei@university.edu", "13800138001", "信息楼301",
                "周一至周五 14:00-16:00", "计算机科学博士，IEEE高级会员",
                "算法设计与分析、机器学习", teacherRole);

        createTeacher("李娜", "T001002", "lina", "计算机科学与技术系",
                TeacherTitle.ASSOCIATE_PROFESSOR, "数据库系统", Gender.FEMALE,
                "lina@university.edu", "13800138002", "信息楼302",
                "周二、周四 10:00-12:00", "计算机应用博士，CCF会员",
                "数据库理论、大数据处理", teacherRole);

        createTeacher("王强", "T001003", "wangqiang", "计算机科学与技术系",
                TeacherTitle.ASSISTANT_PROFESSOR, "软件工程", Gender.MALE,
                "wangqiang@university.edu", "13800138003", "信息楼303",
                "周一、周三 15:30-17:30", "软件工程硕士，CSDN专家",
                "软件架构设计、敏捷开发", teacherRole);

        createTeacher("陈红", "T001004", "chenhong", "计算机科学与技术系",
                TeacherTitle.LECTURER, "计算机网络", Gender.FEMALE,
                "chenhong@university.edu", "13800138004", "信息楼304",
                "周二、周五 09:00-11:00", "计算机网络硕士",
                "网络安全、物联网技术", teacherRole);

        createTeacher("刘明", "T001005", "liuming", "计算机科学与技术系",
                TeacherTitle.PROFESSOR, "人工智能", Gender.MALE,
                "liuming@university.edu", "13800138005", "信息楼305",
                "周三、周四 13:00-15:00", "人工智能博士，中科院客座研究员",
                "深度学习、计算机视觉", teacherRole);

        // 创建数学系教师
        createTeacher("赵敏", "T002001", "zhaomin", "数学系",
                TeacherTitle.PROFESSOR, "高等数学", Gender.FEMALE,
                "zhaomin@university.edu", "13800138006", "理学楼201",
                "周一、周三、周五 10:00-12:00", "数学博士，数学学会理事",
                "数学分析、实变函数", teacherRole);

        createTeacher("孙涛", "T002002", "suntao", "数学系",
                TeacherTitle.ASSOCIATE_PROFESSOR, "线性代数", Gender.MALE,
                "suntao@university.edu", "13800138007", "理学楼202",
                "周二、周四 14:00-16:00", "应用数学博士",
                "矩阵理论、数值计算", teacherRole);

        createTeacher("周静", "T002003", "zhoujing", "数学系",
                TeacherTitle.LECTURER, "概率论与数理统计", Gender.FEMALE,
                "zhoujing@university.edu", "13800138008", "理学楼203",
                "周一、周三 16:00-18:00", "统计学硕士",
                "数理统计、随机过程", teacherRole);

        // 创建物理系教师
        createTeacher("吴勇", "T003001", "wuyong", "物理系",
                TeacherTitle.PROFESSOR, "大学物理", Gender.MALE,
                "wuyong@university.edu", "13800138009", "理学楼301",
                "周二、周四 08:00-10:00", "理论物理博士，物理学会会员",
                "量子力学、相对论", teacherRole);

        createTeacher("郑雪", "T003002", "zhengxue", "物理系",
                TeacherTitle.ASSOCIATE_PROFESSOR, "电路分析", Gender.FEMALE,
                "zhengxue@university.edu", "13800138010", "理学楼302",
                "周一、周五 13:30-15:30", "电子工程博士",
                "电路设计、信号处理", teacherRole);

        // 创建英语系教师
        createTeacher("何文", "T004001", "hewen", "外国语学院",
                TeacherTitle.ASSOCIATE_PROFESSOR, "大学英语", Gender.MALE,
                "hewen@university.edu", "13800138011", "文科楼401",
                "周一至周五 09:00-11:00", "英语文学硕士，英语专业八级",
                "英语文学、翻译理论", teacherRole);

        createTeacher("马丽", "T004002", "mali", "外国语学院",
                TeacherTitle.LECTURER, "科技英语写作", Gender.FEMALE,
                "mali@university.edu", "13800138012", "文科楼402",
                "周二、周四 10:30-12:30", "应用语言学硕士，TESOL认证",
                "学术英语、英语写作", teacherRole);

        logger.info("已创建 {} 位教师", teacherRepository.count());
    }

    private void createTeacher(String name, String employeeCode, String username,
                               String department, TeacherTitle title, String specialization,
                               Gender gender, String email, String phone, String office,
                               String officeHours, String qualifications, String researchInterests,
                               Role teacherRole) {

        // 检查用户是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            logger.debug("用户 {} 已存在，跳过创建", username);
            return;
        }

        // 创建用户账号
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setRoles(Set.of(teacherRole));
        user.setUiType(UIType.TEACHER);
        userRepository.save(user);

        // 创建用户资料
        UserProfile userProfile = new UserProfile(user, name, gender);
        userProfile.setEmail(email);
        userProfile.setPhone(phone);
        userProfile.setAvatarUrl("https://raw.githubusercontent.com/morethan987/hugo_main/refs/heads/main/assets/img/figure_transparent.png");
        userProfileRepository.save(userProfile);

        // 创建教师信息
        Teacher teacher = new Teacher(user, employeeCode, department, title);
        teacher.setSpecialization(specialization);
        teacher.setHireDate(LocalDateTime.now().minusYears(Math.abs(employeeCode.hashCode()) % 10 + 1));
        teacher.setStatus(TeacherStatus.ACTIVE);
        teacher.setSalary(calculateSalaryByTitle(title));
        teacher.setWorkload(3.0); // 默认工作量
        teacher.setMaxCourses(6); // 最大课程数
        teacher.setOffice(office);
        teacher.setOfficePhone("021-" + (60000000 + Math.abs(employeeCode.hashCode()) % 10000000));
        teacher.setOfficeHours(officeHours);
        teacher.setQualifications(qualifications);
        teacher.setResearchInterests(researchInterests);

        teacherRepository.save(teacher);

        logger.debug("创建教师: {} - {} - {} - {}", name, employeeCode, department, title.getDescription());
    }

    private Double calculateSalaryByTitle(TeacherTitle title) {
        switch (title) {
            case DISTINGUISHED_PROFESSOR:
                return 25000.0;
            case PROFESSOR:
            case RESEARCH_PROFESSOR:
                return 20000.0;
            case CLINICAL_PROFESSOR:
                return 18000.0;
            case ASSOCIATE_PROFESSOR:
                return 15000.0;
            case VISITING_PROFESSOR:
                return 12000.0;
            case ASSISTANT_PROFESSOR:
                return 10000.0;
            case ADJUNCT_PROFESSOR:
                return 8000.0;
            case LECTURER:
                return 7000.0;
            case TEACHING_ASSISTANT:
                return 5000.0;
            default:
                return 6000.0;
        }
    }
}
