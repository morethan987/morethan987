import type { User } from "./user";

/**
 * 教师职称枚举
 * 定义教师在学校系统中的各种职称等级
 */
export enum TeacherTitle {
  /** 教授 - 最高职称 */
  PROFESSOR = "PROFESSOR",
  /** 副教授 - 高级职称 */
  ASSOCIATE_PROFESSOR = "ASSOCIATE_PROFESSOR",
  /** 助理教授 - 中级职称 */
  ASSISTANT_PROFESSOR = "ASSISTANT_PROFESSOR",
  /** 讲师 - 基础职称 */
  LECTURER = "LECTURER",
  /** 助教 - 初级职称 */
  TEACHING_ASSISTANT = "TEACHING_ASSISTANT",
  /** 研究教授 - 专门从事研究的教授 */
  RESEARCH_PROFESSOR = "RESEARCH_PROFESSOR",
  /** 临床教授 - 医学院等专业的临床教授 */
  CLINICAL_PROFESSOR = "CLINICAL_PROFESSOR",
  /** 兼职教授 - 外聘教授 */
  ADJUNCT_PROFESSOR = "ADJUNCT_PROFESSOR",
  /** 荣誉教授 - 退休或特聘的荣誉职称 */
  EMERITUS_PROFESSOR = "EMERITUS_PROFESSOR",
  /** 客座教授 - 短期聘请的教授 */
  VISITING_PROFESSOR = "VISITING_PROFESSOR",
  /** 首席教授 - 特殊荣誉职称 */
  DISTINGUISHED_PROFESSOR = "DISTINGUISHED_PROFESSOR",
}

/**
 * 教师状态枚举
 * 定义教师在学校系统中的各种工作状态
 */
export enum TeacherStatus {
  /** 在职 - 正常工作状态 */
  ACTIVE = "ACTIVE",
  /** 休假 - 临时离岗但保留职位 */
  ON_LEAVE = "ON_LEAVE",
  /** 停薪留职 - 暂停工作但保留职位 */
  UNPAID_LEAVE = "UNPAID_LEAVE",
  /** 退休 - 正常退休 */
  RETIRED = "RETIRED",
  /** 辞职 - 主动离职 */
  RESIGNED = "RESIGNED",
  /** 解雇 - 被学校解雇 */
  TERMINATED = "TERMINATED",
  /** 调动 - 调到其他部门或学校 */
  TRANSFERRED = "TRANSFERRED",
  /** 临时聘用 - 短期合同教师 */
  TEMPORARY = "TEMPORARY",
  /** 兼职 - 非全职教师 */
  PART_TIME = "PART_TIME",
  /** 访问学者 - 临时来校的访问教师 */
  VISITING = "VISITING",
  /** 暂停 - 因违纪等原因暂停工作 */
  SUSPENDED = "SUSPENDED",
}

/**
 * 教师职称信息
 */
export interface TeacherTitleInfo {
  title: TeacherTitle;
  description: string;
  level: number;
  isProfessorLevel: boolean;
  isPermanent: boolean;
  isTemporary: boolean;
  canTeachIndependently: boolean;
  canSuperviseDoctorate: boolean;
  canSuperviseMaster: boolean;
}

/**
 * 教师状态信息
 */
export interface TeacherStatusInfo {
  status: TeacherStatus;
  description: string;
  isActive: boolean;
  isLeft: boolean;
  isTemporary: boolean;
  canTeach: boolean;
  hasSalary: boolean;
}

/**
 * 教师实体接口
 */
export interface Teacher {
  id: string;
  user: User;
  employeeCode: string;
  department?: string;
  title: TeacherTitle;
  specialization?: string;
  hireDate?: string;
  status: TeacherStatus;
  salary?: number;
  workload?: number;
  maxCourses?: number;
  office?: string;
  officePhone?: string;
  officeHours?: string;
  qualifications?: string;
  researchInterests?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 创建教师请求
 */
export interface CreateTeacherRequest {
  userId: string;
  employeeCode: string;
  department?: string;
  title: TeacherTitle;
  specialization?: string;
  hireDate?: string;
  salary?: number;
  maxCourses?: number;
  office?: string;
  officePhone?: string;
  officeHours?: string;
  qualifications?: string;
  researchInterests?: string;
}

/**
 * 更新教师请求
 */
export interface UpdateTeacherRequest {
  department?: string;
  title?: TeacherTitle;
  specialization?: string;
  status?: TeacherStatus;
  salary?: number;
  workload?: number;
  maxCourses?: number;
  office?: string;
  officePhone?: string;
  officeHours?: string;
  qualifications?: string;
  researchInterests?: string;
}

/**
 * 教师查询参数
 */
export interface TeacherQueryParams {
  department?: string;
  title?: TeacherTitle;
  status?: TeacherStatus;
  specialization?: string;
  keyword?: string;
  page?: number;
  limit?: number;
  sortBy?: "name" | "hireDate" | "title" | "department";
  sortOrder?: "asc" | "desc";
}

/**
 * 教师职称描述映射
 */
export const TEACHER_TITLE_DESCRIPTIONS: Record<TeacherTitle, string> = {
  [TeacherTitle.PROFESSOR]: "教授",
  [TeacherTitle.ASSOCIATE_PROFESSOR]: "副教授",
  [TeacherTitle.ASSISTANT_PROFESSOR]: "助理教授",
  [TeacherTitle.LECTURER]: "讲师",
  [TeacherTitle.TEACHING_ASSISTANT]: "助教",
  [TeacherTitle.RESEARCH_PROFESSOR]: "研究教授",
  [TeacherTitle.CLINICAL_PROFESSOR]: "临床教授",
  [TeacherTitle.ADJUNCT_PROFESSOR]: "兼职教授",
  [TeacherTitle.EMERITUS_PROFESSOR]: "荣誉教授",
  [TeacherTitle.VISITING_PROFESSOR]: "客座教授",
  [TeacherTitle.DISTINGUISHED_PROFESSOR]: "首席教授",
};

/**
 * 教师状态描述映射
 */
export const TEACHER_STATUS_DESCRIPTIONS: Record<TeacherStatus, string> = {
  [TeacherStatus.ACTIVE]: "在职",
  [TeacherStatus.ON_LEAVE]: "休假",
  [TeacherStatus.UNPAID_LEAVE]: "停薪留职",
  [TeacherStatus.RETIRED]: "退休",
  [TeacherStatus.RESIGNED]: "辞职",
  [TeacherStatus.TERMINATED]: "解雇",
  [TeacherStatus.TRANSFERRED]: "调动",
  [TeacherStatus.TEMPORARY]: "临时聘用",
  [TeacherStatus.PART_TIME]: "兼职",
  [TeacherStatus.VISITING]: "访问学者",
  [TeacherStatus.SUSPENDED]: "暂停",
};

/**
 * 教师工具函数类
 */
export class TeacherUtils {
  /**
   * 获取职称等级（数字越高职称越高）
   */
  static getTitleLevel(title: TeacherTitle): number {
    const levelMap: Record<TeacherTitle, number> = {
      [TeacherTitle.DISTINGUISHED_PROFESSOR]: 11,
      [TeacherTitle.PROFESSOR]: 10,
      [TeacherTitle.EMERITUS_PROFESSOR]: 10,
      [TeacherTitle.RESEARCH_PROFESSOR]: 9,
      [TeacherTitle.CLINICAL_PROFESSOR]: 9,
      [TeacherTitle.ASSOCIATE_PROFESSOR]: 8,
      [TeacherTitle.VISITING_PROFESSOR]: 7,
      [TeacherTitle.ASSISTANT_PROFESSOR]: 6,
      [TeacherTitle.ADJUNCT_PROFESSOR]: 5,
      [TeacherTitle.LECTURER]: 4,
      [TeacherTitle.TEACHING_ASSISTANT]: 3,
    };
    return levelMap[title] || 0;
  }

  /**
   * 检查是否为教授级别
   */
  static isProfessorLevel(title: TeacherTitle): boolean {
    return [
      TeacherTitle.PROFESSOR,
      TeacherTitle.RESEARCH_PROFESSOR,
      TeacherTitle.CLINICAL_PROFESSOR,
      TeacherTitle.EMERITUS_PROFESSOR,
      TeacherTitle.VISITING_PROFESSOR,
      TeacherTitle.ADJUNCT_PROFESSOR,
      TeacherTitle.DISTINGUISHED_PROFESSOR,
    ].includes(title);
  }

  /**
   * 检查是否为正式编制教师
   */
  static isPermanentTitle(title: TeacherTitle): boolean {
    return [
      TeacherTitle.PROFESSOR,
      TeacherTitle.ASSOCIATE_PROFESSOR,
      TeacherTitle.ASSISTANT_PROFESSOR,
      TeacherTitle.LECTURER,
      TeacherTitle.TEACHING_ASSISTANT,
      TeacherTitle.RESEARCH_PROFESSOR,
      TeacherTitle.CLINICAL_PROFESSOR,
      TeacherTitle.DISTINGUISHED_PROFESSOR,
    ].includes(title);
  }

  /**
   * 检查是否为临时或访问教师
   */
  static isTemporaryTitle(title: TeacherTitle): boolean {
    return [
      TeacherTitle.VISITING_PROFESSOR,
      TeacherTitle.ADJUNCT_PROFESSOR,
      TeacherTitle.EMERITUS_PROFESSOR,
    ].includes(title);
  }

  /**
   * 检查是否有独立授课资格
   */
  static canTeachIndependently(title: TeacherTitle): boolean {
    return title !== TeacherTitle.TEACHING_ASSISTANT;
  }

  /**
   * 检查是否有研究生指导资格
   */
  static canSuperviseDoctorate(title: TeacherTitle): boolean {
    return (
      this.isProfessorLevel(title) && title !== TeacherTitle.ADJUNCT_PROFESSOR
    );
  }

  /**
   * 检查是否有硕士生指导资格
   */
  static canSuperviseMaster(title: TeacherTitle): boolean {
    return this.getTitleLevel(title) >= 6; // 助理教授及以上
  }

  /**
   * 获取下一个可晋升的职称
   */
  static getNextTitle(title: TeacherTitle): TeacherTitle | null {
    const promotionMap: Record<TeacherTitle, TeacherTitle | null> = {
      [TeacherTitle.TEACHING_ASSISTANT]: TeacherTitle.LECTURER,
      [TeacherTitle.LECTURER]: TeacherTitle.ASSISTANT_PROFESSOR,
      [TeacherTitle.ASSISTANT_PROFESSOR]: TeacherTitle.ASSOCIATE_PROFESSOR,
      [TeacherTitle.ASSOCIATE_PROFESSOR]: TeacherTitle.PROFESSOR,
      [TeacherTitle.PROFESSOR]: TeacherTitle.DISTINGUISHED_PROFESSOR,
      [TeacherTitle.RESEARCH_PROFESSOR]: null,
      [TeacherTitle.CLINICAL_PROFESSOR]: null,
      [TeacherTitle.ADJUNCT_PROFESSOR]: null,
      [TeacherTitle.EMERITUS_PROFESSOR]: null,
      [TeacherTitle.VISITING_PROFESSOR]: null,
      [TeacherTitle.DISTINGUISHED_PROFESSOR]: null,
    };
    return promotionMap[title] || null;
  }

  /**
   * 比较职称高低
   */
  static isHigherTitle(title1: TeacherTitle, title2: TeacherTitle): boolean {
    return this.getTitleLevel(title1) > this.getTitleLevel(title2);
  }

  /**
   * 检查教师是否为活跃状态（可以正常教学）
   */
  static isActiveStatus(status: TeacherStatus): boolean {
    return [
      TeacherStatus.ACTIVE,
      TeacherStatus.TEMPORARY,
      TeacherStatus.PART_TIME,
      TeacherStatus.VISITING,
    ].includes(status);
  }

  /**
   * 检查教师是否已离职
   */
  static isLeftStatus(status: TeacherStatus): boolean {
    return [
      TeacherStatus.RETIRED,
      TeacherStatus.RESIGNED,
      TeacherStatus.TERMINATED,
      TeacherStatus.TRANSFERRED,
    ].includes(status);
  }

  /**
   * 检查是否为临时状态
   */
  static isTemporaryStatus(status: TeacherStatus): boolean {
    return [
      TeacherStatus.ON_LEAVE,
      TeacherStatus.UNPAID_LEAVE,
      TeacherStatus.SUSPENDED,
      TeacherStatus.TEMPORARY,
      TeacherStatus.VISITING,
    ].includes(status);
  }

  /**
   * 检查是否可以分配教学任务
   */
  static canTeach(status: TeacherStatus): boolean {
    return [
      TeacherStatus.ACTIVE,
      TeacherStatus.TEMPORARY,
      TeacherStatus.PART_TIME,
      TeacherStatus.VISITING,
    ].includes(status);
  }

  /**
   * 检查是否有工资
   */
  static hasSalary(status: TeacherStatus): boolean {
    return [
      TeacherStatus.ACTIVE,
      TeacherStatus.ON_LEAVE,
      TeacherStatus.TEMPORARY,
      TeacherStatus.PART_TIME,
      TeacherStatus.VISITING,
    ].includes(status);
  }

  /**
   * 获取教师职称信息
   */
  static getTitleInfo(title: TeacherTitle): TeacherTitleInfo {
    return {
      title,
      description: TEACHER_TITLE_DESCRIPTIONS[title],
      level: this.getTitleLevel(title),
      isProfessorLevel: this.isProfessorLevel(title),
      isPermanent: this.isPermanentTitle(title),
      isTemporary: this.isTemporaryTitle(title),
      canTeachIndependently: this.canTeachIndependently(title),
      canSuperviseDoctorate: this.canSuperviseDoctorate(title),
      canSuperviseMaster: this.canSuperviseMaster(title),
    };
  }

  /**
   * 获取教师状态信息
   */
  static getStatusInfo(status: TeacherStatus): TeacherStatusInfo {
    return {
      status,
      description: TEACHER_STATUS_DESCRIPTIONS[status],
      isActive: this.isActiveStatus(status),
      isLeft: this.isLeftStatus(status),
      isTemporary: this.isTemporaryStatus(status),
      canTeach: this.canTeach(status),
      hasSalary: this.hasSalary(status),
    };
  }

  /**
   * 格式化教师显示名称
   */
  static formatTeacherName(teacher: Teacher): string {
    const titleDesc = TEACHER_TITLE_DESCRIPTIONS[teacher.title];
    return `${titleDesc} ${teacher.user.username}`;
  }

  /**
   * 获取教师状态颜色（用于UI显示）
   */
  static getStatusColor(status: TeacherStatus): string {
    const colorMap: Record<TeacherStatus, string> = {
      [TeacherStatus.ACTIVE]: "green",
      [TeacherStatus.TEMPORARY]: "blue",
      [TeacherStatus.PART_TIME]: "blue",
      [TeacherStatus.VISITING]: "blue",
      [TeacherStatus.ON_LEAVE]: "orange",
      [TeacherStatus.UNPAID_LEAVE]: "orange",
      [TeacherStatus.SUSPENDED]: "red",
      [TeacherStatus.RETIRED]: "gray",
      [TeacherStatus.RESIGNED]: "gray",
      [TeacherStatus.TERMINATED]: "red",
      [TeacherStatus.TRANSFERRED]: "gray",
    };
    return colorMap[status] || "gray";
  }
}
