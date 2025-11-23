package com.example.view;

import com.example.dispatcher.MenuItem;
import com.example.dispatcher.MenuRegistry;
import com.example.model.dto.BinaryMessage;
import java.util.List;

/**
 * 菜单渲染器 - 负责显示菜单和获取用户输入
 *
 * 这个类替代了原有的硬编码菜单显示逻辑，提供：
 * 1. 基于权限的动态菜单渲染
 * 2. 统一的用户输入处理
 * 3. 美观的菜单格式化显示
 */
public class MenuRenderer extends BaseView {

    private final MenuRegistry menuRegistry;

    /**
     * 构造函数
     * @param menuRegistry 菜单注册表实例
     */
    public MenuRenderer(MenuRegistry menuRegistry) {
        this.menuRegistry = menuRegistry;
    }

    /**
     * 显示菜单并获取用户选择
     * @param menu 菜单名称
     * @param sessionId 会话ID（用于权限过滤）
     * @return 用户选择的选项编号，如果无可用选项返回 -1
     */
    public int showMenuAndGetChoice(String menu, String sessionId) {
        List<MenuItem> items = menuRegistry.getAccessibleMenuItems(
            menu,
            sessionId
        );

        if (items.isEmpty()) {
            show("暂无可用选项");
            return -1;
        }

        // 显示菜单标题
        String menuTitle = getMenuTitle(menu);
        showMenuHeader(menuTitle);

        // 显示菜单选项
        for (MenuItem item : items) {
            showMenuItem(item);
        }

        // 显示分隔线和提示
        showMenuFooter();

        // 获取用户选择
        return getUserChoice(items);
    }

    /**
     * 仅显示菜单，不获取用户输入
     * @param menu 菜单名称
     * @param sessionId 会话ID
     */
    public void showMenu(String menu, String sessionId) {
        List<MenuItem> items = menuRegistry.getAccessibleMenuItems(
            menu,
            sessionId
        );

        if (items.isEmpty()) {
            show("暂无可用选项");
            return;
        }

        String menuTitle = getMenuTitle(menu);
        showMenuHeader(menuTitle);

        for (MenuItem item : items) {
            showMenuItem(item);
        }

        showMenuFooter();
    }

    /**
     * 显示菜单头部
     * @param title 菜单标题
     */
    private void showMenuHeader(String title) {
        String border = "=".repeat(Math.max(20, title.length() + 10));
        show(border);
        show(
            String.format(
                "%s %s %s",
                "=".repeat((border.length() - title.length() - 2) / 2),
                title,
                "=".repeat((border.length() - title.length() - 2) / 2)
            )
        );
        show(border);
    }

    /**
     * 显示单个菜单项
     * @param item 菜单项
     */
    private void showMenuItem(MenuItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d. %s", item.getOption(), item.getTitle()));

        // 添加图标（如果有）
        if (!item.getIcon().isEmpty()) {
            sb.append(" ").append(item.getIcon());
        }

        // 添加描述（如果有且不为空）
        if (!item.getDescription().isEmpty()) {
            sb.append(" - ").append(item.getDescription());
        }

        show(sb.toString());
    }

    /**
     * 显示菜单底部
     */
    private void showMenuFooter() {
        show("=".repeat(30));
    }

    /**
     * 获取用户选择
     * @param items 可用的菜单项列表
     * @return 用户选择的选项编号
     */
    private int getUserChoice(List<MenuItem> items) {
        show("请输入选项编号：");

        // 构建有效选项集合
        java.util.Set<Integer> validOptions = new java.util.HashSet<>();
        for (MenuItem item : items) {
            validOptions.add(item.getOption());
        }

        int choice;

        while (true) {
            try {
                choice = scanner.nextInt();
                if (validOptions.contains(choice)) {
                    break;
                } else {
                    show("无效的选项，请重新输入：");
                }
            } catch (Exception e) {
                show("输入格式错误，请输入数字：");
                scanner.nextLine(); // 清除错误输入
            }
        }

        return choice;
    }

    /**
     * 获取菜单标题
     * @param menu 菜单名称
     * @return 显示用的菜单标题
     */
    private String getMenuTitle(String menu) {
        return switch (menu.toLowerCase()) {
            case "main" -> "主菜单";
            case "student" -> "学生管理";
            case "course" -> "课程管理";
            case "score" -> "成绩管理";
            case "system" -> "系统设置";
            case "user" -> "用户管理";
            case "init" -> "初始界面";
            default -> menu + "菜单";
        };
    }

    /**
     * 显示错误消息
     * @param message 错误消息
     */
    public void showError(String message) {
        show("❌ 错误: " + message);
    }

    /**
     * 显示成功消息
     * @param message 成功消息
     */
    public void showSuccess(String message) {
        show("✅ " + message);
    }

    /**
     * 显示警告消息
     * @param message 警告消息
     */
    public void showWarning(String message) {
        show("⚠️  警告: " + message);
    }

    /**
     * 显示信息消息
     * @param message 信息消息
     */
    public void showInfo(String message) {
        show("ℹ️  " + message);
    }

    /**
     * 显示 BinaryMessage
     * @param message BinaryMessage 对象
     */
    public void showBinaryMessage(BinaryMessage message) {
        if (message.isBool_result()) {
            showSuccess(message.getMessage());
        } else {
            showError(message.getMessage());
        }
    }

    /**
     * 确认对话框
     * @param message 确认消息
     * @return 用户是否确认
     */
    public boolean confirm(String message) {
        show(message + " (y/n): ");
        String input = scanner.nextLine().toLowerCase().trim();
        return "y".equals(input) || "yes".equals(input) || "是".equals(input);
    }

    /**
     * 等待用户按回车继续
     */
    public void waitForEnter() {
        show("按回车键继续...");
        scanner.nextLine();
    }

    /**
     * 显示分隔线
     */
    public void showSeparator() {
        show("-".repeat(50));
    }

    /**
     * 获取菜单注册表（用于调试）
     * @return 菜单注册表实例
     */
    public MenuRegistry getMenuRegistry() {
        return menuRegistry;
    }

    /**
     * 显示菜单结构（调试用）
     */
    public void showMenuStructure() {
        show("=== 菜单结构调试信息 ===");
        show(menuRegistry.getMenuStructure());
        show("=========================");
    }
}
