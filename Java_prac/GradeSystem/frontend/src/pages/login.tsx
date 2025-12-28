import { LoginForm } from "@/components/login-form";
import { useAuthContext } from "@/contexts/auth-context";
import { Redirect } from "wouter";
import { ROUTES } from "@/routes";

export function LoginPage() {
  const { isAuthenticated, isLoading } = useAuthContext();

  // 如果用户已经登录，重定向到 dashboard
  if (isAuthenticated) {
    return <Redirect to={ROUTES.DASHBOARD} />;
  }

  // 显示加载状态
  if (isLoading) {
    return (
      <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10">
        <div className="w-full max-w-sm">
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
            <p>正在检查登录状态...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10">
      <div className="w-full max-w-sm">
        <LoginForm />
      </div>
    </div>
  );
}
