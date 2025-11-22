package com.example.controller;

import com.example.auth.AuthService;
import com.example.model.dto.BinaryMessage;
import com.example.model.dto.LoginMessage;
import com.example.view.LoginView;
import com.example.view.Router;

/**
 * 对系统功能中和系统本身相关的请求进行分组
 */
public class SystemController {

    /**
     * 初始化系统，触发登录并启动路由器
     */
    public static void init() {
        // 进行登录获取sessionId
        String sessionId = login();

        // 启动前端路由器
        Router router = new Router(sessionId);
        router.start();
    }

    /**
     * 处理用户登录逻辑
     * @return 登录成功后的sessionId
     */
    public static String login() {
        // 创建前端登录视图
        LoginView loginView = new LoginView();

        // 进行登录，直到成功为止
        String sessionId = null;
        while (true) {
            loginView.show_init();
            LoginMessage loginMessage = loginView.getUserInput();

            // 根据前端的登录请求进行登录
            BinaryMessage login_res = AuthService.authLogin(loginMessage);
            if (!login_res.isBool_result()) {
                loginView.show(login_res);
            } else {
                loginView.show(new BinaryMessage(true, "登录成功"));
                sessionId = login_res.getMessage();
                break;
            }
        }
        return sessionId;
    }

    /**
     * 处理用户登出逻辑
     */
    public void logout(String sessionId) {}
}
