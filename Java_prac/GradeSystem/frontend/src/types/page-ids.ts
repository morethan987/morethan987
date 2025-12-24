// Page ID constants for the application
export const PAGE_IDS = {
  // Main navigation pages
  DASHBOARD: "dashboard",
  LIFECYCLE: "lifecycle",
  ANALYTICS: "analytics",
  PROJECTS: "projects",
  TEAM: "team",

  // Content pages
  GENERAL: "general",
  SETTINGS: "settings",

  // Cloud/Categories
  CAPTURE: "capture",
  PROPOSAL: "proposals",
  PROMPTS: "prompts",

  // Basic
  STU_GRADES: "student-grades",
  STU_COURSES: "student-courses",
  STU_SELECT_COURSES: "student-select-courses",
  TEA_COURSES: "teacher-courses",
  TEA_GRADE_VIEW: "teacher-grade-view",
  TEA_GRADE_INPUT: "teacher-grade-input",
} as const;

// Page types for type safety
export type PageId = (typeof PAGE_IDS)[keyof typeof PAGE_IDS];
