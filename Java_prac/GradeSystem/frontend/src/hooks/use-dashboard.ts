import { useState, useCallback } from "react";
import { dashboardApi } from "@/api/v1/modules/dashboard";
import {
  type CardData,
  type CardDataResponse,
  mapCardDataResponse,
} from "@/types/card-data";

interface UseDashboardReturn {
  cards: CardData[];
  loading: boolean;
  error: string | null;

  getAdminDashboard: () => Promise<void>;
  getStudentDashboard: (studentId: string) => Promise<void>;
  getTeacherDashboard: (teacherId: string) => Promise<void>;
}

export function useDashboard(): UseDashboardReturn {
  const [cards, setCards] = useState<CardData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getAdminDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = (await dashboardApi.getAdminDashboard()) as CardDataResponse[];
      setCards(data.map(mapCardDataResponse));
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "获取管理员仪表盘失败";
      setError(message);
      console.error("获取管理员仪表盘失败:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  const getStudentDashboard = useCallback(async (studentId: string) => {
    setLoading(true);
    setError(null);
    try {
      const data = (await dashboardApi.getStudentDashboard(
        studentId
      )) as CardDataResponse[];
      setCards(data.map(mapCardDataResponse));
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "获取学生仪表盘失败";
      setError(message);
      console.error("获取学生仪表盘失败:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  const getTeacherDashboard = useCallback(async (teacherId: string) => {
    setLoading(true);
    setError(null);
    try {
      const data = (await dashboardApi.getTeacherDashboard(
        teacherId
      )) as CardDataResponse[];
      setCards(data.map(mapCardDataResponse));
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "获取教师仪表盘失败";
      setError(message);
      console.error("获取教师仪表盘失败:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    cards,
    loading,
    error,
    getAdminDashboard,
    getStudentDashboard,
    getTeacherDashboard,
  };
}
