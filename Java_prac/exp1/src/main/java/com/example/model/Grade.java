package com.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Grade extends BaseModel {

    private Map<String, List<String>> gradeData;
    private static final String GRADE_FILE = "data/grade.csv";

    public Grade() {
        gradeData = super.readFile(GRADE_FILE);
    }

    public void setGradeData(Map<String, List<String>> data) {
        this.gradeData = data;
    }

    public String addGrade(String sid, String cid) {
        UUID gid = UUID.randomUUID();
        gradeData.get("gid").add(gid.toString());
        gradeData.get("sid").add(sid);
        gradeData.get("cid").add(cid);
        return "Grade added successfully.";
    }

    public boolean flush() {
        return super.writeFile(GRADE_FILE, gradeData);
    }

    /**
     * 根据学生ID获取该学生所有课程的成绩
     *
     * @param studentId 学生ID
     * @return 包含该学生所有成绩的Map，键为显示的列名，值为该列的数据列表
     */
    public Map<String, List<String>> getGradesByStudentId(String studentId) {
        // 准备一个新的 Map 用于存放筛选后的学生个人成绩
        Map<String, List<String>> studentGrades = new LinkedHashMap<>();
        studentGrades.put("课程ID", new ArrayList<>());
        studentGrades.put("平时成绩", new ArrayList<>());
        studentGrades.put("期中成绩", new ArrayList<>());
        studentGrades.put("实验成绩", new ArrayList<>());
        studentGrades.put("期末成绩", new ArrayList<>());

        // 获取所有成绩数据中的相关列
        List<String> sids = gradeData.get("sid");
        List<String> cids = gradeData.get("cid");
        List<String> usualScores = gradeData.get("usual_score");
        List<String> midScores = gradeData.get("mid_score");
        List<String> expScores = gradeData.get("exp_score");
        List<String> finalScores = gradeData.get("final_score");

        // 遍历所有成绩记录，找到属于该学生的记录
        for (int i = 0; i < sids.size(); i++) {
            if (sids.get(i).equals(studentId)) {
                // 如果记录的 sid 与传入的 studentId 匹配
                String courseId = cids.get(i);

                // 5. 将该条成绩记录的各个分项添加到 studentGrades 中
                studentGrades.get("课程ID").add(courseId);
                studentGrades.get("平时成绩").add(usualScores.get(i));
                studentGrades.get("期中成绩").add(midScores.get(i));
                studentGrades.get("实验成绩").add(expScores.get(i));
                studentGrades.get("期末成绩").add(finalScores.get(i));
            }
        }

        return studentGrades;
    }

    /**
     * 将成绩字符串解析为 double 类型，无效或空值返回 0.0。
     * 这是一个私有辅助方法。
     */
    private double parseScore(String scoreStr) {
        if (
            scoreStr == null ||
            scoreStr.isEmpty() ||
            scoreStr.equalsIgnoreCase("N/A")
        ) {
            return 0.0;
        }
        try {
            return Double.parseDouble(scoreStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 根据学生ID和课程ID获取单个成绩记录（包含总评成绩）
     */
    public Map<String, String> getGrade(String studentId, String courseId) {
        List<String> sids = gradeData.get("sid");
        List<String> cids = gradeData.get("cid");

        for (int i = 0; i < sids.size(); i++) {
            if (sids.get(i).equals(studentId) && cids.get(i).equals(courseId)) {
                Map<String, String> result = new HashMap<>();
                // 包含所有成绩字段
                for (String key : gradeData.keySet()) {
                    if (
                        !key.equals("gid") &&
                        !key.equals("sid") &&
                        !key.equals("cid")
                    ) {
                        result.put(key, gradeData.get(key).get(i));
                    }
                }
                return result;
            }
        }
        return null; // 如果未找到成绩则返回null
    }

    /**
     * 更新或添加一条成绩记录，并自动计算和存储总评成绩
     */
    public boolean updateOrAddGrade(
        String sid,
        String cid,
        Map<String, String> newScores
    ) {
        // 1. 计算总评成绩
        // 权重假定为：平时20%，期中20%，实验20%，期末40%
        double totalScore =
            parseScore(newScores.get("usual_score")) * 0.2 +
            parseScore(newScores.get("mid_score")) * 0.2 +
            parseScore(newScores.get("exp_score")) * 0.2 +
            parseScore(newScores.get("final_score")) * 0.4;

        // 将计算出的总分也放入 newScores 中，以便统一处理
        newScores.put("total_score", String.format("%.2f", totalScore));

        List<String> sids = gradeData.get("sid");
        List<String> cids = gradeData.get("cid");
        int index = -1;

        // 2. 查找是否已存在记录
        for (int i = 0; i < sids.size(); i++) {
            if (sids.get(i).equals(sid) && cids.get(i).equals(cid)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            // 3. 如果存在，则更新所有成绩（包括总评成绩）
            for (Map.Entry<String, String> entry : newScores.entrySet()) {
                String scoreKey = entry.getKey();
                String scoreValue = entry.getValue();
                if (gradeData.containsKey(scoreKey)) {
                    gradeData.get(scoreKey).set(index, scoreValue);
                }
            }
        } else {
            // 4. 如果不存在，则添加新记录（包括总评成绩）
            gradeData.get("gid").add(UUID.randomUUID().toString());
            gradeData.get("sid").add(sid);
            gradeData.get("cid").add(cid);
            gradeData
                .get("usual_score")
                .add(newScores.getOrDefault("usual_score", ""));
            gradeData
                .get("mid_score")
                .add(newScores.getOrDefault("mid_score", ""));
            gradeData
                .get("exp_score")
                .add(newScores.getOrDefault("exp_score", ""));
            gradeData
                .get("final_score")
                .add(newScores.getOrDefault("final_score", ""));
            gradeData
                .get("total_score")
                .add(newScores.getOrDefault("total_score", "0.00"));
        }

        return true;
    }
}
