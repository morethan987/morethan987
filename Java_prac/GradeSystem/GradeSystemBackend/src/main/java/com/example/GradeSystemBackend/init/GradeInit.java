package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.grade.Grade;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import com.example.GradeSystemBackend.repository.GradeRepository;
import com.example.GradeSystemBackend.repository.TeachingClassRepository;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(7)
@Component
public class GradeInit implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(
        GradeInit.class
    );

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("开始初始化成绩数据...");

        try {
            initSampleGrades();
            logger.info("成绩数据初始化完成");
        } catch (Exception e) {
            logger.error("成绩数据初始化失败", e);
            throw new RuntimeException("成绩数据初始化失败", e);
        }
    }

    private void initSampleGrades() {
        // 如果已有成绩数据，跳过初始化
        if (gradeRepository.count() > 0) {
            logger.info("成绩数据已存在，跳过初始化");
            return;
        }

        // 获取所有教学班
        List<TeachingClass> teachingClasses = teachingClassRepository.findAll();
        if (teachingClasses.isEmpty()) {
            logger.warn("没有教学班数据，跳过成绩初始化");
            return;
        }

        int gradeCount = 0;

        // 为每个教学班的每个学生生成成绩
        for (TeachingClass teachingClass : teachingClasses) {
            Course course = teachingClass.getCourse();

            if (
                teachingClass.getStudents() == null ||
                teachingClass.getStudents().isEmpty()
            ) {
                continue;
            }

            for (Student student : teachingClass.getStudents()) {
                Grade grade = createGradeForStudent(course, student);
                gradeRepository.save(grade);
                gradeCount++;

                logger.debug(
                    "创建成绩: 学生 {} - 课程 {} - 总分 {}",
                    student.getStudentCode(),
                    course.getName(),
                    grade.getFinalScore()
                );
            }
        }

        logger.info("已创建 {} 条成绩记录", gradeCount);
    }

    private Grade createGradeForStudent(Course course, Student student) {
        Grade grade = new Grade();
        grade.setCourse(course);
        grade.setStudent(student);

        // 根据学生学号生成相对固定但有差异的成绩
        // 使用学号和课程名的哈希值作为随机种子，确保每次运行结果一致
        int seed = (student.getStudentCode() + course.getName()).hashCode();
        Random studentRandom = new Random(seed);

        // 生成基础能力分数（60-95之间）
        double baseScore = 60 + studentRandom.nextDouble() * 35;

        // 根据课程类型调整分数
        double difficultyFactor = getDifficultyFactor(course.getName());
        baseScore *= difficultyFactor;

        // 确保分数在合理范围内
        baseScore = Math.max(40, Math.min(98, baseScore));

        // 生成各项成绩
        grade.setUsualScore(
            generateScoreAround(baseScore + 5, 5, studentRandom)
        ); // 平时成绩通常较高
        grade.setMidScore(generateScoreAround(baseScore, 8, studentRandom));
        grade.setExperimentScore(
            generateScoreAround(baseScore + 3, 6, studentRandom)
        ); // 实验成绩通常较好
        grade.setFinalExamScore(
            generateScoreAround(baseScore - 2, 10, studentRandom)
        ); // 期末考试相对严格

        // 成绩会在PrePersist时自动计算finalScore和gpa
        return grade;
    }

    private double getDifficultyFactor(String courseName) {
        // 根据课程难度调整成绩分布
        switch (courseName) {
            case "高等数学A":
            case "离散数学":
            case "数据结构与算法":
            case "编译原理":
            case "操作系统":
                return 0.85; // 难课程，成绩普遍偏低
            case "计算机组成原理":
            case "计算机网络":
            case "数据库系统原理":
            case "面向对象程序设计":
                return 0.92; // 中等难度
            case "程序设计基础":
            case "Web开发技术":
            case "移动应用开发":
            case "大学英语":
                return 0.98; // 相对容易
            case "思想道德与法治":
            case "中国近现代史纲要":
            case "体育":
            case "军事理论":
                return 1.05; // 公共课，成绩通常较高
            case "人工智能基础":
            case "计算机图形学":
            case "大数据技术":
            case "网络安全":
                return 0.88; // 新兴技术课程，有一定难度
            default:
                return 0.95; // 默认难度
        }
    }

    private Double generateScoreAround(
        double center,
        double variance,
        Random rand
    ) {
        // 生成正态分布的分数
        double score = center + (rand.nextGaussian() * variance);

        // 确保分数在0-100范围内
        score = Math.max(0, Math.min(100, score));

        // 保留一位小数
        return Math.round(score * 10.0) / 10.0;
    }
}
