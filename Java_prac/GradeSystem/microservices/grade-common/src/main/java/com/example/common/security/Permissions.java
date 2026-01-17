package com.example.common.security;

public final class Permissions {
    
    public static final String ADMIN_ALL = "admin:all";
    
    public static final String STUDENT_VIEW = "student:view";
    public static final String STUDENT_EDIT = "student:edit";
    
    public static final String TEACHER_VIEW = "teacher:view";
    public static final String TEACHER_EDIT = "teacher:edit";
    
    public static final String COURSE_VIEW = "course:view";
    public static final String COURSE_EDIT = "course:edit";
    public static final String COURSE_ENROLL = "course:enroll";
    public static final String COURSE_DROP = "course:drop";
    
    public static final String GRADE_VIEW = "grade:view";
    public static final String GRADE_VIEW_OWN = "grade:view:own";
    public static final String GRADE_EDIT = "grade:edit";
    public static final String GRADE_IMPORT = "grade:import";
    public static final String GRADE_EXPORT = "grade:export";
    
    public static final String USER_VIEW = "user:view";
    public static final String USER_EDIT = "user:edit";
    
    public static final String ROLE_VIEW = "role:view";
    public static final String ROLE_EDIT = "role:edit";
    
    public static final String DASHBOARD_VIEW = "dashboard:view";
    
    private Permissions() {
    }
}
