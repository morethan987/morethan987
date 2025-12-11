export enum TrendDirection {
  UP = "up",
  DOWN = "down",
  NEUTRAL = "neutral",
}

export interface CardData {
  id: string; // 唯一标识符，用于 key
  title: string; // 卡片左上角的标题 (例如：GPA, 教学班数量)
  value: string; // 中间的大数字 (例如：3.8, 12)
  trend: {
    direction: TrendDirection; // 趋势方向：上升、下降、持平
    value: string; // 趋势显示的数值 (例如：+5%, +0.2)
    isVisible: boolean; // 是否显示趋势角标（某些静态数据可能不需要）
  };
  footer: {
    status: string; // 底部左侧的状态文案 (例如：比上学期进步)
    description: string; // 底部灰色的辅助说明 (例如：统计截止今日)
  };
}
