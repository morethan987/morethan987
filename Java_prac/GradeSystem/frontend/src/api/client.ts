import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosResponse,
} from "axios";
import { ROUTES } from "@/routes";

/**
 * Axios 实例
 */
const client: AxiosInstance = axios.create({
  baseURL: ROUTES.BACKEND_BASE_URL,
  timeout: 10000,
  withCredentials: true, // 携带 Cookie 进行 Session 认证
});

/**
 * 请求拦截器
 */
client.interceptors.request.use((config) => {
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
  (response: AxiosResponse) => {
    return response.data;
  },
  async (error: AxiosError) => {
    if (!error.response) {
      return Promise.reject(new Error("无法连接服务器"));
    }

    const status = error.response.status;
    // 获取后端返回的错误消息
    // 注意：如果后端直接返回字符串，data 就是该字符串；如果返回 JSON，data 就是对象
    const backendMessage = error.response.data;

    switch (status) {
      case 400:
        // 如果后端传回了具体消息，则抛出该消息
        return Promise.reject(
          new Error(
            typeof backendMessage === "string"
              ? backendMessage
              : "请求参数错误",
          ),
        );
      case 401:
        const currentPath = window.location.pathname;
        if (currentPath !== ROUTES.LOGIN && currentPath !== ROUTES.SIGNUP) {
          window.location.href = ROUTES.LOGIN;
        }
        break;
      case 403:
        return Promise.reject(new Error("无权限访问该资源"));
      case 500:
        return Promise.reject(new Error("服务器内部错误"));
    }

    return Promise.reject(error);
  },
);

export default client;
