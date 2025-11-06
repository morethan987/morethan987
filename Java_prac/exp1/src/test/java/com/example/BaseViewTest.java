package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.view.BaseView;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BaseViewTest {

    // 用于捕获 System.out 的输出
    private final ByteArrayOutputStream outContent =
        new ByteArrayOutputStream();

    // 保存原始的 System 流
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    /**
     * 在每个测试方法执行前，重定向 System.out 以捕获输出。
     */
    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    /**
     * 在每个测试方法执行后，恢复原始的 System 流。
     * 关键：这里不再调用 closeScanner()。
     */
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    /**
     * 辅助方法，用于模拟用户的控制台输入。
     * @param data 要模拟的输入字符串
     */
    private void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    @Test
    @DisplayName("测试 readInput 方法能正确读取用户输入")
    void testReadInput() {
        // 1. 准备模拟输入
        provideInput("test input\n");
        // 2. 在重定向 System.in 之后创建 BaseView 实例
        BaseView view = new BaseView();

        String prompt = "请输入: ";
        String input = view.readInput(prompt);

        // 3. 断言
        assertEquals(prompt, outContent.toString());
        assertEquals("test input", input);
    }

    @Test
    @DisplayName("测试 clearScreen 方法能正常执行不抛出异常")
    void testClearScreen() {
        BaseView view = new BaseView();
        assertDoesNotThrow(view::clearScreen);
    }

    @Test
    @DisplayName("测试 showSortedData 方法能按指定顺序打印数据")
    void testShowSortedData() {
        BaseView view = new BaseView();
        // 使用 LinkedHashMap 保证 key 的顺序是可预测的
        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put("ID", Arrays.asList("1", "2", "3"));
        data.put("Name", Arrays.asList("Alice", "Bob", "Charlie"));
        List<Integer> order = Arrays.asList(2, 0); // 期望顺序: 先打印索引2，再打印索引0

        view.showSortedData(data, order);

        // 构建期望的输出字符串，使用 System.lineSeparator() 保证跨平台兼容性
        String expectedOutput =
            "ID\tName\t" +
            System.lineSeparator() +
            "3\tCharlie\t" +
            System.lineSeparator() +
            "1\tAlice\t" +
            System.lineSeparator();

        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    @DisplayName("测试 showPersonalInfo 方法能正确打印单行信息")
    void testShowPersonalInfo() {
        BaseView view = new BaseView();
        Map<String, String> info = new LinkedHashMap<>();
        info.put("ID", "101");
        info.put("Name", "John Doe");
        info.put("Email", "john.doe@example.com");

        view.showPersonalInfo(info);

        String expectedOutput =
            "ID\tName\tEmail\t" + "101\tJohn Doe\tjohn.doe@example.com\t";

        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    @DisplayName("测试 showMessage 方法能正确显示消息")
    void testShowMessage() {
        BaseView view = new BaseView();
        String message = "操作成功！";
        view.showMessage(message);
        assertEquals(message + System.lineSeparator(), outContent.toString());
    }

    @Test
    @DisplayName("测试 getChoice 能接受有效输入")
    void testGetChoice_ValidInput() {
        provideInput("3\n");
        BaseView view = new BaseView(); // 在设置好输入后创建实例

        int choice = view.getChoice(5);
        assertEquals(3, choice);
    }

    @Test
    @DisplayName("测试 getChoice 能处理无效输入后接受有效输入")
    void testGetChoice_InvalidThenValidInput() {
        String simulatedInput =
            "abc\n" + // 无效字符
            "10\n" + // 超出范围
            "5\n"; // 有效输入
        provideInput(simulatedInput);
        BaseView view = new BaseView(); // 在设置好输入后创建实例

        int choice = view.getChoice(8);
        assertEquals(5, choice);

        // 验证是否向用户打印了正确的错误提示
        String output = outContent.toString();
        assertTrue(output.contains("错误：请输入一个有效的整数"));
        assertTrue(output.contains("错误：输入的数字超出范围"));
    }
}
