export interface ChartSeries {
  key: string; // 数据键名，如 "gpa"
  label: string; // 显示名，如 "个人绩点"
}

export interface GenericChartData {
  title: string;
  description?: string;
  xAxisKey: string; // X轴字段名
  series: ChartSeries[]; // 需要渲染的线条列表（包含key和中文label）
  data: Record<string, string | number>[]; // 具体数据
}
