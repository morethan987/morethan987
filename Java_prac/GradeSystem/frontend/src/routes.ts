// 定义路由路径常量
export const ROUTES = {
  BACKEND_BASE_URL: "http://localhost:8081/api/v1",
  HOME: "/",
  FORGOT_PASSWORD: "/forgot-password",
  LOGIN: "/login",
  SIGNUP: "/signup",
  DASHBOARD: "/dashboard",
  SETTINGS: "/settings",
  USER_PROFILE: "/user/profile",

  // 带有参数的动态路由（重点）
  // wouter 使用 :paramname 语法
  COURSE_DETAIL: "/courses/:courseId",
  GRADE_DETAIL: "/grades/:gradeId",
  USER_GRADES: "/users/:userId/grades",
  COURSE_STUDENTS: "/courses/:courseId/students",

  // 多参数动态路由
  ASSIGNMENT_DETAIL: "/courses/:courseId/assignments/:assignmentId",
  GRADE_EDIT: "/courses/:courseId/students/:studentId/grades/:gradeId",
};

/**
 * 针对动态路由的辅助函数
 * 用于在代码中生成实际的跳转链接
 */

// 单参数路由辅助函数
export const makeCourseUrl = (courseId: string | number) => {
  return ROUTES.COURSE_DETAIL.replace(":courseId", String(courseId));
};

export const makeGradeUrl = (gradeId: string | number) => {
  return ROUTES.GRADE_DETAIL.replace(":gradeId", String(gradeId));
};

export const makeUserGradesUrl = (userId: string | number) => {
  return ROUTES.USER_GRADES.replace(":userId", String(userId));
};

export const makeCourseStudentsUrl = (courseId: string | number) => {
  return ROUTES.COURSE_STUDENTS.replace(":courseId", String(courseId));
};

// 多参数路由辅助函数
export const makeAssignmentUrl = (
  courseId: string | number,
  assignmentId: string | number,
) => {
  return ROUTES.ASSIGNMENT_DETAIL.replace(
    ":courseId",
    String(courseId),
  ).replace(":assignmentId", String(assignmentId));
};

export const makeGradeEditUrl = (
  courseId: string | number,
  studentId: string | number,
  gradeId: string | number,
) => {
  return ROUTES.GRADE_EDIT.replace(":courseId", String(courseId))
    .replace(":studentId", String(studentId))
    .replace(":gradeId", String(gradeId));
};

/**
 * 通用的路由参数替换函数
 * @param route 路由模板
 * @param params 参数对象
 * @returns 替换后的路由
 */
export const buildRoute = (
  route: string,
  params: Record<string, string | number>,
) => {
  let result = route;
  Object.entries(params).forEach(([key, value]) => {
    result = result.replace(`:${key}`, String(value));
  });
  return result;
};
