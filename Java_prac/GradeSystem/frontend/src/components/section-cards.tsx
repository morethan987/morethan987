import { IconTrendingDown, IconTrendingUp } from "@tabler/icons-react";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardAction,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { CardData } from "@/types/card-data";

interface SectionCardsProps {
  cardsData: CardData[];
}

export function SectionCards({ cardsData }: SectionCardsProps) {
  const getTrendIcon = (direction: "up" | "down") => {
    return direction === "up" ? <IconTrendingUp /> : <IconTrendingDown />;
  };

  return (
    <div className="*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid grid-cols-1 gap-4 px-4 *:data-[slot=card]:bg-linear-to-t *:data-[slot=card]:shadow-xs lg:px-6 @xl/main:grid-cols-2 @5xl/main:grid-cols-4">
      {cardsData.map((card, index) => (
        <Card key={index} className="@container/card">
          <CardHeader>
            <CardDescription>{card.title}</CardDescription>
            <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
              {card.value}
            </CardTitle>
            <CardAction>
              <Badge variant="outline">
                {getTrendIcon(card.trend.direction)}
                {card.trend.value}
              </Badge>
            </CardAction>
          </CardHeader>
          <CardFooter className="flex-col items-start gap-1.5 text-sm">
            <div className="line-clamp-1 flex gap-2 font-medium">
              {card.footer.status} {getTrendIcon(card.trend.direction)}
            </div>
            <div className="text-muted-foreground">
              {card.footer.description}
            </div>
          </CardFooter>
        </Card>
      ))}
    </div>
  );
}
