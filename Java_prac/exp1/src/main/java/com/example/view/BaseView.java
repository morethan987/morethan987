package com.example.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BaseView {

    protected Scanner scanner;

    public BaseView() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * 关闭 Scanner 对象，释放系统资源
     * 通常在应用程序生命周期结束时调用
     */
    public void closeScanner() {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * 显示提示信息并读取用户的输入。
     *
     * @param prompt 提示用户输入的信息（如："请输入用户名："）
     * @return 用户输入的字符串
     */
    public String readInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls")
                    .inheritIO()
                    .start()
                    .waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Print data according to the order list.
     * @param data
     * @param order
     */
    public void showSortedData(
        Map<String, List<String>> data,
        List<Integer> order
    ) {
        // 打印表头
        for (String key : data.keySet()) {
            System.out.print(key + "\t");
        }
        System.out.println();

        // 按照order顺序打印数据行
        for (Integer index : order) {
            for (String key : data.keySet()) {
                List<String> column = data.get(key);
                // 防止索引越界
                if (index >= 0 && index < column.size()) {
                    System.out.print(column.get(index) + "\t");
                } else {
                    System.out.print("\t"); // 空格占位
                }
            }
            System.out.println();
        }
    }

    /**
     * Print personal information.
     * @param info
     */
    public void showPersonalInfo(Map<String, String> info) {
        // 打印表头
        for (String key : info.keySet()) {
            System.out.print(key + "\t");
        }
        // 打印数据项
        for (String key : info.keySet()) {
            System.out.print(info.get(key) + "\t");
        }
        System.out.println();
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * Get user choice within a specified range.
     * @param min_choice
     * @param max_choice
     * @return the valid choice input by the user
     */
    public Integer getChoice(int num_options) {
        int min_choice = 1;
        int max_choice = num_options;
        int choice = -1;
        boolean isValid = false;

        while (!isValid) {
            System.out.print(
                "请输入一个数字 (" + min_choice + "~" + max_choice + "): "
            );
            String input = scanner.nextLine();

            try {
                choice = Integer.parseInt(input);

                if (choice >= min_choice && choice <= max_choice) {
                    isValid = true; // 输入有效，退出循环
                } else {
                    System.out.println(
                        "错误：输入的数字超出范围，请重新输入 " +
                            min_choice +
                            " 到 " +
                            max_choice +
                            " 之间的数字。"
                    );
                }
            } catch (NumberFormatException e) {
                // 处理用户输入非数字字符的情况
                System.out.println("错误：请输入一个有效的整数");
            }
        }
        return choice;
    }

    public String getFuctionCodeFromInteger(
        Map<Integer, String[]> map,
        Integer idx
    ) {
        return map.get(idx)[0];
    }

    public Map<Integer, String[]> initCodeMap(
        Map<String, String> sysFuctionMap
    ) {
        // 初始化功能代码映射
        Map<Integer, String[]> codeMap = new HashMap<>();
        int index = 1;
        for (String code : sysFuctionMap.keySet()) {
            String description = sysFuctionMap.get(code);
            codeMap.put(index, new String[] { code, description });
            index++;
        }
        return codeMap;
    }

    public void showOperationMenu(Map<Integer, String[]> codeMap) {
        for (Integer idx : codeMap.keySet()) {
            String description = codeMap.get(idx)[1];
            System.out.println("[" + idx + "] " + description);
        }
    }

    public void show_init() {}

    public void showModifyPersonalInfo() {}

    public Map<Integer, String[]> getCodeMap() {
        return null;
    }
}
