package com.example.dispatcher;

/**
 * 方法调度器 - 处理方法调用、权限校验
 */
public class MethodDispatcher {

    private MenuRegistry menuRegistry;

    /**
     * 默认构造函数，创建新的菜单注册表
     */
    public MethodDispatcher() {
        this.menuRegistry = new MenuRegistry();
    }

    /**
     * 构造函数，使用指定的菜单注册表
     * @param menuRegistry 菜单注册表实例
     */
    public MethodDispatcher(MenuRegistry menuRegistry) {
        this.menuRegistry = menuRegistry;
    }

    /**
     * 根据菜单和选项进行调度
     * @param sessionId 会话ID
     * @param menu 菜单名称
     * @param option 选项编号
     * @param args 方法参数
     */
    public void dispatch(
        String sessionId,
        String menu,
        int option,
        Object... args
    ) {
        MenuItem menuItem = menuRegistry.getMenuItem(menu, option);

        if (menuItem == null) {
            throw new IllegalArgumentException(
                String.format("菜单项不存在: menu=%s, option=%d", menu, option)
            );
        }

        // 权限检查
        if (!menuItem.hasAccess(sessionId)) {
            throw new SecurityException(
                String.format("权限不足，无法访问: %s", menuItem.getTitle())
            );
        }

        try {
            // 调用方法，传入sessionId作为第一个参数
            Object[] methodArgs = new Object[args.length + 1];
            methodArgs[0] = sessionId;
            System.arraycopy(args, 0, methodArgs, 1, args.length);

            menuItem.getMethod().invoke(menuItem.getController(), methodArgs);
        } catch (Exception e) {
            throw new RuntimeException(
                String.format(
                    "执行菜单项失败: %s - %s",
                    menuItem.getTitle(),
                    e.getMessage()
                ),
                e
            );
        }
    }

    /**
     * 获取菜单注册表
     * @return 菜单注册表实例
     */
    public MenuRegistry getMenuRegistry() {
        return menuRegistry;
    }

    /**
     * 注册控制器到菜单系统
     * @param controller 要注册的控制器
     */
    public void registerController(Object controller) {
        menuRegistry.registerController(controller);
    }

    /**
     * 批量注册控制器
     * @param controllers 控制器数组
     */
    public void registerControllers(Object... controllers) {
        menuRegistry.registerControllers(controllers);
    }
}
