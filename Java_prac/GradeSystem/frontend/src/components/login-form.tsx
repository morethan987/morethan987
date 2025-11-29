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
import { Link } from "wouter";
import { ROUTES } from "@/routes";

export function LoginForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card>
        <CardHeader>
          <CardTitle>账户登陆</CardTitle>
          <CardDescription>在下方输入用户名和密码以登陆</CardDescription>
        </CardHeader>
        <CardContent>
          <form>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="username">用户名</FieldLabel>
                <Input
                  id="username"
                  type="username"
                  placeholder="请输入用户名"
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
                <Input id="password" type="password" required />
              </Field>
              <Field>
                <Button type="submit">登陆</Button>
                {/*<Button variant="outline" type="button">
                  Login with Google
                </Button>*/}
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
