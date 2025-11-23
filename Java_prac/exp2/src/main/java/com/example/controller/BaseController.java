package com.example.controller;

import com.example.dispatcher.MenuRegistry;
import com.example.dispatcher.MethodDispatcher;
import com.example.model.dao.RoleDAO;
import com.example.model.dao.impl.RoleDAOImpl;
import com.example.util.LoggerUtil;
import com.example.view.MenuRenderer;
import com.example.view.Router;

public abstract class BaseController {

    protected MethodDispatcher dispatcher;
    protected Router router = new Router();
    public static MenuRenderer menuRenderer;
    protected static final RoleDAO roleDAO = new RoleDAOImpl();
    protected static MenuRegistry menuRegistry; // 静态菜单注册表，所有控制器共享

    public void initialize() {
        LoggerUtil.debug("初始化控制器: " + this.getClass().getSimpleName());
        setDispatcher();
        setRouter();
        LoggerUtil.debug(
            "控制器初始化完成: " + this.getClass().getSimpleName()
        );
    }

    /**
     * 初始化菜单系统
     */
    public static void initializeMenuSystem() {
        LoggerUtil.debug("初始化菜单系统");
        if (menuRegistry == null) {
            menuRegistry = new MenuRegistry();
        }
        menuRenderer = new MenuRenderer(menuRegistry);
        LoggerUtil.debug("菜单系统初始化完成");
    }

    private void setDispatcher() {
        LoggerUtil.debug("设置Dispatcher: " + this.getClass().getSimpleName());
        this.dispatcher = new MethodDispatcher(menuRegistry);
        // 注册当前控制器到菜单系统
        this.dispatcher.registerController(this);
    }

    private void setRouter() {
        LoggerUtil.debug("设置Router: " + this.getClass().getSimpleName());
        this.router = new Router(menuRegistry);
        this.router.setMenuRenderer(menuRenderer);
    }

    /**
     * 获取菜单注册表（静态方法，方便其他类访问）
     */
    public static MenuRegistry getMenuRegistry() {
        return menuRegistry;
    }

    /**
     * 注册控制器到菜单系统（静态方法）
     */
    public static void registerController(Object controller) {
        if (menuRegistry == null) {
            menuRegistry = new MenuRegistry();
        }
        LoggerUtil.debug(
            "注册控制器: " + controller.getClass().getSimpleName()
        );
        menuRegistry.registerController(controller);
    }

    /**
     * 批量注册控制器到菜单系统（静态方法）
     */
    public static void registerControllers(Object... controllers) {
        if (menuRegistry == null) {
            menuRegistry = new MenuRegistry();
        }
        LoggerUtil.debug("批量注册控制器，数量: " + controllers.length);
        for (Object controller : controllers) {
            LoggerUtil.debug(
                "注册控制器: " + controller.getClass().getSimpleName()
            );
        }
        menuRegistry.registerControllers(controllers);
        LoggerUtil.debug("批量控制器注册完成");
    }
}
