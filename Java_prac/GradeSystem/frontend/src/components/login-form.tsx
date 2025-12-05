import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Link, Redirect } from "wouter";
import { ROUTES } from "@/routes";
import { useAuthContext } from "@/contexts/auth-context";
import { useState } from "react";

export function LoginForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  const { login, isLoading, error, clearError } = useAuthContext();

  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });

  const updateField = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (error) clearError();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.username.trim() || !formData.password.trim()) return;

    await login(formData);
  };

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card>
        <CardHeader>
          <CardTitle>账户登陆</CardTitle>
          <CardDescription>在下方输入用户名和密码以登陆</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <FieldGroup>
              {error && (
                <Field>
                  <div className="rounded-md bg-red-50 p-3 border border-red-200">
                    <p className="text-sm text-red-600">{error.message}</p>
                  </div>
                </Field>
              )}

              <Field>
                <FieldLabel htmlFor="username">用户名</FieldLabel>
                <Input
                  id="username"
                  type="text"
                  placeholder="请输入用户名"
                  value={formData.username}
                  onChange={(e) => updateField("username", e.target.value)}
                  disabled={isLoading}
                  required
                />
              </Field>

              <Field>
                <div className="flex items-center">
                  <FieldLabel htmlFor="password">密码</FieldLabel>
                  <Link
                    href={ROUTES.FORGOT_PASSWORD}
                    className="ml-auto inline-block text-sm underline-offset-4 hover:underline"
                  >
                    忘记密码
                  </Link>
                </div>
                <Input
                  id="password"
                  type="password"
                  placeholder="请输入密码"
                  value={formData.password}
                  onChange={(e) => updateField("password", e.target.value)}
                  disabled={isLoading}
                  required
                />
              </Field>

              <Field>
                <Button
                  type="submit"
                  disabled={
                    isLoading ||
                    !formData.username.trim() ||
                    !formData.password.trim()
                  }
                  className="w-full"
                >
                  {isLoading ? "登录中..." : "登陆"}
                </Button>

                <FieldDescription className="text-center">
                  还没有账号?{" "}
                  <Link
                    href={ROUTES.SIGNUP}
                    className="text-sm underline-offset-4 hover:underline"
                  >
                    立即注册
                  </Link>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
