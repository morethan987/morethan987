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
import { PAGE_IDS } from "@/types/page-ids";

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
        return <TeacherGradeView />;
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
