import client from "@/api/client";
import type { Grade } from "@/types/grade";

export interface GradeFilters {
  semester?: string;
  courseType?: string;
}

export interface GradeStats {
  totalCredits: number;
  averageGPA: number;
  averageScore: number;
  passedCourses: number;
}

/**
 * 成绩相关 API
 */
export const gradeApi = {
  /**
   * 获取学生成绩列表
   */
  getStudentGrades: async (
    studentId: string,
    filters?: GradeFilters,
  ): Promise<Grade[]> => {
    const params = new URLSearchParams();

    if (filters?.semester) {
      params.append("semester", filters.semester);
    }

    if (filters?.courseType) {
      params.append("courseType", filters.courseType);
    }

    const queryString = params.toString();
    const url = `/grades/student/${studentId}${
      queryString ? `?${queryString}` : ""
    }`;

    return await client.get(url);
  },

  /**
   * 获取学生成绩统计信息
   */
  getStudentGradeStats: async (studentId: string): Promise<GradeStats> => {
    return await client.get(`/grades/student/${studentId}/stats`);
  },

  /**
   * 获取学生指定学期的成绩统计
   */
  getStudentSemesterStats: async (
    studentId: string,
    semester: string,
  ): Promise<GradeStats> => {
    return await client.get(
      `/grades/student/${studentId}/semester/${semester}/stats`,
    );
  },

  /**
   * 获取学生所有学期列表
   */
  getStudentSemesters: async (studentId: string): Promise<string[]> => {
    return await client.get(`/grades/student/${studentId}/semesters`);
  },

  /**
   * 获取单个成绩详情
   */
  getGradeById: async (gradeId: string): Promise<Grade> => {
    return await client.get(`/grades/${gradeId}`);
  },

  /**
   * 教师录入/更新成绩
   */
  updateGrade: async (
    gradeId: string,
    gradeData: Partial<Grade>,
  ): Promise<Grade> => {
    return await client.put(`/grades/${gradeId}`, gradeData);
  },
};
