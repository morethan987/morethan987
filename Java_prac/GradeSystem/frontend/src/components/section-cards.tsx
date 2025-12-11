import {
  IconTrendingDown,
  IconTrendingUp,
  IconMinus,
} from "@tabler/icons-react";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardAction,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { TrendDirection, type CardData } from "@/types/card-data";
import { cn } from "@/lib/utils";

interface SectionCardsProps {
  cardsData: CardData[];
}

export function SectionCards({ cardsData }: SectionCardsProps) {
  // 渲染趋势图标
  const getTrendIcon = (direction: TrendDirection) => {
    switch (direction) {
      case TrendDirection.UP:
        return <IconTrendingUp className="h-4 w-4" />;
      case TrendDirection.DOWN:
        return <IconTrendingDown className="h-4 w-4" />;
      default:
        return <IconMinus className="h-4 w-4" />;
    }
  };

  // 根据趋势获取颜色样式
  const getTrendColor = (direction: TrendDirection) => {
    if (direction === TrendDirection.UP) return "text-emerald-500";
    if (direction === TrendDirection.DOWN) return "text-rose-500";
    return "text-muted-foreground";
  };

  return (
    <div className="grid grid-cols-1 gap-4 px-4 lg:px-6 @xl/main:grid-cols-2 @5xl/main:grid-cols-4">
      {cardsData.map((card) => (
        <Card
          key={card.id}
          className="relative overflow-hidden bg-linear-to-t from-primary/5 to-card shadow-sm dark:bg-card @container/card hover:shadow-md transition-shadow"
        >
          <CardHeader className="pb-2">
            <CardAction className="flex justify-between items-start">
              <CardDescription className="text-sm font-medium text-muted-foreground">
                {card.title}
              </CardDescription>

              {/* 右上角的 Badge (Action) */}
              {card.trend.isVisible && (
                <Badge
                  variant="outline"
                  className="flex gap-1 items-center font-normal"
                >
                  <span className={getTrendColor(card.trend.direction)}>
                    {getTrendIcon(card.trend.direction)}
                  </span>
                  <span>{card.trend.value}</span>
                </Badge>
              )}
            </CardAction>

            <CardTitle className="text-3xl font-bold tabular-nums tracking-tight mt-2">
              {card.value}
            </CardTitle>
          </CardHeader>

          <CardFooter className="flex flex-col items-start gap-1 pt-4 text-sm">
            <div className="flex w-full items-center gap-2 font-medium">
              <span
                className={cn(
                  "flex items-center gap-1",
                  getTrendColor(card.trend.direction),
                )}
              >
                {card.trend.isVisible && getTrendIcon(card.trend.direction)}
                {card.footer.status}
              </span>
            </div>
            <p className="text-xs text-muted-foreground line-clamp-1">
              {card.footer.description}
            </p>
          </CardFooter>
        </Card>
      ))}
    </div>
  );
}
