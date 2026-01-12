export enum TrendDirection {
  UP = "up",
  DOWN = "down",
  NEUTRAL = "neutral",
}

export interface CardDataResponse {
  id: string;
  title: string;
  value: string;
  trend: {
    direction: string;
    value: string;
    isVisible: boolean;
  };
  footer: {
    status: string;
    description: string;
  };
}

export interface CardData {
  id: string;
  title: string;
  value: string;
  trend: {
    direction: TrendDirection;
    value: string;
    isVisible: boolean;
  };
  footer: {
    status: string;
    description: string;
  };
}

export function mapCardDataResponse(response: CardDataResponse): CardData {
  const directionMap: Record<string, TrendDirection> = {
    up: TrendDirection.UP,
    down: TrendDirection.DOWN,
    neutral: TrendDirection.NEUTRAL,
  };

  return {
    ...response,
    trend: {
      ...response.trend,
      direction: directionMap[response.trend.direction] || TrendDirection.NEUTRAL,
    },
  };
}
