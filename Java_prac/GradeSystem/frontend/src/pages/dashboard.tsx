import { useState } from "react";
import { AppSidebar } from "@/components/app-sidebar";
import { GeneralView } from "./general";
import { SettingsView } from "./settings";
import {
  StudentCourseView,
  StudentGradeView,
  StudentSelectCourseView,
} from "./student";
import { TeacherCourses, TeacherGradeInput, TeacherGradeView } from "./teacher";
import { NotFoundPage } from "./not-found";
import { SiteHeader } from "@/components/site-header";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import type {
  StudentGrade,
  DistributionData,
} from "@/types/teacher-grade-view";
import { PAGE_IDS } from "@/types/page-ids";

// 成绩分布数据样例
const gradeDistribution: DistributionData[] = [
  { range: "90-100", count: 5 },
  { range: "80-89", count: 10 },
  { range: "70-79", count: 8 },
  { range: "60-69", count: 3 },
  { range: "0-59", count: 1 },
];

const mockGrades: StudentGrade[] = [
  {
    id: "1",
    studentCode: "2021001",
    name: "张三",
    className: "计算机科学与技术1班",
    usualScore: 85,
    midtermScore: 88,
    finalExamScore: 90,
    experimentScore: 92,
    finalScore: 88.5,
    gpa: 3.85,
  },
  {
    id: "2",
    studentCode: "2021002",
    name: "李四",
    className: "计算机科学与技术1班",
    usualScore: 78,
    midtermScore: 82,
    finalExamScore: 85,
    experimentScore: 80,
    finalScore: 81.5,
    gpa: 3.35,
  },
  {
    id: "3",
    studentCode: "2021003",
    name: "王五",
    className: "计算机科学与技术1班",
    usualScore: 92,
    midtermScore: 90,
    finalExamScore: 95,
    experimentScore: 88,
    finalScore: 91.5,
    gpa: 4.0,
  },
  {
    id: "4",
    studentCode: "2021004",
    name: "赵六",
    className: "计算机科学与技术1班",
    usualScore: 65,
    midtermScore: 70,
    finalExamScore: 68,
    experimentScore: 72,
    finalScore: 68.5,
    gpa: 2.35,
  },
  {
    id: "5",
    studentCode: "2021005",
    name: "钱七",
    className: "计算机科学与技术1班",
    usualScore: 88,
    midtermScore: 85,
    finalExamScore: 87,
    experimentScore: 90,
    finalScore: 87.0,
    gpa: 3.7,
  },
  {
    id: "6",
    studentCode: "2021006",
    name: "孙八",
    className: "计算机科学与技术1班",
    usualScore: 75,
    midtermScore: 78,
    finalExamScore: 80,
    experimentScore: 76,
    finalScore: 77.5,
    gpa: 2.85,
  },
  {
    id: "7",
    studentCode: "2021007",
    name: "周九",
    className: "计算机科学与技术1班",
    usualScore: 95,
    midtermScore: 92,
    finalExamScore: 93,
    experimentScore: 95,
    finalScore: 93.5,
    gpa: 4.0,
  },
  {
    id: "8",
    studentCode: "2021008",
    name: "吴十",
    className: "计算机科学与技术1班",
    usualScore: 58,
    midtermScore: 62,
    finalExamScore: 60,
    experimentScore: 65,
    finalScore: 61.0,
    gpa: 1.85,
  },
  {
    id: "9",
    studentCode: "2021009",
    name: "郑十一",
    className: "计算机科学与技术1班",
    usualScore: 82,
    midtermScore: 80,
    finalExamScore: 84,
    experimentScore: 85,
    finalScore: 82.5,
    gpa: 3.25,
  },
  {
    id: "10",
    studentCode: "2021010",
    name: "王十二",
    className: "计算机科学与技术1班",
    usualScore: 72,
    midtermScore: 75,
    finalExamScore: 73,
    experimentScore: 74,
    finalScore: 73.5,
    gpa: 2.55,
  },
];

export function Dashboard() {
  const [currentPage, setCurrentPage] = useState<string>(PAGE_IDS.GENERAL);

  const renderContent = () => {
    switch (currentPage) {
      case PAGE_IDS.GENERAL:
        return <GeneralView />;
      case PAGE_IDS.SETTINGS:
        return <SettingsView />;
      case PAGE_IDS.STU_GRADES:
        return <StudentGradeView />;
      case PAGE_IDS.STU_COURSES:
        return <StudentCourseView />;
      case PAGE_IDS.STU_SELECT_COURSES:
        return <StudentSelectCourseView />;
      case PAGE_IDS.TEA_COURSES:
        return <TeacherCourses />;
      case PAGE_IDS.TEA_GRADE_INPUT:
        return <TeacherGradeInput />;
      case PAGE_IDS.TEA_GRADE_VIEW:
        return (
          <TeacherGradeView
            distribution={gradeDistribution}
            grades={mockGrades}
          />
        );
      default:
        return <NotFoundPage />;
    }
  };

  return (
    <SidebarProvider
      style={
        {
          "--sidebar-width": "calc(var(--spacing) * 72)",
          "--header-height": "calc(var(--spacing) * 12)",
        } as React.CSSProperties
      }
    >
      <AppSidebar
        variant="inset"
        onPageChange={setCurrentPage}
        currentPage={currentPage}
      />
      <SidebarInset>
        <SiteHeader />
        {renderContent()}
      </SidebarInset>
    </SidebarProvider>
  );
}
