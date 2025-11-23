package com.example.controller;

import com.example.auth.annotation.MenuAction;
import com.example.model.dto.BinaryMessage;

/**
 * 对系统功能中和成绩相关的请求进行分组
 */
public class ScoreController extends BaseController {

    /**
     * 查看成绩列表
     */
    @MenuAction(
        menu = "score",
        option = 1,
        title = "查看成绩列表",
        permission = "score.view",
        description = "查看所有学生成绩"
    )
    public void listScores(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "显示成绩列表功能"));
            // TODO: 实现查看成绩列表逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "查看成绩列表失败: " + e.getMessage())
            );
        }
    }

    /**
     * 录入成绩
     */
    @MenuAction(
        menu = "score",
        option = 2,
        title = "录入成绩",
        permission = "score.create",
        description = "录入学生成绩"
    )
    public void addScore(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "录入成绩功能"));
            // TODO: 实现录入成绩逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "录入成绩失败: " + e.getMessage())
            );
        }
    }

    /**
     * 修改成绩
     */
    @MenuAction(
        menu = "score",
        option = 3,
        title = "修改成绩",
        permission = "score.edit",
        description = "修改已录入的成绩"
    )
    public void editScore(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "修改成绩功能"));
            // TODO: 实现修改成绩逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "修改成绩失败: " + e.getMessage())
            );
        }
    }

    /**
     * 删除成绩
     */
    @MenuAction(
        menu = "score",
        option = 4,
        title = "删除成绩",
        permission = "score.delete",
        description = "删除成绩记录"
    )
    public void deleteScore(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "删除成绩功能"));
            // TODO: 实现删除成绩逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "删除成绩失败: " + e.getMessage())
            );
        }
    }

    /**
     * 成绩统计
     */
    @MenuAction(
        menu = "score",
        option = 5,
        title = "成绩统计",
        permission = "score.statistics",
        description = "查看成绩统计信息"
    )
    public void scoreStatistics(String sessionId) {
        try {
            router.show(new BinaryMessage(true, "成绩统计功能"));
            // TODO: 实现成绩统计逻辑
        } catch (Exception e) {
            router.show(
                new BinaryMessage(false, "成绩统计失败: " + e.getMessage())
            );
        }
    }

    /**
     * 返回主菜单
     */
    @MenuAction(
        menu = "score",
        option = 0,
        title = "返回主菜单",
        requireAuth = false,
        description = "返回到主菜单"
    )
    public void backToMain(String sessionId) {
        // 返回逻辑由调用方处理
    }
}
