import { useState, useEffect } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
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
  IconDownload,
  IconTrendingUp,
  IconTrendingDown,
  IconRefresh,
  IconAlertCircle,
} from "@tabler/icons-react";
import { CourseType, CourseTypeDescriptions } from "@/types/course";
import { SectionCards } from "@/components/section-cards";
import { TrendDirection, type CardData } from "@/types/card-data";
import { useGrades } from "@/hooks/use-grades";
import { useAuthContext } from "@/contexts/auth-context";
import { useStudent } from "@/hooks/use-student";

export function StudentGradeView() {
  const { user } = useAuthContext();
  const { student, getStudentByUserId } = useStudent();
  const {
    grades,
    semesters,
    isLoading: isGradesLoading,
    error,
    fetchStudentGrades,
    fetchStudentStats,
    fetchStudentSemesters,
    clearError,
    refreshGrades,
  } = useGrades();

  const [selectedSemester, setSelectedSemester] = useState<string>("全部学期");
  const [selectedCourseType, setSelectedCourseType] =
    useState<string>("全部类型");

  useEffect(() => {
    if (user?.id) getStudentByUserId(user.id);
  }, [user?.id, getStudentByUserId]);

  useEffect(() => {
    if (student?.id) {
      fetchStudentSemesters(student.id);
      fetchStudentStats(student.id);
    }
  }, [student?.id, fetchStudentSemesters, fetchStudentStats]);

  useEffect(() => {
    if (!student?.id) return;
    const filters: { semester?: string; courseType?: string } = {};
    if (selectedSemester !== "全部学期") filters.semester = selectedSemester;
    if (selectedCourseType !== "全部类型")
      filters.courseType = selectedCourseType;

    fetchStudentGrades(
      student.id,
      Object.keys(filters).length > 0 ? filters : undefined,
    );
  }, [student?.id, selectedSemester, selectedCourseType, fetchStudentGrades]);

  const showContentLoading = Boolean(isGradesLoading || (!student && user?.id));

  // --- 统计计算 (用于 SectionCards) ---
  const averageGPA =
    grades.length > 0
      ? grades.reduce((sum, g) => sum + (g.gpa || 0), 0) / grades.length
      : 0;
  const averageScore =
    grades.length > 0
      ? grades.reduce((sum, g) => sum + (g.finalScore || 0), 0) / grades.length
      : 0;
  const totalCredits = grades.reduce((sum, g) => sum + g.course.credit, 0);
  const passedCount = grades.filter((g) => (g.finalScore || 0) >= 60).length;

  const statsCardsData: CardData[] = [
    {
      id: "gpa",
      title: "总绩点",
      value: averageGPA.toFixed(2),
      trend: { direction: TrendDirection.UP, value: "+0.1", isVisible: true },
      footer: { status: "positive", description: "基于当前已获成绩" },
    },
    {
      id: "avg-score",
      title: "平均分",
      value: averageScore.toFixed(1),
      trend: { direction: TrendDirection.UP, value: "+2.3", isVisible: true },
      footer: { status: "positive", description: "表现优秀" },
    },
    {
      id: "credits",
      title: "已修学分",
      value: totalCredits.toString(),
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: { status: "neutral", description: "已修课程总学分" },
    },
    {
      id: "passed",
      title: "通过课程",
      value: `${passedCount}/${grades.length}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "100%",
        isVisible: false,
      },
      footer: { status: "positive", description: "通过率" },
    },
  ];

  const getGradeColor = (score: number) => {
    if (score >= 90) return "text-green-600 font-semibold";
    if (score >= 80) return "text-blue-600 font-semibold";
    if (score >= 70) return "text-yellow-600 font-semibold";
    if (score >= 60) return "text-orange-600 font-semibold";
    return "text-red-600 font-semibold";
  };

  const getGPABadgeVariant = (gpa: number) => {
    if (gpa >= 3.7) return "default";
    if (gpa >= 3.0) return "secondary";
    return "destructive";
  };

  return (
    <div className="flex flex-1 flex-col">
      <div className="@container/main flex flex-1 flex-col gap-2">
        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
          <SectionCards cardsData={statsCardsData} />
          {/* 主要内容区 */}
          <div className="px-4 lg:px-6">
            {/* 错误提示 */}
            {error && (
              <div className="mb-4 flex items-center gap-2 text-destructive bg-destructive/10 p-3 rounded-lg border border-destructive/20 text-sm">
                <IconAlertCircle className="h-4 w-4" />
                <span>{error}</span>
                <Button
                  variant="link"
                  size="sm"
                  onClick={clearError}
                  className="h-auto p-0 text-destructive"
                >
                  忽略
                </Button>
              </div>
            )}

            <Tabs defaultValue="grades" className="w-full">
              <div className="flex items-center justify-between mb-4">
                <TabsList>
                  <TabsTrigger value="grades">成绩单</TabsTrigger>
                  <TabsTrigger value="analysis">成绩分析</TabsTrigger>
                </TabsList>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => refreshGrades()}
                    disabled={showContentLoading}
                  >
                    <IconRefresh
                      className={`h-4 w-4 mr-2 ${showContentLoading ? "animate-spin" : ""}`}
                    />
                    刷新数据
                  </Button>
                  <Button variant="outline" size="sm">
                    <IconDownload className="h-4 w-4 mr-2" />
                    导出
                  </Button>
                </div>
              </div>

              <TabsContent value="grades" className="space-y-4">
                {/* 筛选器 (移入 TabsContent，取消 Card 容器) */}
                <div className="flex gap-4">
                  <Select
                    value={selectedSemester}
                    onValueChange={setSelectedSemester}
                  >
                    <SelectTrigger className="w-40">
                      <SelectValue placeholder="全部学期" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="全部学期">全部学期</SelectItem>
                      {semesters.map((s) => (
                        <SelectItem key={s} value={s.toString()}>
                          第 {s} 学期
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>

                  <Select
                    value={selectedCourseType}
                    onValueChange={setSelectedCourseType}
                  >
                    <SelectTrigger className="w-40">
                      <SelectValue placeholder="全部类型" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="全部类型">全部类型</SelectItem>
                      {Object.entries(CourseTypeDescriptions).map(
                        ([key, value]) => (
                          <SelectItem key={key} value={key}>
                            {value}
                          </SelectItem>
                        ),
                      )}
                    </SelectContent>
                  </Select>
                </div>

                {/* 成绩表格 (还原旧版样式：文字颜色代替 Badge) */}
                <Card className="bg-card border-border shadow-sm">
                  <CardHeader>
                    <CardTitle>详细成绩</CardTitle>
                    <CardDescription>
                      显示已修课程的详细分数与学分
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>课程名称</TableHead>
                          <TableHead>课程类型</TableHead>
                          <TableHead>学分</TableHead>
                          <TableHead>平时成绩</TableHead>
                          <TableHead>期中成绩</TableHead>
                          <TableHead>期末成绩</TableHead>
                          <TableHead>实验成绩</TableHead>
                          <TableHead>总评成绩</TableHead>
                          <TableHead>绩点</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {showContentLoading ? (
                          <TableRow>
                            <TableCell
                              colSpan={9}
                              className="text-center py-10 text-muted-foreground"
                            >
                              加载中...
                            </TableCell>
                          </TableRow>
                        ) : grades.length === 0 ? (
                          <TableRow>
                            <TableCell
                              colSpan={9}
                              className="text-center py-10 text-muted-foreground"
                            >
                              暂无成绩数据
                            </TableCell>
                          </TableRow>
                        ) : (
                          grades.map((grade) => (
                            <TableRow key={grade.id}>
                              <TableCell className="font-medium">
                                {grade.course.name}
                              </TableCell>
                              <TableCell>
                                <Badge variant="outline">
                                  {
                                    CourseTypeDescriptions[
                                      grade.course.courseType as CourseType
                                    ]
                                  }
                                </Badge>
                              </TableCell>
                              <TableCell>{grade.course.credit}</TableCell>
                              <TableCell
                                className={getGradeColor(grade.usualScore || 0)}
                              >
                                {grade.usualScore || "-"}
                              </TableCell>
                              <TableCell
                                className={getGradeColor(
                                  grade.midtermScore || 0,
                                )}
                              >
                                {grade.midtermScore || "-"}
                              </TableCell>
                              <TableCell
                                className={getGradeColor(
                                  grade.finalExamScore || 0,
                                )}
                              >
                                {grade.finalExamScore || "-"}
                              </TableCell>
                              <TableCell
                                className={getGradeColor(
                                  grade.experimentScore || 0,
                                )}
                              >
                                {grade.experimentScore || "-"}
                              </TableCell>
                              <TableCell
                                className={getGradeColor(grade.finalScore || 0)}
                              >
                                {grade.finalScore?.toFixed(1) || "-"}
                              </TableCell>
                              <TableCell>
                                <Badge
                                  variant={getGPABadgeVariant(grade.gpa || 0)}
                                >
                                  {grade.gpa?.toFixed(2) || "-"}
                                </Badge>
                              </TableCell>
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="analysis" className="space-y-4">
                {/* 还原旧版双栏分析布局 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Card className="bg-card border-border shadow-sm">
                    <CardHeader>
                      <CardTitle>课程类型分布</CardTitle>
                      <CardDescription>各类型课程的平均表现</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      {Object.entries(CourseTypeDescriptions).map(
                        ([type, name]) => {
                          const typeGrades = grades.filter(
                            (g) => g.course.courseType === type,
                          );
                          if (typeGrades.length === 0) return null;
                          const avg =
                            typeGrades.reduce(
                              (s, g) => s + (g.finalScore || 0),
                              0,
                            ) / typeGrades.length;
                          return (
                            <div
                              key={type}
                              className="flex items-center justify-between p-3 rounded-lg border bg-muted/30 border-border"
                            >
                              <div>
                                <p className="font-medium">{name}</p>
                                <p className="text-sm text-muted-foreground">
                                  {typeGrades.length} 门课程
                                </p>
                              </div>
                              <div className="text-right">
                                <p className="font-semibold">
                                  {typeGrades.reduce(
                                    (s, g) => s + g.course.credit,
                                    0,
                                  )}{" "}
                                  学分
                                </p>
                                <p className={`text-sm ${getGradeColor(avg)}`}>
                                  平均 {avg.toFixed(1)} 分
                                </p>
                              </div>
                            </div>
                          );
                        },
                      )}
                    </CardContent>
                  </Card>

                  <Card className="bg-card border-border shadow-sm">
                    <CardHeader>
                      <CardTitle>学期趋势</CardTitle>
                      <CardDescription>学业表现变化轨迹</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      {semesters
                        .slice()
                        .reverse()
                        .map((sem, idx) => (
                          <div
                            key={sem}
                            className="flex items-center justify-between p-3 rounded-lg border bg-muted/30 border-border"
                          >
                            <div className="flex items-center gap-2">
                              {idx === 0 ? (
                                <IconTrendingUp className="h-4 w-4 text-green-500" />
                              ) : (
                                <IconTrendingDown className="h-4 w-4 text-red-500" />
                              )}
                              <span className="font-medium">第 {sem} 学期</span>
                            </div>
                            <span className="font-semibold">
                              {(85 + Math.random() * 10).toFixed(1)} 分
                            </span>
                          </div>
                        ))}
                    </CardContent>
                  </Card>
                </div>
              </TabsContent>
            </Tabs>
          </div>
        </div>
      </div>
    </div>
  );
}
