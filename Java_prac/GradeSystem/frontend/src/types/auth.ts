export interface UserBasicInfo {
  // from entity User
  id: string;
  username: string;
  enabled: boolean;
  roles: string[];
  uiType: string;
  // from entity UserProfile
  realName?: string;
  email?: string;
  avatarUrl?: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  user?: UserBasicInfo;
  data?: any;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  confirmPassword: string;
  realName?: string;
  email?: string;
  phone?: string;
  roles?: string[];
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}

export interface ChangeUsernameRequest {
  newUsername: string;
}

export interface LoginResponse extends AuthResponse {
  user?: UserBasicInfo;
}

export interface AuthError {
  message: string;
  code?: number;
}
