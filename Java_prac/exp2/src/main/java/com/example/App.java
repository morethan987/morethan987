package com.example;

import com.example.controller.BaseController;
import com.example.controller.CourseController;
import com.example.controller.ScoreController;
import com.example.controller.StudentController;
import com.example.controller.SystemController;
import com.example.util.LoggerUtil;

/**
 * Students Score Management System
 * A simple Java application to manage and track student scores in terminal.
 * Using MVC architecture.
 *
 * This class displays a banner, trigger login and start MenuRouter.
 *
 * @author morethan987
 */
public class App {

    public static void main(String[] args) {
        // 初始化日志系统
        LoggerUtil.initializeLogger();
        LoggerUtil.logSystemEvent("系统启动", "学生成绩管理系统开始启动");

        printBanner();

        // 初始化所有控制器
        LoggerUtil.info("开始初始化控制器");
        SystemController systemController = new SystemController();
        StudentController studentController = new StudentController();
        CourseController courseController = new CourseController();
        ScoreController scoreController = new ScoreController();
        LoggerUtil.info("控制器初始化完成");

        // 注册所有控制器到菜单系统
        LoggerUtil.info("开始注册控制器到菜单系统");
        BaseController.registerControllers(
            systemController,
            studentController,
            courseController,
            scoreController
        );

        BaseController.initializeMenuSystem();
        LoggerUtil.info("菜单系统初始化完成");

        // 注册dispatcher和router
        LoggerUtil.info("开始初始化各控制器的dispatcher和router");
        systemController.initialize();
        studentController.initialize();
        courseController.initialize();
        scoreController.initialize();
        LoggerUtil.info("所有控制器初始化完成");

        // 启动系统
        LoggerUtil.logSystemEvent("系统启动完成", "进入用户交互界面");
        systemController.init();
    }

    private static void printBanner() {
        String banner =
            "\n  ,ad8888ba,                                    88                ,ad8888ba,                                                     88\n" +
            " d8\"'    `\"8b                                   88               d8\"'    `\"8b                                                    88\n" +
            "d8'                                             88              d8'                                                              88\n" +
            "88             8b,dPPYba,  ,adPPYYba,   ,adPPYb,88   ,adPPYba,  88              ,adPPYba,   8b,dPPYba,   ,adPPYba,   ,adPPYba,   88   ,adPPYba,\n" +
            "88      88888  88P'   \"Y8  \"\"     `Y8  a8\"    `Y88  a8P_____88  88             a8\"     \"8a  88P'   `\"8a  I8[    \"\"  a8\"     \"8a  88  a8P_____88\n" +
            "Y8,        88  88          ,adPPPPP88  8b       88  8PP\"\"\"\"\"\"\"  Y8,            8b       d8  88       88   `\"Y8ba,   8b       d8  88  8PP\"\"\"\"\"\"\"\n" +
            " Y8a.    .a88  88          88,    ,88  \"8a,   ,d88  \"8b,   ,aa   Y8a.    .a8P  \"8a,   ,a8\"  88       88  aa    ]8I  \"8a,   ,a8\"  88  \"8b,   ,aa\n" +
            "  `\"Y88888P\"   88          `\"8bbdP\"Y8   `\"8bbdP\"Y8   `\"Ybbd8\"'    `\"Y8888Y\"'    `\"YbbdP\"'   88       88  `\"YbbdP\"'   `\"YbbdP\"'   88   `\"Ybbd8\"'\n";
        System.out.println(banner);
    }
}
