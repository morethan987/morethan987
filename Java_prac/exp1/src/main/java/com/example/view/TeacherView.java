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
    public Map<Integer, String[]> getCodeMap() {
        return codeMap;
    }
}
