import { useState } from "react";
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
} from "@tabler/icons-react";
import type { Grade } from "@/types/grade";
import type { Course, CourseType } from "@/types/course";
import { CourseTypeDescriptions } from "@/types/course";
import { SectionCards } from "@/components/section-cards";
import { TrendDirection } from "@/types/card-data";

// 样例数据
const sampleGrades: Grade[] = [
  {
    id: "1",
    student: {
      id: "stu001",
      user: {
        id: "u001",
        username: "张三",
        email: "zhangsan@example.com",
        role: "STUDENT",
        isActive: true,
      },
      studentCode: "2022001234",
      major: "计算机科学与技术",
      className: "计科22-1班",
      enrollmentYear: 2022,
      currentSemester: 5,
      status: "ENROLLED" as any,
      totalCredits: 86,
      createdAt: "2022-09-01T00:00:00Z",
      updatedAt: "2024-12-01T00:00:00Z",
    },
    course: {
      id: "cs001",
      name: "数据结构与算法",
      description: "介绍基本数据结构和算法设计分析",
      credit: 4,
      semester: 3,
      courseType: "REQUIRED",
    },
    usualScore: 88,
    midtermScore: 85,
    finalExamScore: 90,
    experimentScore: 92,
    finalScore: 89,
    gpa: 3.9,
  },
  {
    id: "2",
    student: {} as any,
    course: {
      id: "cs002",
      name: "操作系统",
      description: "操作系统原理与实现",
      credit: 3,
      semester: 4,
      courseType: "REQUIRED",
    },
    usualScore: 85,
    midtermScore: 88,
    finalExamScore: 87,
    experimentScore: 90,
    finalScore: 87,
    gpa: 3.7,
  },
  {
    id: "3",
    student: {} as any,
    course: {
      id: "cs003",
      name: "计算机网络",
      description: "计算机网络基础知识与协议",
      credit: 3,
      semester: 4,
      courseType: "REQUIRED",
    },
    usualScore: 92,
    midtermScore: 90,
    finalExamScore: 94,
    experimentScore: 95,
    finalScore: 93,
    gpa: 4.0,
  },
  {
    id: "4",
    student: {} as any,
    course: {
      id: "cs004",
      name: "Web开发技术",
      description: "前端后端Web开发技术",
      credit: 2,
      semester: 5,
      courseType: "ELECTIVE",
    },
    usualScore: 95,
    midtermScore: 92,
    finalExamScore: 96,
    experimentScore: 98,
    finalScore: 95,
    gpa: 4.0,
  },
  {
    id: "5",
    student: {} as any,
    course: {
      id: "cs005",
      name: "软件工程",
      description: "软件开发生命周期管理",
      credit: 3,
      semester: 5,
      courseType: "REQUIRED",
    },
    usualScore: 80,
    midtermScore: 78,
    finalExamScore: 82,
    experimentScore: 85,
    finalScore: 81,
    gpa: 3.1,
  },
];

const sampleSemesters = [
  "全部学期",
  "2022秋",
  "2023春",
  "2023秋",
  "2024春",
  "2024秋",
];

export function GradeView() {
  const [selectedSemester, setSelectedSemester] = useState("全部学期");
  const [selectedCourseType, setSelectedCourseType] = useState("全部类型");

  // 统计数据
  const totalCredits = sampleGrades.reduce(
    (sum, grade) => sum + grade.course.credit,
    0,
  );
  const averageGPA =
    sampleGrades.reduce((sum, grade) => sum + (grade.gpa || 0), 0) /
    sampleGrades.length;
  const averageScore =
    sampleGrades.reduce((sum, grade) => sum + (grade.finalScore || 0), 0) /
    sampleGrades.length;
  const passedCourses = sampleGrades.filter(
    (grade) => (grade.finalScore || 0) >= 60,
  ).length;

  const statsCardsData = [
    {
      id: "gpa",
      title: "总绩点",
      value: averageGPA.toFixed(2),
      trend: {
        direction: TrendDirection.UP,
        value: "+0.2",
        isVisible: true,
      },
      footer: {
        status: "较上学期提升",
        description: "基于所有已修课程",
      },
    },
    {
      id: "average-score",
      title: "平均分",
      value: averageScore.toFixed(1),
      trend: {
        direction: TrendDirection.UP,
        value: "+2.3",
        isVisible: true,
      },
      footer: {
        status: "表现优秀",
        description: "专业排名前10%",
      },
    },
    {
      id: "total-credits",
      title: "总学分",
      value: `${totalCredits}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: {
        status: "已修读学分",
        description: `还需${120 - totalCredits}学分毕业`,
      },
    },
    {
      id: "passed-courses",
      title: "通过课程",
      value: `${passedCourses}/${sampleGrades.length}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "100%",
        isVisible: false,
      },
      footer: {
        status: "通过率",
        description: "所有课程均已通过",
      },
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
          {/* 统计卡片 */}
          <SectionCards cardsData={statsCardsData} />

          {/* 主要内容 */}
          <div className="px-4 lg:px-6">
            <Tabs defaultValue="grades" className="w-full">
              <div className="flex items-center justify-between mb-4">
                <TabsList>
                  <TabsTrigger value="grades">成绩单</TabsTrigger>
                  <TabsTrigger value="analysis">成绩分析</TabsTrigger>
                </TabsList>
                <Button variant="outline" size="sm">
                  <IconDownload className="h-4 w-4 mr-2" />
                  导出成绩单
                </Button>
              </div>

              <TabsContent value="grades" className="space-y-4">
                {/* 筛选器 */}
                <div className="flex gap-4">
                  <Select
                    value={selectedSemester}
                    onValueChange={setSelectedSemester}
                  >
                    <SelectTrigger className="w-40">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {sampleSemesters.map((semester) => (
                        <SelectItem key={semester} value={semester}>
                          {semester}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>

                  <Select
                    value={selectedCourseType}
                    onValueChange={setSelectedCourseType}
                  >
                    <SelectTrigger className="w-40">
                      <SelectValue />
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

                {/* 成绩表格 */}
                <Card className="bg-card border-border shadow-sm">
                  <CardHeader>
                    <CardTitle>详细成绩</CardTitle>
                    <CardDescription>
                      显示所有已修课程的详细成绩信息
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
                        {sampleGrades.map((grade) => (
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
                              className={getGradeColor(grade.midtermScore || 0)}
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
                              {grade.finalScore || "-"}
                            </TableCell>
                            <TableCell>
                              <Badge
                                variant={getGPABadgeVariant(grade.gpa || 0)}
                              >
                                {grade.gpa?.toFixed(1) || "-"}
                              </Badge>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="analysis" className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Card className="bg-card border-border shadow-sm">
                    <CardHeader>
                      <CardTitle>课程类型分布</CardTitle>
                      <CardDescription>
                        不同类型课程的学分和成绩统计
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      {Object.entries(CourseTypeDescriptions).map(
                        ([type, name]) => {
                          const typeGrades = sampleGrades.filter(
                            (g) => g.course.courseType === type,
                          );
                          const typeCredits = typeGrades.reduce(
                            (sum, g) => sum + g.course.credit,
                            0,
                          );
                          const typeAvgScore =
                            typeGrades.length > 0
                              ? typeGrades.reduce(
                                  (sum, g) => sum + (g.finalScore || 0),
                                  0,
                                ) / typeGrades.length
                              : 0;

                          return (
                            typeGrades.length > 0 && (
                              <div
                                key={type}
                                className="flex items-center justify-between p-3 rounded-lg border bg-muted/30 dark:bg-muted/50 border-border"
                              >
                                <div>
                                  <p className="font-medium">{name}</p>
                                  <p className="text-sm text-muted-foreground">
                                    {typeGrades.length}门课程
                                  </p>
                                </div>
                                <div className="text-right">
                                  <p className="font-semibold">
                                    {typeCredits}学分
                                  </p>
                                  <p className="text-sm text-muted-foreground">
                                    平均{typeAvgScore.toFixed(1)}分
                                  </p>
                                </div>
                              </div>
                            )
                          );
                        },
                      )}
                    </CardContent>
                  </Card>

                  <Card className="bg-card border-border shadow-sm">
                    <CardHeader>
                      <CardTitle>成绩趋势</CardTitle>
                      <CardDescription>学期成绩变化趋势</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        {["2023春", "2023秋", "2024春", "2024秋"].map(
                          (semester, index) => {
                            const trend = index % 2 === 0 ? "up" : "down";
                            const score = 85 + Math.random() * 10;
                            return (
                              <div
                                key={semester}
                                className="flex items-center justify-between p-3 rounded-lg border bg-muted/30 dark:bg-muted/50 border-border"
                              >
                                <div className="flex items-center gap-2">
                                  {trend === "up" ? (
                                    <IconTrendingUp className="h-4 w-4 text-green-500" />
                                  ) : (
                                    <IconTrendingDown className="h-4 w-4 text-red-500" />
                                  )}
                                  <span className="font-medium">
                                    {semester}
                                  </span>
                                </div>
                                <div className="text-right">
                                  <span className="font-semibold">
                                    {score.toFixed(1)}
                                  </span>
                                  <span className="text-sm text-muted-foreground ml-1">
                                    分
                                  </span>
                                </div>
                              </div>
                            );
                          },
                        )}
                      </div>
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
