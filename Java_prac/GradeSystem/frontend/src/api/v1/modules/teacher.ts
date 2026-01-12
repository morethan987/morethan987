import client from "@/api/client";
import type {
  TeachingClassWithStats,
  StudentGradeInput,
  DistributionData,
  BatchGradeUpdateResponse,
} from "@/types/teaching-class";
import type { TeacherDTO } from "@/types/teacher";

export const teacherApi = {
  getTeacherByUserId: async (userId: string): Promise<TeacherDTO> => {
    return await client.get(`/teacher/by-user/${userId}`);
  },

  getTeachingClasses: async (
    teacherId: string
  ): Promise<TeachingClassWithStats[]> => {
    return await client.get(`/teacher/${teacherId}/teaching-classes`);
  },

  getStudentsInTeachingClass: async (
    teachingClassId: string
  ): Promise<StudentGradeInput[]> => {
    return await client.get(
      `/teacher/teaching-classes/${teachingClassId}/students`
    );
  },

  getGradeDistribution: async (
    teachingClassId: string
  ): Promise<DistributionData[]> => {
    return await client.get(
      `/teacher/teaching-classes/${teachingClassId}/grades/distribution`
    );
  },

  batchUpdateGrades: async (
    teachingClassId: string,
    grades: StudentGradeInput[]
  ): Promise<BatchGradeUpdateResponse> => {
    return await client.post(
      `/teacher/teaching-classes/${teachingClassId}/grades/batch`,
      { grades }
    );
  },

  exportGrades: async (teachingClassId: string): Promise<Blob> => {
    const response = await client.get(
      `/teacher/teaching-classes/${teachingClassId}/grades/export`,
      { responseType: "blob" }
    );
    return response as unknown as Blob;
  },

  importGrades: async (
    teachingClassId: string,
    file: File
  ): Promise<BatchGradeUpdateResponse> => {
    const formData = new FormData();
    formData.append("file", file);
    return await client.post(
      `/teacher/teaching-classes/${teachingClassId}/grades/import`,
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
      }
    );
  },
};
