package com.example.controller;

import com.example.dispatcher.MenuRegistry;
import com.example.dispatcher.MethodDispatcher;
import com.example.model.dao.RoleDAO;
import com.example.model.dao.impl.RoleDAOImpl;
import com.example.view.MenuRenderer;
import com.example.view.Router;

public abstract class BaseController {

    protected MethodDispatcher dispatcher;
    protected Router router = new Router();
    public static MenuRenderer menuRenderer;
    protected static final RoleDAO roleDAO = new RoleDAOImpl();
    protected static MenuRegistry menuRegistry; // 静态菜单注册表，所有控制器共享

    public void initialize() {
        setDispatcher();
        setRouter();
    }

    /**
     * 初始化菜单系统
     */
    public static void initializeMenuSystem() {
        if (menuRegistry == null) {
            menuRegistry = new MenuRegistry();
        }
        menuRenderer = new MenuRenderer(menuRegistry);
    }

    private void setDispatcher() {
        this.dispatcher = new MethodDispatcher(menuRegistry);
        // 注册当前控制器到菜单系统
        this.dispatcher.registerController(this);
    }

    private void setRouter() {
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
        menuRegistry.registerController(controller);
    }

    /**
     * 批量注册控制器到菜单系统（静态方法）
     */
    public static void registerControllers(Object... controllers) {
        if (menuRegistry == null) {
            menuRegistry = new MenuRegistry();
        }
        menuRegistry.registerControllers(controllers);
    }
}
