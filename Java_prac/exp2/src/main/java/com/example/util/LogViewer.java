package com.example.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 日志查看工具类
 * 提供日志文件读取、过滤和显示功能
 *
 * @author morethan987
 */
public class LogViewer {

    private static final String LOG_DIR = "logs";
    private static final String SYSTEM_LOG = "system.log";

    /**
     * 显示最新的日志条目
     * @param lines 要显示的行数
     */
    public static void showLatestLogs(int lines) {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (!Files.exists(logPath)) {
                System.out.println("日志文件不存在: " + logPath);
                return;
            }

            List<String> allLines = Files.readAllLines(logPath);
            if (allLines.isEmpty()) {
                System.out.println("日志文件为空");
                return;
            }

            int start = Math.max(0, allLines.size() - lines);
            List<String> latestLines = allLines.subList(start, allLines.size());

            System.out.println(
                "=== 最新 " + latestLines.size() + " 条日志 ==="
            );
            for (String line : latestLines) {
                System.out.println(line);
            }
            System.out.println("=== 日志结束 ===");
        } catch (IOException e) {
            System.err.println("读取日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据日志级别过滤显示日志
     * @param level 日志级别 (INFO, WARNING, SEVERE等)
     * @param lines 最大显示行数，-1表示显示所有
     */
    public static void showLogsByLevel(String level, int lines) {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (!Files.exists(logPath)) {
                System.out.println("日志文件不存在: " + logPath);
                return;
            }

            List<String> allLines = Files.readAllLines(logPath);
            List<String> filteredLines = allLines
                .stream()
                .filter(line -> line.contains("[" + level.toUpperCase() + "]"))
                .collect(Collectors.toList());

            if (filteredLines.isEmpty()) {
                System.out.println("没有找到 " + level + " 级别的日志");
                return;
            }

            List<String> displayLines = lines > 0 &&
                lines < filteredLines.size()
                ? filteredLines.subList(
                      Math.max(0, filteredLines.size() - lines),
                      filteredLines.size()
                  )
                : filteredLines;

            System.out.println(
                "=== " +
                    level.toUpperCase() +
                    " 级别日志 (" +
                    displayLines.size() +
                    " 条) ==="
            );
            for (String line : displayLines) {
                System.out.println(line);
            }
            System.out.println("=== 日志结束 ===");
        } catch (IOException e) {
            System.err.println("读取日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 搜索包含指定关键词的日志
     * @param keyword 搜索关键词
     * @param lines 最大显示行数，-1表示显示所有
     */
    public static void searchLogs(String keyword, int lines) {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (!Files.exists(logPath)) {
                System.out.println("日志文件不存在: " + logPath);
                return;
            }

            List<String> allLines = Files.readAllLines(logPath);
            List<String> matchedLines = allLines
                .stream()
                .filter(line ->
                    line.toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());

            if (matchedLines.isEmpty()) {
                System.out.println("没有找到包含 '" + keyword + "' 的日志");
                return;
            }

            List<String> displayLines = lines > 0 && lines < matchedLines.size()
                ? matchedLines.subList(
                      Math.max(0, matchedLines.size() - lines),
                      matchedLines.size()
                  )
                : matchedLines;

            System.out.println(
                "=== 搜索 '" +
                    keyword +
                    "' 的结果 (" +
                    displayLines.size() +
                    " 条) ==="
            );
            for (String line : displayLines) {
                System.out.println(line);
            }
            System.out.println("=== 搜索结束 ===");
        } catch (IOException e) {
            System.err.println("搜索日志失败: " + e.getMessage());
        }
    }

    /**
     * 显示今天的日志
     */
    public static void showTodayLogs() {
        String today = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );
        searchLogs(today, -1);
    }

    /**
     * 显示用户操作日志
     * @param sessionId 会话ID，null表示显示所有用户操作
     * @param lines 最大显示行数，-1表示显示所有
     */
    public static void showUserActionLogs(String sessionId, int lines) {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (!Files.exists(logPath)) {
                System.out.println("日志文件不存在: " + logPath);
                return;
            }

            List<String> allLines = Files.readAllLines(logPath);
            List<String> userActionLines = allLines
                .stream()
                .filter(line -> line.contains("用户操作"))
                .filter(line -> sessionId == null || line.contains(sessionId))
                .collect(Collectors.toList());

            if (userActionLines.isEmpty()) {
                String msg = sessionId != null
                    ? "没有找到用户 " + sessionId + " 的操作日志"
                    : "没有找到用户操作日志";
                System.out.println(msg);
                return;
            }

            List<String> displayLines = lines > 0 &&
                lines < userActionLines.size()
                ? userActionLines.subList(
                      Math.max(0, userActionLines.size() - lines),
                      userActionLines.size()
                  )
                : userActionLines;

            String title = sessionId != null
                ? "用户 " +
                  sessionId +
                  " 的操作日志 (" +
                  displayLines.size() +
                  " 条)"
                : "用户操作日志 (" + displayLines.size() + " 条)";
            System.out.println("=== " + title + " ===");
            for (String line : displayLines) {
                System.out.println(line);
            }
            System.out.println("=== 日志结束 ===");
        } catch (IOException e) {
            System.err.println("读取用户操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 显示系统事件日志
     * @param lines 最大显示行数，-1表示显示所有
     */
    public static void showSystemEventLogs(int lines) {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (!Files.exists(logPath)) {
                System.out.println("日志文件不存在: " + logPath);
                return;
            }

            List<String> allLines = Files.readAllLines(logPath);
            List<String> systemEventLines = allLines
                .stream()
                .filter(line -> line.contains("系统事件"))
                .collect(Collectors.toList());

            if (systemEventLines.isEmpty()) {
                System.out.println("没有找到系统事件日志");
                return;
            }

            List<String> displayLines = lines > 0 &&
                lines < systemEventLines.size()
                ? systemEventLines.subList(
                      Math.max(0, systemEventLines.size() - lines),
                      systemEventLines.size()
                  )
                : systemEventLines;

            System.out.println(
                "=== 系统事件日志 (" + displayLines.size() + " 条) ==="
            );
            for (String line : displayLines) {
                System.out.println(line);
            }
            System.out.println("=== 日志结束 ===");
        } catch (IOException e) {
            System.err.println("读取系统事件日志失败: " + e.getMessage());
        }
    }

    /**
     * 显示认证相关日志
     * @param lines 最大显示行数，-1表示显示所有
     */
    public static void showAuthLogs(int lines) {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (!Files.exists(logPath)) {
                System.out.println("日志文件不存在: " + logPath);
                return;
            }

            List<String> allLines = Files.readAllLines(logPath);
            List<String> authLines = allLines
                .stream()
                .filter(
                    line ->
                        line.contains("认证事件") ||
                        line.contains("登录") ||
                        line.contains("注册")
                )
                .collect(Collectors.toList());

            if (authLines.isEmpty()) {
                System.out.println("没有找到认证相关日志");
                return;
            }

            List<String> displayLines = lines > 0 && lines < authLines.size()
                ? authLines.subList(
                      Math.max(0, authLines.size() - lines),
                      authLines.size()
                  )
                : authLines;

            System.out.println(
                "=== 认证相关日志 (" + displayLines.size() + " 条) ==="
            );
            for (String line : displayLines) {
                System.out.println(line);
            }
            System.out.println("=== 日志结束 ===");
        } catch (IOException e) {
            System.err.println("读取认证日志失败: " + e.getMessage());
        }
    }

    /**
     * 显示数据库操作日志
     * @param lines 最大显示行数，-1表示显示所有
     */
    public static void showDatabaseLogs(int lines) {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (!Files.exists(logPath)) {
                System.out.println("日志文件不存在: " + logPath);
                return;
            }

            List<String> allLines = Files.readAllLines(logPath);
            List<String> dbLines = allLines
                .stream()
                .filter(line -> line.contains("数据库操作"))
                .collect(Collectors.toList());

            if (dbLines.isEmpty()) {
                System.out.println("没有找到数据库操作日志");
                return;
            }

            List<String> displayLines = lines > 0 && lines < dbLines.size()
                ? dbLines.subList(
                      Math.max(0, dbLines.size() - lines),
                      dbLines.size()
                  )
                : dbLines;

            System.out.println(
                "=== 数据库操作日志 (" + displayLines.size() + " 条) ==="
            );
            for (String line : displayLines) {
                System.out.println(line);
            }
            System.out.println("=== 日志结束 ===");
        } catch (IOException e) {
            System.err.println("读取数据库日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取日志文件信息
     */
    public static void showLogFileInfo() {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (!Files.exists(logPath)) {
                System.out.println("日志文件不存在: " + logPath);
                return;
            }

            long fileSize = Files.size(logPath);
            long lineCount = Files.lines(logPath).count();
            String lastModified = Files.getLastModifiedTime(logPath).toString();

            System.out.println("=== 日志文件信息 ===");
            System.out.println("文件路径: " + logPath.toAbsolutePath());
            System.out.println("文件大小: " + formatFileSize(fileSize));
            System.out.println("行数: " + lineCount);
            System.out.println("最后修改时间: " + lastModified);
            System.out.println("==================");
        } catch (IOException e) {
            System.err.println("获取日志文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 清空日志文件
     */
    public static void clearLogs() {
        try {
            Path logPath = Paths.get(LOG_DIR, SYSTEM_LOG);
            if (Files.exists(logPath)) {
                Files.write(
                    logPath,
                    new byte[0],
                    StandardOpenOption.TRUNCATE_EXISTING
                );
                System.out.println("日志文件已清空");
            } else {
                System.out.println("日志文件不存在，无需清空");
            }
        } catch (IOException e) {
            System.err.println("清空日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 格式化文件大小
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * 主方法，用于命令行测试
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("日志查看工具使用说明:");
            System.out.println(
                "java LogViewer latest [行数]     - 显示最新的日志"
            );
            System.out.println(
                "java LogViewer level [级别] [行数] - 显示指定级别的日志"
            );
            System.out.println(
                "java LogViewer search [关键词] [行数] - 搜索日志"
            );
            System.out.println(
                "java LogViewer today             - 显示今天的日志"
            );
            System.out.println(
                "java LogViewer user [sessionId] [行数] - 显示用户操作日志"
            );
            System.out.println(
                "java LogViewer system [行数]     - 显示系统事件日志"
            );
            System.out.println(
                "java LogViewer auth [行数]       - 显示认证日志"
            );
            System.out.println(
                "java LogViewer db [行数]         - 显示数据库日志"
            );
            System.out.println(
                "java LogViewer info              - 显示日志文件信息"
            );
            System.out.println(
                "java LogViewer clear             - 清空日志文件"
            );
            return;
        }

        String command = args[0];
        switch (command.toLowerCase()) {
            case "latest":
                int latestLines = args.length > 1
                    ? Integer.parseInt(args[1])
                    : 50;
                showLatestLogs(latestLines);
                break;
            case "level":
                String level = args.length > 1 ? args[1] : "INFO";
                int levelLines = args.length > 2
                    ? Integer.parseInt(args[2])
                    : -1;
                showLogsByLevel(level, levelLines);
                break;
            case "search":
                String keyword = args.length > 1 ? args[1] : "";
                int searchLines = args.length > 2
                    ? Integer.parseInt(args[2])
                    : -1;
                if (keyword.isEmpty()) {
                    System.out.println("请提供搜索关键词");
                } else {
                    searchLogs(keyword, searchLines);
                }
                break;
            case "today":
                showTodayLogs();
                break;
            case "user":
                String sessionId = args.length > 1 ? args[1] : null;
                int userLines = args.length > 2
                    ? Integer.parseInt(args[2])
                    : -1;
                showUserActionLogs(sessionId, userLines);
                break;
            case "system":
                int systemLines = args.length > 1
                    ? Integer.parseInt(args[1])
                    : -1;
                showSystemEventLogs(systemLines);
                break;
            case "auth":
                int authLines = args.length > 1
                    ? Integer.parseInt(args[1])
                    : -1;
                showAuthLogs(authLines);
                break;
            case "db":
                int dbLines = args.length > 1 ? Integer.parseInt(args[1]) : -1;
                showDatabaseLogs(dbLines);
                break;
            case "info":
                showLogFileInfo();
                break;
            case "clear":
                clearLogs();
                break;
            default:
                System.out.println("未知命令: " + command);
                break;
        }
    }
}
