package com.example.view;

import com.example.controller.*;
import com.example.dispatcher.MethodDispatcher;

/**
 * 菜单路由器，负责不同菜单选项的路由，同时处理用户输入
 */
public class Router extends BaseView {

    private final MethodDispatcher dispatcher;
    private final StudentController studentController = new StudentController();
    private final SystemController systemController = new SystemController();

    public Router(String sessionId) {
        this.dispatcher = new MethodDispatcher(sessionId);
        this.setSessionId(sessionId);
    }

    public void start() {
        try {
            mainMenu();
        } finally {
            close(); // 确保在程序结束时关闭Scanner
        }
    }

    public void mainMenu() {
        while (true) {
            System.out.println("\n== 主菜单 ==");
            System.out.println("1. 系统设置");
            System.out.println("2. 学生管理");
            System.out.println("0. 退出");

            switch (scanner.nextLine()) {
                // 使用统一的scanner
                case "1":
                    systemMenu();
                    break;
                case "2":
                    studentMenu();
                    break;
                case "0":
                    return;
            }
        }
    }

    private void systemMenu() {
        System.out.println("\n== 系统设置 ==");
        System.out.println("1. 查看系统信息");
        System.out.println("2. 修改角色权限");

        switch (scanner.nextLine()) {
            // 使用统一的scanner
            case "1":
                dispatcher.dispatch(systemController, "showSystemInfo");
                break;
            case "2":
                dispatcher.dispatch(systemController, "editRoles");
                break;
        }
    }

    private void studentMenu() {
        System.out.println("\n== 学生管理 ==");
        System.out.println("1. 添加学生");
        System.out.println("2. 查看学生");

        switch (scanner.nextLine()) {
            // 使用统一的scanner
            case "1":
                dispatcher.dispatch(studentController, "addStudent");
                break;
            case "2":
                dispatcher.dispatch(studentController, "listStudents");
                break;
        }
    }
}
