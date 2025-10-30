package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.view.LoginView;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginViewTest {

    // 用于保存原始的 System.in 和 System.out
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    // 用于捕获 System.out 的输出
    private ByteArrayOutputStream outContent;

    /**
     * 在每个测试方法执行前运行
     * 设置重定向的输出流
     */
    @BeforeEach
    void setUpStreams() {
        // 创建一个 ByteArrayOutputStream 来捕获输出
        outContent = new ByteArrayOutputStream();
        // 将 System.out 重定向到我们的捕获流
        System.setOut(new PrintStream(outContent));
    }

    /**
     * 在每个测试方法执行后运行
     * 恢复原始的 System.in 和 System.out
     */
    @AfterEach
    void restoreStreams() {
        // 恢复 System.in 和 System.out
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    /**
     * 辅助方法，用于模拟用户输入
     * @param data 要模拟的输入字符串
     */
    private void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    @Test
    @DisplayName("测试正常输入用户名和密码")
    void getLoginCredentials_SuccessfulInputTest() {
        // 1. 准备模拟输入，使用 System.lineSeparator() 保证跨平台兼容性
        String input =
            "testUser" +
            System.lineSeparator() +
            "testPass123" +
            System.lineSeparator();
        provideInput(input);

        // 2. 创建被测试对象并执行方法
        LoginView loginView = new LoginView();
        Map<String, String> credentials = loginView.getLoginCredentials();
        loginView.closeScanner(); // 关闭资源

        // 3. 断言结果
        assertEquals("testUser", credentials.get("username"));
        assertEquals("testPass123", credentials.get("password"));

        // 4. 断言输出
        String expectedOutput = "请输入用户名：" + "请输入密码：";
        // 使用 replaceAll 去除换行符和回车符，简化断言
        String actualOutput = outContent
            .toString()
            .replaceAll("\\r\\n|\\n", "");
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    @DisplayName("测试用户名为空时，会提示重新输入")
    void getLoginCredentials_EmptyUsernameThenValidInputTest() {
        // 第一次输入空用户名，第二次输入有效用户名和密码
        String input =
            "" +
            System.lineSeparator() +
            "validUser" +
            System.lineSeparator() +
            "validPass" +
            System.lineSeparator();
        provideInput(input);

        LoginView loginView = new LoginView();
        Map<String, String> credentials = loginView.getLoginCredentials();
        loginView.closeScanner();

        // 断言最终获取的凭证是否正确
        assertEquals("validUser", credentials.get("username"));
        assertEquals("validPass", credentials.get("password"));

        // 断言控制台是否输出了错误提示
        String output = outContent.toString();
        assertTrue(output.contains("用户名不能为空，请重新输入。"));
    }

    @Test
    @DisplayName("测试密码为空时，会提示重新输入")
    void getLoginCredentials_EmptyPasswordThenValidInputTest() {
        // 输入有效用户名，但第一次输入空密码，第二次输入有效密码
        String input =
            "validUser" +
            System.lineSeparator() +
            "" +
            System.lineSeparator() +
            "validPass" +
            System.lineSeparator();
        provideInput(input);

        LoginView loginView = new LoginView();
        Map<String, String> credentials = loginView.getLoginCredentials();
        loginView.closeScanner();

        // 断言最终获取的凭证是否正确
        assertEquals("validUser", credentials.get("username"));
        assertEquals("validPass", credentials.get("password"));

        // 断言控制台是否输出了错误提示
        String output = outContent.toString();
        assertTrue(output.contains("密码不能为空，请重新输入。"));
    }

    @Test
    @DisplayName("测试用户名和密码都曾为空的输入情况")
    void getLoginCredentials_BothEmptyThenValidInputTest() {
        // 模拟复杂输入：空用户名 -> 有效用户名 -> 空密码 -> 有效密码
        String input =
            "" +
            System.lineSeparator() + // 用户名为空
            "finalUser" +
            System.lineSeparator() + // 用户名有效
            "" +
            System.lineSeparator() + // 密码为空
            "finalPass" +
            System.lineSeparator(); // 密码有效
        provideInput(input);

        LoginView loginView = new LoginView();
        Map<String, String> credentials = loginView.getLoginCredentials();
        loginView.closeScanner();

        // 断言最终获取的凭证是否正确
        assertEquals("finalUser", credentials.get("username"));
        assertEquals("finalPass", credentials.get("password"));

        // 断言控制台是否输出了所有必要的错误提示
        String output = outContent.toString();
        assertTrue(output.contains("用户名不能为空，请重新输入。"));
        assertTrue(output.contains("密码不能为空，请重新输入。"));
    }
}
