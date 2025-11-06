package com.example.view;

import java.util.Map;

public class StudentView extends BaseView {

    private static Map<Integer, String[]> codeMap;

    public StudentView(Map<String, String> sysFuctionMap) {
        super();
        codeMap = super.initCodeMap(sysFuctionMap);
    }

    @Override
    public void show_init() {
        System.out.println("欢迎进入学生界面");
        showOperationMenu(codeMap);
    }

    @Override
    public Map<Integer, String[]> getCodeMap() {
        return codeMap;
    }
}
