package com.example.view;

import java.util.Map;

public class AdminView extends BaseView {

    private static Map<Integer, String[]> codeMap;

    public AdminView(Map<String, String> sysFuctionMap) {
        super();
        codeMap = super.initCodeMap(sysFuctionMap);
    }

    @Override
    public void show_init() {
        System.out.println("=== 管理员界面 ===");
        showOperationMenu(codeMap);
    }

    @Override
    public Map<Integer, String[]> getCodeMap() {
        return codeMap;
    }
}
