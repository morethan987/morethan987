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
import { ROUTES } from "@/routes";
import { Link } from "wouter";

export function SignupForm({ ...props }: React.ComponentProps<typeof Card>) {
  return (
    <Card {...props}>
      <CardHeader>
        <CardTitle>创建账户</CardTitle>
        <CardDescription>在下方填写信息以创建一个新账户</CardDescription>
      </CardHeader>
      <CardContent>
        <form>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="name">用户名</FieldLabel>
              <Input
                id="name"
                type="text"
                placeholder="请输入用户名"
                required
              />
            </Field>
            <Field>
              <FieldLabel htmlFor="email">邮箱</FieldLabel>
              <Input
                id="email"
                type="email"
                placeholder="m@example.com"
                required
              />
            </Field>
            <Field>
              <FieldLabel htmlFor="password">密码</FieldLabel>
              <Input id="password" type="password" required />
              <FieldDescription>
                至少包含 8 个字符，建议使用大小写字母、数字和特殊符号的组合
              </FieldDescription>
            </Field>
            <Field>
              <FieldLabel htmlFor="confirm-password">确认密码</FieldLabel>
              <Input id="confirm-password" type="password" required />
              <FieldDescription>请重新输入密码</FieldDescription>
            </Field>
            <FieldGroup>
              <Field>
                <Button type="submit">创建账户</Button>
                {/*<Button variant="outline" type="button">
                  Sign up with Google
                </Button>*/}
                <FieldDescription className="px-6 text-center">
                  已经有账户了？{" "}
                  <Link
                    href={ROUTES.LOGIN}
                    className="text-sm underline-offset-4 hover:underline"
                  >
                    登陆
                  </Link>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </FieldGroup>
        </form>
      </CardContent>
    </Card>
  );
}
