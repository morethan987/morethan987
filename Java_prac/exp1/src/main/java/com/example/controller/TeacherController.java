package com.example.controller;

import com.example.model.Grade;
import com.example.model.user.Teacher;
import com.example.view.BaseView;
import com.example.view.TeacherView;
import java.util.HashMap;
import java.util.Map;

public class TeacherController extends BaseUserController {

    private final Teacher teacherDta = new Teacher();
    private final Grade gradeData = new Grade();

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
            "manage_course",
            "管理课程",
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
            case "manage_course":
                manageCourse();
                break;
            case "input_grades":
                inputGrades();
                break;
            default:
                userView.showMessage("无效的操作码: " + operationCode);
                break;
        }
    }

    /**
     * 管理课程（需要 token 验证）
     */
    private void manageCourse() {
        userView.showMessage("=== 课程管理 ===");
        // 这里实现管理课程的逻辑
        // token 已在调用前验证
        userView.showMessage("正在加载课程管理界面...");
    }

    /**
     * 录入成绩（需要 token 验证）
     */
    private void inputGrades() {
        userView.showMessage("=== 成绩录入 ===");
        // 这里实现录入成绩的逻辑
        // token 已在调用前验证
        userView.showMessage("正在加载成绩录入界面...");
    }
}
