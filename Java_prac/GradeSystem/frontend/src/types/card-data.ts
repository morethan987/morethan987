interface CardData {
  title: string;
  description: string;
  value: string;
  trend: {
    direction: "up" | "down";
    value: string;
  };
  footer: {
    status: string;
    description: string;
  };
}

/**
 * JSON / API 原始对象类型（不可信）
 */
type RawCardData = {
  title?: unknown;
  description?: unknown;
  value?: unknown;
  trend?: {
    direction?: unknown;
    value?: unknown;
  };
  footer?: {
    status?: unknown;
    description?: unknown;
  };
};

/**
 * 单条记录解析
 */
function parseCardData(raw: RawCardData): CardData {
  return {
    title: typeof raw.title === "string" ? raw.title : "",
    description: typeof raw.description === "string" ? raw.description : "",
    value: typeof raw.value === "string" ? raw.value : "",
    trend: {
      direction: raw.trend?.direction === "up" ? "up" : "down",
      value: typeof raw.trend?.value === "string" ? raw.trend.value : "",
    },
    footer: {
      status: typeof raw.footer?.status === "string" ? raw.footer.status : "",
      description:
        typeof raw.footer?.description === "string"
          ? raw.footer.description
          : "",
    },
  };
}

/**
 * 数组装载器（你页面将永远只用它）
 */
function loadCardDataArray(raw: unknown): CardData[] {
  if (!Array.isArray(raw)) return [];
  return raw.map(parseCardData);
}

export type { CardData };
export { parseCardData, loadCardDataArray };
