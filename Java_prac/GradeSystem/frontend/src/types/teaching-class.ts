export interface TeachingClassWithStats {
  id: string;
  className: string;
  courseName: string;
  courseType?: string;
  credit?: number;
  studentCount?: number;
  semester?: string;
  schedule?: string;
  location?: string;
}

export interface StudentGradeInput {
  id?: string;
  studentCode: string;
  name?: string;
  className?: string;
  usualScore?: number | null;
  midtermScore?: number | null;
  finalExamScore?: number | null;
  experimentScore?: number | null;
  finalScore?: number | null;
  gpa?: number | null;
  isModified?: boolean;
  version?: number;
}

export interface DistributionData {
  range: string;
  count: number;
}

export interface BatchGradeUpdateRequest {
  grades: StudentGradeInput[];
}

export interface BatchGradeUpdateResponse {
  success: boolean;
  updatedCount: number;
  errors?: string[];
}

export const COURSE_TYPE_MAP: Record<string, string> = {
  REQUIRED: "必修课",
  ELECTIVE: "选修课",
  LIMITED_ELECTIVE: "限选课",
  GENERAL: "通识课",
  PROFESSIONAL: "专业课",
};
