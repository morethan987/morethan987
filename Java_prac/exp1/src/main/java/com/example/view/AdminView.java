package com.example.view;

import java.util.Map;

public class AdminView extends BaseView {

    private static Map<Integer, String[]> codeMap;

    public AdminView(Map<String, String> sysFuctionMap) {
        super();
        codeMap = super.initCodeMap(sysFuctionMap);
    }

    public void show_init() {
        System.out.println("=== 管理员界面 ===");
        // TODO 使用 BaseView 中的 showOperationMenu 方法显示功能列表
        System.out.println("[1] 管理用户");
        System.out.println("[2] 管理课程");
        System.out.println("[3] 管理教学班");
        System.out.println("[4] 查询某学生成绩");
        System.out.println("[5] 查询某教学班成绩");
        System.out.println("[6] 查看某教学班成绩分布");
        System.out.println("[7] 查询某课程成绩");
        System.out.println("[8] 查看某课程成绩分布");
        System.out.println("[9] 清空屏幕内容");
        System.out.println("[10] 退出登陆");
    }
}
