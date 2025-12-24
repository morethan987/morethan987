import type { Course } from "./course";
import type { Student } from "./student";

export interface Grade {
  id: string;
  student: Student;
  course: Course;
  usualScore?: number;
  midtermScore?: number;
  finalExamScore?: number;
  experimentScore?: number;
  finalScore?: number;
  gpa?: number;
}
