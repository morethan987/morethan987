import { useState, useEffect, useCallback } from "react";
import { useLocation } from "wouter";
import { authApi } from "@/api/v1/modules/auth";
import type { LoginRequest, UserBasicInfo, AuthError } from "@/types/auth";
import { ROUTES } from "@/routes";

interface UseAuthReturn {
  // 状态
  user: UserBasicInfo | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  error: AuthError | null;

  // 操作
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

/**
 * 认证状态管理 Hook
 */
export function useAuth(): UseAuthReturn {
  const [user, setUser] = useState<UserBasicInfo | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<AuthError | null>(null);
  const [, setLocation] = useLocation();

  const isAuthenticated = Boolean(user);

  /**
   * 清除错误信息
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  /**
   * 登录
   */
  const login = useCallback(
    async (credentials: LoginRequest) => {
      try {
        setIsLoading(true);
        setError(null);

        const response = await authApi.login(credentials);

        if (response.success && response.user) {
          setUser(response.user);
          setLocation(ROUTES.DASHBOARD);
        } else {
          setError({
            message: response.message || "登录失败",
          });
        }
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "登录过程中发生未知错误";
        setError({
          message: errorMessage,
        });
      } finally {
        setIsLoading(false);
      }
    },
    [setLocation],
  );

  /**
   * 登出
   */
  const logout = useCallback(async () => {
    try {
      setIsLoading(true);
      await authApi.logout();
    } catch (err) {
      // 即使登出请求失败，也要清除本地状态
      console.warn("登出请求失败:", err);
    } finally {
      setUser(null);
      setIsLoading(false);
      // 跳转到登录页
      setLocation(ROUTES.LOGIN);
    }
  }, [setLocation]);

  /**
   * 检查当前登录状态
   */
  const checkAuthStatus = useCallback(async () => {
    // 防止重复调用
    if (isLoading) return;

    try {
      setIsLoading(true);
      const response = await authApi.checkAuth();

      if (response.success && response.user) {
        setUser(response.user);
      } else {
        setUser(null);
      }
    } catch (err) {
      // 检查登录状态失败，可能是未登录
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, [isLoading]);

  // 组件挂载时检查登录状态，只执行一次
  useEffect(() => {
    let isMounted = true;

    const initAuth = async () => {
      try {
        setIsLoading(true);
        const response = await authApi.checkAuth();

        if (isMounted) {
          if (response.success && response.user) {
            setUser(response.user);
          } else {
            setUser(null);
          }
        }
      } catch (err) {
        // 检查登录状态失败，可能是未登录
        if (isMounted) {
          setUser(null);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    initAuth();

    // 清理函数
    return () => {
      isMounted = false;
    };
  }, []); // 空依赖数组，只在挂载时执行

  return {
    user,
    isLoading,
    isAuthenticated,
    error,
    login,
    logout,
    clearError,
  };
}
