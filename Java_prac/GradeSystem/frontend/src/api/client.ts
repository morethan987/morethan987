import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
} from "axios";
import { ROUTES } from "@/routes";

/**
 * 后端统一返回结构
 */
export interface ApiResponse<T = any> {
  code: number;
  msg: string;
  data: T;
}

/**
 * Token Key
 */
const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";

/**
 * Axios 实例
 */
const client: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  withCredentials: false, // JWT 通常不需要 Cookie
});

/**
 * Token 管理
 */
function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

function setAccessToken(token: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

function setRefreshToken(token: string) {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

function clearToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

/**
 * 是否正在刷新 Token
 */
let isRefreshing = false;

/**
 * 等待刷新队列
 */
let pendingRequests: Array<(token: string) => void> = [];

/**
 * 刷新 token
 */
async function refreshTokenRequest(): Promise<string> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error("No refresh token");
  }

  const response = await axios.post<
    ApiResponse<{
      accessToken: string;
      expiresIn: number;
    }>
  >(`${import.meta.env.VITE_API_BASE_URL}/auth/refresh`, { refreshToken });

  const { accessToken } = response.data.data;
  setAccessToken(accessToken);
  return accessToken;
}

/**
 * 请求拦截器：注入 Token
 */
client.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  if (import.meta.env.DEV) {
    console.debug(
      "[Request]",
      config.method?.toUpperCase(),
      config.url,
      config.params || config.data,
    );
  }

  return config;
});

/**
 * 响应拦截器
 */
client.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data;

    // 业务失败
    if (res.code !== 0) {
      return Promise.reject(new Error(res.msg || "业务异常"));
    }

    return res.data;
  },

  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & {
      _retry?: boolean;
    };

    if (!error.response) {
      console.error("网络异常");
      return Promise.reject(new Error("无法连接服务器"));
    }

    const status = error.response.status;

    // ====== Token 过期，自动刷新 ======
    if (status === 401 && !originalRequest._retry) {
      // 标记防止死循环
      originalRequest._retry = true;

      // 若正在刷新：挂起请求
      if (isRefreshing) {
        return new Promise((resolve) => {
          pendingRequests.push((token: string) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            resolve(client(originalRequest));
          });
        });
      }

      isRefreshing = true;

      try {
        const newToken = await refreshTokenRequest();

        // 唤醒队列
        pendingRequests.forEach((cb) => cb(newToken));
        pendingRequests = [];

        // 重试原请求
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
        }

        return client(originalRequest);
      } catch (err) {
        console.warn("刷新失败，跳转登录页");
        clearToken();
        window.location.href = ROUTES.LOGIN;
        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }

    // ====== 其他错误 ======
    switch (status) {
      case 403:
        console.warn("无权限访问该资源");
        break;
      case 500:
        console.error("服务器错误");
        break;
    }

    return Promise.reject(error);
  },
);

export default client;
