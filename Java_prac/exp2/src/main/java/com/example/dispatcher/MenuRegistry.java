package com.example.dispatcher;

import com.example.auth.annotation.MenuAction;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单注册表 - 管理所有菜单项的注册和查询
 *
 * 这个类负责：
 * 1. 扫描控制器中的 MenuAction 注解
 * 2. 注册菜单项到内部映射表
 * 3. 提供菜单项查询功能
 * 4. 根据权限过滤可访问的菜单项
 */
public class MenuRegistry {

    // 菜单项映射表：菜单名 -> 选项编号 -> 菜单项
    private final Map<String, Map<Integer, MenuItem>> menuItems =
        new HashMap<>();

    // 已注册的控制器，避免重复注册
    private final Set<Object> registeredControllers = new HashSet<>();

    /**
     * 注册控制器，扫描其中的菜单项注解
     * @param controller 要注册的控制器实例
     */
    public void registerController(Object controller) {
        if (registeredControllers.contains(controller)) {
            return; // 避免重复注册
        }

        Class<?> clazz = controller.getClass();
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(MenuAction.class)) {
                MenuAction annotation = method.getAnnotation(MenuAction.class);
                MenuItem item = new MenuItem(annotation, controller, method);

                // 注册到映射表
                menuItems
                    .computeIfAbsent(item.getMenu(), k -> new TreeMap<>())
                    .put(item.getOption(), item);
            }
        }

        registeredControllers.add(controller);
    }

    /**
     * 批量注册多个控制器
     * @param controllers 控制器实例数组
     */
    public void registerControllers(Object... controllers) {
        for (Object controller : controllers) {
            registerController(controller);
        }
    }

    /**
     * 获取指定菜单的指定选项
     * @param menu 菜单名称
     * @param option 选项编号
     * @return 对应的菜单项，如果不存在返回 null
     */
    public MenuItem getMenuItem(String menu, int option) {
        Map<Integer, MenuItem> items = menuItems.get(menu);
        if (items == null) {
            return null;
        }
        return items.get(option);
    }

    /**
     * 获取用户有权限访问的菜单项列表
     * @param menu 菜单名称
     * @param sessionId 会话ID
     * @return 有权限访问的菜单项列表，按顺序排序
     */
    public List<MenuItem> getAccessibleMenuItems(
        String menu,
        String sessionId
    ) {
        Map<Integer, MenuItem> items = menuItems.get(menu);
        if (items == null) {
            return new ArrayList<>();
        }

        return items
            .values()
            .stream()
            .filter(item -> item.hasAccess(sessionId))
            .sorted(
                Comparator.comparing(MenuItem::getOrder).thenComparing(
                    MenuItem::getOption
                )
            )
            .collect(Collectors.toList());
    }

    /**
     * 获取所有菜单名称
     * @return 所有已注册的菜单名称集合
     */
    public Set<String> getAllMenus() {
        return new HashSet<>(menuItems.keySet());
    }

    /**
     * 获取指定菜单的所有选项编号
     * @param menu 菜单名称
     * @return 选项编号集合
     */
    public Set<Integer> getMenuOptions(String menu) {
        Map<Integer, MenuItem> items = menuItems.get(menu);
        if (items == null) {
            return new HashSet<>();
        }
        return new HashSet<>(items.keySet());
    }

    /**
     * 检查菜单项是否存在
     * @param menu 菜单名称
     * @param option 选项编号
     * @return 是否存在
     */
    public boolean hasMenuItem(String menu, int option) {
        return getMenuItem(menu, option) != null;
    }

    /**
     * 获取菜单项总数
     * @return 总菜单项数量
     */
    public int getTotalMenuItemCount() {
        return menuItems.values().stream().mapToInt(Map::size).sum();
    }

    /**
     * 获取指定菜单的菜单项数量
     * @param menu 菜单名称
     * @return 菜单项数量
     */
    public int getMenuItemCount(String menu) {
        Map<Integer, MenuItem> items = menuItems.get(menu);
        return items == null ? 0 : items.size();
    }

    /**
     * 清空所有注册的菜单项（通常用于测试）
     */
    public void clear() {
        menuItems.clear();
        registeredControllers.clear();
    }

    /**
     * 获取菜单项的详细信息（调试用）
     * @return 菜单结构的字符串表示
     */
    public String getMenuStructure() {
        StringBuilder sb = new StringBuilder();
        sb.append("菜单结构:\n");

        for (Map.Entry<
            String,
            Map<Integer, MenuItem>
        > menuEntry : menuItems.entrySet()) {
            String menuName = menuEntry.getKey();
            sb.append("菜单: ").append(menuName).append("\n");

            Map<Integer, MenuItem> items = menuEntry.getValue();
            for (Map.Entry<Integer, MenuItem> itemEntry : items.entrySet()) {
                MenuItem item = itemEntry.getValue();
                sb
                    .append("  ")
                    .append(item.getOption())
                    .append(". ")
                    .append(item.getTitle());

                if (!item.getPermission().isEmpty()) {
                    sb
                        .append(" [权限: ")
                        .append(item.getPermission())
                        .append("]");
                }

                if (!item.getDescription().isEmpty()) {
                    sb.append(" - ").append(item.getDescription());
                }

                sb.append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
