// 相当于后端的 UserDTO
export interface User {
  id: string;
  username: string;
  enabled: boolean;
  roles: string[];
  uiType: string;
}

/**
 * 用户性别枚举
 */
export enum Gender {
  UNKNOWN = 0,
  MALE = 1,
  FEMALE = 2,
  OTHER = 3,
}

/**
 * 性别显示文本映射
 */
export const GenderLabels: Record<Gender | string, string> = {
  [Gender.UNKNOWN]: "未知",
  [Gender.MALE]: "男",
  [Gender.FEMALE]: "女",
  [Gender.OTHER]: "其他",
  UNKNOWN: "未知",
  MALE: "男",
  FEMALE: "女",
  OTHER: "其他",
};

/**
 * 性别选项（用于表单）
 */
export const GenderOptions = [
  { value: Gender.UNKNOWN, label: GenderLabels[Gender.UNKNOWN] },
  { value: Gender.MALE, label: GenderLabels[Gender.MALE] },
  { value: Gender.FEMALE, label: GenderLabels[Gender.FEMALE] },
  { value: Gender.OTHER, label: GenderLabels[Gender.OTHER] },
];

/**
 * 用户资料信息（对应后端 UserProfile 实体）
 */
export interface UserProfile {
  realName: string;
  gender: Gender;
  birthDate?: string; // ISO date string (YYYY-MM-DD)
  email?: string;
  phone?: string;
  address?: string;
  bio?: string;
  avatarUrl?: string;
  createdAt: string; // ISO datetime string
  updatedAt: string; // ISO datetime string
}

/**
 * 创建用户资料的请求数据
 */
export interface CreateUserProfileRequest {
  userId: string;
  realName: string;
  gender: Gender;
  birthDate?: string;
  email?: string;
  phone?: string;
  address?: string;
  bio?: string;
  avatarUrl?: string;
}

/**
 * 更新用户资料的请求数据
 */
export interface UpdateUserProfileRequest {
  realName?: string;
  gender?: Gender;
  birthDate?: string;
  email?: string;
  phone?: string;
  address?: string;
  bio?: string;
  avatarUrl?: string;
}

/**
 * 用户资料列表响应
 */
export interface UserProfileListResponse {
  profiles: UserProfile[];
  total: number;
  page: number;
  size: number;
}

/**
 * 用户资料查询参数
 */
export interface UserProfileQueryParams {
  page?: number;
  size?: number;
  search?: string;
  gender?: Gender;
  sortBy?: "realName" | "createdAt" | "updatedAt";
  sortOrder?: "asc" | "desc";
}
