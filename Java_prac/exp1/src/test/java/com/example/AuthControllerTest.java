package com.example;

import static org.junit.jupiter.api.Assertions.*;

import com.example.controller.AuthController;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AuthController 的单元测试类。
 *
 * 这个测试类依赖于 com.example.model.user 包中的 Student, Teacher, 和 Admin 类的模拟实现。
 * 确保这些模拟类在测试的 classpath 中可用。
 */
class AuthControllerTest {

    private AuthController authController;

    // @BeforeEach 注解表示在每个测试方法执行前都会运行此方法
    @BeforeEach
    void setUp() {
        // 为每个测试创建一个新的 AuthController 实例，以确保测试之间相互独立
        authController = new AuthController();
    }

    @Test
    @DisplayName("测试学生用户成功登录")
    void handleLogin_SuccessfulStudentLogin() {
        Map<String, String> result = authController.handleLogin(
            "student1",
            "studentpass1"
        );

        assertEquals("true", result.get("res"), "登录结果应为 'true'");
        assertEquals(
            "登录成功",
            result.get("reason"),
            "登录信息应为 '登录成功'"
        );
        assertEquals("student", result.get("role"), "用户角色应为 'student'");
        assertEquals("student1", result.get("userid"), "用户ID应匹配");
        assertNotNull(result.get("token"), "登录成功后应返回一个 token");
    }

    @Test
    @DisplayName("测试教师用户成功登录")
    void handleLogin_SuccessfulTeacherLogin() {
        Map<String, String> result = authController.handleLogin(
            "teacher1",
            "teacherpass1"
        );

        assertEquals("true", result.get("res"));
        assertEquals("登录成功", result.get("reason"));
        assertEquals("teacher", result.get("role"));
        assertEquals("teacher1", result.get("userid"));
        assertNotNull(result.get("token"));
    }

    @Test
    @DisplayName("测试管理员用户成功登录")
    void handleLogin_SuccessfulAdminLogin() {
        Map<String, String> result = authController.handleLogin(
            "admin1",
            "adminpass1"
        );

        assertEquals("true", result.get("res"));
        assertEquals("登录成功", result.get("reason"));
        assertEquals("admin", result.get("role"));
        assertEquals("admin1", result.get("userid"));
        assertNotNull(result.get("token"));
    }

    @Test
    @DisplayName("测试用户密码错误")
    void handleLogin_IncorrectPassword() {
        Map<String, String> result = authController.handleLogin(
            "student1",
            "wrongpassword"
        );

        assertEquals("false", result.get("res"), "登录结果应为 'false'");
        assertEquals(
            "密码错误",
            result.get("reason"),
            "登录信息应为 '密码错误'"
        );
        assertNull(result.get("token"), "密码错误时不应生成 token");
    }

    @Test
    @DisplayName("测试用户不存在")
    void handleLogin_UserNotFound() {
        Map<String, String> result = authController.handleLogin(
            "unknownuser",
            "somepassword"
        );

        assertEquals("false", result.get("res"));
        assertEquals("用户不存在", result.get("reason"));
        assertNull(result.get("role"), "用户不存在时，不应返回角色");
        assertNull(result.get("token"));
    }

    @Test
    @DisplayName("使用登录后获取的有效 token进行验证")
    void checkToken_ValidToken() {
        // 首先，成功登录以获取一个有效的 session token
        Map<String, String> loginResult = authController.handleLogin(
            "student1",
            "studentpass1"
        );
        String validToken = loginResult.get("token");

        assertTrue(
            authController.checkToken(validToken),
            "使用有效的 token 验证应返回 true"
        );
    }

    @Test
    @DisplayName("使用无效的 token 进行验证")
    void checkToken_InvalidToken() {
        // 先登录，以确保控制器内部有一个 session token
        authController.handleLogin("student1", "studentpass1");

        String invalidToken = UUID.randomUUID().toString();
        assertFalse(
            authController.checkToken(invalidToken),
            "使用无效的 token 验证应返回 false"
        );
    }

    @Test
    @DisplayName("使用 null token 进行验证")
    void checkToken_NullToken() {
        // 先登录，以确保控制器内部的 session token 不为 null
        authController.handleLogin("teacher1", "teacherpass1");

        assertFalse(
            authController.checkToken(null),
            "使用 null token 验证应返回 false"
        );
    }

    @Test
    @DisplayName("在没有任何用户登录的情况下检查 token")
    void checkToken_BeforeLogin() {
        // 此时控制器内部的 sessionToken 应该是 null
        assertFalse(
            authController.checkToken(UUID.randomUUID().toString()),
            "在登录前检查 token 应返回 false"
        );
    }
}
