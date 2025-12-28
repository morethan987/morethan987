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

/**
 * 教学班状态枚举
 * 对应后端 TeachingClassStatus
 */
export enum TeachingClassStatus {
  PLANNED = "planned",
  OPEN_FOR_ENROLLMENT = "open_for_enrollment",
  ENROLLMENT_CLOSED = "enrollment_closed",
  ACTIVE = "active",
  COMPLETED = "completed",
  CANCELLED = "cancelled",
  SUSPENDED = "suspended",
  MERGED = "merged",
}

/**
 * 教学班状态描述
 */
export const TeachingClassStatusDescriptions: Record<
  TeachingClassStatus,
  string
> = {
  [TeachingClassStatus.PLANNED]: "计划中",
  [TeachingClassStatus.OPEN_FOR_ENROLLMENT]: "开放选课",
  [TeachingClassStatus.ENROLLMENT_CLOSED]: "选课结束",
  [TeachingClassStatus.ACTIVE]: "进行中",
  [TeachingClassStatus.COMPLETED]: "已完成",
  [TeachingClassStatus.CANCELLED]: "已取消",
  [TeachingClassStatus.SUSPENDED]: "暂停",
  [TeachingClassStatus.MERGED]: "合并",
};

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
  status: TeachingClassStatus;
  semesterName: string;
}
