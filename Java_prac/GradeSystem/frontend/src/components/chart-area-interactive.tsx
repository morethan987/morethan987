"use client";

import * as React from "react";
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart";

export interface GenericChartData {
  title: string;
  description?: string;
  xAxisKey: string;
  series: { key: string; label: string }[];
  data: Record<string, string | number>[];
}

interface GenericAreaChartProps {
  chartData: GenericChartData;
  className?: string;
}

export function GenericAreaChart({
  chartData,
  className,
}: GenericAreaChartProps) {
  const { title, description, xAxisKey, series, data } = chartData;

  // 计算Y轴的范围，用于自动调整刻度
  const yAxisDomain = React.useMemo(() => {
    if (!data || data.length === 0) return [0, 1];

    let min = Number.MAX_VALUE;
    let max = Number.MIN_VALUE;

    // 判断是否为堆叠图（根据数据判断）
    data.forEach((item) => {
      series.forEach((s) => {
        const value = Number(item[s.key]);
        if (!isNaN(value)) {
          min = Math.min(min, value);
          max = Math.max(max, value);
        }
      });
    });

    // 最小值不能小于0并且添加10%的边距
    min = Math.max(0, min - (max - min) * 0.1);

    // 添加20%的边距，避免数据点贴着边界
    const padding = (max - min) * 0.1;
    return [min, max + padding];
  }, [data, series]);

  const chartConfig = React.useMemo(() => {
    const config: ChartConfig = {};
    series.forEach((item, index) => {
      const colorVar =
        index === 0 ? "var(--primary)" : "var(--muted-foreground)";

      config[item.key] = {
        label: item.label,
        color: colorVar,
      };
    });
    return config;
  }, [series]);

  return (
    <Card className={`@container/card w-full ${className}`}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
        <ChartContainer
          config={chartConfig}
          className="aspect-auto h-[250px] w-full"
        >
          <AreaChart data={data} margin={{ left: 10, right: 10 }}>
            <defs>
              {series.map((item) => (
                <linearGradient
                  key={item.key}
                  id={`fill-${item.key}`}
                  x1="0"
                  y1="0"
                  x2="0"
                  y2="1"
                >
                  <stop
                    offset="5%"
                    stopColor={`var(--color-${item.key})`}
                    stopOpacity={0.8}
                  />
                  <stop
                    offset="95%"
                    stopColor={`var(--color-${item.key})`}
                    stopOpacity={0.1}
                  />
                </linearGradient>
              ))}
            </defs>

            <CartesianGrid vertical={false} />

            <XAxis
              dataKey={xAxisKey}
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              minTickGap={32}
              tickFormatter={(value) => {
                const str = String(value);
                return str.length > 10 ? `${str.slice(0, 10)}...` : str;
              }}
            />

            <YAxis
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              domain={yAxisDomain}
              tickFormatter={(value) => {
                // 根据数值大小自动调整单位
                if (value >= 1000) {
                  return `${(value / 1000).toFixed(1)}k`;
                }
                if (value >= 1000000) {
                  return `${(value / 1000000).toFixed(1)}M`;
                }
                return value.toFixed(1);
              }}
            />

            <ChartTooltip
              cursor={false}
              content={
                <ChartTooltipContent
                  indicator="dot"
                  labelFormatter={(value) => value}
                />
              }
            />

            {/* === 3. 线条渲染 === */}
            {series.map((item) => (
              <Area
                key={item.key}
                dataKey={item.key}
                type="natural"
                fill={`url(#fill-${item.key})`}
                stroke={`var(--color-${item.key})`}
                // 删除 stackId，使用非堆叠模式
              />
            ))}
          </AreaChart>
        </ChartContainer>
      </CardContent>
    </Card>
  );
}
