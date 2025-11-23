package com.example.view;

import com.example.model.dto.BinaryMessage;
import com.example.model.dto.LoginMessage;
import com.example.model.dto.RegistMessage;

public class LoginView extends BaseView {

    private static final String CANCEL_COMMAND = "0";

    /**
     * 显示登录界面标题
     */
    private void showLoginHeader() {
        clearScreen();
        show("========= 登录界面 =========");
        show("请输入用户名和密码进行登录");
        show("提示: 输入 0 可以返回上级菜单");
        show("============================");
    }

    /**
     * 显示注册界面标题
     */
    private void showRegistHeader() {
        clearScreen();
        show("========= 注册界面 =========");
        show("请输入相关信息进行注册");
        show("提示: 输入 0 可以返回上级菜单");
        show("============================");
    }

    /**
     * 获取登录输入
     * @return 登录信息，如果用户取消则返回null
     */
    public LoginMessage getLoginInput() {
        showLoginHeader();

        // 获取用户名
        System.out.print("用户名: ");
        String username = scanner.nextLine().trim();

        // 检查是否取消
        if (CANCEL_COMMAND.equals(username)) {
            return null;
        }

        // 验证用户名
        if (username.isEmpty()) {
            show(new BinaryMessage(false, "用户名不能为空！"));
            return getLoginInput(); // 递归重新获取
        }

        // 获取密码
        System.out.print("密码: ");
        String password = scanner.nextLine().trim();

        // 检查是否取消
        if (CANCEL_COMMAND.equals(password)) {
            return null;
        }

        // 验证密码
        if (password.isEmpty()) {
            show(new BinaryMessage(false, "密码不能为空！"));
            return getLoginInput(); // 递归重新获取
        }

        return new LoginMessage(username, password);
    }

    /**
     * 获取注册输入
     * @return 注册信息，如果用户取消则返回null
     */
    public RegistMessage getRegistInput() {
        showRegistHeader();

        // 获取角色
        String role = getRoleInput();
        if (role == null) {
            return null; // 用户取消
        }

        // 获取用户名
        System.out.print("用户名: ");
        String username = scanner.nextLine().trim();

        // 检查是否取消
        if (CANCEL_COMMAND.equals(username)) {
            return null;
        }

        // 验证用户名
        if (username.isEmpty()) {
            show(new BinaryMessage(false, "用户名不能为空！"));
            return getRegistInput(); // 递归重新获取
        }

        // 获取密码
        System.out.print("密码: ");
        String password = scanner.nextLine().trim();

        // 检查是否取消
        if (CANCEL_COMMAND.equals(password)) {
            return null;
        }

        // 验证密码
        if (password.isEmpty()) {
            show(new BinaryMessage(false, "密码不能为空！"));
            return getRegistInput(); // 递归重新获取
        }

        return new RegistMessage(role, username, password);
    }

    /**
     * 获取角色输入
     * @return 角色类型，如果用户取消则返回null
     */
    private String getRoleInput() {
        scanner.nextLine(); // 清除输入缓冲区
        while (true) {
            System.out.print("角色 (student/admin/teacher): ");
            String role = scanner.nextLine().trim().toLowerCase();

            // 检查是否取消
            if (CANCEL_COMMAND.equals(role)) {
                return null;
            }

            // 验证角色
            if (
                role.equals("student") ||
                role.equals("admin") ||
                role.equals("teacher")
            ) {
                return role;
            }

            show(
                new BinaryMessage(
                    false,
                    "角色输入无效，请输入 student, admin 或 teacher！"
                )
            );
        }
    }

    /**
     * 旧方法 - 保持向后兼容
     * @deprecated 使用 getLoginInput() 替代
     */
    @Deprecated
    public void show_init() {
        showLoginHeader();
    }

    /**
     * 旧方法 - 保持向后兼容
     * @deprecated 使用 getRegistInput() 替代
     */
    @Deprecated
    public void show_regist() {
        showRegistHeader();
    }

    /**
     * 旧方法 - 保持向后兼容
     * @deprecated 使用 getLoginInput() 替代
     */
    @Deprecated
    public LoginMessage getUserInput() {
        return getLoginInput();
    }
}
