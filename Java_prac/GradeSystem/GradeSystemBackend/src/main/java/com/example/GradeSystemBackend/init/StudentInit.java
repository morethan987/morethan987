package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.domain.auth.RoleConstants;
import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.info.Gender;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.student.StudentStatus;
import com.example.GradeSystemBackend.repository.RoleRepository;
import com.example.GradeSystemBackend.repository.StudentRepository;
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

@Order(5)
@Component
public class StudentInit implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StudentInit.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoleRepository roleRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        logger.info("开始初始化学生数据...");

        try {
            initSampleStudents();
            logger.info("学生数据初始化完成");
        } catch (Exception e) {
            logger.error("学生数据初始化失败", e);
            throw new RuntimeException("学生数据初始化失败", e);
        }
    }

    private void initSampleStudents() {
        // 如果已有学生数据，跳过初始化
        if (studentRepository.count() > 0) {
            logger.info("学生数据已存在，跳过初始化");
            return;
        }

        // 获取学生角色
        Optional<Role> studentRoleOpt = roleRepository.findByName(RoleConstants.ROLE_STUDENT);
        if (!studentRoleOpt.isPresent()) {
            logger.error("学生角色不存在，无法创建学生数据");
            return;
        }
        Role studentRole = studentRoleOpt.get();

        // 创建2021级计算机科学与技术专业学生
        createStudent("李明", "2021001001", "limeng", "计算机科学与技术", "计科21-1班", 2021, 6,
                Gender.MALE, "limeng@student.edu", "13900139001", "王强", studentRole);

        createStudent("王小红", "2021001002", "wangxiaohong", "计算机科学与技术", "计科21-1班", 2021, 6,
                Gender.FEMALE, "wangxiaohong@student.edu", "13900139002", "王强", studentRole);

        createStudent("张三", "2021001003", "zhangsan", "计算机科学与技术", "计科21-1班", 2021, 6,
                Gender.MALE, "zhangsan@student.edu", "13900139003", "王强", studentRole);

        createStudent("李四", "2021001004", "lisi", "计算机科学与技术", "计科21-1班", 2021, 6,
                Gender.MALE, "lisi@student.edu", "13900139004", "王强", studentRole);

        createStudent("王五", "2021001005", "wangwu", "计算机科学与技术", "计科21-1班", 2021, 6,
                Gender.MALE, "wangwu@student.edu", "13900139005", "王强", studentRole);

        createStudent("赵六", "2021001006", "zhaoliu", "计算机科学与技术", "计科21-2班", 2021, 6,
                Gender.MALE, "zhaoliu@student.edu", "13900139006", "李娜", studentRole);

        createStudent("孙七", "2021001007", "sunqi", "计算机科学与技术", "计科21-2班", 2021, 6,
                Gender.FEMALE, "sunqi@student.edu", "13900139007", "李娜", studentRole);

        createStudent("周八", "2021001008", "zhouba", "计算机科学与技术", "计科21-2班", 2021, 6,
                Gender.MALE, "zhouba@student.edu", "13900139008", "李娜", studentRole);

        // 创建2022级计算机科学与技术专业学生
        createStudent("吴九", "2022001001", "wujiu", "计算机科学与技术", "计科22-1班", 2022, 4,
                Gender.MALE, "wujiu@student.edu", "13900139009", "张伟", studentRole);

        createStudent("郑十", "2022001002", "zhengshi", "计算机科学与技术", "计科22-1班", 2022, 4,
                Gender.FEMALE, "zhengshi@student.edu", "13900139010", "张伟", studentRole);

        createStudent("钱一", "2022001003", "qianyi", "计算机科学与技术", "计科22-1班", 2022, 4,
                Gender.MALE, "qianyi@student.edu", "13900139011", "张伟", studentRole);

        createStudent("冯二", "2022001004", "fenger", "计算机科学与技术", "计科22-2班", 2022, 4,
                Gender.FEMALE, "fenger@student.edu", "13900139012", "陈红", studentRole);

        createStudent("陈三", "2022001005", "chensan", "计算机科学与技术", "计科22-2班", 2022, 4,
                Gender.MALE, "chensan@student.edu", "13900139013", "陈红", studentRole);

        createStudent("褚四", "2022001006", "chusi", "计算机科学与技术", "计科22-2班", 2022, 4,
                Gender.FEMALE, "chusi@student.edu", "13900139014", "陈红", studentRole);

        // 创建2023级计算机科学与技术专业学生
        createStudent("卫五", "2023001001", "weiwu", "计算机科学与技术", "计科23-1班", 2023, 2,
                Gender.MALE, "weiwu@student.edu", "13900139015", "刘明", studentRole);

        createStudent("蒋六", "2023001002", "jiangliu", "计算机科学与技术", "计科23-1班", 2023, 2,
                Gender.FEMALE, "jiangliu@student.edu", "13900139016", "刘明", studentRole);

        createStudent("沈七", "2023001003", "shenqi", "计算机科学与技术", "计科23-1班", 2023, 2,
                Gender.MALE, "shenqi@student.edu", "13900139017", "刘明", studentRole);

        createStudent("韩八", "2023001004", "hanba", "计算机科学与技术", "计科23-2班", 2023, 2,
                Gender.FEMALE, "hanba@student.edu", "13900139018", "张伟", studentRole);

        createStudent("杨九", "2023001005", "yangjiu", "计算机科学与技术", "计科23-2班", 2023, 2,
                Gender.MALE, "yangjiu@student.edu", "13900139019", "张伟", studentRole);

        // 创建软件工程专业学生
        createStudent("朱十", "2022002001", "zhushi", "软件工程", "软工22-1班", 2022, 4,
                Gender.MALE, "zhushi@student.edu", "13900139020", "王强", studentRole);

        createStudent("秦一", "2022002002", "qinyi", "软件工程", "软工22-1班", 2022, 4,
                Gender.FEMALE, "qinyi@student.edu", "13900139021", "王强", studentRole);

        createStudent("尤二", "2022002003", "youer", "软件工程", "软工22-1班", 2022, 4,
                Gender.MALE, "youer@student.edu", "13900139022", "王强", studentRole);

        createStudent("许三", "2023002001", "xusan", "软件工程", "软工23-1班", 2023, 2,
                Gender.FEMALE, "xusan@student.edu", "13900139023", "李娜", studentRole);

        createStudent("何四", "2023002002", "hesi", "软件工程", "软工23-1班", 2023, 2,
                Gender.MALE, "hesi@student.edu", "13900139024", "李娜", studentRole);

        // 创建数据科学与大数据技术专业学生
        createStudent("吕五", "2023003001", "lvwu", "数据科学与大数据技术", "数据23-1班", 2023, 2,
                Gender.MALE, "lvwu@student.edu", "13900139025", "刘明", studentRole);

        createStudent("施六", "2023003002", "shiliu", "数据科学与大数据技术", "数据23-1班", 2023, 2,
                Gender.FEMALE, "shiliu@student.edu", "13900139026", "刘明", studentRole);

        createStudent("张七", "2023003003", "zhangqi", "数据科学与大数据技术", "数据23-1班", 2023, 2,
                Gender.MALE, "zhangqi@student.edu", "13900139027", "刘明", studentRole);

        // 创建人工智能专业学生
        createStudent("孔八", "2023004001", "kongba", "人工智能", "人工智能23-1班", 2023, 2,
                Gender.FEMALE, "kongba@student.edu", "13900139028", "刘明", studentRole);

        createStudent("曹九", "2023004002", "caojiu", "人工智能", "人工智能23-1班", 2023, 2,
                Gender.MALE, "caojiu@student.edu", "13900139029", "刘明", studentRole);

        createStudent("严十", "2023004003", "yanshi", "人工智能", "人工智能23-1班", 2023, 2,
                Gender.FEMALE, "yanshi@student.edu", "13900139030", "刘明", studentRole);

        logger.info("已创建 {} 位学生", studentRepository.count());
    }

    private void createStudent(String name, String studentCode, String username,
                               String major, String className, Integer enrollmentYear,
                               Integer currentSemester, Gender gender, String email,
                               String phone, String advisor, Role studentRole) {

        // 检查用户是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            logger.debug("用户 {} 已存在，跳过创建", username);
            return;
        }

        // 创建用户账号
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setRoles(Set.of(studentRole));
        userRepository.save(user);

        // 创建用户资料
        UserProfile userProfile = new UserProfile(user, name, gender);
        userProfile.setEmail(email);
        userProfile.setPhone(phone);
        userProfile.setAvatarUrl("https://raw.githubusercontent.com/morethan987/hugo_main/refs/heads/main/assets/img/figure_transparent.png");
        userProfileRepository.save(userProfile);

        // 创建学生信息
        Student student = new Student(user, studentCode, major, className, enrollmentYear);
        student.setCurrentSemester(currentSemester);
        student.setStatus(StudentStatus.ENROLLED);
        student.setTotalCredits(128.0); // 默认总学分要求
        student.setAdvisor(advisor);

        // 设置预期毕业时间（本科4年）
        LocalDateTime expectedGraduation = LocalDateTime.of(enrollmentYear + 4, 7, 1, 0, 0);
        student.setExpectedGraduationDate(expectedGraduation);

        studentRepository.save(student);

        logger.debug("创建学生: {} - {} - {} - {} - 第{}学期",
                     name, studentCode, major, className, currentSemester);
    }
}
