import type { Course } from "./course";
import type { Teacher } from "./teacher";

export interface TeachingClass {
  id: string;
  name?: string;
  teacher: Teacher;
  course: Course;
}
