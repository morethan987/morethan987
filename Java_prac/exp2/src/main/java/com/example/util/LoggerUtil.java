package com.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.*;

/**
 * 日志工具类
 * 封装Java自带的logging功能，提供统一的日志记录接口
 *
 * @author morethan987
 */
public class LoggerUtil {

    private static final Logger logger = Logger.getLogger("StudentScoreSystem");
    private static boolean initialized = false;

    /**
     * 初始化日志配置
     */
    public static void initializeLogger() {
        if (initialized) {
            return;
        }

        try {
            // 尝试加载配置文件
            loadLoggingConfiguration();

            // 移除默认的控制台处理器
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // 设置日志级别
            logger.setLevel(Level.ALL);

            // 创建自定义格式器
            Formatter formatter = new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format(
                        "[%1$tF %1$tT] [%2$-7s] [%3$s] %4$s %n",
                        new java.util.Date(record.getMillis()),
                        record.getLevel().getLocalizedName(),
                        getSimpleLoggerName(record.getLoggerName()),
                        record.getMessage()
                    );
                }
            };

            // 确保logs目录存在
            createLogsDirectory();

            // 添加控制台处理器
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(formatter);
            logger.addHandler(consoleHandler);

            // 添加文件处理器
            try {
                FileHandler fileHandler = new FileHandler(
                    "logs/system.log",
                    true
                );
                fileHandler.setLevel(Level.ALL);
                fileHandler.setFormatter(formatter);
                logger.addHandler(fileHandler);
            } catch (IOException e) {
                // 如果无法创建文件日志，只使用控制台日志
                System.err.println(
                    "无法创建日志文件，仅使用控制台日志: " + e.getMessage()
                );
            }

            initialized = true;
            info("日志系统初始化成功");
        } catch (Exception e) {
            System.err.println("日志系统初始化失败: " + e.getMessage());
        }
    }

    /**
     * 记录INFO级别日志
     */
    public static void info(String message) {
        ensureInitialized();
        logger.info(message);
    }

    /**
     * 记录INFO级别日志（带参数）
     */
    public static void info(String message, Object... params) {
        ensureInitialized();
        logger.info(String.format(message, params));
    }

    /**
     * 记录WARNING级别日志
     */
    public static void warning(String message) {
        ensureInitialized();
        logger.warning(message);
    }

    /**
     * 记录WARNING级别日志（带参数）
     */
    public static void warning(String message, Object... params) {
        ensureInitialized();
        logger.warning(String.format(message, params));
    }

    /**
     * 记录ERROR级别日志
     */
    public static void error(String message) {
        ensureInitialized();
        logger.severe(message);
    }

    /**
     * 记录ERROR级别日志（带参数）
     */
    public static void error(String message, Object... params) {
        ensureInitialized();
        logger.severe(String.format(message, params));
    }

    /**
     * 记录ERROR级别日志（带异常）
     */
    public static void error(String message, Throwable throwable) {
        ensureInitialized();
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * 记录DEBUG级别日志
     */
    public static void debug(String message) {
        ensureInitialized();
        logger.fine(message);
    }

    /**
     * 记录DEBUG级别日志（带参数）
     */
    public static void debug(String message, Object... params) {
        ensureInitialized();
        logger.fine(String.format(message, params));
    }

    /**
     * 记录用户操作日志
     */
    public static void logUserAction(
        String sessionId,
        String action,
        String details
    ) {
        String userInfo = sessionId != null
            ? "用户[" + sessionId + "]"
            : "未登录用户";
        info("用户操作 - %s 执行操作: %s, 详情: %s", userInfo, action, details);
    }

    /**
     * 记录用户操作日志（无详情）
     */
    public static void logUserAction(String sessionId, String action) {
        logUserAction(sessionId, action, "无");
    }

    /**
     * 记录系统事件
     */
    public static void logSystemEvent(String event, String details) {
        info("系统事件 - %s: %s", event, details);
    }

    /**
     * 记录系统事件（无详情）
     */
    public static void logSystemEvent(String event) {
        logSystemEvent(event, "无");
    }

    /**
     * 记录数据库操作
     */
    public static void logDatabaseOperation(
        String operation,
        String table,
        String details
    ) {
        debug("数据库操作 - %s 表[%s]: %s", operation, table, details);
    }

    /**
     * 记录认证事件
     */
    public static void logAuthEvent(
        String username,
        String event,
        boolean success
    ) {
        String status = success ? "成功" : "失败";
        info("认证事件 - 用户[%s] %s: %s", username, event, status);
    }

    /**
     * 确保日志系统已初始化
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initializeLogger();
        }
    }

    /**
     * 设置日志级别
     */
    public static void setLogLevel(Level level) {
        ensureInitialized();
        logger.setLevel(level);

        // 同时设置所有处理器的级别
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                // 控制台处理器保持INFO级别，避免过多输出
                handler.setLevel(
                    level.intValue() > Level.INFO.intValue()
                        ? level
                        : Level.INFO
                );
            } else {
                handler.setLevel(level);
            }
        }
    }

    /**
     * 获取当前日志级别
     */
    public static Level getLogLevel() {
        ensureInitialized();
        return logger.getLevel();
    }

    /**
     * 加载日志配置文件
     */
    private static void loadLoggingConfiguration() {
        try {
            // 尝试从classpath加载配置文件
            InputStream configStream =
                LoggerUtil.class.getClassLoader().getResourceAsStream(
                    "logging.properties"
                );

            if (configStream != null) {
                LogManager.getLogManager().readConfiguration(configStream);
                configStream.close();
                return;
            }

            // 尝试从文件系统加载配置文件
            if (
                Files.exists(Paths.get("src/main/resources/logging.properties"))
            ) {
                try (
                    InputStream fileStream = Files.newInputStream(
                        Paths.get("src/main/resources/logging.properties")
                    )
                ) {
                    LogManager.getLogManager().readConfiguration(fileStream);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println(
                "加载日志配置文件失败，使用默认配置: " + e.getMessage()
            );
        }
    }

    /**
     * 创建日志目录
     */
    private static void createLogsDirectory() {
        try {
            if (!Files.exists(Paths.get("logs"))) {
                Files.createDirectories(Paths.get("logs"));
            }
        } catch (IOException e) {
            System.err.println("创建日志目录失败: " + e.getMessage());
        }
    }

    /**
     * 简化日志记录器名称，只显示类名
     */
    private static String getSimpleLoggerName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "Unknown";
        }

        // 如果是包名，只返回最后一部分
        if (fullName.contains(".")) {
            String[] parts = fullName.split("\\.");
            return parts[parts.length - 1];
        }

        return fullName;
    }
}
