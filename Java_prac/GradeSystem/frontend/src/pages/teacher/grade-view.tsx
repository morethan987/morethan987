import { useState, useEffect, useMemo } from "react";
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
import { useAuthContext } from "@/contexts/auth-context";
import { useTeacher } from "@/hooks/use-teacher";
import { teacherApi } from "@/api/v1/modules/teacher";
import type {
  StudentGradeInput,
  DistributionData,
} from "@/types/teaching-class";

type SortField =
  | "usualScore"
  | "midtermScore"
  | "finalExamScore"
  | "experimentScore"
  | "finalScore"
  | "gpa";
type SortOrder = "asc" | "desc" | null;

const calculateStats = (grades: StudentGradeInput[]) => {
  const validGrades = grades.filter((g) => g.finalScore != null);
  if (validGrades.length === 0) {
    return {
      avgScore: "-",
      maxScore: "-",
      minScore: "-",
      passRate: "-",
      excellentRate: "-",
    };
  }

  const scores = validGrades.map((g) => g.finalScore!);
  const avgScore = scores.reduce((sum, s) => sum + s, 0) / scores.length;
  const maxScore = Math.max(...scores);
  const minScore = Math.min(...scores);
  const passCount = scores.filter((s) => s >= 60).length;
  const excellentCount = scores.filter((s) => s >= 85).length;

  return {
    avgScore: avgScore.toFixed(1),
    maxScore: maxScore.toFixed(1),
    minScore: minScore.toFixed(1),
    passRate: ((passCount / scores.length) * 100).toFixed(1),
    excellentRate: ((excellentCount / scores.length) * 100).toFixed(1),
  };
};

const chartConfig = {
  count: {
    label: "人数",
    color: "var(--primary)",
  },
} satisfies ChartConfig;

export function TeacherGradeView() {
  const { user } = useAuthContext();
  const {
    teachingClasses,
    students,
    distribution,
    loading,
    error,
    getTeachingClasses,
    getStudentsInTeachingClass,
    getGradeDistribution,
  } = useTeacher();

  const [teacherId, setTeacherId] = useState<string | null>(null);
  const [selectedClassId, setSelectedClassId] = useState<string>("");
  const [sortField, setSortField] = useState<SortField>("finalScore");
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc");
  const [initError, setInitError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) return;

    const fetchTeacher = async () => {
      try {
        const teacher = await teacherApi.getTeacherByUserId(user.id);
        setTeacherId(teacher.id);
        await getTeachingClasses(teacher.id);
      } catch (err) {
        const message = err instanceof Error ? err.message : "获取教师信息失败";
        setInitError(message);
      }
    };

    fetchTeacher();
  }, [user, getTeachingClasses]);

  useEffect(() => {
    if (teachingClasses.length > 0 && !selectedClassId) {
      const firstClass = teachingClasses[0];
      if (firstClass) {
        setSelectedClassId(firstClass.id);
      }
    }
  }, [teachingClasses, selectedClassId]);

  useEffect(() => {
    if (selectedClassId) {
      getStudentsInTeachingClass(selectedClassId);
      getGradeDistribution(selectedClassId);
    }
  }, [selectedClassId, getStudentsInTeachingClass, getGradeDistribution]);

  const handleClassChange = (classId: string) => {
    setSelectedClassId(classId);
  };

  const stats = useMemo(() => calculateStats(students), [students]);

  const yAxisDomain = useMemo(() => {
    if (distribution.length === 0) return [0, 10];

    let min = Number.MAX_VALUE;
    let max = Number.MIN_VALUE;

    distribution.forEach((item: DistributionData) => {
      const value = Number(item.count);
      if (!isNaN(value)) {
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    });

    min = Math.max(0, min - (max - min) * 0.1);
    const padding = (max - min) * 0.1;
    return [min, max + padding];
  }, [distribution]);

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

  const sortedGrades = useMemo(() => {
    return [...students].sort((a, b) => {
      if (!sortOrder) return 0;
      const aVal = a[sortField];
      const bVal = b[sortField];
      if (aVal == null && bVal == null) return 0;
      if (aVal == null) return sortOrder === "asc" ? -1 : 1;
      if (bVal == null) return sortOrder === "asc" ? 1 : -1;
      if (typeof aVal === "number" && typeof bVal === "number") {
        return sortOrder === "asc" ? aVal - bVal : bVal - aVal;
      }
      return 0;
    });
  }, [students, sortField, sortOrder]);

  const renderSortIcon = (field: SortField) => {
    if (sortField !== field) return null;
    if (sortOrder === "asc") return <IconChevronUp className="size-4" />;
    if (sortOrder === "desc") return <IconChevronDown className="size-4" />;
    return null;
  };

  const getGradeBadge = (score: number | null | undefined) => {
    if (score == null) return <Badge variant="outline">未录入</Badge>;
    if (score >= 90) return <Badge className="bg-emerald-500">优秀</Badge>;
    if (score >= 80) return <Badge className="bg-blue-500">良好</Badge>;
    if (score >= 70) return <Badge className="bg-yellow-500">中等</Badge>;
    if (score >= 60) return <Badge className="bg-orange-500">及格</Badge>;
    return <Badge variant="destructive">不及格</Badge>;
  };

  const displayError = error || initError;

  if (loading && !teacherId) {
    return (
      <div className="flex flex-1 items-center justify-center p-6">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p>正在加载...</p>
        </div>
      </div>
    );
  }

  if (displayError && !teacherId) {
    return (
      <div className="flex flex-1 items-center justify-center p-6">
        <div className="text-center text-destructive">
          <p>加载失败: {displayError}</p>
        </div>
      </div>
    );
  }

  const avgUsual =
    students.length > 0
      ? (
          students.reduce((sum, g) => sum + (g.usualScore ?? 0), 0) /
          students.filter((g) => g.usualScore != null).length
        ).toFixed(1)
      : "-";
  const avgMidterm =
    students.length > 0
      ? (
          students.reduce((sum, g) => sum + (g.midtermScore ?? 0), 0) /
          students.filter((g) => g.midtermScore != null).length
        ).toFixed(1)
      : "-";
  const avgFinal =
    students.length > 0
      ? (
          students.reduce((sum, g) => sum + (g.finalExamScore ?? 0), 0) /
          students.filter((g) => g.finalExamScore != null).length
        ).toFixed(1)
      : "-";
  const avgExperiment =
    students.length > 0
      ? (
          students.reduce((sum, g) => sum + (g.experimentScore ?? 0), 0) /
          students.filter((g) => g.experimentScore != null).length
        ).toFixed(1)
      : "-";

  return (
    <div className="flex flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">成绩查看</h1>
          <p className="text-muted-foreground">查看学生成绩及统计分析</p>
        </div>
        <div className="flex items-center gap-2">
          <Select value={selectedClassId} onValueChange={handleClassChange}>
            <SelectTrigger className="w-[250px]">
              <SelectValue placeholder="选择教学班" />
            </SelectTrigger>
            <SelectContent>
              {teachingClasses.map((tc) => (
                <SelectItem key={tc.id} value={tc.id}>
                  {tc.courseName} - {tc.className}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {selectedClassId && (
        <>
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
                <CardTitle className="text-2xl">
                  {stats.passRate !== "-" ? `${stats.passRate}%` : "-"}
                </CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>优秀率</CardDescription>
                <CardTitle className="text-2xl">
                  {stats.excellentRate !== "-"
                    ? `${stats.excellentRate}%`
                    : "-"}
                </CardTitle>
              </CardHeader>
            </Card>
          </div>

          <Tabs defaultValue="table" className="w-full">
            <TabsList>
              <TabsTrigger value="table">表格视图</TabsTrigger>
              <TabsTrigger value="chart">分布图表</TabsTrigger>
            </TabsList>

            <TabsContent value="table" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle>学生成绩列表</CardTitle>
                  <CardDescription>
                    点击表头可进行排序，共 {students.length} 名学生
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {loading ? (
                    <div className="flex justify-center py-8">
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                    </div>
                  ) : students.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      暂无学生数据
                    </div>
                  ) : (
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
                          <TableRow key={grade.id || grade.studentCode}>
                            <TableCell>{grade.studentCode}</TableCell>
                            <TableCell className="font-medium">
                              {grade.name}
                            </TableCell>
                            <TableCell>{grade.className}</TableCell>
                            <TableCell>{grade.usualScore ?? "-"}</TableCell>
                            <TableCell>{grade.midtermScore ?? "-"}</TableCell>
                            <TableCell>{grade.finalExamScore ?? "-"}</TableCell>
                            <TableCell>
                              {grade.experimentScore ?? "-"}
                            </TableCell>
                            <TableCell className="font-bold">
                              {grade.finalScore != null
                                ? grade.finalScore.toFixed(1)
                                : "-"}
                            </TableCell>
                            <TableCell>
                              {grade.gpa != null ? grade.gpa.toFixed(2) : "-"}
                            </TableCell>
                            <TableCell>
                              {getGradeBadge(grade.finalScore)}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="chart" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle>成绩分布图</CardTitle>
                  <CardDescription>学生成绩区间分布统计</CardDescription>
                </CardHeader>
                <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
                  {distribution.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      暂无分布数据
                    </div>
                  ) : (
                    <ChartContainer
                      config={chartConfig}
                      className="aspect-auto h-[300px] w-full"
                    >
                      <AreaChart
                        data={distribution}
                        margin={{ left: 10, right: 10 }}
                      >
                        <defs>
                          <linearGradient
                            id="fill-count"
                            x1="0"
                            y1="0"
                            x2="0"
                            y2="1"
                          >
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
                  )}
                </CardContent>
              </Card>

              <div className="grid grid-cols-1 gap-4 mt-4 md:grid-cols-4">
                <Card>
                  <CardHeader className="pb-2">
                    <CardDescription>平时成绩平均</CardDescription>
                    <CardTitle className="text-2xl">{avgUsual}</CardTitle>
                  </CardHeader>
                </Card>
                <Card>
                  <CardHeader className="pb-2">
                    <CardDescription>期中成绩平均</CardDescription>
                    <CardTitle className="text-2xl">{avgMidterm}</CardTitle>
                  </CardHeader>
                </Card>
                <Card>
                  <CardHeader className="pb-2">
                    <CardDescription>期末成绩平均</CardDescription>
                    <CardTitle className="text-2xl">{avgFinal}</CardTitle>
                  </CardHeader>
                </Card>
                <Card>
                  <CardHeader className="pb-2">
                    <CardDescription>实验成绩平均</CardDescription>
                    <CardTitle className="text-2xl">{avgExperiment}</CardTitle>
                  </CardHeader>
                </Card>
              </div>
            </TabsContent>
          </Tabs>
        </>
      )}

      {!selectedClassId && teachingClasses.length > 0 && (
        <div className="flex flex-1 items-center justify-center">
          <p className="text-muted-foreground">请选择一个教学班查看成绩</p>
        </div>
      )}

      {!selectedClassId && teachingClasses.length === 0 && !loading && (
        <div className="flex flex-1 items-center justify-center">
          <p className="text-muted-foreground">暂无教学班数据</p>
        </div>
      )}
    </div>
  );
}
