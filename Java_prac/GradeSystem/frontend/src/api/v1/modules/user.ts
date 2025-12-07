import client from "@/api/client";
import type {
  UserProfile,
  CreateUserProfileRequest,
  UpdateUserProfileRequest,
  UserProfileListResponse,
  UserProfileQueryParams,
} from "@/types/user";

/**
 * 用户资料相关 API
 */
export const userProfileApi = {
  /**
   * 获取用户资料详细信息
   */
  getUserProfileByUserid: async (userId: string): Promise<UserProfile> => {
    return await client.get(`/user/profile/${userId}`);
  },

  /**
   * 根据用户名获取用户资料详细信息
   */
  getUserProfileByUsername: async (userName: string): Promise<UserProfile> => {
    return await client.get(`/user/profile/by-username/${userName}`);
  },

  /**
   * 获取当前用户资料
   */
  getCurrentUserProfile: async (): Promise<UserProfile> => {
    return await client.get("/user/profile/me");
  },

  /**
   * 创建用户资料
   */
  createUserProfile: async (
    profileData: CreateUserProfileRequest,
  ): Promise<UserProfile> => {
    return await client.post("/user/profile", profileData);
  },

  /**
   * 更新用户资料
   */
  updateUserProfile: async (
    userId: string,
    profileData: UpdateUserProfileRequest,
  ): Promise<UserProfile> => {
    return await client.put(`/user/profile/${userId}`, profileData);
  },

  /**
   * 更新当前用户资料
   */
  updateCurrentUserProfile: async (
    profileData: UpdateUserProfileRequest,
  ): Promise<UserProfile> => {
    return await client.put("/user/profile/me", profileData);
  },

  /**
   * 获取用户资料列表
   */
  getUserProfileList: async (
    params?: UserProfileQueryParams,
  ): Promise<UserProfileListResponse> => {
    return await client.get("/user/profile", { params });
  },

  /**
   * 上传用户头像
   */
  uploadAvatar: async (
    userId: string,
    file: File,
  ): Promise<{ avatarUrl: string }> => {
    const formData = new FormData();
    formData.append("avatar", file);
    return await client.post(`/user/profile/${userId}/avatar`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  /**
   * 删除用户头像
   */
  deleteAvatar: async (userId: string): Promise<void> => {
    return await client.delete(`/user/profile/${userId}/avatar`);
  },
};

/**
 * 用户认证相关 API (保留原有的认证功能)
 */
export const userApi = {
  /**
   * 修改密码
   */
  changePassword: async (
    userId: string,
    oldPassword: string,
    newPassword: string,
    newPasswordConfirm: string,
  ): Promise<void> => {
    return await client.post(`/user/${userId}/password`, {
      oldPassword,
      newPassword,
      newPasswordConfirm,
    });
  },

  /**
   * 修改用户名
   */
  changeUsername: async (
    userId: string,
    password: string,
    newUsername: string,
  ): Promise<void> => {
    return await client.post(`/user/${userId}/username`, {
      password,
      newUsername,
    });
  },

  /**
   * 启用/禁用用户
   */
  toggleUserStatus: async (userId: string, enabled: boolean): Promise<void> => {
    return await client.patch(`/user/${userId}/status`, { enabled });
  },
};
