package com.example.dispatcher;

import com.example.auth.AuthService;
import com.example.auth.Permission;
import com.example.model.dto.BinaryMessage;
import java.lang.reflect.Method;

/**
 * 解析注解 + 权限校验 + 调用
 */
public class MethodDispatcher {

    private final String sessionId;

    public MethodDispatcher(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 直接使用 Method 对象进行调用
     * @param controller 控制器实例
     * @param method 要调用的方法对象
     * @param args 方法参数
     */
    public BinaryMessage dispatch(
        Object controller,
        Method method,
        Object... args
    ) {
        try {
            // 权限检查
            if (method.isAnnotationPresent(Permission.class)) {
                String required = method
                    .getAnnotation(Permission.class)
                    .value();
                if (!AuthService.hasPermission(sessionId, required)) {
                    return new BinaryMessage(false, "权限不足: " + required);
                }
            }

            method.invoke(controller, args);
            return new BinaryMessage(true, "方法调用成功: " + method.getName());
        } catch (Exception e) {
            return new BinaryMessage(false, "方法调用失败: " + e.getMessage());
        }
    }

    /**
     * 保持向后兼容的方法名调用方式
     * @param controller 控制器实例
     * @param methodName 方法名
     * @param args 方法参数
     */
    public BinaryMessage dispatch(
        Object controller,
        String methodName,
        Object... args
    ) {
        try {
            Method method = controller.getClass().getMethod(methodName);
            return dispatch(controller, method, args);
        } catch (NoSuchMethodException e) {
            return new BinaryMessage(false, "无此方法: " + methodName);
        }
    }
}
