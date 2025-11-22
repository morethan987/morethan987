package com.example.view;

import com.example.model.dto.BinaryMessage;
import com.example.model.dto.LoginMessage;

public class LoginView extends BaseView {

    public void show_init() {
        System.out.println("========= 登录界面 =========");
        System.out.println("请输入用户名和密码进行登录");
    }

    /**
     * 读取用户的登陆信息
     * @return {@link LoginMessage}
     */
    public LoginMessage getUserInput() {
        scanner.nextLine(); // 清除缓冲区

        String username;
        do {
            System.out.print("用户名: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                this.show(
                    new BinaryMessage(false, "用户名不能为空，请重新输入！")
                );
            }
        } while (username.isEmpty());

        String password;
        do {
            System.out.print("密码: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                this.show(
                    new BinaryMessage(false, "密码不能为空，请重新输入！")
                );
            }
        } while (password.isEmpty());

        return new LoginMessage(username, password);
    }
}
