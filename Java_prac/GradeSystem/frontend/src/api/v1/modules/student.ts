import client from "@/api/client";
import type { StudentDTO } from "@/types/student";

/**
 * 认证相关 API
 */
export const studentApi = {
  /**
   * 用户登录
   */
  getStudentByUserId: async (userId: string): Promise<StudentDTO> => {
    return await client.get(`/student/by-user/${userId}`);
  },
};
