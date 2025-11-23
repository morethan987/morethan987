package com.example.view;

import com.example.controller.BaseController;
import com.example.dispatcher.MenuRegistry;
import com.example.model.dto.LoginMessage;
import com.example.model.dto.RegistMessage;

/**
 * 菜单路由器，持有所有菜单视图的实例
 */
public class Router extends BaseView {

    private LoginView loginView = new LoginView();

    /**
     * 默认构造函数
     */
    public Router() {
        super();
    }

    /**
     * 使用菜单注册表的构造函数
     * @param menuRegistry 菜单注册表
     */
    public Router(MenuRegistry menuRegistry) {
        super();
        BaseController.menuRenderer = new MenuRenderer(menuRegistry);
    }

    /**
     * 设置菜单渲染器
     * @param menuRenderer 菜单渲染器实例
     */
    public void setMenuRenderer(MenuRenderer menuRenderer) {
        BaseController.menuRenderer = menuRenderer;
    }

    /**
     * 显示登录界面并获取用户输入
     * 支持用户取消操作
     * @return 登录信息，如果用户取消则返回null
     */
    public LoginMessage login() {
        return loginView.getLoginInput();
    }

    /**
     * 显示注册界面并获取用户输入
     * 支持用户取消操作
     * @return 注册信息，如果用户取消则返回null
     */
    public RegistMessage regist() {
        return loginView.getRegistInput();
    }

    /**
     * 显示指定菜单并获取用户选择
     * @param menuName 菜单名称
     * @param sessionId 会话ID
     * @return 用户选择的选项编号
     */
    public Integer showMenu(String menuName, String sessionId) {
        clearScreen();
        if (BaseController.menuRenderer == null) {
            throw new IllegalStateException(
                "MenuRenderer 未初始化，请先设置 MenuRenderer"
            );
        }
        return BaseController.menuRenderer.showMenuAndGetChoice(
            menuName,
            sessionId
        );
    }

    /**
     * 获取菜单渲染器
     * @return 菜单渲染器实例
     */
    public MenuRenderer getMenuRenderer() {
        return BaseController.menuRenderer;
    }
}
