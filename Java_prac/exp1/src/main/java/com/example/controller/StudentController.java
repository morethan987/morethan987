package com.example.controller;

import com.example.model.user.Student;
import com.example.view.BaseView;
import com.example.view.StudentView;
import java.util.Map;

public class StudentController extends BaseUserController {

    private final Student studentData = new Student();

    public StudentController(
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
            "view_grades",
            "查看成绩",
            "select_course",
            "选课",
            "exit",
            "退出"
        );
    }

    @Override
    protected boolean flushData() {
        return studentData.flush();
    }

    @Override
    protected BaseView createUserView() {
        return new StudentView(codeMap);
    }

    @Override
    protected void showPersonalInfo() {
        userView.showMessage("=== 学生个人信息 ===");
        Map<String, String> info = studentData.getPersonalInfoById(userId);
        userView.showPersonalInfo(info);
    }

    @Override
    protected void handleCustomOperation(String operationCode) {
        switch (operationCode) {
            case "view_grades":
                viewGrades();
                break;
            case "select_course":
                selectCourse();
                break;
            default:
                userView.showMessage("无效的操作码: " + operationCode);
                break;
        }
    }

    /**
     * 查看成绩（需要 token 验证）
     */
    private void viewGrades() {
        userView.showMessage("=== 成绩查询 ===");
        // 这里实现查看成绩的逻辑
        userView.showMessage("正在加载成绩信息...");
    }

    /**
     * 选课（需要 token 验证）
     */
    private void selectCourse() {
        userView.showMessage("=== 选课系统 ===");
        // 这里实现选课的逻辑
        userView.showMessage("正在加载课程列表...");
    }
}
