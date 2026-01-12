import { useState, useCallback } from "react";
import { teacherApi } from "@/api/v1/modules/teacher";
import type {
  TeachingClassWithStats,
  StudentGradeInput,
  DistributionData,
  BatchGradeUpdateResponse,
} from "@/types/teaching-class";

interface UseTeacherReturn {
  teachingClasses: TeachingClassWithStats[];
  students: StudentGradeInput[];
  distribution: DistributionData[];
  loading: boolean;
  error: string | null;

  getTeachingClasses: (teacherId: string) => Promise<void>;
  getStudentsInTeachingClass: (teachingClassId: string) => Promise<void>;
  getGradeDistribution: (teachingClassId: string) => Promise<void>;
  batchUpdateGrades: (
    teachingClassId: string,
    grades: StudentGradeInput[]
  ) => Promise<BatchGradeUpdateResponse | null>;
  exportGrades: (teachingClassId: string) => Promise<void>;
  importGrades: (
    teachingClassId: string,
    file: File
  ) => Promise<BatchGradeUpdateResponse | null>;
}

export function useTeacher(): UseTeacherReturn {
  const [teachingClasses, setTeachingClasses] = useState<
    TeachingClassWithStats[]
  >([]);
  const [students, setStudents] = useState<StudentGradeInput[]>([]);
  const [distribution, setDistribution] = useState<DistributionData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getTeachingClasses = useCallback(async (teacherId: string) => {
    setLoading(true);
    setError(null);
    try {
      const data = await teacherApi.getTeachingClasses(teacherId);
      setTeachingClasses(data);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "获取教学班列表失败";
      setError(message);
      console.error("获取教学班列表失败:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  const getStudentsInTeachingClass = useCallback(
    async (teachingClassId: string) => {
      setLoading(true);
      setError(null);
      try {
        const data = await teacherApi.getStudentsInTeachingClass(
          teachingClassId
        );
        setStudents(data);
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "获取学生列表失败";
        setError(message);
        console.error("获取学生列表失败:", err);
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const getGradeDistribution = useCallback(async (teachingClassId: string) => {
    setLoading(true);
    setError(null);
    try {
      const data = await teacherApi.getGradeDistribution(teachingClassId);
      setDistribution(data);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "获取成绩分布失败";
      setError(message);
      console.error("获取成绩分布失败:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  const batchUpdateGrades = useCallback(
    async (
      teachingClassId: string,
      grades: StudentGradeInput[]
    ): Promise<BatchGradeUpdateResponse | null> => {
      setLoading(true);
      setError(null);
      try {
        const response = await teacherApi.batchUpdateGrades(
          teachingClassId,
          grades
        );
        return response;
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "批量更新成绩失败";
        setError(message);
        console.error("批量更新成绩失败:", err);
        return null;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const exportGrades = useCallback(async (teachingClassId: string) => {
    setLoading(true);
    setError(null);
    try {
      const blob = await teacherApi.exportGrades(teachingClassId);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `grades_${teachingClassId}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (err) {
      const message = err instanceof Error ? err.message : "导出成绩失败";
      setError(message);
      console.error("导出成绩失败:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  const importGrades = useCallback(
    async (
      teachingClassId: string,
      file: File
    ): Promise<BatchGradeUpdateResponse | null> => {
      setLoading(true);
      setError(null);
      try {
        const response = await teacherApi.importGrades(teachingClassId, file);
        return response;
      } catch (err) {
        const message = err instanceof Error ? err.message : "导入成绩失败";
        setError(message);
        console.error("导入成绩失败:", err);
        return null;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  return {
    teachingClasses,
    students,
    distribution,
    loading,
    error,
    getTeachingClasses,
    getStudentsInTeachingClass,
    getGradeDistribution,
    batchUpdateGrades,
    exportGrades,
    importGrades,
  };
}
