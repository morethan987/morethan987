import {
  IconDashboard,
  IconReport,
  IconSettings,
  IconCalendarWeek,
  IconClick,
} from "@tabler/icons-react";
import { PAGE_IDS } from "@/types/page-ids";

export const ui_config = {
  navMain: [
    {
      id: PAGE_IDS.GENERAL,
      title: "总体概览",
      url: "#",
      icon: IconDashboard,
    },
  ],
  navSecondary: [
    {
      id: PAGE_IDS.SETTINGS,
      title: "系统设置",
      url: "#",
      icon: IconSettings,
    },
  ],
  student_basic: [
    {
      id: PAGE_IDS.STU_GRADES,
      name: "我的成绩",
      url: "#",
      icon: IconReport,
    },
    {
      id: PAGE_IDS.STU_COURSES,
      name: "我的课程",
      url: "#",
      icon: IconCalendarWeek,
    },
    {
      id: PAGE_IDS.STU_SELECT_COURSES,
      name: "选择课程",
      url: "#",
      icon: IconClick,
    },
  ],
  teacher_basic: [
    {
      id: PAGE_IDS.TEA_GRADE_VIEW,
      name: "成绩详情",
      url: "#",
      icon: IconReport,
    },
    {
      id: PAGE_IDS.TEA_COURSES,
      name: "我的课程",
      url: "#",
      icon: IconCalendarWeek,
    },
    {
      id: PAGE_IDS.TEA_GRADE_INPUT,
      name: "录入成绩",
      url: "#",
      icon: IconClick,
    },
  ],
  // TODO: Admin UI config
  admin_basic: [
    {
      id: PAGE_IDS.STU_GRADES,
      name: "我的成绩",
      url: "#",
      icon: IconReport,
    },
    {
      id: PAGE_IDS.STU_COURSES,
      name: "我的课程",
      url: "#",
      icon: IconCalendarWeek,
    },
    {
      id: PAGE_IDS.STU_SELECT_COURSES,
      name: "选择课程",
      url: "#",
      icon: IconClick,
    },
  ],
};

export const getUiData = (uiType: string) => {
  switch (uiType) {
    case "DEFAULT":
      return ui_config.teacher_basic;
    case "STUDENT":
      return ui_config.student_basic;
    case "TEACHER":
      return ui_config.teacher_basic;
    case "ADMIN":
      return ui_config.admin_basic;
    default:
      return [];
  }
};
