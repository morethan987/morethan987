package com.example.GradeSystemBackend.domain.auth;

/**
 * 角色常量定义
 */
public final class RoleConstants {

    private RoleConstants() {
        // 工具类，禁止实例化
    }

    // ==================== 角色常量 ====================

    /**
     * 管理员角色
     */
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * 教师角色
     */
    public static final String ROLE_TEACHER = "TEACHER";

    /**
     * 学生角色
     */
    public static final String ROLE_STUDENT = "STUDENT";

    /**
     * 默认注册角色
     */
    public static final String DEFAULT_ROLE = ROLE_STUDENT;

    // ==================== 特殊权限常量 ====================

    /**
     * 管理员全部权限（特殊权限，不从Controller扫描）
     */
    public static final String ADMIN_ALL_PERMISSION = "admin:all";

    // ==================== 角色描述 ====================

    public static final String ROLE_ADMIN_DESC = "系统管理员，拥有所有权限";
    public static final String ROLE_TEACHER_DESC =
        "教师，可以管理课程和录入成绩";
    public static final String ROLE_STUDENT_DESC =
        "学生，可以查看自己的成绩和课程信息";

    // ==================== 工具方法 ====================

    /**
     * 检查是否为有效角色
     */
    public static boolean isValidRole(String role) {
        return (
            ROLE_ADMIN.equals(role) ||
            ROLE_TEACHER.equals(role) ||
            ROLE_STUDENT.equals(role)
        );
    }

    /**
     * 获取角色描述
     */
    public static String getRoleDescription(String role) {
        switch (role) {
            case ROLE_ADMIN:
                return ROLE_ADMIN_DESC;
            case ROLE_TEACHER:
                return ROLE_TEACHER_DESC;
            case ROLE_STUDENT:
                return ROLE_STUDENT_DESC;
            default:
                return "未知角色";
        }
    }

    /**
     * 检查角色是否有管理员权限
     */
    public static boolean isAdminRole(String role) {
        return ROLE_ADMIN.equals(role);
    }

    /**
     * 检查角色是否有教师权限
     */
    public static boolean isTeacherRole(String role) {
        return ROLE_TEACHER.equals(role) || ROLE_ADMIN.equals(role);
    }

    /**
     * 检查角色是否有学生权限
     */
    public static boolean isStudentRole(String role) {
        return ROLE_STUDENT.equals(role);
    }

    /**
     * 获取所有可用角色
     */
    public static String[] getAllRoles() {
        return new String[] { ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT };
    }

    /**
     * 获取用户可选择的注册角色（通常不包括管理员）
     */
    public static String[] getRegistrationRoles() {
        return new String[] { ROLE_TEACHER, ROLE_STUDENT };
    }
}
