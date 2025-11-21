package com.example.view;

import com.example.controller.*;
import com.example.dispatcher.MethodDispatcher;
import java.util.Scanner;

/**
 * 菜单路由器，负责不同菜单选项的路由，同时处理用户输入
 */
public class MenuRouter {

    private final MethodDispatcher dispatcher;
    private final Printer printer = new Printer();
    private final StudentController studentController = new StudentController();
    private final SystemController systemController = new SystemController();

    public MenuRouter(String sessionId) {
        this.dispatcher = new MethodDispatcher(sessionId);
    }

    public void start() {
        mainMenu();
    }

    public void mainMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n== 主菜单 ==");
            System.out.println("1. 系统设置");
            System.out.println("2. 学生管理");
            System.out.println("0. 退出");

            switch (sc.nextLine()) {
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

        Scanner sc = new Scanner(System.in);
        switch (sc.nextLine()) {
            case "1":
                printer.show(
                    dispatcher.dispatch(systemController, "showSystemInfo")
                );
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

        Scanner sc = new Scanner(System.in);
        switch (sc.nextLine()) {
            case "1":
                dispatcher.dispatch(studentController, "addStudent");
                break;
            case "2":
                dispatcher.dispatch(studentController, "listStudents");
                break;
        }
    }
}
