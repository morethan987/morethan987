import { useState, useEffect, useCallback } from "react";
import { gradeApi, type GradeFilters, type Grade, type GradeStats } from "@/api/v1";

interface UseGradesReturn {
  // 状态
  grades: Grade[];
  stats: GradeStats | null;
  semesters: string[];
  isLoading: boolean;
  error: string | null;

  // 操作
  fetchStudentGrades: (studentId: string, filters?: GradeFilters) => Promise<void>;
  fetchStudentStats: (studentId: string) => Promise<void>;
  fetchSemesterStats: (studentId: string, semester: string) => Promise<GradeStats | null>;
  fetchStudentSemesters: (studentId: string) => Promise<void>;
  updateGrade: (gradeId: string, gradeData: Partial<Grade>) => Promise<boolean>;
  clearError: () => void;
  refreshGrades: () => Promise<void>;
}

/**
 * 成绩管理 Hook
 */
export function useGrades(): UseGradesReturn {
  const [grades, setGrades] = useState<Grade[]>([]);
  const [stats, setStats] = useState<GradeStats | null>(null);
  const [semesters, setSemesters] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastFetch, setLastFetch] = useState<{
    studentId?: string;
    filters?: GradeFilters;
  } | null>(null);

  /**
   * 清除错误信息
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  /**
   * 获取学生成绩列表
   */
  const fetchStudentGrades = useCallback(
    async (studentId: string, filters?: GradeFilters) => {
      try {
        setIsLoading(true);
        setError(null);

        const data = await gradeApi.getStudentGrades(studentId, filters);
        setGrades(data);
        setLastFetch({ studentId, filters });
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "获取成绩列表失败";
        setError(errorMessage);
        setGrades([]);
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  /**
   * 获取学生成绩统计信息
   */
  const fetchStudentStats = useCallback(
    async (studentId: string) => {
      try {
        setError(null);

        const data = await gradeApi.getStudentGradeStats(studentId);
        setStats(data);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "获取成绩统计失败";
        setError(errorMessage);
        setStats(null);
      }
    },
    []
  );

  /**
   * 获取学生指定学期的成绩统计
   */
  const fetchSemesterStats = useCallback(
    async (studentId: string, semester: string): Promise<GradeStats | null> => {
      try {
        setError(null);

        const data = await gradeApi.getStudentSemesterStats(studentId, semester);
        return data;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "获取学期统计失败";
        setError(errorMessage);
        return null;
      }
    },
    []
  );

  /**
   * 获取学生所有学期列表
   */
  const fetchStudentSemesters = useCallback(
    async (studentId: string) => {
      try {
        setError(null);

        const data = await gradeApi.getStudentSemesters(studentId);
        setSemesters(data);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "获取学期列表失败";
        setError(errorMessage);
        setSemesters([]);
      }
    },
    []
  );

  /**
   * 更新成绩
   */
  const updateGrade = useCallback(
    async (gradeId: string, gradeData: Partial<Grade>): Promise<boolean> => {
      try {
        setError(null);
        await gradeApi.updateGrade(gradeId, gradeData);

        // 更新成功后刷新成绩列表
        if (lastFetch?.studentId) {
          await fetchStudentGrades(lastFetch.studentId, lastFetch.filters);
          await fetchStudentStats(lastFetch.studentId);
        }

        return true;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "更新成绩失败";
        setError(errorMessage);
        return false;
      }
    },
    [lastFetch, fetchStudentGrades, fetchStudentStats]
  );

  /**
   * 刷新当前成绩列表
   */
  const refreshGrades = useCallback(async () => {
    if (!lastFetch?.studentId) return;

    await Promise.all([
      fetchStudentGrades(lastFetch.studentId, lastFetch.filters),
      fetchStudentStats(lastFetch.studentId),
      fetchStudentSemesters(lastFetch.studentId),
    ]);
  }, [lastFetch, fetchStudentGrades, fetchStudentStats, fetchStudentSemesters]);

  return {
    grades,
    stats,
    semesters,
    isLoading,
    error,
    fetchStudentGrades,
    fetchStudentStats,
    fetchSemesterStats,
    fetchStudentSemesters,
    updateGrade,
    clearError,
    refreshGrades,
  };
}
