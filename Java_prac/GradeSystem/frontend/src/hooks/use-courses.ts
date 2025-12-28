import { useState, useEffect, useCallback } from "react";
import { courseApi, type CourseFilters, type TeachingClass } from "@/api/v1";

interface UseCoursesReturn {
  // 状态
  courses: TeachingClass[];
  isLoading: boolean;
  error: string | null;

  // 操作
  fetchStudentCourses: (
    studentId: string,
    filters?: CourseFilters,
  ) => Promise<void>;
  fetchAvailableCourses: (
    studentId: string,
    filters?: CourseFilters,
  ) => Promise<void>;
  selectCourse: (
    studentId: string,
    teachingClassId: string,
  ) => Promise<boolean>;
  dropCourse: (studentId: string, teachingClassId: string) => Promise<boolean>;
  clearError: () => void;
  refreshCourses: () => Promise<void>;
}

/**
 * 课程管理 Hook
 */
export function useCourses(): UseCoursesReturn {
  const [courses, setCourses] = useState<TeachingClass[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastFetch, setLastFetch] = useState<{
    type: "student" | "available";
    studentId?: string;
    filters?: CourseFilters;
  } | null>(null);

  /**
   * 清除错误信息
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  /**
   * 获取学生的课程列表
   */
  const fetchStudentCourses = useCallback(
    async (studentId: string, filters?: CourseFilters) => {
      try {
        setIsLoading(true);
        setError(null);

        const data = await courseApi.getStudentCourses(studentId, filters);
        setCourses(data);
        setLastFetch({ type: "student", studentId, filters });
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "获取课程列表失败";
        setError(errorMessage);
        setCourses([]);
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  /**
   * 获取可选课程列表
   */
  const fetchAvailableCourses = useCallback(
    async (studentId: string, filters?: CourseFilters) => {
      try {
        setIsLoading(true);
        setError(null);

        const data = await courseApi.getAvailableCourses(studentId, filters);
        setCourses(data);
        setLastFetch({ type: "available", studentId, filters });
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "获取可选课程失败";
        setError(errorMessage);
        setCourses([]);
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  /**
   * 学生选课
   */
  const selectCourse = useCallback(
    async (studentId: string, teachingClassId: string): Promise<boolean> => {
      try {
        setError(null);
        await courseApi.selectCourse(studentId, teachingClassId);

        // 选课成功后刷新课程列表
        if (lastFetch) {
          if (lastFetch.type === "student" && lastFetch.studentId) {
            await fetchStudentCourses(lastFetch.studentId, lastFetch.filters);
          } else if (lastFetch.type === "available" && lastFetch.studentId) {
            await fetchAvailableCourses(lastFetch.studentId, lastFetch.filters);
          }
        }

        return true;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "选课失败";
        setError(errorMessage);
        return false;
      }
    },
    [lastFetch, fetchStudentCourses, fetchAvailableCourses],
  );

  /**
   * 学生退课
   */
  const dropCourse = useCallback(
    async (studentId: string, teachingClassId: string): Promise<boolean> => {
      try {
        setError(null);
        await courseApi.dropCourse(studentId, teachingClassId);

        // 退课成功后刷新课程列表
        if (lastFetch) {
          if (lastFetch.type === "student" && lastFetch.studentId) {
            await fetchStudentCourses(lastFetch.studentId, lastFetch.filters);
          } else if (lastFetch.type === "available" && lastFetch.studentId) {
            await fetchAvailableCourses(lastFetch.studentId, lastFetch.filters);
          }
        }

        return true;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "退课失败";
        setError(errorMessage);
        return false;
      }
    },
    [lastFetch, fetchStudentCourses, fetchAvailableCourses],
  );

  /**
   * 刷新当前课程列表
   */
  const refreshCourses = useCallback(async () => {
    if (!lastFetch) return;

    if (lastFetch.type === "student" && lastFetch.studentId) {
      await fetchStudentCourses(lastFetch.studentId, lastFetch.filters);
    } else if (lastFetch.type === "available" && lastFetch.studentId) {
      await fetchAvailableCourses(lastFetch.studentId, lastFetch.filters);
    }
  }, [lastFetch, fetchStudentCourses, fetchAvailableCourses]);

  return {
    courses,
    isLoading,
    error,
    fetchStudentCourses,
    fetchAvailableCourses,
    selectCourse,
    dropCourse,
    clearError,
    refreshCourses,
  };
}
