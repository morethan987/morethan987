import client from "@/api/client";
import type { LoginRequest, AuthResponse } from "@/types/auth";

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    return await client.post("/auth/login", credentials);
  },

  /**
   * 用户登出
   */
  logout: async (): Promise<void> => {
    return await client.post("/auth/logout");
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser: async (): Promise<AuthResponse> => {
    return await client.get("/auth/me");
  },

  /**
   * 检查登录状态
   */
  checkAuth: async (): Promise<AuthResponse> => {
    return await client.get("/auth/status");
  },
};
