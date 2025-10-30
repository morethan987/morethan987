package com.example.controller;

import com.example.view.LoginView;
import java.util.Map;

public class SystemController {

    private final LoginView loginView;

    public SystemController() {
        this.loginView = new LoginView();
    }

    /**
     * Runs the system controller.
     *
     * @param none
     * @return void
     */
    public void run() {
        // 获取登录凭据
        Map<String, String> credentials = loginView.getLoginCredentials();
        String username = credentials.get("username");
        String password = credentials.get("password");
        // 在此处可以添加进一步的处理逻辑，例如验证用户名和密码
    }
}
