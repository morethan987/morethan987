package com.example.controller;

import com.example.auth.annotation.MenuAction;
import com.example.model.dto.BinaryMessage;

/**
 * 对系统功能中和学生相关的请求进行分组
 */
public class StudentController extends BaseController {

    /**
     * 查看学生列表
     */
    @MenuAction(
        menu = "student",
        option = 1,
        title = "查看学生列表",
        roles = { "teacher", "admin" },
        permission = "student.view",
        description = "查看所有学生信息"
    )
    public void listStudents(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "显示学生列表功能"));
            // TODO: 实现查看学生列表逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "查看学生列表失败: " + e.getMessage())
            );
        }
    }

    /**
     * 添加学生
     */
    @MenuAction(
        menu = "student",
        option = 2,
        title = "添加学生",
        roles = { "teacher", "admin" },
        permission = "student.create",
        description = "添加新的学生信息"
    )
    public void addStudent(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "添加学生功能"));
            // TODO: 实现添加学生逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "添加学生失败: " + e.getMessage())
            );
        }
    }

    /**
     * 修改学生信息
     */
    @MenuAction(
        menu = "student",
        option = 3,
        title = "修改学生信息",
        permission = "student.edit",
        description = "修改现有学生的信息"
    )
    public void editStudent(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "修改学生信息功能"));
            // TODO: 实现修改学生信息逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "修改学生信息失败: " + e.getMessage())
            );
        }
    }

    /**
     * 删除学生
     */
    @MenuAction(
        menu = "student",
        option = 4,
        title = "删除学生",
        permission = "student.delete",
        description = "删除学生信息"
    )
    public void deleteStudent(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "删除学生功能"));
            // TODO: 实现删除学生逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "删除学生失败: " + e.getMessage())
            );
        }
    }

    /**
     * 返回主菜单
     */
    @MenuAction(
        menu = "student",
        option = 0,
        title = "返回主菜单",
        requireAuth = false,
        description = "返回到主菜单"
    )
    public void backToMain(String sessionId) {
        // 返回逻辑由调用方处理
    }
}
