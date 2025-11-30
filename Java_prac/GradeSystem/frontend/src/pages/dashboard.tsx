import { useState } from "react";
import { AppSidebar } from "@/components/app-sidebar";
import { GeneralData } from "./general";
import { SettingsData } from "./settings";
import { SiteHeader } from "@/components/site-header";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import { PAGE_IDS } from "@/types/page-ids";

export function Dashboard() {
  const [currentPage, setCurrentPage] = useState<string>(PAGE_IDS.GENERAL);

  const renderContent = () => {
    switch (currentPage) {
      case PAGE_IDS.GENERAL:
        return <GeneralData />;
      case PAGE_IDS.SETTINGS:
        return <SettingsData />;
      default:
        return <GeneralData />;
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
