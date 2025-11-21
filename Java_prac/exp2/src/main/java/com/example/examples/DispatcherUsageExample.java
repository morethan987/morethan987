package com.example.examples;

import com.example.auth.Permission;
import com.example.dispatcher.MethodDispatcher;
import java.lang.reflect.Method;

/**
 * MethodDispatcher 使用示例
 * 展示改进后的 dispatch 方法的各种用法
 */
public class DispatcherUsageExample {

    public static void main(String[] args) {
        // 创建调度器
        MethodDispatcher dispatcher = new MethodDispatcher("admin_session");

        // 创建示例控制器
        ExampleController controller = new ExampleController();

        System.out.println("=== MethodDispatcher 使用示例 ===\n");

        // 示例1: 使用 Method 对象直接调用
        demonstrateMethodObjectDispatch(dispatcher, controller);

        // 示例2: 使用传统的方法名调用（向后兼容）
        demonstrateMethodNameDispatch(dispatcher, controller);

        // 示例3: 使用函数式接口调用
        demonstrateFunctionalDispatch(dispatcher, controller);
    }

    /**
     * 示例1: 使用 Method 对象直接调用
     */
    private static void demonstrateMethodObjectDispatch(MethodDispatcher dispatcher, ExampleController controller) {
        System.out.println("1. 使用 Method 对象直接调用:");

        try {
            // 获取方法对象
            Method publicMethod = controller.getClass().getMethod("publicMethod", String.class);
            Method protectedMethod = controller.getClass().getMethod("protectedMethod");

            // 直接使用 Method 对象调用
            dispatcher.dispatch(controller, publicMethod, "Hello World");
            dispatcher.dispatch(controller, protectedMethod);

        } catch (NoSuchMethodException e) {
            System.out.println("❌ 方法不存在: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * 示例2: 使用传统的方法名调用（向后兼容）
     */
    private static void demonstrateMethodNameDispatch(MethodDispatcher dispatcher, ExampleController controller) {
        System.out.println("2. 使用方法名调用（向后兼容）:");

        // 传统方式调用
        dispatcher.dispatch(controller, "publicMethod", "Hello from method name");
        dispatcher.dispatch(controller, "protectedMethod");
        dispatcher.dispatch(controller, "nonExistentMethod"); // 测试错误处理

        System.out.println();
    }

    /**
     * 示例3: 使用函数式接口调用
     */
    private static void demonstrateFunctionalDispatch(MethodDispatcher dispatcher, ExampleController controller) {
        System.out.println("3. 使用函数式接口调用:");

        // 使用 Runnable (无参数无返回值)
        dispatcher.dispatch(
            () -> controller.publicMethod("Lambda call"),
            controller,
            "publicMethod"
        );

        // 使用 Consumer (有参数无返回值)
        dispatcher.dispatch(
            (String msg) -> controller.publicMethod(msg),
            "Consumer call",
            controller,
            "publicMethod"
        );

        // 使用 Function (有参数有返回值)
        String result = dispatcher.dispatch(
            (String input) -> controller.getProcessedData(input),
            "Function input",
            controller,
            "getProcessedData"
        );
        System.out.println("✅ Function 调用结果: " + result);

        // 使用 Supplier (无参数有返回值)
        String data = dispatcher.dispatch(
            () -> controller.getData(),
            controller,
            "getData"
        );
        System.out.println("✅ Supplier 调用结果: " + data);

        System.out.println();
    }

    /**
     * 示例控制器类
     */
    static class ExampleController {

        /**
         * 公共方法，无权限要求
         */
        public void publicMethod(String message) {
            System.out.println("✅ 公共方法执行: " + message);
        }

        /**
         * 受保护的方法，需要管理员权限
         */
        @Permission("ADMIN")
        public void protectedMethod() {
            System.out.println("✅ 受保护方法执行");
        }

        /**
         * 获取数据的方法
         */
        public String getData() {
            return "Sample Data";
        }

        /**
         * 处理数据的方法
         */
        public String getProcessedData(String input) {
            return "Processed: " + input;
        }

        /**
         * 需要特殊权限的方法
         */
        @Permission("SUPER_ADMIN")
        public void superAdminMethod() {
            System.out.println("✅ 超级管理员方法执行");
        }
    }
}
