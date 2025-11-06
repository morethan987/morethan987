package com.example.controller;

import com.example.model.Grade;
import com.example.model.user.Admin;
import com.example.view.AdminView;
import com.example.view.BaseView;
import java.util.HashMap;
import java.util.Map;

public class AdminController extends BaseUserController {

    private final Admin adminData = new Admin();
    private final Grade gradeData = new Grade();

    public AdminController(
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
            "manage_users",
            "用户管理",
            "system_config",
            "系统配置",
            "exit",
            "退出"
        );
    }

    @Override
    protected boolean flushData() {
        return adminData.flush();
    }

    @Override
    protected BaseView createUserView() {
        return new AdminView(codeMap);
    }

    @Override
    protected void showPersonalInfo() {
        // Token 已在 executeOperation 中验证
        userView.showMessage("=== 管理员个人信息 ===");
        Map<String, String> info = adminData.getPersonalInfoById(userId);
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

        String[] res = adminData.updateInfo(userId, updates);

        if (res[0].equals("false")) {
            userView.showMessage("个人信息修改失败: " + res[1]);
            return;
        }
        userView.showMessage("个人信息修改成功！");
    }

    @Override
    protected void handleCustomOperation(String operationCode) {
        switch (operationCode) {
            case "manage_users":
                manageUsers();
                break;
            case "system_config":
                systemConfig();
                break;
            default:
                userView.showMessage("无效的操作码: " + operationCode);
                break;
        }
    }

    /**
     * 用户管理（需要 token 验证）
     */
    private void manageUsers() {
        userView.showMessage("=== 用户管理 ===");
        // 这里实现用户管理的逻辑
        // token 已在调用前验证
        userView.showMessage("正在加载用户管理界面...");
    }

    /**
     * 系统配置（需要 token 验证）
     */
    private void systemConfig() {
        userView.showMessage("=== 系统配置 ===");
        // 这里实现系统配置的逻辑
        // token 已在调用前验证
        userView.showMessage("正在加载系统配置界面...");
    }
}
