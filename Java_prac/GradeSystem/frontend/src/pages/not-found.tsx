import { Button } from "@/components/ui/button";
import { ROUTES } from "@/routes";
import { IconFlagFilled } from "@tabler/icons-react";
import { useLocation } from "wouter";

export function NotFoundPage() {
  const [, setLocation] = useLocation();

  function handleGoHome() {
    setLocation(ROUTES.HOME);
  }

  return (
    <div className="h-screen mx-auto grid place-items-center text-center px-8">
      <div>
        <IconFlagFilled className="w-20 h-20 mx-auto" />
        <h1 className="mt-10 text-3xl font-bold leading-snug tracking-normal text-blue-gray-900 md:text-4xl text-foreground">
          404 错误
          <br />
          页面未找到
        </h1>

        <p className="mt-8 mb-14 text-[18px] font-normal text-muted-foreground mx-auto md:max-w-sm antialiased">
          您貌似意外进入了一个网页的荒原
        </p>
        <Button onClick={handleGoHome} className="w-full px-4 md:w-32">
          返回首页
        </Button>
      </div>
    </div>
  );
}
