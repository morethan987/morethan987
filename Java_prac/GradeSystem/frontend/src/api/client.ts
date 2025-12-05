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
    // 业务失败
    if (response.status !== 200) {
      return Promise.reject(new Error("业务异常"));
    }

    // 返回完整的响应对象，而不是只有 data 字段
    return response.data;
  },

  async (error: AxiosError) => {
    if (!error.response) {
      console.error("网络异常");
      return Promise.reject(new Error("无法连接服务器"));
    }

    const status = error.response.status;

    // ====== Session 失效处理 ======
    switch (status) {
      case 401:
        console.warn("Session 已失效，跳转登录页");
        window.location.href = ROUTES.LOGIN;
        break;
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
