import { createContext, useContext, type ReactNode } from "react";
import { useAuth } from "@/hooks/use-auth";
import type { UserInfo, AuthError, LoginRequest } from "@/types/auth";

interface AuthContextType {
  // 状态
  user: UserInfo | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  error: AuthError | null;

  // 操作
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * 认证上下文提供器
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const auth = useAuth();

  return <AuthContext.Provider value={auth}>{children}</AuthContext.Provider>;
}

/**
 * 使用认证上下文的 Hook
 */
export function useAuthContext(): AuthContextType {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuthContext must be used within an AuthProvider");
  }

  return context;
}

/**
 * 需要认证的路由保护组件
 */
interface ProtectedRouteProps {
  children: ReactNode;
  fallback?: ReactNode;
}

export function ProtectedRoute({
  children,
  fallback = <div>需要登录才能访问此页面</div>,
}: ProtectedRouteProps) {
  const { isAuthenticated, isLoading } = useAuthContext();

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p>正在验证登录状态...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
