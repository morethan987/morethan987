package com.example.controller;

import com.example.model.Grade;
import com.example.model.TeachingClass;
import com.example.model.user.Student;
import com.example.model.user.Teacher;
import com.example.view.BaseView;
import com.example.view.TeacherView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TeacherController extends BaseUserController {

    private final Teacher teacherDta = new Teacher();
    private final Student studentData = new Student();
    private final Grade gradeData = new Grade();
    private final TeachingClass teachingClassData = new TeachingClass();

    public TeacherController(
        String userId,
        String token,
        AuthController authController
    ) {
        super(userId, token, authController);
    }

    @Override
    protected Map<String, String> initCodeMap() {
        return Map.of(
            "show_init",
            "显示主菜单",
            "clear_screen",
            "清屏",
            "show_personal_info",
            "查看个人信息",
            "update_personal_info",
            "修改个人信息",
            "show_grade_table",
            "查看某教学班的成绩表",
            "show_grade_distribution_chart",
            "查看某教学班的成绩分布图",
            "input_grades",
            "录入成绩",
            "exit",
            "退出"
        );
    }

    @Override
    protected boolean flushData() {
        return teacherDta.flush();
    }

    @Override
    protected BaseView createUserView() {
        return new TeacherView(codeMap);
    }

    @Override
    protected void showPersonalInfo() {
        // Token 已在 executeOperation 中验证
        userView.showMessage("=== 教师个人信息 ===");
        Map<String, String> info = teacherDta.getPersonalInfoById(userId);
        userView.showPersonalInfo(info);
    }

    @Override
    protected void updatePersonalInfo() {
        // Token 已在 executeOperation 中验证
        userView.showMessage("=== 原个人信息 ===");
        showPersonalInfo();

        // 获取用户输入
        Map<String, String> updates = new HashMap<>();
        String newName = userView.readInput("请输入新的姓名（留空则不修改）: ");
        updates.put("name", newName);
        String newGender = userView.readInput(
            "请输入新的性别（留空则不修改）: "
        );
        updates.put("gender", newGender);
        String newAge = userView.readInput("请输入新的年龄（留空则不修改）: ");
        updates.put("age", newAge);
        String newPassword = userView.readInput(
            "请输入新的密码（留空则不修改）: "
        );
        updates.put("password", newPassword);

        String[] res = teacherDta.updateInfo(userId, updates);

        if (res[0].equals("false")) {
            userView.showMessage("个人信息修改失败: " + res[1]);
            return;
        }
        userView.showMessage("个人信息修改成功！");
    }

    @Override
    protected void handleCustomOperation(String operationCode) {
        switch (operationCode) {
            case "input_grades":
                inputGrades();
                break;
            case "show_grade_table":
                shwoGradeTable();
                break;
            case "show_grade_distribution_chart":
                showGradeDistributionChart();
                break;
            default:
                userView.showMessage("无效的操作码: " + operationCode);
                break;
        }
    }

    private void shwoGradeTable() {
        userView.showMessage("=== 成绩表 ===");
        String tcid = getTeachingClassIdFromUser();
        if (tcid == null) return;
        String sortKey = getSortKeyFromUser();
        boolean decrease = getSortOrderFromUser();
        Map<String, List<String>> gradeTable = getGradeTable(tcid);
        List<Integer> sortedIndex = getSortedIndex(
            gradeTable,
            sortKey,
            decrease
        );
        userView.showSortedData(gradeTable, sortedIndex);
    }

    private void showGradeDistributionChart() {
        userView.showMessage("=== 成绩分布图 ===");
        String tcid = getTeachingClassIdFromUser();
        if (tcid == null) {
            return;
        }
        userView.showDistributionChart(getGradeDistribution(tcid));
    }

    /**
     * 成绩录入功能的完整业务逻辑
     */
    private void inputGrades() {
        userView.showMessage("=== 成绩录入 ===");

        // 1. 获取并验证教学班号
        String tcid = getTeachingClassIdFromUser();
        if (tcid == null) return;

        // 2. 验证教师权限
        String actualTid = teachingClassData.getTeacherIdByTeachingClassId(
            tcid
        );
        if (!this.userId.equals(actualTid)) {
            userView.showMessage(
                "错误：您不是该教学班的任课教师，无权录入成绩。"
            );
            return;
        }

        // 3. 准备数据
        String cid = teachingClassData.getCourseIdByTeachingClassId(tcid);
        List<String> studentIds =
            teachingClassData.getStudentIdsByTeachingClassId(tcid);

        if (studentIds.isEmpty()) {
            userView.showMessage("该教学班当前没有学生。");
            return;
        }

        userView.showMessage(
            "开始为教学班 " +
                tcid +
                " 录入成绩。输入 'q' 可在任何时候中断并返回主菜单。"
        );

        // 4. 遍历学生列表，逐个录入成绩
        for (String sid : studentIds) {
            Map<String, String> studentInfo = studentData.getPersonalInfoById(
                sid
            );
            userView.showMessage(
                "\n--- 正在为学生: " +
                    studentInfo.get("学号") +
                    " - " +
                    studentInfo.get("姓名") +
                    " 录入成绩 ---"
            );

            // 显示已有成绩
            Map<String, String> existingGrade = gradeData.getGrade(sid, cid);
            if (existingGrade == null) {
                existingGrade = new HashMap<>(); // 防止空指针
                userView.showMessage("该学生暂无成绩记录。");
            }

            // 准备一个Map来存储用户输入的新成绩
            Map<String, String> newScores = new HashMap<>();
            String[] scoreKeys = {
                "usual_score",
                "mid_score",
                "exp_score",
                "final_score",
            };
            String[] scoreNames = {
                "平时成绩",
                "期中成绩",
                "实验成绩",
                "期末成绩",
            };

            for (int i = 0; i < scoreKeys.length; i++) {
                String key = scoreKeys[i];
                String name = scoreNames[i];
                String currentValue = existingGrade.getOrDefault(key, "N/A");

                String prompt = String.format(
                    "请输入新的%s (当前: %s, 留空不改): ",
                    name,
                    currentValue
                );
                String input = userView.readInput(prompt);

                if (input.equalsIgnoreCase("q")) {
                    userView.showMessage("操作已中断，返回主菜单。");
                    return; // 直接退出整个方法
                }

                if (!input.isEmpty()) {
                    // 只有当用户输入了内容时，才记录为新成绩
                    newScores.put(key, input);
                } else {
                    // 如果用户没输入，但原来有成绩，则保留原成绩
                    if (existingGrade.containsKey(key)) {
                        newScores.put(key, existingGrade.get(key));
                    }
                }
            }

            // 5. 调用模型层方法更新数据（在内存中）
            gradeData.updateOrAddGrade(sid, cid, newScores);
            userView.showMessage(studentInfo.get("姓名") + " 的成绩已更新。");
        }

        // 6. 所有学生处理完毕后，一次性将数据写入文件
        userView.showMessage("\n所有学生成绩录入/更新完毕，正在保存数据...");
        if (gradeData.flush()) {
            userView.showMessage("数据保存成功！");
        } else {
            userView.showMessage("错误：数据保存失败！");
        }
    }

    private String getTeachingClassIdFromUser() {
        while (true) {
            String tcid = userView.readInput("请输入教学班号: ");
            if (!teachingClassData.isTeachingClassExist(tcid)) {
                userView.showMessage("教学班号不存在，请重新输入。");
                continue;
            }
            return tcid;
        }
    }

    private boolean getSortOrderFromUser() {
        // 注意：您原来的代码中注释有误，"asc" 通常是升序，"desc" 是降序。
        // 当前实现遵循您代码的逻辑：输入 "desc" 对应降序， "asc" 对应升序。
        while (true) {
            String order = userView.readInput(
                "请输入排序方式（asc 升序，desc 降序）: "
            );
            if (order.equalsIgnoreCase("asc")) {
                return false; // 非降序，即升序
            } else if (order.equalsIgnoreCase("desc")) {
                return true; // 降序
            } else {
                userView.showMessage("无效的排序方式，请重新输入。");
            }
        }
    }

    private String getSortKeyFromUser() {
        while (true) {
            String key = userView.readInput(
                "请输入排序关键字\nsid 学号, name 姓名, usual_score 平时成绩, mid_score 期中成绩, exp_score 实验成绩, final_score 期末成绩, total_score 总评成绩: "
            );
            if (
                key.equals("sid") ||
                key.equals("name") ||
                key.equals("usual_score") ||
                key.equals("mid_score") ||
                key.equals("exp_score") ||
                key.equals("final_score") ||
                key.equals("total_score")
            ) {
                return key;
            } else {
                userView.showMessage("无效的排序关键字，请重新输入。");
            }
        }
    }

    /**
     * 根据数据、排序键和排序顺序，获取排序后的索引列表。
     */
    private List<Integer> getSortedIndex(
        Map<String, List<String>> data,
        String key,
        boolean decrease // true for 降序, false for 升序
    ) {
        List<String> columnData = data.get(key);
        if (columnData == null || columnData.isEmpty()) {
            return new ArrayList<>();
        }
        int n = columnData.size();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            indices.add(i);
        }
        // 自定义比较器，用于根据指定列的值对索引进行排序
        indices.sort((index1, index2) -> {
            String val1 = columnData.get(index1);
            String val2 = columnData.get(index2);

            // 将"N/A"值视为最小值，排在末尾
            if (val1.equalsIgnoreCase("N/A")) return 1;
            if (val2.equalsIgnoreCase("N/A")) return -1;

            // 优先尝试按数字进行比较
            try {
                double num1 = Double.parseDouble(val1);
                double num2 = Double.parseDouble(val2);
                int result = Double.compare(num1, num2);
                return decrease ? -result : result; // 根据 decrease 标志反转结果
            } catch (NumberFormatException e) {
                // 如果不是数字，则按字符串进行比较
                int result = val1.compareTo(val2);
                return decrease ? -result : result;
            }
        });
        return indices;
    }

    /**
     * 获取指定教学班的成绩表
     */
    private Map<String, List<String>> getGradeTable(String tcid) {
        Map<String, List<String>> gradeTable = new LinkedHashMap<>();
        gradeTable.put("sid", new ArrayList<>());
        gradeTable.put("name", new ArrayList<>());
        gradeTable.put("usual_score", new ArrayList<>());
        gradeTable.put("mid_score", new ArrayList<>());
        gradeTable.put("exp_score", new ArrayList<>());
        gradeTable.put("final_score", new ArrayList<>());
        gradeTable.put("total_score", new ArrayList<>()); // 列名保持不变

        String cid = teachingClassData.getCourseIdByTeachingClassId(tcid);
        if (cid == null) {
            userView.showMessage("错误：无法找到该教学班对应的课程。");
            return gradeTable;
        }

        List<String> studentIdsInClass =
            teachingClassData.getStudentIdsByTeachingClassId(tcid);

        for (String sid : studentIdsInClass) {
            Map<String, String> studentInfo = studentData.getPersonalInfoById(
                sid
            );
            Map<String, String> gradeInfo = gradeData.getGrade(sid, cid); // gradeInfo 现在包含 total_score

            gradeTable.get("sid").add(sid);
            gradeTable.get("name").add(studentInfo.getOrDefault("姓名", "N/A"));

            if (gradeInfo != null) {
                gradeTable
                    .get("usual_score")
                    .add(gradeInfo.getOrDefault("usual_score", "N/A"));
                gradeTable
                    .get("mid_score")
                    .add(gradeInfo.getOrDefault("mid_score", "N/A"));
                gradeTable
                    .get("exp_score")
                    .add(gradeInfo.getOrDefault("exp_score", "N/A"));
                gradeTable
                    .get("final_score")
                    .add(gradeInfo.getOrDefault("final_score", "N/A"));
                // 直接读取已存储的总评成绩
                gradeTable
                    .get("total_score")
                    .add(gradeInfo.getOrDefault("total_score", "N/A"));
            } else {
                // 学生已选课但成绩尚未录入
                gradeTable.get("usual_score").add("N/A");
                gradeTable.get("mid_score").add("N/A");
                gradeTable.get("exp_score").add("N/A");
                gradeTable.get("final_score").add("N/A");
                gradeTable.get("total_score").add("N/A");
            }
        }
        return gradeTable;
    }

    /**
     * 获取指定教学班的成绩分布
     * @param tcid 教学班ID
     * @return 包含成绩分布信息的Map
     */
    public Map<String, Integer> getGradeDistribution(String tcid) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("90-100 (优秀)", 0);
        distribution.put("80-89 (良好)", 0);
        distribution.put("70-79 (中等)", 0);
        distribution.put("60-69 (及格)", 0);
        distribution.put("<60 (不及格)", 0);
        distribution.put("未录入", 0);

        List<String> totalScores = getGradeTable(tcid).get("total_score");
        if (totalScores == null) return distribution;

        for (String scoreStr : totalScores) {
            if (scoreStr.equalsIgnoreCase("N/A")) {
                distribution.merge("未录入", 1, Integer::sum);
                continue;
            }
            try {
                double score = Double.parseDouble(scoreStr);
                if (score >= 90) {
                    distribution.merge("90-100 (优秀)", 1, Integer::sum);
                } else if (score >= 80) {
                    distribution.merge("80-89 (良好)", 1, Integer::sum);
                } else if (score >= 70) {
                    distribution.merge("70-79 (中等)", 1, Integer::sum);
                } else if (score >= 60) {
                    distribution.merge("60-69 (及格)", 1, Integer::sum);
                } else {
                    distribution.merge("<60 (不及格)", 1, Integer::sum);
                }
            } catch (NumberFormatException e) {
                distribution.merge("未录入", 1, Integer::sum);
            }
        }
        return distribution;
    }
}
