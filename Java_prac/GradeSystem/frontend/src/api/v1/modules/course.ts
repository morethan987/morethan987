import client from "@/api/client";
import type { Course, TeachingClass, CourseType } from "@/types/course";

export interface CourseFilters {
  courseType?: CourseType;
  search?: string;
}

/**
 * 课程相关 API
 */
export const courseApi = {
  /**
   * 获取学生的课程列表（包含教学班信息）
   */
  getStudentCourses: async (
    studentId: string,
    filters?: CourseFilters,
  ): Promise<TeachingClass[]> => {
    const params: Record<string, string> = {};

    if (filters?.courseType) {
      params.courseType = filters.courseType;
    }

    return await client.get(`/courses/student/${studentId}`, { params });
  },

  /**
   * 获取所有可选课程（选课用）
   */
  getAvailableCourses: async (
    studentId: string,
    filters?: CourseFilters,
  ): Promise<TeachingClass[]> => {
    const params: Record<string, string> = { studentId };

    if (filters?.courseType) {
      params.courseType = filters.courseType;
    }

    if (filters?.search) {
      params.search = filters.search;
    }

    return await client.get("/courses/available", { params });
  },

  /**
   * 学生选课
   */
  selectCourse: async (
    studentId: string,
    teachingClassId: string,
  ): Promise<string> => {
    const params = new URLSearchParams();
    params.append("studentId", studentId);
    params.append("teachingClassId", teachingClassId);

    return await client.post("/courses/select", params, {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
  },

  /**
   * 学生退课
   */
  dropCourse: async (
    studentId: string,
    teachingClassId: string,
  ): Promise<string> => {
    const params = new URLSearchParams();
    params.append("studentId", studentId);
    params.append("teachingClassId", teachingClassId);

    return await client.delete("/courses/drop", {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      data: params,
    });
  },

  /**
   * 获取课程详情
   */
  getCourseById: async (courseId: string): Promise<Course> => {
    return await client.get(`/courses/${courseId}`);
  },
};
