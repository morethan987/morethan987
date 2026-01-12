export { authApi } from "./modules/auth";
export { userApi } from "./modules/user";
export { courseApi } from "./modules/course";
export { gradeApi } from "./modules/grade";
export { teacherApi } from "./modules/teacher";
export { dashboardApi } from "./modules/dashboard";

// Re-export types for convenience
export type { LoginRequest, AuthResponse, UserBasicInfo } from "@/types/auth";
export type { Course, TeachingClass, CourseType } from "@/types/course";
export type { Grade } from "@/types/grade";
export type { CourseFilters } from "./modules/course";
export type { GradeFilters, GradeStats } from "./modules/grade";
export type { TeacherDTO } from "@/types/teacher";
export type { BatchGradeUpdateResponse } from "@/types/teaching-class";
