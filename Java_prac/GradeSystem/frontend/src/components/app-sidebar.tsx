import * as React from "react";
import {
  IconCamera,
  IconChartBar,
  IconDashboard,
  IconDatabase,
  IconFileAi,
  IconFileDescription,
  IconFileWord,
  IconFolder,
  IconHelp,
  IconInnerShadowTop,
  IconListDetails,
  IconReport,
  IconSearch,
  IconSettings,
  IconUsers,
} from "@tabler/icons-react";

import { NavDocuments } from "@/components/nav-documents";
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
import { PAGE_IDS } from "@/types/page-ids";

const data = {
  user: {
    name: "shadcn",
    email: "m@example.com",
    avatar: "/avatars/shadcn.jpg",
  },
  navMain: [
    {
      id: PAGE_IDS.GENERAL,
      title: "总体概览",
      url: "#",
      icon: IconDashboard,
    },
    // {
    //   id: PAGE_IDS.LIFECYCLE,
    //   title: "Lifecycle",
    //   url: "#",
    //   icon: IconListDetails,
    // },
    // {
    //   id: PAGE_IDS.ANALYTICS,
    //   title: "Analytics",
    //   url: "#",
    //   icon: IconChartBar,
    // },
    // {
    //   id: PAGE_IDS.PROJECTS,
    //   title: "Projects",
    //   url: "#",
    //   icon: IconFolder,
    // },
    // {
    //   id: PAGE_IDS.TEAM,
    //   title: "Team",
    //   url: "#",
    //   icon: IconUsers,
    // },
  ],
  navClouds: [
    {
      id: PAGE_IDS.CAPTURE,
      title: "Capture",
      icon: IconCamera,
      isActive: true,
      url: "#",
      items: [
        {
          title: "Active Proposals",
          url: "#",
        },
        {
          title: "Archived",
          url: "#",
        },
      ],
    },
    {
      id: PAGE_IDS.PROPOSAL,
      title: "Proposal",
      icon: IconFileDescription,
      url: "#",
      items: [
        {
          title: "Active Proposals",
          url: "#",
        },
        {
          title: "Archived",
          url: "#",
        },
      ],
    },
    {
      id: PAGE_IDS.PROMPTS,
      title: "Prompts",
      icon: IconFileAi,
      url: "#",
      items: [
        {
          title: "Active Proposals",
          url: "#",
        },
        {
          title: "Archived",
          url: "#",
        },
      ],
    },
  ],
  navSecondary: [
    {
      id: PAGE_IDS.SETTINGS,
      title: "设置",
      url: "#",
      icon: IconSettings,
    },
    // {
    //   title: "Get Help",
    //   url: "#",
    //   icon: IconHelp,
    // },
    // {
    //   title: "Search",
    //   url: "#",
    //   icon: IconSearch,
    // },
  ],
  documents: [
    {
      id: PAGE_IDS.DATA_LIBRARY,
      name: "Data Library",
      url: "#",
      icon: IconDatabase,
    },
    {
      id: PAGE_IDS.REPORTS,
      name: "Reports",
      url: "#",
      icon: IconReport,
    },
    {
      id: PAGE_IDS.WORD_ASSISTANT,
      name: "Word Assistant",
      url: "#",
      icon: IconFileWord,
    },
  ],
};

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
          items={data.navMain}
          onPageChange={onPageChange}
          currentPage={currentPage}
        />
        <NavDocuments items={data.documents} />
        <NavSecondary
          items={data.navSecondary}
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
