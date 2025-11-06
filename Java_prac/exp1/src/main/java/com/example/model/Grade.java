package com.example.model;

import java.util.ArrayList;
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
}
