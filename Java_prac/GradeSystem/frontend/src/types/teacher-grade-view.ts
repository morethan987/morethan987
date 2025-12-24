export interface StudentGrade {
  id: string;
  studentCode: string;
  name: string;
  className: string;
  usualScore: number;
  midtermScore: number;
  finalExamScore: number;
  experimentScore: number;
  finalScore: number;
  gpa: number;
}

export interface DistributionData {
  range: string;
  count: number;
}

export interface TeacherGradeViewProps {
  distribution: DistributionData[];
  grades: StudentGrade[];
}

export type SortField = keyof StudentGrade;
export type SortOrder = "asc" | "desc" | null;
