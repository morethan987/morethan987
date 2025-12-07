import type { Course } from "./course";
import type { Teacher } from "./teacher";

export interface TeachingClass {
  id: string;
  name?: string;
  semester?: string;
  teacher: Teacher;
  course: Course;
}
