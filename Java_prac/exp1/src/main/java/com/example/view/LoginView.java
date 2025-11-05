package com.example.view;

import java.util.HashMap;
import java.util.Map;

/**
 * LoginView 类用于处理从用户处读取用户名和密码的逻辑
 */
public class LoginView extends BaseView {

    /**
     * 构造函数，初始化 Scanner 对象用于读取输入。
     */
    public LoginView() {
        super();
    }

    /**
     * 从用户处读取用户名和密码。
     * @return 包含 "username" 和 "password" 键值对的 Map
     */
    public Map<String, String> getLoginCredentials() {
        String userid = "";
        String password = "";

        // 循环直到用户输入非空的用户名
        while (userid.isEmpty()) {
            userid = readInput("请输入用户id：");
            if (userid.isEmpty()) {
                System.out.println("用户id不能为空，请重新输入");
            }
        }

        // 循环直到用户输入非空的密码
        while (password.isEmpty()) {
            // TODO 在实际应用中，密码输入应该使用特殊方法来避免在屏幕上显示密码字符
            password = readInput("请输入密码：");
            if (password.isEmpty()) {
                System.out.println("密码不能为空，请重新输入");
            }
        }

        // 将用户名和密码存储在 Map 中返回
        Map<String, String> credentials = new HashMap<>();
        credentials.put("userid", userid);
        credentials.put("password", password);

        return credentials;
    }
}
