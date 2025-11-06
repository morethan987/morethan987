package com.example.controller;

import com.example.view.BaseView;
import java.util.Map;

public abstract class BaseUserController {

    protected BaseView userView;
    protected Map<String, String> codeMap;
    protected String userId;
    protected String token;
    protected AuthController authController;

    public BaseUserController(
        String userId,
        String token,
        AuthController authController
    ) {
        this.userId = userId;
        this.token = token;
        this.authController = authController;
        this.codeMap = initCodeMap();
        this.userView = createUserView();
    }

    /**
     * 初始化操作码映射
     */
    protected abstract Map<String, String> initCodeMap();

    /**
     * 创建对应的用户视图
     */
    protected abstract BaseView createUserView();

    /**
     * 创建对应的用户视图
     */
    protected abstract boolean flushData();

    /**
     * 验证 token 是否有效
     */
    protected boolean validateToken() {
        return authController.checkToken(token);
    }

    /**
     * 运行用户界面循环
     * @return true 表示正常退出，false 表示需要重新登录
     */
    public boolean run() {
        String operation = "";
        userView.show_init();

        while (true) {
            operation = userView.getFuctionCodeFromInteger(
                userView.getCodeMap(),
                userView.getChoice(codeMap.size())
            );

            if (operation.equals("exit")) {
                return true; // 正常退出
            }

            // 执行操作，如果返回 false 表示需要重新登录
            if (!executeOperation(operation)) {
                return false; // token 验证失败，需要重新登录
            }
        }
    }

    /**
     * 执行操作（在操作前验证 token）
     * @return true 表示操作成功，false 表示 token 验证失败需要重新登录
     */
    protected boolean executeOperation(String operationCode) {
        // Token 验证 - 对于需要权限的操作
        if (requiresAuthentication(operationCode)) {
            if (!validateToken()) {
                userView.showMessage("身份验证失败，请重新登录");
                return false; // 返回 false 表示需要重新登录
            }
        }

        switch (operationCode) {
            case "show_init":
                userView.show_init();
                break;
            case "clear_screen":
                userView.clearScreen();
                userView.show_init();
                break;
            case "show_personal_info":
                showPersonalInfo();
                break;
            default:
                handleCustomOperation(operationCode);
                break;
        }

        return true; // 操作成功执行
    }

    /**
     * 判断操作是否需要身份验证
     * 子类可以重写此方法来定义哪些操作需要验证
     */
    protected boolean requiresAuthentication(String operationCode) {
        // 基本操作不需要验证
        switch (operationCode) {
            case "show_init":
            case "clear_screen":
            case "exit":
                return false;
            default:
                // 其他操作默认需要验证
                return true;
        }
    }

    /**
     * 处理自定义操作
     */
    protected void handleCustomOperation(String operationCode) {
        userView.showMessage("无效的操作码: " + operationCode);
    }

    /**
     * 显示个人信息
     */
    protected void showPersonalInfo() {
        userView.showMessage("用户ID: " + userId);
    }
}
