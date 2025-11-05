package com.example.view;

import java.util.Map;

public class TeacherView extends BaseView {

    private static Map<Integer, String[]> codeMap;

    public TeacherView(Map<String, String> sysFuctionMap) {
        super();
        codeMap = super.initCodeMap(sysFuctionMap);
    }

    @Override
    public void show_init() {
        System.out.println("欢迎进入教师界面");
        showOperationMenu(codeMap);
    }

    @Override
    public void showModifyPersonalInfo() {
        System.out.println("请选择要修改的信息：");
        System.out.println("[1] 修改姓名");
        System.out.println("[2] 修改性别");
        System.out.println("[3] 修改年龄");
        System.out.println("[4] 修改密码");
        System.out.println("[5] 退出修改界面");
    }

    @Override
    public Map<Integer, String[]> getCodeMap() {
        return codeMap;
    }
}
