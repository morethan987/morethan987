package com.example.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 菜单项注解 - 用于声明方法对应的菜单选项和权限要求
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MenuAction {
    /**
     * 菜单名称，支持多级菜单
     * 例如：main（主菜单）、student（学生管理）、course（课程管理）
     */
    String menu() default "main";

    /**
     * 选项编号，在同一菜单内应该唯一
     */
    int option();

    /**
     * 显示标题，用于在菜单中显示
     */
    String title();

    /**
     * 所需权限标识符，可选
     * 如果为空，则表示不需要特殊权限
     */
    String permission() default "";

    /**
     * 描述信息，用于显示更详细的功能说明
     */
    String description() default "";

    /**
     * 所需角色，与权限是OR关系，满足其一即可
     */
    String[] roles() default {};

    /**
     * 是否需要登录，默认需要
     */
    boolean requireAuth() default true;

    /**
     * 排序权重，用于控制菜单项的显示顺序
     * 数字越小越靠前，相同权重按选项编号排序
     */
    int order() default 0;

    /**
     * 图标或标记，可用于UI显示（预留）
     */
    String icon() default "";
}
