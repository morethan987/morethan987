// 定义路由路径常量
export const ROUTES = {
  HOME: "/",
  FORGOT_PASSWORD: "/forgot-password",
  LOGIN: "/login",
  SIGNUP: "/signup",
  DASHBOARD: "/dashboard",
  SETTINGS: "/settings",

  // 带有参数的动态路由（重点）
  // wouter 使用 :paramname 语法
  USER_PROFILE: "/users/:id",
};

/**
 * 针对动态路由的辅助函数
 * 用于在代码中生成实际的跳转链接
 * 例如: makeUserUrl(123) -> "/users/123"
 */
export const makeUserUrl = (id: string) => {
  return ROUTES.USER_PROFILE.replace(":id", id);
};
