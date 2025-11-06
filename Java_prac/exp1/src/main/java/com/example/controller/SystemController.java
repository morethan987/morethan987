package com.example.controller;

import com.example.view.LoginView;
import java.util.HashMap;
import java.util.Map;

public class SystemController {

    private Map<String, String> loginResult;
    private final LoginView loginView = new LoginView();
    private final AuthController authController = new AuthController();

    public SystemController() {
        loginResult = new HashMap<>();
        loginResult.put("res", "false");
        loginResult.put("role", "unknown");
        loginResult.put("reason", "未登录");
        loginResult.put("userid", "");
        loginResult.put("token", "");
    }

    /**
     * 运行系统控制器
     */
    public void run() {
        // 主循环：登录 -> 使用系统 -> 如果 token 失效则重新登录
        while (true) {
            // 登录循环
            while (true) {
                this.login();
                if (loginResult.get("res").equals("true")) {
                    break;
                }
            }

            // 根据角色创建对应的控制器并运行
            // 注意：AuthController 实例会被传递给用户控制器用于 token 验证
            BaseUserController userController = createUserController(
                loginResult.get("role"),
                loginResult.get("userid"),
                loginResult.get("token")
            );

            if (userController != null) {
                boolean normalExit = userController.run();
                if (normalExit) {
                    // 用户选择退出，结束整个系统
                    logout();
                    if (userController.flushData()) {
                        loginView.showMessage("数据已保存");
                    } else {
                        loginView.showMessage("数据保存失败");
                    }
                    loginView.showMessage("感谢使用，再见！");
                    break;
                }
                // 如果 normalExit 为 false，表示 token 验证失败，继续外层循环重新登录
                loginView.showMessage("会话已过期，请重新登录");
            } else {
                loginView.showMessage("无法识别的用户角色");
                break;
            }
        }
    }

    /**
     * 登录处理
     */
    private void login() {
        Map<String, String> credentials = loginView.getLoginCredentials();
        loginResult = authController.handleLogin(
            credentials.get("userid"),
            credentials.get("password")
        );

        if (loginResult.get("res").equals("false")) {
            loginView.showMessage("登录失败: " + loginResult.get("reason"));
        } else {
            loginView.showMessage(
                "登录成功，您的身份: " + loginResult.get("role")
            );
        }
    }

    /**
     * 根据角色创建对应的用户控制器
     * AuthController 被传递给每个控制器以便进行 token 验证
     */
    private BaseUserController createUserController(
        String role,
        String userId,
        String token
    ) {
        switch (role) {
            case "student":
                return new StudentController(userId, token, authController);
            case "teacher":
                return new TeacherController(userId, token, authController);
            default:
                return null;
        }
    }

    private void logout() {
        loginResult.put("res", "false");
        loginResult.put("role", "unknown");
        loginResult.put("reason", "未登录");
        loginResult.put("userid", "");
        loginResult.put("token", "");
    }
}
