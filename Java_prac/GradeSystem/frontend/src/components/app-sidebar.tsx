import * as React from "react";
import { IconInnerShadowTop } from "@tabler/icons-react";
import { NavBasic } from "@/components/nav-basic";
import { NavMain } from "@/components/nav-main";
import { NavSecondary } from "@/components/nav-secondary";
import { NavUser } from "@/components/nav-user";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";
import { ROUTES } from "@/routes";
import { useAuthContext } from "@/contexts/auth-context";
import { ui_config, getUiData } from "@/types/sidebar-config";

export function AppSidebar({
  onPageChange,
  currentPage,
  ...props
}: React.ComponentProps<typeof Sidebar> & {
  onPageChange?: (page: string) => void;
  currentPage?: string;
}) {
  const { user, logout } = useAuthContext();

  return (
    <Sidebar collapsible="offcanvas" {...props}>
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              asChild
              className="data-[slot=sidebar-menu-button]:p-1.5!"
            >
              <a href={ROUTES.DASHBOARD}>
                <IconInnerShadowTop className="size-5!" />
                <span className="text-base font-semibold">Grade System</span>
              </a>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <NavMain
          items={ui_config.navMain}
          onPageChange={onPageChange}
          currentPage={currentPage}
        />
        <NavBasic
          items={getUiData(user?.uiType as string)}
          onPageChange={onPageChange}
          currentPage={currentPage}
        />
        <NavSecondary
          items={ui_config.navSecondary}
          className="mt-auto"
          onPageChange={onPageChange}
          currentPage={currentPage}
        />
      </SidebarContent>
      <SidebarFooter>
        <NavUser user={user} logout={logout} />
      </SidebarFooter>
    </Sidebar>
  );
}
