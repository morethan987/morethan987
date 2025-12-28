package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.course.CourseType;
import com.example.GradeSystemBackend.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class CourseInit implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CourseInit.class);

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("开始初始化课程数据...");

        try {
            initSampleCourses();
            logger.info("课程数据初始化完成");
        } catch (Exception e) {
            logger.error("课程数据初始化失败", e);
            throw new RuntimeException("课程数据初始化失败", e);
        }
    }

    private void initSampleCourses() {
        // 如果已有课程数据，跳过初始化
        if (courseRepository.count() > 0) {
            logger.info("课程数据已存在，跳过初始化");
            return;
        }

        // 计算机科学专业必修课
        createCourse("高等数学A", "微积分、线性代数、概率论与数理统计", 4.0, 1, CourseType.REQUIRED);
        createCourse("程序设计基础", "C语言程序设计基础", 3.0, 1, CourseType.REQUIRED);
        createCourse("离散数学", "集合论、图论、数理逻辑基础", 3.0, 2, CourseType.REQUIRED);
        createCourse("数据结构与算法", "线性表、树、图、排序和查找算法", 4.0, 2, CourseType.REQUIRED);
        createCourse("计算机组成原理", "计算机硬件系统的基本组成和工作原理", 3.5, 3, CourseType.REQUIRED);
        createCourse("操作系统", "进程管理、内存管理、文件系统", 3.5, 3, CourseType.REQUIRED);
        createCourse("数据库系统原理", "关系型数据库设计与SQL", 3.0, 4, CourseType.REQUIRED);
        createCourse("计算机网络", "网络协议、网络安全基础", 3.0, 4, CourseType.REQUIRED);

        // 专业课
        createCourse("面向对象程序设计", "Java/C++面向对象编程", 3.0, 2, CourseType.PROFESSIONAL);
        createCourse("软件工程", "软件开发生命周期、项目管理", 3.0, 5, CourseType.PROFESSIONAL);
        createCourse("编译原理", "词法分析、语法分析、代码生成", 3.0, 5, CourseType.PROFESSIONAL);
        createCourse("人工智能基础", "机器学习、神经网络入门", 3.0, 6, CourseType.PROFESSIONAL);
        createCourse("计算机图形学", "2D/3D图形处理技术", 2.5, 6, CourseType.PROFESSIONAL);

        // 选修课
        createCourse("Web开发技术", "前端后端全栈开发", 2.5, 4, CourseType.ELECTIVE);
        createCourse("移动应用开发", "Android/iOS应用开发", 2.5, 5, CourseType.ELECTIVE);
        createCourse("大数据技术", "Hadoop、Spark等大数据处理", 2.5, 6, CourseType.ELECTIVE);
        createCourse("网络安全", "密码学、网络攻防技术", 2.5, 5, CourseType.ELECTIVE);
        createCourse("算法竞赛", "ACM竞赛算法训练", 2.0, 3, CourseType.ELECTIVE);

        // 限选课
        createCourse("数学建模", "数学建模方法与应用", 2.0, 4, CourseType.LIMITED_ELECTIVE);
        createCourse("科技英语写作", "学术论文写作技巧", 1.5, 5, CourseType.LIMITED_ELECTIVE);
        createCourse("创新创业实践", "创新思维与创业能力培养", 2.0, 6, CourseType.LIMITED_ELECTIVE);

        // 公共课
        createCourse("思想道德与法治", "思想政治理论课程", 2.0, 1, CourseType.GENERAL);
        createCourse("中国近现代史纲要", "历史理论课程", 2.0, 2, CourseType.GENERAL);
        createCourse("马克思主义基本原理", "马克思主义理论", 3.0, 3, CourseType.GENERAL);
        createCourse("毛泽东思想和中国特色社会主义理论体系概论", "政治理论课程", 4.0, 4, CourseType.GENERAL);
        createCourse("大学英语", "英语听说读写训练", 4.0, 1, CourseType.GENERAL);
        createCourse("体育", "体育锻炼与健康", 1.0, 1, CourseType.GENERAL);
        createCourse("军事理论", "国防教育", 1.0, 1, CourseType.GENERAL);

        // 其他学科课程
        createCourse("线性代数", "线性代数基础理论", 3.0, 1, CourseType.REQUIRED);
        createCourse("概率论与数理统计", "概率统计理论与应用", 3.0, 2, CourseType.REQUIRED);
        createCourse("大学物理", "经典物理学基础", 4.0, 2, CourseType.GENERAL);
        createCourse("电路分析", "电子电路基础", 3.0, 3, CourseType.PROFESSIONAL);

        logger.info("已创建 {} 门课程", courseRepository.count());
    }

    private void createCourse(String name, String description, Double credit, Integer semester, CourseType courseType) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setCredit(credit);
        course.setSemester(semester);
        course.setCourseType(courseType);

        courseRepository.save(course);
        logger.debug("创建课程: {} - {} 学分 - 第{}学期 - {}",
                     name, credit, semester, courseType.getDescription());
    }
}
