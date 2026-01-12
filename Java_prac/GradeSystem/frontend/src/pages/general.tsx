import { useEffect, useState } from "react";
import { GenericAreaChart } from "@/components/chart-area-interactive";
import { SectionCards } from "@/components/section-cards";
import { DataTable } from "@/components/data-table";
import data from "./data.json";
import { useAuthContext } from "@/contexts/auth-context";
import { useDashboard } from "@/hooks/use-dashboard";
import { studentApi } from "@/api/v1/modules/student";
import { teacherApi } from "@/api/v1/modules/teacher";

// TODO: Replace with real data from API
const sampleChartData = {
  title: "学业成绩趋势",
  description: "个人绩点与班级平均分对比",
  xAxisKey: "semester",
  series: [
    { key: "gpa", label: "个人 GPA" },
    { key: "class_avg", label: "班级平均" },
  ],
  data: [
    { semester: "2022 秋", gpa: 3.2, class_avg: 3.0 },
    { semester: "2023 春", gpa: 3.5, class_avg: 3.1 },
    { semester: "2023 秋", gpa: 3.4, class_avg: 3.2 },
    { semester: "2024 春", gpa: 3.8, class_avg: 3.3 },
    { semester: "2024 秋", gpa: 3.9, class_avg: 3.2 },
  ],
};

function getUserRole(roles: string[]): "admin" | "teacher" | "student" | null {
  if (roles.includes("ADMIN")) return "admin";
  if (roles.includes("TEACHER")) return "teacher";
  if (roles.includes("STUDENT")) return "student";
  return null;
}

export function GeneralView() {
  const { user } = useAuthContext();
  const {
    cards,
    loading,
    error,
    getAdminDashboard,
    getStudentDashboard,
    getTeacherDashboard,
  } = useDashboard();
  const [initError, setInitError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) return;

    const role = getUserRole(user.roles);
    setInitError(null);

    const fetchDashboard = async () => {
      try {
        if (role === "admin") {
          await getAdminDashboard();
        } else if (role === "student") {
          const student = await studentApi.getStudentByUserId(user.id);
          await getStudentDashboard(student.id);
        } else if (role === "teacher") {
          const teacher = await teacherApi.getTeacherByUserId(user.id);
          await getTeacherDashboard(teacher.id);
        }
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "获取仪表盘数据失败";
        setInitError(message);
        console.error("获取仪表盘数据失败:", err);
      }
    };

    fetchDashboard();
  }, [user, getAdminDashboard, getStudentDashboard, getTeacherDashboard]);

  const displayError = error || initError;

  if (loading) {
    return (
      <div className="flex flex-1 items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p>正在加载仪表盘数据...</p>
        </div>
      </div>
    );
  }

  if (displayError) {
    return (
      <div className="flex flex-1 items-center justify-center">
        <div className="text-center text-destructive">
          <p>加载失败: {displayError}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-1 flex-col">
      <div className="@container/main flex flex-1 flex-col gap-2">
        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
          <SectionCards cardsData={cards} />
          <div className="px-4 lg:px-6">
            <GenericAreaChart chartData={sampleChartData} />
          </div>
          <DataTable data={data} />
        </div>
      </div>
    </div>
  );
}
