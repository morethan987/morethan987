import { useState } from "react";
import React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { IconChevronUp, IconChevronDown } from "@tabler/icons-react";
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart";
import type {
  StudentGrade,
  DistributionData,
  TeacherGradeViewProps,
  SortField,
  SortOrder,
} from "@/types/teacher-grade-view";

// 计算统计数据
const calculateStats = (grades: StudentGrade[]) => {
  const avgScore =
    grades.reduce((sum, g) => sum + g.finalScore, 0) / grades.length;
  const maxScore = Math.max(...grades.map((g) => g.finalScore));
  const minScore = Math.min(...grades.map((g) => g.finalScore));
  const passCount = grades.filter((g) => g.finalScore >= 60).length;
  const excellentCount = grades.filter((g) => g.finalScore >= 85).length;

  return {
    avgScore: avgScore.toFixed(1),
    maxScore: maxScore.toFixed(1),
    minScore: minScore.toFixed(1),
    passRate: ((passCount / grades.length) * 100).toFixed(1),
    excellentRate: ((excellentCount / grades.length) * 100).toFixed(1),
  };
};

const chartConfig = {
  count: {
    label: "人数",
    color: "var(--primary)",
  },
} satisfies ChartConfig;

export function TeacherGradeView({
  distribution,
  grades,
}: TeacherGradeViewProps) {
  const [sortField, setSortField] = useState<SortField>("finalScore");
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc");
  const [selectedClass, setSelectedClass] = useState<string>("all");

  const stats = calculateStats(grades);

  // 计算Y轴的范围，用于自动调整刻度
  const yAxisDomain = React.useMemo(() => {
    let min = Number.MAX_VALUE;
    let max = Number.MIN_VALUE;

    distribution.forEach((item: DistributionData) => {
      const value = Number(item.count);
      if (!isNaN(value)) {
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    });

    // 最小值不能小于0并且添加10%的边距
    min = Math.max(0, min - (max - min) * 0.1);

    // 添加20%的边距，避免数据点贴着边界
    const padding = (max - min) * 0.1;
    return [min, max + padding];
  }, [distribution]);

  // 排序函数
  const handleSort = (field: SortField) => {
    if (sortField === field) {
      if (sortOrder === "desc") {
        setSortOrder("asc");
      } else if (sortOrder === "asc") {
        setSortOrder(null);
      } else {
        setSortOrder("desc");
      }
    } else {
      setSortField(field);
      setSortOrder("desc");
    }
  };

  // 排序后的数据
  const sortedGrades = [...grades].sort((a, b) => {
    if (!sortOrder) return 0;
    const aVal = a[sortField];
    const bVal = b[sortField];
    if (typeof aVal === "number" && typeof bVal === "number") {
      return sortOrder === "asc" ? aVal - bVal : bVal - aVal;
    }
    return 0;
  });

  // 渲染排序图标
  const renderSortIcon = (field: SortField) => {
    if (sortField !== field) return null;
    if (sortOrder === "asc") return <IconChevronUp className="size-4" />;
    if (sortOrder === "desc") return <IconChevronDown className="size-4" />;
    return null;
  };

  // 获取成绩等级样式
  const getGradeBadge = (score: number) => {
    if (score >= 90) return <Badge className="bg-emerald-500">优秀</Badge>;
    if (score >= 80) return <Badge className="bg-blue-500">良好</Badge>;
    if (score >= 70) return <Badge className="bg-yellow-500">中等</Badge>;
    if (score >= 60) return <Badge className="bg-orange-500">及格</Badge>;
    return <Badge variant="destructive">不及格</Badge>;
  };

  return (
    <div className="flex flex-col gap-6 p-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">成绩查看</h1>
          <p className="text-muted-foreground">查看学生成绩及统计分析</p>
        </div>
        <div className="flex items-center gap-2">
          <Select value={selectedClass} onValueChange={setSelectedClass}>
            <SelectTrigger className="w-[200px]">
              <SelectValue placeholder="选择教学班" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部教学班</SelectItem>
              <SelectItem value="TC001">数据结构与算法</SelectItem>
              <SelectItem value="TC002">数据库系统原理</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* 统计卡片 */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-5">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>平均分</CardDescription>
            <CardTitle className="text-2xl">{stats.avgScore}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>最高分</CardDescription>
            <CardTitle className="text-2xl">{stats.maxScore}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>最低分</CardDescription>
            <CardTitle className="text-2xl">{stats.minScore}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>及格率</CardDescription>
            <CardTitle className="text-2xl">{stats.passRate}%</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>优秀率</CardDescription>
            <CardTitle className="text-2xl">{stats.excellentRate}%</CardTitle>
          </CardHeader>
        </Card>
      </div>

      {/* 切换视图 */}
      <Tabs defaultValue="table" className="w-full">
        <TabsList>
          <TabsTrigger value="table">表格视图</TabsTrigger>
          <TabsTrigger value="chart">分布图表</TabsTrigger>
        </TabsList>

        {/* 表格视图 */}
        <TabsContent value="table" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>学生成绩列表</CardTitle>
              <CardDescription>
                点击表头可进行排序，共 {grades.length} 名学生
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>学号</TableHead>
                    <TableHead>姓名</TableHead>
                    <TableHead>班级</TableHead>
                    <TableHead
                      className="cursor-pointer hover:bg-muted/50"
                      onClick={() => handleSort("usualScore")}
                    >
                      <div className="flex items-center gap-1">
                        平时成绩
                        {renderSortIcon("usualScore")}
                      </div>
                    </TableHead>
                    <TableHead
                      className="cursor-pointer hover:bg-muted/50"
                      onClick={() => handleSort("midtermScore")}
                    >
                      <div className="flex items-center gap-1">
                        期中成绩
                        {renderSortIcon("midtermScore")}
                      </div>
                    </TableHead>
                    <TableHead
                      className="cursor-pointer hover:bg-muted/50"
                      onClick={() => handleSort("finalExamScore")}
                    >
                      <div className="flex items-center gap-1">
                        期末成绩
                        {renderSortIcon("finalExamScore")}
                      </div>
                    </TableHead>
                    <TableHead
                      className="cursor-pointer hover:bg-muted/50"
                      onClick={() => handleSort("experimentScore")}
                    >
                      <div className="flex items-center gap-1">
                        实验成绩
                        {renderSortIcon("experimentScore")}
                      </div>
                    </TableHead>
                    <TableHead
                      className="cursor-pointer hover:bg-muted/50"
                      onClick={() => handleSort("finalScore")}
                    >
                      <div className="flex items-center gap-1">
                        总评成绩
                        {renderSortIcon("finalScore")}
                      </div>
                    </TableHead>
                    <TableHead
                      className="cursor-pointer hover:bg-muted/50"
                      onClick={() => handleSort("gpa")}
                    >
                      <div className="flex items-center gap-1">
                        绩点
                        {renderSortIcon("gpa")}
                      </div>
                    </TableHead>
                    <TableHead>等级</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sortedGrades.map((grade) => (
                    <TableRow key={grade.id}>
                      <TableCell>{grade.studentCode}</TableCell>
                      <TableCell className="font-medium">
                        {grade.name}
                      </TableCell>
                      <TableCell>{grade.className}</TableCell>
                      <TableCell>{grade.usualScore}</TableCell>
                      <TableCell>{grade.midtermScore}</TableCell>
                      <TableCell>{grade.finalExamScore}</TableCell>
                      <TableCell>{grade.experimentScore}</TableCell>
                      <TableCell className="font-bold">
                        {grade.finalScore.toFixed(1)}
                      </TableCell>
                      <TableCell>{grade.gpa.toFixed(2)}</TableCell>
                      <TableCell>{getGradeBadge(grade.finalScore)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* 分布图表 */}
        <TabsContent value="chart" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>成绩分布图</CardTitle>
              <CardDescription>学生成绩区间分布统计</CardDescription>
            </CardHeader>
            <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
              <ChartContainer
                config={chartConfig}
                className="aspect-auto h-[300px] w-full"
              >
                <AreaChart data={distribution} margin={{ left: 10, right: 10 }}>
                  <defs>
                    <linearGradient id="fill-count" x1="0" y1="0" x2="0" y2="1">
                      <stop
                        offset="5%"
                        stopColor="var(--color-count)"
                        stopOpacity={0.8}
                      />
                      <stop
                        offset="95%"
                        stopColor="var(--color-count)"
                        stopOpacity={0.1}
                      />
                    </linearGradient>
                  </defs>
                  <CartesianGrid vertical={false} />
                  <XAxis
                    dataKey="range"
                    tickLine={false}
                    axisLine={false}
                    tickMargin={8}
                    minTickGap={32}
                  />
                  <YAxis
                    tickLine={false}
                    axisLine={false}
                    domain={yAxisDomain}
                    tickMargin={8}
                    tickFormatter={(value) => value.toFixed(0)}
                  />
                  <ChartTooltip
                    cursor={false}
                    content={
                      <ChartTooltipContent
                        indicator="dot"
                        labelFormatter={(value) => `分数段: ${value}`}
                      />
                    }
                  />
                  <Area
                    dataKey="count"
                    type="natural"
                    fill="url(#fill-count)"
                    stroke="var(--color-count)"
                  />
                </AreaChart>
              </ChartContainer>
            </CardContent>
          </Card>

          {/* 各项成绩统计 */}
          <div className="grid grid-cols-1 gap-4 mt-4 md:grid-cols-4">
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>平时成绩平均</CardDescription>
                <CardTitle className="text-2xl">
                  {(
                    grades.reduce((sum, g) => sum + g.usualScore, 0) /
                    grades.length
                  ).toFixed(1)}
                </CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>期中成绩平均</CardDescription>
                <CardTitle className="text-2xl">
                  {(
                    grades.reduce((sum, g) => sum + g.midtermScore, 0) /
                    grades.length
                  ).toFixed(1)}
                </CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>期末成绩平均</CardDescription>
                <CardTitle className="text-2xl">
                  {(
                    grades.reduce((sum, g) => sum + g.finalExamScore, 0) /
                    grades.length
                  ).toFixed(1)}
                </CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>实验成绩平均</CardDescription>
                <CardTitle className="text-2xl">
                  {(
                    grades.reduce((sum, g) => sum + g.experimentScore, 0) /
                    grades.length
                  ).toFixed(1)}
                </CardTitle>
              </CardHeader>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}
