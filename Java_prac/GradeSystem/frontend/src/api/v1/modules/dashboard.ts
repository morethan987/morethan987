import type { CardData } from "@/types/card-data";
import client from "@/api/client";

export const dashboardApi = {
  getAdminDashboard: async (): Promise<CardData[]> => {
    return await client.get("/dashboard/admin");
  },

  getStudentDashboard: async (studentId: string): Promise<CardData[]> => {
    return await client.get(`/dashboard/student/${studentId}`);
  },

  getTeacherDashboard: async (teacherId: string): Promise<CardData[]> => {
    return await client.get(`/dashboard/teacher/${teacherId}`);
  },
};
