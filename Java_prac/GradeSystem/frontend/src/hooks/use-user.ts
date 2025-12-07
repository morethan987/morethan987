import { useState, useCallback } from "react";
import { userProfileApi, userApi } from "@/api/v1/modules/user";
import type {
  UserProfile,
  CreateUserProfileRequest,
  UpdateUserProfileRequest,
  UserProfileQueryParams,
  UserProfileListResponse,
} from "@/types/user";

interface UseUserProfileReturn {
  // 状态
  userProfile: UserProfile | null;
  isLoading: boolean;
  error: string | null;

  // 操作
  fetchUserProfile: (userId: string) => Promise<void>;
  updateUserProfile: (
    userId: string,
    profileData: UpdateUserProfileRequest,
  ) => Promise<void>;
  createUserProfile: (profileData: CreateUserProfileRequest) => Promise<void>;
  uploadAvatar: (userId: string, file: File) => Promise<void>;
  deleteAvatar: (userId: string) => Promise<void>;
  clearError: () => void;
}

/**
 * 用户资料管理 Hook
 */
export function useUserProfile(): UseUserProfileReturn {
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 清除错误信息
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  /**
   * 获取用户资料信息
   */
  const fetchUserProfile = useCallback(async (userId: string) => {
    try {
      setIsLoading(true);
      setError(null);

      const profileData = await userProfileApi.getUserProfileByUserid(userId);
      setUserProfile(profileData);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "获取用户资料失败";
      setError(errorMessage);
      setUserProfile(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * 创建用户资料
   */
  const createUserProfile = useCallback(
    async (profileData: CreateUserProfileRequest) => {
      try {
        setIsLoading(true);
        setError(null);

        const newProfile = await userProfileApi.createUserProfile(profileData);
        setUserProfile(newProfile);
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "创建用户资料失败";
        setError(errorMessage);
        throw err; // 重新抛出错误，让调用方处理
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  /**
   * 更新用户资料
   */
  const updateUserProfile = useCallback(
    async (userId: string, profileData: UpdateUserProfileRequest) => {
      try {
        setIsLoading(true);
        setError(null);

        const updatedProfile = await userProfileApi.updateUserProfile(
          userId,
          profileData,
        );
        setUserProfile(updatedProfile);
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "更新用户资料失败";
        setError(errorMessage);
        throw err; // 重新抛出错误，让调用方处理
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  /**
   * 上传用户头像
   */
  const uploadAvatar = useCallback(
    async (userId: string, file: File) => {
      try {
        setIsLoading(true);
        setError(null);

        const response = await userProfileApi.uploadAvatar(userId, file);

        // 更新当前用户资料的头像 URL
        if (userProfile) {
          setUserProfile({
            ...userProfile,
            avatarUrl: response.avatarUrl,
          });
        }
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "上传头像失败";
        setError(errorMessage);
        throw err; // 重新抛出错误，让调用方处理
      } finally {
        setIsLoading(false);
      }
    },
    [userProfile],
  );

  /**
   * 删除用户头像
   */
  const deleteAvatar = useCallback(
    async (userId: string) => {
      try {
        setIsLoading(true);
        setError(null);

        await userProfileApi.deleteAvatar(userId);

        // 清除当前用户资料的头像 URL
        if (userProfile) {
          setUserProfile({
            ...userProfile,
            avatarUrl: undefined,
          });
        }
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "删除头像失败";
        setError(errorMessage);
        throw err; // 重新抛出错误，让调用方处理
      } finally {
        setIsLoading(false);
      }
    },
    [userProfile],
  );

  return {
    userProfile,
    isLoading,
    error,
    fetchUserProfile,
    updateUserProfile,
    createUserProfile,
    uploadAvatar,
    deleteAvatar,
    clearError,
  };
}

interface UseUserProfileListReturn {
  // 状态
  profiles: UserProfile[];
  isLoading: boolean;
  error: string | null;
  total: number;
  page: number;
  size: number;

  // 操作
  fetchUserProfiles: (params?: UserProfileQueryParams) => Promise<void>;
  clearError: () => void;
}

/**
 * 用户资料列表管理 Hook
 */
export function useUserProfileList(): UseUserProfileListReturn {
  const [profiles, setProfiles] = useState<UserProfile[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);

  /**
   * 清除错误信息
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  /**
   * 获取用户资料列表
   */
  const fetchUserProfiles = useCallback(
    async (params?: UserProfileQueryParams) => {
      try {
        setIsLoading(true);
        setError(null);

        const response: UserProfileListResponse =
          await userProfileApi.getUserProfileList(params);

        setProfiles(response.profiles);
        setTotal(response.total);
        setPage(response.page);
        setSize(response.size);
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "获取用户资料列表失败";
        setError(errorMessage);
        setProfiles([]);
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  return {
    profiles,
    isLoading,
    error,
    total,
    page,
    size,
    fetchUserProfiles,
    clearError,
  };
}

interface UseCurrentUserProfileReturn {
  // 状态
  userProfile: UserProfile | null;
  isLoading: boolean;
  error: string | null;

  // 操作
  fetchCurrentUserProfile: () => Promise<void>;
  updateCurrentUserProfile: (
    profileData: UpdateUserProfileRequest,
  ) => Promise<void>;
  changePassword: (
    userId: string,
    oldPassword: string,
    newPassword: string,
    newPasswordConfirm: string,
  ) => Promise<void>;
  changeUsername: (
    userId: string,
    password: string,
    newUsername: string,
  ) => Promise<void>;
  uploadCurrentUserAvatar: (file: File) => Promise<void>;
  deleteCurrentUserAvatar: () => Promise<void>;
  clearError: () => void;
}
