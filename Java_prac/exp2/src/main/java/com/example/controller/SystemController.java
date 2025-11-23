package com.example.controller;

import com.example.auth.AuthService;
import com.example.auth.annotation.MenuAction;
import com.example.model.dto.BinaryMessage;
import com.example.model.dto.LoginMessage;
import com.example.model.dto.RegistMessage;
import com.example.util.LoggerUtil;

/**
 * 对系统功能中和系统本身相关的请求进行分组
 */
public class SystemController extends BaseController {

    /**
     * 程序启动函数
     * 重构：简化了登录流程，使用状态标记代替嵌套循环
     */
    public void init() {
        LoggerUtil.logSystemEvent("用户交互开始", "显示登录/注册界面");
        boolean isLoggedIn = false;

        while (!isLoggedIn) {
            Integer initChoice = router.showMenu("init", null);

            if (initChoice == 0) {
                // 退出程序
                LoggerUtil.logSystemEvent("用户退出", "用户选择退出系统");
                router.show(new BinaryMessage(true, "正在退出..."));
                System.exit(0);
            }

            if (initChoice == -1) {
                continue; // 无可用选项，重新显示菜单
            }

            try {
                // 尝试执行登录或注册操作
                dispatcher.dispatch(null, "init", initChoice);

                // 检查是否已登录（sessionId不为空表示登录成功）
                if (router.getSessionId() != null) {
                    isLoggedIn = true;
                    LoggerUtil.logSystemEvent(
                        "用户登录成功",
                        "用户成功进入主系统"
                    );
                    router.show(new BinaryMessage(true, "欢迎进入系统！"));
                }
            } catch (Exception e) {
                LoggerUtil.error("初始化操作失败", e);
                router.show(
                    new BinaryMessage(false, "操作失败: " + e.getMessage())
                );
            }
        }

        // 登录成功后，进入主程序循环
        run();
    }

    public void run() {
        LoggerUtil.logSystemEvent("进入主菜单", "用户进入主程序循环");
        // 程序主循环
        while (true) {
            try {
                Integer choice = router.showMenu("main", router.getSessionId());

                if (choice == -1) break; // 无可用选项
                if (choice == 0) break; // 退出程序

                dispatcher.dispatch(router.getSessionId(), "main", choice);
            } catch (Exception e) {
                LoggerUtil.error("主菜单操作失败: " + e.getMessage(), e);
                router.show(
                    new BinaryMessage(false, "操作失败: " + e.getMessage())
                );
            }
        }

        // 退出主循环后，清理会话并退出系统
        LoggerUtil.logSystemEvent("退出主菜单", "用户退出主程序循环");
        cleanupAndExit();
    }

    /**
     * 处理用户登录逻辑
     * 重构：移除了内部无限循环，改为单次尝试，失败则返回init菜单
     */
    @MenuAction(
        menu = "init",
        option = 1,
        title = "用户登录",
        requireAuth = false,
        description = "使用用户名和密码登录系统"
    )
    public void login(String unused) {
        LoggerUtil.info("用户尝试登录");
        LoginMessage loginMessage = router.login();

        // 检查用户是否取消了登录
        if (loginMessage == null) {
            LoggerUtil.logUserAction(null, "取消登录");
            router.show(new BinaryMessage(false, "已取消登录"));
            return;
        }

        // 尝试登录
        LoggerUtil.logAuthEvent(loginMessage.getUsername(), "登录尝试", false);
        BinaryMessage loginRes = AuthService.authLogin(loginMessage);

        if (!loginRes.isBool_result()) {
            // 登录失败，显示错误信息
            LoggerUtil.logAuthEvent(loginMessage.getUsername(), "登录", false);
            LoggerUtil.warning(
                "用户登录失败: %s - %s",
                loginMessage.getUsername(),
                loginRes.getMessage()
            );
            router.show(loginRes);
        } else {
            // 登录成功
            String sessionId = loginRes.getMessage();
            router.setSessionId(sessionId);

            LoggerUtil.logAuthEvent(loginMessage.getUsername(), "登录", true);
            LoggerUtil.info(
                "用户登录成功: %s, SessionID: %s",
                loginMessage.getUsername(),
                sessionId
            );
            router.show(new BinaryMessage(true, "登录成功！"));
        }
    }

    /**
     * 处理用户注册逻辑
     * 重构：注册成功后不自动登录，而是提示用户返回登录
     */
    @MenuAction(
        menu = "init",
        option = 2,
        title = "用户注册",
        requireAuth = false,
        description = "创建新用户账号"
    )
    public void regist(String unused) {
        LoggerUtil.info("用户尝试注册");
        RegistMessage registMessage = router.regist();

        // 检查用户是否取消了注册
        if (registMessage == null) {
            LoggerUtil.logUserAction(null, "取消注册");
            router.show(new BinaryMessage(false, "已取消注册"));
            return;
        }

        // 尝试注册
        LoggerUtil.logAuthEvent(registMessage.getUsername(), "注册尝试", false);
        BinaryMessage registRes = AuthService.registerUser(registMessage);
        router.show(registRes);

        if (registRes.isBool_result()) {
            // 注册成功，提示用户可以登录
            LoggerUtil.logAuthEvent(registMessage.getUsername(), "注册", true);
            LoggerUtil.info("用户注册成功: %s", registMessage.getUsername());
            router.show(
                new BinaryMessage(
                    true,
                    "注册成功！请返回登录菜单使用新账号登录。"
                )
            );
        } else {
            LoggerUtil.logAuthEvent(registMessage.getUsername(), "注册", false);
            LoggerUtil.warning(
                "用户注册失败: %s - %s",
                registMessage.getUsername(),
                registRes.getMessage()
            );
        }
        // 注册失败则返回init菜单，用户可以选择重新注册或登录
    }

    @MenuAction(
        menu = "init",
        option = 0,
        title = "退出系统",
        requireAuth = false,
        description = "退出系统"
    )
    public void backToPreviousFromInit(String unused) {
        // 此方法为占位方法，实际退出逻辑在init()中处理
    }

    /**
     * 处理用户登出逻辑
     * 重构：清理会话并返回登录界面，而不是直接退出程序
     */
    @MenuAction(
        menu = "main",
        option = 9,
        title = "退出登录",
        roles = { "admin", "teacher", "student" },
        description = "安全退出当前会话"
    )
    public void logout(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "用户登出");
            // 清理会话
            if (sessionId != null) {
                // 如果AuthService有logout方法，调用它
                // AuthService.logout(sessionId);
                router.setSessionId(null);
            }

            LoggerUtil.info("用户安全退出登录，SessionID: %s", sessionId);
            router.show(
                new BinaryMessage(true, "已安全退出登录，返回登录界面...")
            );

            // 重新初始化并返回登录界面
            init();
        } catch (Exception e) {
            LoggerUtil.error("用户退出失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(false, "退出失败: " + e.getMessage())
            );
        }
    }

    /**
     * 清理会话并退出系统
     */
    private void cleanupAndExit() {
        try {
            String sessionId = router.getSessionId();
            LoggerUtil.logSystemEvent("系统退出", "开始清理会话并退出系统");
            if (sessionId != null) {
                // 如果AuthService有logout方法，调用它
                // AuthService.logout(sessionId);
                LoggerUtil.info("清理用户会话，SessionID: %s", sessionId);
                router.setSessionId(null);
            }
            LoggerUtil.logSystemEvent("系统正常退出", "所有清理工作完成");
            router.show(new BinaryMessage(true, "正在退出系统..."));
            System.exit(0);
        } catch (Exception e) {
            LoggerUtil.error("系统退出时发生错误: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(false, "退出时发生错误: " + e.getMessage())
            );
            System.exit(1);
        }
    }

    /**
     * 学生管理菜单
     */
    @MenuAction(
        menu = "main",
        option = 1,
        title = "学生管理",
        roles = { "admin", "teacher" },
        description = "管理学生信息"
    )
    public void studentManagement(String sessionId) {
        LoggerUtil.logUserAction(sessionId, "进入学生管理菜单");
        while (true) {
            try {
                int choice = router.showMenu("student", sessionId);
                if (choice == 0) {
                    LoggerUtil.logUserAction(sessionId, "退出学生管理菜单");
                    break; // 返回上级菜单
                }

                dispatcher.dispatch(sessionId, "student", choice);
            } catch (Exception e) {
                LoggerUtil.error("学生管理操作失败: " + e.getMessage(), e);
                router.show(
                    new BinaryMessage(false, "操作失败: " + e.getMessage())
                );
            }
        }
    }

    /**
     * 课程管理菜单
     */
    @MenuAction(
        menu = "main",
        option = 2,
        title = "课程管理",
        roles = { "admin", "teacher" },
        description = "管理课程信息"
    )
    public void courseManagement(String sessionId) {
        LoggerUtil.logUserAction(sessionId, "进入课程管理菜单");
        while (true) {
            try {
                int choice = router.showMenu("course", sessionId);
                if (choice == 0) {
                    LoggerUtil.logUserAction(sessionId, "退出课程管理菜单");
                    break; // 返回上级菜单
                }

                dispatcher.dispatch(sessionId, "course", choice);
            } catch (Exception e) {
                LoggerUtil.error("课程管理操作失败: " + e.getMessage(), e);
                router.show(
                    new BinaryMessage(false, "操作失败: " + e.getMessage())
                );
            }
        }
    }

    /**
     * 成绩管理菜单
     */
    @MenuAction(
        menu = "main",
        option = 3,
        title = "成绩管理",
        roles = { "admin", "teacher" },
        description = "管理学生成绩"
    )
    public void scoreManagement(String sessionId) {
        LoggerUtil.logUserAction(sessionId, "进入成绩管理菜单");
        while (true) {
            try {
                int choice = router.showMenu("score", sessionId);
                if (choice == 0) {
                    LoggerUtil.logUserAction(sessionId, "退出成绩管理菜单");
                    break; // 返回上级菜单
                }

                dispatcher.dispatch(sessionId, "score", choice);
            } catch (Exception e) {
                LoggerUtil.error("成绩管理操作失败: " + e.getMessage(), e);
                router.show(
                    new BinaryMessage(false, "操作失败: " + e.getMessage())
                );
            }
        }
    }

    /**
     * 系统设置菜单
     */
    @MenuAction(
        menu = "main",
        option = 8,
        title = "系统设置",
        roles = { "admin" },
        description = "系统配置和管理"
    )
    public void systemSettings(String sessionId) {
        LoggerUtil.logUserAction(sessionId, "进入系统设置菜单");
        while (true) {
            try {
                int choice = router.showMenu("system", sessionId);
                if (choice == 0) {
                    LoggerUtil.logUserAction(sessionId, "退出系统设置菜单");
                    break; // 返回上级菜单
                }

                dispatcher.dispatch(sessionId, "system", choice);
            } catch (Exception e) {
                LoggerUtil.error("系统设置操作失败: " + e.getMessage(), e);
                router.show(
                    new BinaryMessage(false, "操作失败: " + e.getMessage())
                );
            }
        }
    }

    /**
     * 查看系统信息
     */
    @MenuAction(
        menu = "system",
        option = 1,
        title = "查看系统信息",
        roles = { "admin" },
        description = "查看系统运行状态和信息"
    )
    public void systemInfo(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "查看系统信息");
            StringBuilder info = new StringBuilder();
            info.append("系统信息:\n");
            info
                .append("Java版本: ")
                .append(System.getProperty("java.version"))
                .append("\n");
            info
                .append("操作系统: ")
                .append(System.getProperty("os.name"))
                .append("\n");
            info
                .append("用户目录: ")
                .append(System.getProperty("user.dir"))
                .append("\n");
            info
                .append("内存使用: ")
                .append(Runtime.getRuntime().totalMemory() / 1024 / 1024)
                .append("MB\n");

            router.show(new BinaryMessage(true, info.toString()));
        } catch (Exception e) {
            LoggerUtil.error("获取系统信息失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(false, "获取系统信息失败: " + e.getMessage())
            );
        }
    }

    /**
     * 权限管理
     */
    @MenuAction(
        menu = "system",
        option = 2,
        title = "权限管理",
        roles = { "admin" },
        description = "管理用户权限和角色"
    )
    public void permissionManagement(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "权限管理功能"));
            // TODO: 实现权限管理逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "权限管理失败: " + e.getMessage())
            );
        }
    }

    /**
     * 数据库管理
     */
    @MenuAction(
        menu = "system",
        option = 3,
        title = "数据库管理",
        roles = { "admin" },
        description = "数据库备份、恢复等操作"
    )
    public void databaseManagement(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "访问数据库管理");
            router.show(new BinaryMessage(true, "数据库管理功能"));
            // TODO: 实现数据库管理逻辑
        } catch (Exception e) {
            LoggerUtil.error("数据库管理失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(false, "数据库管理失败: " + e.getMessage())
            );
        }
    }

    /**
     * 日志管理
     */
    @MenuAction(
        menu = "system",
        option = 4,
        title = "日志管理",
        roles = { "admin" },
        description = "查看系统日志和操作记录"
    )
    public void logManagement(String sessionId) {
        LoggerUtil.logUserAction(sessionId, "进入日志管理菜单");
        while (true) {
            try {
                int choice = router.showMenu("log", sessionId);
                if (choice == 0) {
                    LoggerUtil.logUserAction(sessionId, "退出日志管理菜单");
                    break; // 返回上级菜单
                }

                dispatcher.dispatch(sessionId, "log", choice);
            } catch (Exception e) {
                LoggerUtil.error("日志管理操作失败: " + e.getMessage(), e);
                router.show(
                    new BinaryMessage(false, "操作失败: " + e.getMessage())
                );
            }
        }
    }

    /**
     * 查看最新日志
     */
    @MenuAction(
        menu = "log",
        option = 1,
        title = "查看最新日志",
        roles = { "admin" },
        description = "显示最新50条日志记录"
    )
    public void viewLatestLogs(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "查看最新日志");
            com.example.util.LogViewer.showLatestLogs(50);
        } catch (Exception e) {
            LoggerUtil.error("查看最新日志失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(false, "查看日志失败: " + e.getMessage())
            );
        }
    }

    /**
     * 查看用户操作日志
     */
    @MenuAction(
        menu = "log",
        option = 2,
        title = "查看用户操作日志",
        roles = { "admin" },
        description = "显示用户操作记录"
    )
    public void viewUserActionLogs(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "查看用户操作日志");
            com.example.util.LogViewer.showUserActionLogs(null, 100);
        } catch (Exception e) {
            LoggerUtil.error("查看用户操作日志失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(
                    false,
                    "查看用户操作日志失败: " + e.getMessage()
                )
            );
        }
    }

    /**
     * 查看系统事件日志
     */
    @MenuAction(
        menu = "log",
        option = 3,
        title = "查看系统事件日志",
        roles = { "admin" },
        description = "显示系统事件记录"
    )
    public void viewSystemEventLogs(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "查看系统事件日志");
            com.example.util.LogViewer.showSystemEventLogs(50);
        } catch (Exception e) {
            LoggerUtil.error("查看系统事件日志失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(
                    false,
                    "查看系统事件日志失败: " + e.getMessage()
                )
            );
        }
    }

    /**
     * 查看认证日志
     */
    @MenuAction(
        menu = "log",
        option = 4,
        title = "查看认证日志",
        roles = { "admin" },
        description = "显示登录注册等认证相关日志"
    )
    public void viewAuthLogs(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "查看认证日志");
            com.example.util.LogViewer.showAuthLogs(50);
        } catch (Exception e) {
            LoggerUtil.error("查看认证日志失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(false, "查看认证日志失败: " + e.getMessage())
            );
        }
    }

    /**
     * 查看错误日志
     */
    @MenuAction(
        menu = "log",
        option = 5,
        title = "查看错误日志",
        roles = { "admin" },
        description = "显示系统错误和警告日志"
    )
    public void viewErrorLogs(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "查看错误日志");
            com.example.util.LogViewer.showLogsByLevel("SEVERE", 50);
        } catch (Exception e) {
            LoggerUtil.error("查看错误日志失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(false, "查看错误日志失败: " + e.getMessage())
            );
        }
    }

    /**
     * 显示日志文件信息
     */
    @MenuAction(
        menu = "log",
        option = 6,
        title = "日志文件信息",
        roles = { "admin" },
        description = "显示日志文件大小、行数等信息"
    )
    public void showLogFileInfo(String sessionId) {
        try {
            LoggerUtil.logUserAction(sessionId, "查看日志文件信息");
            com.example.util.LogViewer.showLogFileInfo();
        } catch (Exception e) {
            LoggerUtil.error("查看日志文件信息失败: " + e.getMessage(), e);
            router.show(
                new BinaryMessage(
                    false,
                    "查看日志文件信息失败: " + e.getMessage()
                )
            );
        }
    }

    /**
     * 返回系统设置菜单
     */
    @MenuAction(
        menu = "log",
        option = 0,
        title = "返回系统设置",
        requireAuth = false,
        description = "返回到系统设置菜单"
    )
    public void backToSystemFromLog(String sessionId) {
        // 此方法为占位方法，返回逻辑在logManagement方法的while循环中处理
    }

    /**
     * 菜单结构调试
     */
    @MenuAction(
        menu = "system",
        option = 9,
        title = "菜单结构调试",
        roles = { "admin" },
        description = "显示当前菜单结构（调试用）"
    )
    public void debugMenuStructure(String sessionId) {
        try {
            if (menuRenderer != null) {
                menuRenderer.showMenuStructure();
            } else {
                router.show(new BinaryMessage(false, "菜单渲染器未初始化"));
            }
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "显示菜单结构失败: " + e.getMessage())
            );
        }
    }

    /**
     * 返回主菜单
     */
    @MenuAction(
        menu = "system",
        option = 0,
        title = "返回主菜单",
        requireAuth = false,
        description = "返回到主菜单"
    )
    public void backToMainFromSystem(String unused) {
        // 此方法为占位方法，返回逻辑在各个子菜单的while循环中处理
    }
}
