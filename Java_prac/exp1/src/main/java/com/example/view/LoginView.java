package com.example.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * LoginView 类用于处理从用户处读取用户名和密码的逻辑。
 */
public class LoginView {

    private final Scanner scanner;

    /**
     * 构造函数，初始化 Scanner 对象用于读取输入。
     */
    public LoginView() {
        // 使用 System.in 初始化 Scanner
        this.scanner = new Scanner(System.in);
    }

    /**
     * 显示提示信息并读取用户的输入。
     *
     * @param prompt 提示用户输入的信息（如："请输入用户名："）
     * @return 用户输入的字符串
     */
    private String readInput(String prompt) {
        System.out.print(prompt);
        // 使用 nextLine() 读取整行输入，防止空格问题
        return scanner.nextLine().trim(); // 使用 trim() 移除首尾空白
    }

    /**
     * 从用户处读取用户名和密码。
     *
     * @return 包含 "username" 和 "password" 键值对的 Map
     */
    public Map<String, String> getLoginCredentials() {
        String username = "";
        String password = "";

        // 循环直到用户输入非空的用户名
        while (username.isEmpty()) {
            username = readInput("请输入用户名：");
            if (username.isEmpty()) {
                System.out.println("用户名不能为空，请重新输入。");
            }
        }

        // 循环直到用户输入非空的密码
        while (password.isEmpty()) {
            // TODO 在实际应用中，密码输入应该使用特殊方法来避免在屏幕上显示密码字符
            password = readInput("请输入密码：");
            if (password.isEmpty()) {
                System.out.println("密码不能为空，请重新输入。");
            }
        }

        // 将用户名和密码存储在 Map 中返回
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        return credentials;
    }

    /**
     * (可选) 关闭 Scanner 对象，释放系统资源。
     * 通常在应用程序生命周期结束时调用。
     */
    public void closeScanner() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
