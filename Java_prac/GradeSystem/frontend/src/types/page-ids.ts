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

  // Documents
  DATA_LIBRARY: "data-library",
  REPORTS: "reports",
  WORD_ASSISTANT: "word-assistant",
} as const;

// Page types for type safety
export type PageId = (typeof PAGE_IDS)[keyof typeof PAGE_IDS];
