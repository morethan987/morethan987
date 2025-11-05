package com.example.view;

import java.util.Map;

public class StudentView extends BaseView {

    private static Map<Integer, String[]> codeMap;

    public StudentView(Map<String, String> sysFuctionMap) {
        super();
        codeMap = super.initCodeMap(sysFuctionMap);
    }

    public void show_init() {
        System.out.println("欢迎进入学生界面");
        // TODO 使用 BaseView 中的 showOperationMenu 方法显示功能列表
        System.out.println("[1] 查看个人信息");
        System.out.println("[2] 修改个人信息");
        System.out.println("[3] 查看课程信息");
        System.out.println("[4] 查询成绩");
        System.out.println("[5] 清空屏幕内容");
        System.out.println("[6] 退出");
    }

    public void showModifyPersonalInfo() {
        System.out.println("请选择要修改的信息：");
        System.out.println("[1] 修改姓名");
        System.out.println("[2] 修改性别");
        System.out.println("[3] 修改年龄");
        System.out.println("[4] 修改密码");
        System.out.println("[5] 退出修改界面");
    }
}
