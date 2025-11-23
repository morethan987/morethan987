package com.example.controller;

import com.example.auth.annotation.MenuAction;
import com.example.model.dto.BinaryMessage;

/**
 * 对系统功能中和课程相关的请求进行分组
 */
public class CourseController extends BaseController {

    /**
     * 查看课程列表
     */
    @MenuAction(
        menu = "course",
        option = 1,
        title = "查看课程列表",
        permission = "course.view",
        description = "查看所有课程信息"
    )
    public void listCourses(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "显示课程列表功能"));
            // TODO: 实现查看课程列表逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "查看课程列表失败: " + e.getMessage())
            );
        }
    }

    /**
     * 添加课程
     */
    @MenuAction(
        menu = "course",
        option = 2,
        title = "添加课程",
        permission = "course.create",
        description = "添加新的课程信息"
    )
    public void addCourse(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "添加课程功能"));
            // TODO: 实现添加课程逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "添加课程失败: " + e.getMessage())
            );
        }
    }

    /**
     * 修改课程信息
     */
    @MenuAction(
        menu = "course",
        option = 3,
        title = "修改课程信息",
        permission = "course.edit",
        description = "修改现有课程的信息"
    )
    public void editCourse(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "修改课程信息功能"));
            // TODO: 实现修改课程信息逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "修改课程信息失败: " + e.getMessage())
            );
        }
    }

    /**
     * 删除课程
     */
    @MenuAction(
        menu = "course",
        option = 4,
        title = "删除课程",
        permission = "course.delete",
        description = "删除课程信息"
    )
    public void deleteCourse(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "删除课程功能"));
            // TODO: 实现删除课程逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "删除课程失败: " + e.getMessage())
            );
        }
    }

    /**
     * 返回主菜单
     */
    @MenuAction(
        menu = "course",
        option = 0,
        title = "返回主菜单",
        requireAuth = false,
        description = "返回到主菜单"
    )
    public void backToMain(String sessionId) {
        // 返回逻辑由调用方处理
    }
}
