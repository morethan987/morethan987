package com.example.auth.domain;

public final class RoleConstants {

    private RoleConstants() {}

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String DEFAULT_ROLE = ROLE_STUDENT;

    public static final String ADMIN_ALL_PERMISSION = "admin:all";

    public static final String ROLE_ADMIN_DESC = "System administrator with full access";
    public static final String ROLE_TEACHER_DESC = "Teacher, can manage courses and input grades";
    public static final String ROLE_STUDENT_DESC = "Student, can view own grades and course info";

    public static boolean isValidRole(String role) {
        return ROLE_ADMIN.equals(role) ||
               ROLE_TEACHER.equals(role) ||
               ROLE_STUDENT.equals(role);
    }

    public static String getRoleDescription(String role) {
        return switch (role) {
            case ROLE_ADMIN -> ROLE_ADMIN_DESC;
            case ROLE_TEACHER -> ROLE_TEACHER_DESC;
            case ROLE_STUDENT -> ROLE_STUDENT_DESC;
            default -> "Unknown role";
        };
    }

    public static boolean isAdminRole(String role) {
        return ROLE_ADMIN.equals(role);
    }

    public static boolean isTeacherRole(String role) {
        return ROLE_TEACHER.equals(role) || ROLE_ADMIN.equals(role);
    }

    public static boolean isStudentRole(String role) {
        return ROLE_STUDENT.equals(role);
    }

    public static String[] getAllRoles() {
        return new String[] { ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT };
    }

    public static String[] getRegistrationRoles() {
        return new String[] { ROLE_TEACHER, ROLE_STUDENT };
    }
}
