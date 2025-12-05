export interface UserInfo {
  id: string;
  username: string;
  enabled: boolean;
  roles: string[];
  realName?: string;
  email?: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  user?: UserInfo;
  data?: any;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse extends AuthResponse {
  user?: UserInfo;
}

export interface AuthError {
  message: string;
  code?: number;
}
