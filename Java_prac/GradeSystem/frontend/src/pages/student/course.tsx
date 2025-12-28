import { useState, useEffect, useMemo } from "react";
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
import { Input } from "@/components/ui/input";
import {
  IconSearch,
  IconBook2,
  IconClock,
  IconUsers,
  IconCalendarEvent,
  IconMapPin,
  IconRefresh,
  IconAlertCircle,
  IconLoader2,
} from "@tabler/icons-react";
import { CourseType, CourseTypeDescriptions } from "@/types/course";
import { SectionCards } from "@/components/section-cards";
import { TrendDirection } from "@/types/card-data";
import { useCourses } from "@/hooks/use-courses";
import { useAuth } from "@/hooks/use-auth";
import { useStudent } from "@/hooks/use-student";

export function StudentCourseView() {
  // --- 数据 Hook ---
  const { user } = useAuth();
  const { student, getStudentByUserId } = useStudent();
  const {
    courses,
    isLoading: isCoursesLoading,
    error,
    fetchStudentCourses,
    refreshCourses,
  } = useCourses();

  // --- 状态 ---
  const [selectedSemester, setSelectedSemester] = useState("全部学期");
  const [selectedCourseType, setSelectedCourseType] = useState("全部类型");
  const [searchQuery, setSearchQuery] = useState("");

  // --- 1. 获取学生档案 ---
  useEffect(() => {
    if (user?.id) getStudentByUserId(user.id);
  }, [user?.id, getStudentByUserId]);

  // --- 2. 加载课程数据 ---
  useEffect(() => {
    if (!student?.id) return;

    const filters: { semester?: number; courseType?: CourseType } = {};
    if (selectedSemester !== "全部学期")
      filters.semester = parseInt(selectedSemester);
    if (selectedCourseType !== "全部类型")
      filters.courseType = selectedCourseType as CourseType;

    fetchStudentCourses(
      student.id,
      Object.keys(filters).length > 0 ? filters : undefined,
    );
  }, [student?.id, selectedSemester, selectedCourseType, fetchStudentCourses]);

  // --- 3. 数据过滤逻辑 ---
  const filteredCourses = useMemo(() => {
    return courses.filter((c) => {
      const matchesSearch =
        c.course.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        c.teacherName.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesSearch;
    });
  }, [courses, searchQuery]);

  // --- 4. 统计计算 ---
  const stats = useMemo(() => {
    const ongoing = courses.filter((c) => c.status === "ongoing").length;
    const completed = courses.filter((c) => c.status === "completed").length;
    const totalCredits = courses.reduce((sum, c) => sum + c.course.credit, 0);
    return { ongoing, completed, totalCredits };
  }, [courses]);

  const statsCardsData = [
    {
      id: "ongoing-courses",
      title: "本学期课程",
      value: `${stats.ongoing}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: { status: "ongoing", description: "当前正在进行中" },
    },
    {
      id: "completed-courses",
      title: "已完成课程",
      value: `${stats.completed}`,
      trend: { direction: TrendDirection.UP, value: "+1", isVisible: true },
      footer: { status: "positive", description: "学业进度稳定" },
    },
    {
      id: "total-credits",
      title: "累计学分",
      value: `${stats.totalCredits}`,
      trend: { direction: TrendDirection.UP, value: "+4", isVisible: true },
      footer: {
        status: "positive",
        description: `距离毕业还需 ${Math.max(0, 120 - stats.totalCredits)} 学分`,
      },
    },
    {
      id: "average-load",
      title: "平均周课时",
      value: "18",
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: { status: "neutral", description: "课程强度适中" },
    },
  ];

  // --- 辅助函数 ---
  const getStatusBadge = (status: string) => {
    switch (status) {
      case "ongoing":
        return (
          <Badge className="bg-green-100 text-green-800 border-none">
            进行中
          </Badge>
        );
      case "completed":
        return <Badge variant="secondary">已完成</Badge>;
      case "upcoming":
        return (
          <Badge className="bg-blue-100 text-blue-800 border-none">
            即将开始
          </Badge>
        );
      default:
        return <Badge variant="outline">未知</Badge>;
    }
  };

  const getEnrollmentColor = (enrolled: number, capacity: number) => {
    const ratio = enrolled / capacity;
    if (ratio >= 0.9) return "text-red-600";
    if (ratio >= 0.7) return "text-yellow-600";
    return "text-green-600";
  };

  // 课表解析逻辑 (匹配 "周一 1-2节")
  const getCourseAt = (day: string, period: number) => {
    return courses.find(
      (c) =>
        c.status === "ongoing" &&
        c.timeSchedule.includes(day) &&
        c.timeSchedule.includes(`${period}`),
    );
  };

  const isInitialLoading = !student && user?.id;

  return (
    <div className="flex flex-1 flex-col">
      <div className="@container/main flex flex-1 flex-col gap-2">
        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
          {/* 顶部标题与刷新 */}
          <div className="px-4 lg:px-6 flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold tracking-tight">
                教学教务系统
              </h1>
              <p className="text-sm text-muted-foreground">
                欢迎回来，{user?.username || "加载中..."}
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => refreshCourses()}
              disabled={isCoursesLoading}
            >
              <IconRefresh
                className={`mr-2 h-4 w-4 ${isCoursesLoading ? "animate-spin" : ""}`}
              />
              刷新数据
            </Button>
          </div>

          {/* 统计卡片 */}
          <SectionCards cardsData={statsCardsData} />

          {/* 主要内容区 */}
          <div className="px-4 lg:px-6">
            <Tabs defaultValue="current" className="w-full">
              <div className="flex items-center justify-between mb-4">
                <TabsList>
                  <TabsTrigger value="current">当前课程</TabsTrigger>
                  <TabsTrigger value="history">历史存档</TabsTrigger>
                  <TabsTrigger value="schedule">我的课表</TabsTrigger>
                </TabsList>
              </div>

              {/* 错误提示 */}
              {error && (
                <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-red-700 text-sm">
                  <IconAlertCircle className="h-4 w-4" />
                  {error}
                </div>
              )}

              {/* Tab 1: 当前课程 (卡片视图) */}
              <TabsContent value="current" className="space-y-4">
                {/* 搜索和筛选栏 */}
                <div className="flex flex-col sm:flex-row gap-4">
                  <div className="relative flex-1">
                    <IconSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground h-4 w-4" />
                    <Input
                      placeholder="搜索课程或教师姓名..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                  <Select
                    value={selectedSemester}
                    onValueChange={setSelectedSemester}
                  >
                    <SelectTrigger className="w-40">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="全部学期">全部学期</SelectItem>
                      {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                        <SelectItem key={s} value={`${s}`}>
                          第 {s} 学期
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Select
                    value={selectedCourseType}
                    onValueChange={setSelectedCourseType}
                  >
                    <SelectTrigger className="w-32">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="全部类型">全部类型</SelectItem>
                      {Object.entries(CourseTypeDescriptions).map(
                        ([key, val]) => (
                          <SelectItem key={key} value={key}>
                            {val}
                          </SelectItem>
                        ),
                      )}
                    </SelectContent>
                  </Select>
                </div>

                {/* 课程列表 */}
                {isCoursesLoading || isInitialLoading ? (
                  <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                    <IconLoader2 className="h-8 w-8 animate-spin mb-2" />
                    <p>正在获取最新课程信息...</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {filteredCourses.map((item) => (
                      <Card
                        key={item.id}
                        className="hover:shadow-md transition-shadow bg-linear-to-t from-primary/5 to-card border-border shadow-sm overflow-hidden"
                      >
                        <CardHeader className="pb-3">
                          <div className="flex items-start justify-between">
                            <CardTitle className="text-lg line-clamp-1">
                              {item.course.name}
                            </CardTitle>
                            {getStatusBadge(item.status)}
                          </div>
                          <div className="flex items-center gap-3 text-sm text-muted-foreground">
                            <div className="flex items-center gap-1">
                              <IconBook2 className="h-4 w-4" />
                              <span>{item.course.credit}学分</span>
                            </div>
                            <Badge
                              variant="outline"
                              className="text-[10px] h-5"
                            >
                              {CourseTypeDescriptions[item.course.courseType]}
                            </Badge>
                          </div>
                        </CardHeader>
                        <CardContent className="space-y-3">
                          <p className="text-sm text-muted-foreground line-clamp-2 min-h-10">
                            {item.course.description || "暂无课程介绍信息。"}
                          </p>
                          <div className="space-y-2 text-sm">
                            <div className="flex items-center gap-2">
                              <IconUsers className="h-4 w-4 text-primary/60" />
                              <span>授课教师: {item.teacherName}</span>
                            </div>
                            <div className="flex items-center gap-2">
                              <IconMapPin className="h-4 w-4 text-primary/60" />
                              <span>教室: {item.classroom}</span>
                            </div>
                            <div className="flex items-center gap-2">
                              <IconClock className="h-4 w-4 text-primary/60" />
                              <span className="line-clamp-1">
                                {item.timeSchedule}
                              </span>
                            </div>
                            <div className="flex items-center gap-2">
                              <IconCalendarEvent className="h-4 w-4 text-primary/60" />
                              <span>学期: {item.semesterName}</span>
                            </div>
                            {item.status === "ongoing" && (
                              <div className="flex items-center justify-between pt-2 border-t mt-2">
                                <span className="text-xs text-muted-foreground">
                                  班级选课率
                                </span>
                                <span
                                  className={`text-sm font-bold ${getEnrollmentColor(item.enrolled, item.capacity)}`}
                                >
                                  {item.enrolled} / {item.capacity}
                                </span>
                              </div>
                            )}
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                )}

                {!isCoursesLoading && filteredCourses.length === 0 && (
                  <Card className="py-12 flex flex-col items-center justify-center text-muted-foreground border-dashed">
                    <IconBook2 className="h-10 w-10 mb-2 opacity-20" />
                    <p>未找到匹配的课程</p>
                  </Card>
                )}
              </TabsContent>

              {/* Tab 2: 历史课程 (表格视图) */}
              <TabsContent value="history">
                <Card className="border-none shadow-sm overflow-hidden">
                  <Table>
                    <TableHeader className="bg-muted/50">
                      <TableRow>
                        <TableHead className="w-[300px]">课程名称</TableHead>
                        <TableHead>学期</TableHead>
                        <TableHead>学分</TableHead>
                        <TableHead>教师</TableHead>
                        <TableHead>课程类型</TableHead>
                        <TableHead>状态</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {courses
                        .filter((c) => c.status === "completed")
                        .map((item) => (
                          <TableRow key={item.id}>
                            <TableCell className="font-medium">
                              {item.course.name}
                            </TableCell>
                            <TableCell>{item.semesterName}</TableCell>
                            <TableCell>{item.course.credit}</TableCell>
                            <TableCell>{item.teacherName}</TableCell>
                            <TableCell>
                              <Badge variant="outline">
                                {CourseTypeDescriptions[item.course.courseType]}
                              </Badge>
                            </TableCell>
                            <TableCell>{getStatusBadge(item.status)}</TableCell>
                          </TableRow>
                        ))}
                      {courses.filter((c) => c.status === "completed")
                        .length === 0 && (
                        <TableRow>
                          <TableCell
                            colSpan={6}
                            className="text-center py-10 text-muted-foreground"
                          >
                            暂无历史修读记录
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </Card>
              </TabsContent>

              {/* Tab 3: 周课表 (网格视图) */}
              <TabsContent value="schedule">
                <Card className="p-0 overflow-hidden border-none shadow-sm">
                  <CardHeader className="bg-muted/30 border-b">
                    <CardTitle className="text-base">
                      2024秋季学期 周课程表
                    </CardTitle>
                    <CardDescription>
                      当前选课状态下的标准教学周排表
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="p-0">
                    <div className="overflow-x-auto">
                      <div className="min-w-[800px] grid grid-cols-6 divide-x divide-y border-b">
                        {/* 表头 */}
                        <div className="bg-muted/50 p-4 font-bold text-center">
                          时间 / 节次
                        </div>
                        {["周一", "周二", "周三", "周四", "周五"].map((day) => (
                          <div
                            key={day}
                            className="bg-muted/50 p-4 font-bold text-center"
                          >
                            {day}
                          </div>
                        ))}

                        {/* 课表行 1-6节 */}
                        {[1, 2, 3, 4, 5, 6].map((period) => (
                          <>
                            <div
                              key={`period-${period}`}
                              className="p-4 text-center text-sm text-muted-foreground bg-muted/20 flex items-center justify-center"
                            >
                              第 {period} 节
                            </div>
                            {["周一", "周二", "周三", "周四", "周五"].map(
                              (day) => {
                                const match = getCourseAt(day, period);
                                return (
                                  <div
                                    key={`${day}-${period}`}
                                    className="p-2 min-h-[100px] transition-colors hover:bg-muted/10"
                                  >
                                    {match ? (
                                      <div className="h-full bg-primary/10 border border-primary/20 rounded-lg p-2 text-xs flex flex-col justify-between">
                                        <div className="font-bold text-primary line-clamp-2">
                                          {match.course.name}
                                        </div>
                                        <div className="mt-2 space-y-1 opacity-80">
                                          <div className="flex items-center gap-1">
                                            <IconMapPin className="h-3 w-3" />{" "}
                                            {match.classroom}
                                          </div>
                                          <div className="flex items-center gap-1">
                                            <IconUsers className="h-3 w-3" />{" "}
                                            {match.teacherName}
                                          </div>
                                        </div>
                                      </div>
                                    ) : (
                                      <div className="h-full border-2 border-dashed border-muted/50 rounded-lg flex items-center justify-center">
                                        <span className="text-[10px] text-muted-foreground/30 italic">
                                          无课
                                        </span>
                                      </div>
                                    )}
                                  </div>
                                );
                              },
                            )}
                          </>
                        ))}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          </div>
        </div>
      </div>
    </div>
  );
}
