package com.example.controller;

import com.example.view.AdminView;
import com.example.view.BaseView;
import com.example.view.LoginView;
import com.example.view.StudentView;
import com.example.view.TeacherView;
import java.util.HashMap;
import java.util.Map;

public class SystemController {

    private Map<String, String> loginResult;
    private Map<String, String> studentCodeMap;
    private Map<String, String> teacherCodeMap;
    private Map<String, String> adminCodeMap;
    private Map<String, String> currentCodeMap;
    private BaseView userView;
    private final LoginView loginView = new LoginView();
    private final AuthController authController = new AuthController();

    public SystemController() {
        loginResult = new HashMap<>();
        loginResult.put("res", "false");
        loginResult.put("role", "unknown");
        loginResult.put("reason", "未登录");
        loginResult.put("userid", "");
        loginResult.put("token", "");
        initCodeMaps();
    }

    private void initCodeMaps() {
        // Initialize code maps for different user roles
        studentCodeMap = Map.of(
            "show_init",
            "显示主菜单",
            "exit",
            "退出",
            "clear_screen",
            "清屏"
        );

        teacherCodeMap = Map.of(
            "show_init",
            "显示主菜单",
            "exit",
            "退出",
            "clear_screen",
            "清屏"
        );

        adminCodeMap = Map.of(
            "show_init",
            "显示主菜单",
            "exit",
            "退出",
            "clear_screen",
            "清屏"
        );
    }

    private void setCurrentCodeMap(String role) {
        switch (role) {
            case "student":
                currentCodeMap = studentCodeMap;
                break;
            case "teacher":
                currentCodeMap = teacherCodeMap;
                break;
            case "admin":
                currentCodeMap = adminCodeMap;
                break;
            default:
                currentCodeMap = Map.of();
                break;
        }
    }

    /**
     * Runs the system controller.
     *
     * @param none
     * @return void
     */
    public void run() {
        // login loop
        while (true) {
            // login
            this.login();
            if (loginResult.get("res") == "true") {
                break;
            }
        }
        setCurrentCodeMap(loginResult.get("role"));

        // user view loop
        String operation = "";
        userView = this.getUserView();
        userView.show_init();
        while (true) {
            operation = userView.getFuctionCodeFromInteger(
                userView.getCodeMap(),
                userView.getChoice(currentCodeMap.size())
            );
            if (operation == "exit") {
                break;
            }
            executeOperation(operation);
        }
    }

    private void executeOperation(String operationCode) {
        // TODO Execute operation based on operation code
        switch (operationCode) {
            case "show_init":
                userView.show_init();
                break;
            case "clear_screen":
                userView.clearScreen();
                userView.show_init();
                break;
            default:
                userView.showMessage("无效的操作码: " + operationCode);
                break;
        }
    }

    /**
     * Login process.
     * @param none
     * @return inner token
     */
    private void login() {
        Map<String, String> credentials = loginView.getLoginCredentials();
        loginResult = authController.handleLogin(
            credentials.get("userid"),
            credentials.get("password")
        );
        if (loginResult.get("res") == "flase") {
            loginView.showMessage("登陆失败" + loginResult.get("reason"));
        } else {
            loginView.showMessage(
                "登陆成功，您的身份：" + loginResult.get("role")
            );
        }
    }

    private BaseView getUserView() {
        String role = loginResult.get("role");
        if (role.equals("admin")) {
            return new AdminView(adminCodeMap);
        } else if (role.equals("student")) {
            return new StudentView(studentCodeMap);
        } else if (role.equals("teacher")) {
            return new TeacherView(teacherCodeMap);
        } else {
            return null;
        }
    }
}
