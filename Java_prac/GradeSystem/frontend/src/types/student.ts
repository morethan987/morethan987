import type { User } from "./user";

/**
 * 学生状态枚举
 * 定义学生在学校系统中的各种状态
 */
export enum StudentStatus {
  /** 在读 - 正常在校学习状态 */
  ENROLLED = "ENROLLED",
  /** 休学 - 暂时离校但保留学籍 */
  SUSPENDED = "SUSPENDED",
  /** 退学 - 主动退出学校 */
  WITHDRAWN = "WITHDRAWN",
  /** 毕业 - 完成学业正常毕业 */
  GRADUATED = "GRADUATED",
  /** 转学 - 转到其他学校 */
  TRANSFERRED = "TRANSFERRED",
  /** 开除 - 被学校开除 */
  EXPELLED = "EXPELLED",
  /** 延期毕业 - 延长学习时间 */
  DEFERRED = "DEFERRED",
  /** 交换生 - 临时在本校学习的交换学生 */
  EXCHANGE = "EXCHANGE",
}

/**
 * 学生状态描述映射
 */
export const StudentStatusDescriptions: Record<StudentStatus, string> = {
  [StudentStatus.ENROLLED]: "在读",
  [StudentStatus.SUSPENDED]: "休学",
  [StudentStatus.WITHDRAWN]: "退学",
  [StudentStatus.GRADUATED]: "毕业",
  [StudentStatus.TRANSFERRED]: "转学",
  [StudentStatus.EXPELLED]: "开除",
  [StudentStatus.DEFERRED]: "延期毕业",
  [StudentStatus.EXCHANGE]: "交换生",
};

/**
 * 学生状态工具函数
 */
export class StudentStatusUtils {
  /**
   * 获取状态描述
   */
  static getDescription(status: StudentStatus): string {
    return StudentStatusDescriptions[status];
  }

  /**
   * 检查是否为活跃状态（可以正常学习）
   */
  static isActive(status: StudentStatus): boolean {
    return (
      status === StudentStatus.ENROLLED ||
      status === StudentStatus.EXCHANGE ||
      status === StudentStatus.DEFERRED
    );
  }

  /**
   * 检查是否已离校
   */
  static isLeft(status: StudentStatus): boolean {
    return (
      status === StudentStatus.WITHDRAWN ||
      status === StudentStatus.GRADUATED ||
      status === StudentStatus.TRANSFERRED ||
      status === StudentStatus.EXPELLED
    );
  }

  /**
   * 检查是否为临时状态
   */
  static isTemporary(status: StudentStatus): boolean {
    return (
      status === StudentStatus.SUSPENDED ||
      status === StudentStatus.EXCHANGE ||
      status === StudentStatus.DEFERRED
    );
  }

  /**
   * 获取所有状态选项（用于下拉框等UI组件）
   */
  static getAllStatusOptions(): Array<{ value: StudentStatus; label: string }> {
    return Object.entries(StudentStatusDescriptions).map(([value, label]) => ({
      value: value as StudentStatus,
      label,
    }));
  }

  /**
   * 获取活跃状态选项
   */
  static getActiveStatusOptions(): Array<{
    value: StudentStatus;
    label: string;
  }> {
    return this.getAllStatusOptions().filter((option) =>
      this.isActive(option.value),
    );
  }
}

export interface Student {
  id: string;
  user: User;
  studentCode: string;
  major?: string;
  className?: string;
  enrollmentYear?: number;
  currentSemester: number;
  status: StudentStatus;
  totalCredits?: number;
  advisor?: string;
  expectedGraduationDate?: string; // ISO date string (YYYY-MM-DD)
  createdAt: string; // ISO datetime string
  updatedAt: string; // ISO datetime string
}
