package com.example.controller;

import com.example.model.user.Teacher;
import com.example.view.BaseView;
import com.example.view.TeacherView;
import java.util.Map;

public class TeacherController extends BaseUserController {

    private final Teacher teacherDta = new Teacher();

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
