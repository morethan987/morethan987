package com.example.model;

import java.util.*;

/**
 * 自动生成符合正态分布的学生成绩数据生成器
 */
public class GradeGenerator {

    private final Grade gradeData = new Grade();

    // 定义教学班信息
    private static final List<Map<String, String>> TEACHING_CLASSES = List.of(
        Map.of(
            "tcid",
            "tc001",
            "cid",
            "Eng101",
            "tid",
            "teacher1",
            "term_idx",
            "1",
            "name",
            "English 101 - Class A"
        ),
        Map.of(
            "tcid",
            "tc002",
            "cid",
            "Eng101",
            "tid",
            "teacher2",
            "term_idx",
            "1",
            "name",
            "English 101 - Class B"
        ),
        Map.of(
            "tcid",
            "tc003",
            "cid",
            "math101",
            "tid",
            "teacher3",
            "term_idx",
            "1",
            "name",
            "Math 101 - Class A"
        ),
        Map.of(
            "tcid",
            "tc004",
            "cid",
            "math101",
            "tid",
            "teacher4",
            "term_idx",
            "1",
            "name",
            "Math 101 - Class B"
        ),
        Map.of(
            "tcid",
            "tc005",
            "cid",
            "math201",
            "tid",
            "teacher5",
            "term_idx",
            "2",
            "name",
            "Math 201 - Class A"
        )
    );

    public void generateGrades() {
        Map<String, List<String>> grades = new LinkedHashMap<>();

        // 定义字段名
        grades.put("gid", new ArrayList<>());
        grades.put("sid", new ArrayList<>());
        grades.put("cid", new ArrayList<>());
        grades.put("usual_score", new ArrayList<>());
        grades.put("mid_score", new ArrayList<>());
        grades.put("exp_score", new ArrayList<>());
        grades.put("final_score", new ArrayList<>());
        grades.put("total_score", new ArrayList<>());

        Random random = new Random();

        // 每个学生选修所有教学班
        for (int i = 1; i <= 100; i++) {
            String sid = "student" + i;

            for (Map<String, String> cls : TEACHING_CLASSES) {
                String gid = UUID.randomUUID().toString();
                String cid = cls.get("cid");

                // 各项成绩服从正态分布
                double usual = getNormalScore(random, 78, 10);
                double mid = getNormalScore(random, 75, 12);
                double exp = getNormalScore(random, 80, 8);
                double fin = getNormalScore(random, 70, 15);

                double total = usual * 0.2 + mid * 0.2 + exp * 0.2 + fin * 0.4;
                total = Math.round(total * 100.0) / 100.0; // 保留两位小数

                grades.get("gid").add(gid);
                grades.get("sid").add(sid);
                grades.get("cid").add(cid);
                grades.get("usual_score").add(String.valueOf(usual));
                grades.get("mid_score").add(String.valueOf(mid));
                grades.get("exp_score").add(String.valueOf(exp));
                grades.get("final_score").add(String.valueOf(fin));
                grades.get("total_score").add(String.valueOf(total));
            }
        }

        gradeData.setGradeData(grades);
        gradeData.flush();
        System.out.println("✅ 成绩数据（正态分布）已生成并保存！");
    }

    /**
     * 生成符合正态分布的成绩（截断在 0~100）
     * @param random 随机数生成器
     * @param mean 平均值
     * @param stdDev 标准差
     * @return 成绩值（0~100，保留一位小数）
     */
    private double getNormalScore(Random random, double mean, double stdDev) {
        double value;
        do {
            value = mean + random.nextGaussian() * stdDev; // 高斯分布
        } while (value < 0 || value > 100); // 截断防止越界
        return Math.round(value * 10.0) / 10.0;
    }
}
