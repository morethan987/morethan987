package com.example.view;

import java.util.ArrayList;
import java.util.Collections;
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

    public void showData(Map<String, List<String>> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("（无数据）");
            return;
        }

        List<String> headers = new ArrayList<>(data.keySet());
        int numRows = data.values().iterator().next().size();
        List<List<String>> rows = new ArrayList<>();

        for (int i = 0; i < numRows; i++) {
            List<String> currentRow = new ArrayList<>();
            for (String header : headers) {
                currentRow.add(data.get(header).get(i));
            }
            rows.add(currentRow);
        }

        printTable(headers, rows);
    }

    /**
     * 根据指定的顺序打印多行数据，确保列对齐
     *
     * @param data  一个Map，其中key是列名，value是该列的数据列表。
     * @param order 一个List，定义了数据行的打印顺序。
     */
    public void showSortedData(
        Map<String, List<String>> data,
        List<Integer> order
    ) {
        if (data == null || data.isEmpty()) {
            System.out.println("（无数据）");
            return;
        }

        List<String> headers = new ArrayList<>(data.keySet());
        List<List<String>> rows = new ArrayList<>();

        for (Integer index : order) {
            List<String> currentRow = new ArrayList<>();
            for (String header : headers) {
                List<String> column = data.get(header);
                if (index >= 0 && index < column.size()) {
                    currentRow.add(column.get(index));
                } else {
                    currentRow.add("");
                }
            }
            rows.add(currentRow);
        }

        printTable(headers, rows);
    }

    /**
     * 打印单行个人信息，确保列对齐
     *
     * @param info 包含个人信息的Map
     */
    public void showPersonalInfo(Map<String, String> info) {
        if (info == null || info.isEmpty()) {
            System.out.println("（无信息）");
            return;
        }

        List<String> headers = new ArrayList<>(info.keySet());
        List<String> rowData = new ArrayList<>();
        for (String header : headers) {
            rowData.add(info.get(header));
        }

        printTable(headers, Collections.singletonList(rowData));
    }

    /**
     * 计算字符串的视觉显示宽度。
     * 全角字符（如汉字）计为2，半角字符（如英文、数字）计为1。
     *
     * @param str 要计算的字符串
     * @return 字符串的视觉宽度
     */
    private int getDisplayLength(String str) {
        if (str == null) {
            return 0;
        }
        // 使用正则表达式将全角字符替换为两个半角字符，然后计算长度
        return str.replaceAll("[^\\x00-\\xff]", "**").length();
    }

    /**
     * 这是所有表格打印功能的核心
     *
     * @param headers 表头列表
     * @param rows    数据行列表，每个内部列表代表一行
     */
    private void printTable(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty()) {
            return;
        }

        // 1. 使用 getDisplayLength 计算每一列的最大视觉宽度
        List<Integer> maxWidths = new ArrayList<>();
        for (String header : headers) {
            maxWidths.add(getDisplayLength(header));
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (i < maxWidths.size()) {
                    maxWidths.set(
                        i,
                        Math.max(maxWidths.get(i), getDisplayLength(row.get(i)))
                    );
                }
            }
        }

        // 2. 打印表头
        printRow(headers, maxWidths);

        // 3. 打印数据行
        for (List<String> row : rows) {
            printRow(row, maxWidths);
        }
    }

    /**
     * 打印一行数据，并根据最大宽度手动填充空格
     * @param rowData       要打印的一行数据
     * @param maxDisplayWidths 各列的最大视觉宽度
     */
    private void printRow(
        List<String> rowData,
        List<Integer> maxDisplayWidths
    ) {
        StringBuilder rowBuilder = new StringBuilder();
        for (int i = 0; i < rowData.size(); i++) {
            String cell = rowData.get(i);
            rowBuilder.append(cell);

            int padding = maxDisplayWidths.get(i) - getDisplayLength(cell);
            // 手动填充空格
            for (int j = 0; j < padding; j++) {
                rowBuilder.append(" ");
            }
            // 在列之间添加2个空格作为分隔符
            rowBuilder.append("  ");
        }
        System.out.println(rowBuilder.toString());
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

    public void showDistributionChart(Map<String, Integer> distribution) {
        // 找到最大值
        int maxCount = distribution
            .values()
            .stream()
            .max(Integer::compare)
            .orElse(1);

        // 设定最大块长度
        final int MAX_BAR_LENGTH = 50;

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            String range = entry.getKey();
            int count = entry.getValue();

            // 按比例缩放块数量
            int barLength = (int) Math.round(
                (count / (double) maxCount) * MAX_BAR_LENGTH
            );
            if (barLength == 0 && count > 0) {
                barLength = 1; // 至少显示一个块
            }

            // 输出条形
            String bar = "█".repeat(barLength);
            System.out.printf("%-10s: %-50s (%d)%n", range, bar, count);
        }
    }
}
