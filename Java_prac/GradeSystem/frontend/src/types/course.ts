export enum CourseType {
  REQUIRED = "REQUIRED",
  ELECTIVE = "ELECTIVE",
  LIMITED_ELECTIVE = "LIMITED_ELECTIVE",
  GENERAL = "GENERAL",
  PROFESSIONAL = "PROFESSIONAL",
}

export const CourseTypeDescriptions: Record<CourseType, string> = {
  [CourseType.REQUIRED]: "必修课",
  [CourseType.ELECTIVE]: "选修课",
  [CourseType.LIMITED_ELECTIVE]: "限选课",
  [CourseType.GENERAL]: "通识课",
  [CourseType.PROFESSIONAL]: "专业课",
};

export interface Course {
  id: string;
  name: string;
  description?: string;
  credit: number;
  semester: number;
  courseType: CourseType;
}

// Extended course interface for teaching class information
export interface TeachingClass {
  id: string;
  name?: string;
  course: Course;
  teacherName: string;
  classroom: string;
  timeSchedule: string;
  capacity: number;
  enrolled: number; // 当前已选人数
  status: "ongoing" | "completed" | "upcoming";
  semesterName: string;
}
