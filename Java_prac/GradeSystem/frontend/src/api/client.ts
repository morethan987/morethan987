import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosResponse,
} from "axios";
import { ROUTES } from "@/routes";

/**
 * 定义后端统一返回格式
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

/**
 * 创建 axios 实例
 */
const client: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  withCredentials: true, // 如果你将来用 Cookie / Session
});

/**
 * Token 获取逻辑集中管理
 */
function getToken(): string | null {
  return localStorage.getItem("token");
}

/**
 * 请求拦截器
 */
client.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 可选：调试日志
    if (import.meta.env.DEV) {
      console.debug(
        "[Request]",
        config.method?.toUpperCase(),
        config.url,
        config.params || config.data,
      );
    }

    return config;
  },
  (error) => Promise.reject(error),
);

/**
 * 响应拦截器
 */
client.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data;

    // 业务失败
    if (res.code !== 0) {
      return Promise.reject(new Error(res.message || "Business Error"));
    }

    return res.data;
  },
  (error: AxiosError) => {
    // 网络错误
    if (!error.response) {
      console.error("Network Error");
      return Promise.reject(new Error("网络异常"));
    }

    const status = error.response.status;

    switch (status) {
      case 401:
        console.warn("登录过期，自动跳转登录页");
        localStorage.removeItem("token");
        window.location.href = ROUTES.LOGIN;
        break;
      case 403:
        console.warn("无权限访问");
        break;
      case 500:
        console.error("服务器异常");
        break;
    }

    return Promise.reject(error);
  },
);

export default client;
