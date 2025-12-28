package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.teacher.Teacher;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClassStatus;
import com.example.GradeSystemBackend.repository.CourseRepository;
import com.example.GradeSystemBackend.repository.StudentRepository;
import com.example.GradeSystemBackend.repository.TeacherRepository;
import com.example.GradeSystemBackend.repository.TeachingClassRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(6)
@Component
public class TeachingClassInit implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(
        TeachingClassInit.class
    );

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("开始初始化教学班数据...");

        try {
            initSampleTeachingClasses();
            logger.info("教学班数据初始化完成");
        } catch (Exception e) {
            logger.error("教学班数据初始化失败", e);
            throw new RuntimeException("教学班数据初始化失败", e);
        }
    }

    private void initSampleTeachingClasses() {
        // 如果已有教学班数据，跳过初始化
        if (teachingClassRepository.count() > 0) {
            logger.info("教学班数据已存在，跳过初始化");
            return;
        }

        // 获取所有课程、教师和学生
        List<Course> courses = courseRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        List<Student> students = studentRepository.findAll();

        if (courses.isEmpty() || teachers.isEmpty() || students.isEmpty()) {
            logger.warn("缺少基础数据，跳过教学班初始化");
            return;
        }

        // 创建高等数学A教学班
        createTeachingClassByCourse(
            "高等数学A",
            "赵敏",
            new String[] {
                "2023001001",
                "2023001002",
                "2023001003",
                "2023001004",
                "2023001005",
                "2023002001",
                "2023002002",
                "2023003001",
                "2023003002",
                "2023004001",
                "2023004002",
            },
            "理学楼A101",
            "周一2-4节，周三2-4节",
            60,
            courses,
            teachers,
            students
        );

        // 创建程序设计基础教学班
        createTeachingClassByCourse(
            "程序设计基础",
            "王强",
            new String[] {
                "2023001001",
                "2023001002",
                "2023001003",
                "2023001004",
                "2023001005",
                "2023002001",
                "2023002002",
            },
            "信息楼B201",
            "周二1-3节，周四1-3节",
            40,
            courses,
            teachers,
            students
        );

        // 创建离散数学教学班
        createTeachingClassByCourse(
            "离散数学",
            "孙涛",
            new String[] {
                "2022001001",
                "2022001002",
                "2022001003",
                "2022001004",
                "2022001005",
                "2022001006",
                "2022002001",
                "2022002002",
                "2022002003",
            },
            "理学楼A203",
            "周一5-6节，周三5-6节",
            45,
            courses,
            teachers,
            students
        );

        // 创建数据结构与算法教学班
        createTeachingClassByCourse(
            "数据结构与算法",
            "张伟",
            new String[] {
                "2022001001",
                "2022001002",
                "2022001003",
                "2022001004",
                "2022001005",
                "2022001006",
                "2022002001",
                "2022002002",
                "2022002003",
            },
            "信息楼B301",
            "周二3-5节，周四3-5节",
            45,
            courses,
            teachers,
            students
        );

        // 创建计算机组成原理教学班
        createTeachingClassByCourse(
            "计算机组成原理",
            "陈红",
            new String[] {
                "2021001001",
                "2021001002",
                "2021001003",
                "2021001004",
                "2021001005",
                "2021001006",
                "2021001007",
                "2021001008",
            },
            "信息楼B401",
            "周一1-2节，周三1-2节",
            40,
            courses,
            teachers,
            students
        );

        // 创建操作系统教学班
        createTeachingClassByCourse(
            "操作系统",
            "李娜",
            new String[] {
                "2021001001",
                "2021001002",
                "2021001003",
                "2021001004",
                "2021001005",
                "2021001006",
                "2021001007",
                "2021001008",
            },
            "信息楼B402",
            "周二7-8节，周四7-8节",
            40,
            courses,
            teachers,
            students
        );

        // 创建数据库系统原理教学班
        createTeachingClassByCourse(
            "数据库系统原理",
            "李娜",
            new String[] {
                "2021001001",
                "2021001002",
                "2021001003",
                "2021001004",
                "2021001005",
            },
            "信息楼B501",
            "周一3-4节，周三3-4节",
            35,
            courses,
            teachers,
            students
        );

        // 创建面向对象程序设计教学班
        createTeachingClassByCourse(
            "面向对象程序设计",
            "王强",
            new String[] {
                "2022001001",
                "2022001002",
                "2022001003",
                "2022001004",
                "2022001005",
                "2022001006",
            },
            "信息楼B302",
            "周二5-6节，周四5-6节",
            40,
            courses,
            teachers,
            students
        );

        // 创建人工智能基础教学班
        createTeachingClassByCourse(
            "人工智能基础",
            "刘明",
            new String[] {
                "2021001001",
                "2021001002",
                "2021001003",
                "2022002001",
                "2023004001",
                "2023004002",
                "2023004003",
            },
            "信息楼A301",
            "周一7-8节，周三7-8节",
            35,
            courses,
            teachers,
            students
        );

        // 创建Web开发技术教学班（选修课）
        createTeachingClassByCourse(
            "Web开发技术",
            "王强",
            new String[] {
                "2021001004",
                "2021001005",
                "2021001006",
                "2022001001",
                "2022002001",
            },
            "信息楼B503",
            "周五3-5节",
            30,
            courses,
            teachers,
            students
        );

        // 创建大学英语教学班
        createTeachingClassByCourse(
            "大学英语",
            "何文",
            new String[] {
                "2023001001",
                "2023001002",
                "2023001003",
                "2023001004",
                "2023001005",
                "2023002001",
                "2023002002",
                "2023003001",
                "2023003002",
            },
            "文科楼201",
            "周一1-2节，周三1-2节",
            50,
            courses,
            teachers,
            students
        );

        // 创建大学物理教学班
        createTeachingClassByCourse(
            "大学物理",
            "吴勇",
            new String[] {
                "2022001001",
                "2022001002",
                "2022001003",
                "2022001004",
                "2022001005",
                "2022001006",
                "2022002001",
                "2022002002",
            },
            "理学楼B301",
            "周二1-2节，周四1-2节",
            50,
            courses,
            teachers,
            students
        );

        // 创建线性代数教学班
        createTeachingClassByCourse(
            "线性代数",
            "孙涛",
            new String[] {
                "2023001001",
                "2023001002",
                "2023001003",
                "2023001004",
                "2023001005",
            },
            "理学楼A205",
            "周一3-4节，周三3-4节",
            40,
            courses,
            teachers,
            students
        );

        // 创建思想道德与法治教学班
        createTeachingClassByCourse(
            "思想道德与法治",
            "何文",
            new String[] {
                "2023001001",
                "2023001002",
                "2023001003",
                "2023001004",
                "2023001005",
                "2023002001",
                "2023002002",
                "2023003001",
                "2023003002",
                "2023004001",
            },
            "文科楼301",
            "周二5-6节",
            50,
            courses,
            teachers,
            students
        );

        logger.info("已创建 {} 个教学班", teachingClassRepository.count());
    }

    private void createTeachingClassByCourse(
        String courseName,
        String teacherName,
        String[] studentCodes,
        String classroom,
        String timeSchedule,
        Integer capacity,
        List<Course> courses,
        List<Teacher> teachers,
        List<Student> students
    ) {
        // 查找课程
        Optional<Course> courseOpt = courses
            .stream()
            .filter(c -> c.getName().equals(courseName))
            .findFirst();
        if (!courseOpt.isPresent()) {
            logger.warn("课程 {} 不存在，跳过教学班创建", courseName);
            return;
        }
        Course course = courseOpt.get();

        // 查找教师
        Optional<Teacher> teacherOpt = teachers
            .stream()
            .filter(t -> {
                // 需要通过UserProfile查找教师姓名
                return (
                    (teacherName.equals("张伟") &&
                        t.getEmployeeCode().equals("T001001")) ||
                    (teacherName.equals("李娜") &&
                        t.getEmployeeCode().equals("T001002")) ||
                    (teacherName.equals("王强") &&
                        t.getEmployeeCode().equals("T001003")) ||
                    (teacherName.equals("陈红") &&
                        t.getEmployeeCode().equals("T001004")) ||
                    (teacherName.equals("刘明") &&
                        t.getEmployeeCode().equals("T001005")) ||
                    (teacherName.equals("赵敏") &&
                        t.getEmployeeCode().equals("T002001")) ||
                    (teacherName.equals("孙涛") &&
                        t.getEmployeeCode().equals("T002002")) ||
                    (teacherName.equals("周静") &&
                        t.getEmployeeCode().equals("T002003")) ||
                    (teacherName.equals("吴勇") &&
                        t.getEmployeeCode().equals("T003001")) ||
                    (teacherName.equals("郑雪") &&
                        t.getEmployeeCode().equals("T003002")) ||
                    (teacherName.equals("何文") &&
                        t.getEmployeeCode().equals("T004001")) ||
                    (teacherName.equals("马丽") &&
                        t.getEmployeeCode().equals("T004002"))
                );
            })
            .findFirst();
        if (!teacherOpt.isPresent()) {
            logger.warn("教师 {} 不存在，跳过教学班创建", teacherName);
            return;
        }
        Teacher teacher = teacherOpt.get();

        // 查找学生
        Set<Student> classStudents = new HashSet<>();
        for (String studentCode : studentCodes) {
            Optional<Student> studentOpt = students
                .stream()
                .filter(s -> s.getStudentCode().equals(studentCode))
                .findFirst();
            if (studentOpt.isPresent()) {
                classStudents.add(studentOpt.get());
            } else {
                logger.debug("学生 {} 不存在", studentCode);
            }
        }

        if (classStudents.isEmpty()) {
            logger.warn("没有找到有效学生，跳过教学班 {} 创建", courseName);
            return;
        }

        // 创建教学班
        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setName(courseName + " - " + teacherName + "班");
        teachingClass.setCourse(course);
        teachingClass.setTeacher(teacher);
        teachingClass.setStudents(classStudents);
        teachingClass.setClassroom(classroom);
        teachingClass.setTimeSchedule(timeSchedule);
        teachingClass.setCapacity(capacity);
        teachingClass.setStatus(TeachingClassStatus.ACTIVE);

        teachingClassRepository.save(teachingClass);

        logger.debug(
            "创建教学班: {} - 教师: {} - 学生数: {} - 教室: {}",
            teachingClass.getName(),
            teacherName,
            classStudents.size(),
            classroom
        );
    }
}
