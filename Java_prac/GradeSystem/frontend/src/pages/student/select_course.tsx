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
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Checkbox } from "@/components/ui/checkbox";
import {
  IconSearch,
  IconBook2,
  IconClock,
  IconUsers,
  IconCalendarEvent,
  IconMapPin,
  IconPlus,
  IconMinus,
  IconAlertCircle,
  IconCheck,
  IconX,
  IconRefresh,
  IconLoader2,
} from "@tabler/icons-react";
import {
  CourseType,
  CourseTypeDescriptions,
  TeachingClassStatus,
} from "@/types/course";
import { SectionCards } from "@/components/section-cards";
import { TrendDirection } from "@/types/card-data";
import { useCourses } from "@/hooks/use-courses";
import { useAuthContext } from "@/contexts/auth-context";
import { useStudent } from "@/hooks/use-student";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export function StudentSelectCourseView() {
  // --- 数据 Hooks ---
  const { user } = useAuthContext();
  const { student, getStudentByUserId } = useStudent();
  const {
    courses: availableCourses,
    isLoading,
    error,
    fetchAvailableCourses,
    selectCourse,
    dropCourse,
    refreshCourses,
    clearError,
    fetchStudentCourses,
    courses,
    studentCourses,
  } = useCourses();

  // --- 状态管理 ---
  const [selectedSemester, setSelectedSemester] = useState("全部学期");
  const [selectedCourseType, setSelectedCourseType] = useState("全部类型");
  const [searchQuery, setSearchQuery] = useState("");
  const [filterAvailableOnly, setFilterAvailableOnly] = useState(false);
  const [batchSelection, setBatchSelection] = useState<Set<string>>(new Set());

  // --- 初始化加载 ---
  useEffect(() => {
    if (user?.id) getStudentByUserId(user.id);
  }, [user?.id, getStudentByUserId]);

  useEffect(() => {
    if (student?.id) {
      const filters: any = {};
      if (selectedCourseType !== "全部类型")
        filters.courseType = selectedCourseType;
      fetchAvailableCourses(student.id, filters);
      // 获取学生已选课程
      fetchStudentCourses(student.id);
    }
  }, [
    student?.id,
    selectedCourseType,
    fetchAvailableCourses,
    fetchStudentCourses,
  ]);

  // --- 获取已选课程ID集合 ---
  const enrolledCourseIds = useMemo(() => {
    return new Set(
      studentCourses
        .filter((c) => c.status === TeachingClassStatus.ACTIVE)
        .map((c) => c.id),
    );
  }, [studentCourses]);

  // --- 逻辑计算 ---
  const filteredCourses = useMemo(() => {
    return availableCourses.filter((item) => {
      const matchesSearch =
        item.course.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.teacherName.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesAvailable =
        !filterAvailableOnly || item.enrolled < item.capacity;
      // 排除已选的课程
      const notEnrolled = !enrolledCourseIds.has(item.id);
      return matchesSearch && matchesAvailable && notEnrolled;
    });
  }, [availableCourses, searchQuery, filterAvailableOnly, enrolledCourseIds]);

  const stats = useMemo(() => {
    const selected = availableCourses.filter(
      (c) =>
        c.status === TeachingClassStatus.ACTIVE ||
        c.status === TeachingClassStatus.COMPLETED,
    ); // 假设逻辑
    const credits = selected.reduce((sum, c) => sum + c.course.credit, 0);
    return { count: selected.length, credits };
  }, [availableCourses]);

  const statsCardsData = [
    {
      id: "available",
      title: "可选课程总数",
      value: `${filteredCourses.length}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: { status: "neutral" as const, description: "当前学期开放" },
    },
    {
      id: "selected-credits",
      title: "已选学分",
      value: `${stats.credits}`,
      trend: { direction: TrendDirection.UP, value: "+3", isVisible: true },
      footer: { status: "positive" as const, description: "建议 20-25 学分" },
    },
    {
      id: "batch-count",
      title: "待选清单",
      value: `${batchSelection.size}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: { status: "neutral" as const, description: "勾选中的课程" },
    },
    {
      id: "selection-status",
      title: "选课阶段",
      value: "第一轮",
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: { status: "positive" as const, description: "正在进行中" },
    },
  ];

  // --- 交互处理 ---
  const handleToggleBatch = (id: string, checked: boolean) => {
    const next = new Set(batchSelection);
    if (checked) next.add(id);
    else next.delete(id);
    setBatchSelection(next);
  };

  const handleSelectOne = async (courseId: string) => {
    if (!student?.id) return;
    await selectCourse(student.id, courseId);
    setBatchSelection((prev) => {
      const next = new Set(prev);
      next.delete(courseId);
      return next;
    });
    // await fetchStudentCourses(student.id);
    // await fetchAvailableCourses(student.id);
    refreshCourses();
  };

  const handleBatchSubmit = async () => {
    if (!student?.id) return;
    for (const id of batchSelection) {
      await selectCourse(student.id, id);
    }
    setBatchSelection(new Set());
    refreshCourses();
  };

  const handleDropCourse = async (teachingClassId: string) => {
    if (!student?.id) return;
    const success = await dropCourse(student.id, teachingClassId);
    if (success) {
      refreshCourses();
    }
  };

  // --- 样式辅助 ---
  const getAvailabilityInfo = (enrolled: number, capacity: number) => {
    const ratio = enrolled / capacity;
    if (ratio >= 1) return { color: "text-red-600", text: "名额已满" };
    if (ratio >= 0.8)
      return {
        color: "text-orange-600",
        text: `紧张 (余 ${capacity - enrolled})`,
      };
    return {
      color: "text-green-600",
      text: `充足 (余 ${capacity - enrolled})`,
    };
  };

  const isInitialLoading = !student && user?.id;

  return (
    <div className="flex flex-1 flex-col">
      <div className="@container/main flex flex-1 flex-col gap-2">
        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
          {/* 标题栏 */}
          <div className="px-4 lg:px-6 flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold tracking-tight">选课中心</h1>
              <p className="text-sm text-muted-foreground">
                欢迎，{user?.username || "正在加载档案..."}
              </p>
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => refreshCourses()}
                disabled={isLoading}
              >
                <IconRefresh
                  className={`mr-2 h-4 w-4 ${isLoading ? "animate-spin" : ""}`}
                />
                重载课程
              </Button>
              {batchSelection.size > 0 && (
                <Button
                  size="sm"
                  onClick={handleBatchSubmit}
                  disabled={isLoading}
                >
                  <IconPlus className="mr-2 h-4 w-4" />
                  提交所选 ({batchSelection.size})
                </Button>
              )}
            </div>
          </div>

          {/* 统计卡片 */}
          <SectionCards cardsData={statsCardsData} />

          {/* 选课提醒 (第一版样式) */}
          <div className="px-4 lg:px-6">
            <Card className="border-orange-200 bg-orange-50/50 dark:bg-orange-900/10 dark:border-orange-900/50">
              <CardContent className="flex items-center gap-3 p-4">
                <IconAlertCircle className="h-5 w-5 text-orange-600" />
                <div className="flex-1">
                  <p className="text-sm font-bold text-orange-800 dark:text-orange-300">
                    2025 秋季学期正选阶段
                  </p>
                  <p className="text-xs text-orange-700 dark:text-orange-400">
                    截止时间：2025-12-28
                    23:00。请确保已完成先修课程认定，避免因时间冲突导致选课失败。
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* 筛选区 */}
          <div className="px-4 lg:px-6">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <IconSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground h-4 w-4" />
                <Input
                  placeholder="输入课程名、教师或关键词搜索..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Select
                value={selectedCourseType}
                onValueChange={setSelectedCourseType}
              >
                <SelectTrigger className="w-40">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="全部类型">全部类型</SelectItem>
                  {Object.entries(CourseTypeDescriptions).map(([k, v]) => (
                    <SelectItem key={k} value={k}>
                      {v}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button
                variant={filterAvailableOnly ? "default" : "outline"}
                onClick={() => setFilterAvailableOnly(!filterAvailableOnly)}
              >
                仅看有名额
              </Button>
            </div>
          </div>

          {/* 错误展示 */}
          {error && (
            <div className="mx-4 lg:mx-6 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center justify-between text-red-700 text-sm">
              <div className="flex items-center gap-2">
                <IconAlertCircle className="h-4 w-4" />
                {error}
              </div>
              <Button variant="ghost" size="sm" onClick={clearError}>
                <IconX className="h-4 w-4" />
              </Button>
            </div>
          )}

          {/* 主要内容 Tabs */}
          <div className="px-4 lg:px-6">
            <Tabs defaultValue="available" className="w-full">
              <TabsList className="mb-4">
                <TabsTrigger value="available">可选课程库</TabsTrigger>
                <TabsTrigger value="selected">已选结果</TabsTrigger>
              </TabsList>

              <TabsContent value="available" className="space-y-4">
                {isLoading || isInitialLoading ? (
                  <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                    <IconLoader2 className="h-10 w-10 animate-spin mb-4 opacity-20" />
                    <p>正在获取实时选课名额...</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {filteredCourses.map((item) => {
                      const avail = getAvailabilityInfo(
                        item.enrolled,
                        item.capacity,
                      );
                      const isConflict = false; // 逻辑预留
                      const isSelectedInBatch = batchSelection.has(item.id);

                      return (
                        <Card
                          key={item.id}
                          className={`hover:shadow-md transition-all bg-linear-to-t from-primary/5 to-card border-border ${
                            isSelectedInBatch ? "ring-2 ring-primary" : ""
                          }`}
                        >
                          <CardHeader className="pb-3">
                            <div className="flex items-start justify-between">
                              <div className="flex items-center gap-3">
                                <Checkbox
                                  checked={isSelectedInBatch}
                                  onCheckedChange={(checked) =>
                                    handleToggleBatch(item.id, !!checked)
                                  }
                                  disabled={item.enrolled >= item.capacity}
                                />
                                <div>
                                  <CardTitle className="text-lg flex items-center gap-2">
                                    {item.course.name}
                                  </CardTitle>
                                  <div className="flex items-center gap-3 text-xs text-muted-foreground mt-1">
                                    <Badge variant="secondary" className="h-5">
                                      {item.course.credit}学分
                                    </Badge>
                                    <span className="flex items-center gap-1">
                                      <IconUsers className="h-3 w-3" />
                                      {item.teacherName}
                                    </span>
                                  </div>
                                </div>
                              </div>
                              <Badge variant="outline" className="text-[10px]">
                                {CourseTypeDescriptions[item.course.courseType]}
                              </Badge>
                            </div>
                          </CardHeader>
                          <CardContent className="space-y-4">
                            <p className="text-sm text-muted-foreground line-clamp-2 min-h-10">
                              {item.course.description || "暂无课程详细描述。"}
                            </p>

                            <div className="grid grid-cols-2 gap-3 text-sm border-t pt-4">
                              <div className="flex items-center gap-2">
                                <IconMapPin className="h-4 w-4 text-primary/60" />
                                <span>{item.classroom}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <IconClock className="h-4 w-4 text-primary/60" />
                                <span className="truncate">
                                  {item.timeSchedule}
                                </span>
                              </div>
                            </div>

                            {/* 选课状态栏 */}
                            <div className="space-y-2">
                              <div className="flex justify-between text-xs">
                                <span className="text-muted-foreground">
                                  选课容量
                                </span>
                                <span className={`font-bold ${avail.color}`}>
                                  {avail.text} ({item.enrolled}/{item.capacity})
                                </span>
                              </div>
                              <div className="w-full bg-muted rounded-full h-1.5 overflow-hidden">
                                <div
                                  className={`h-full transition-all ${item.enrolled >= item.capacity ? "bg-red-500" : "bg-primary"}`}
                                  style={{
                                    width: `${(item.enrolled / item.capacity) * 100}%`,
                                  }}
                                />
                              </div>
                            </div>

                            {/* 冲突提醒 (逻辑预留) */}
                            {isConflict && (
                              <div className="flex items-center gap-2 text-xs text-red-600 bg-red-50 p-2 rounded">
                                <IconX className="h-3 w-3" />{" "}
                                与“数据结构”时间冲突
                              </div>
                            )}

                            <div className="flex justify-end gap-2 pt-2">
                              <Button
                                size="sm"
                                className="w-full sm:w-auto"
                                disabled={
                                  item.enrolled >= item.capacity || isLoading
                                }
                                onClick={() => handleSelectOne(item.id)}
                              >
                                {item.enrolled >= item.capacity
                                  ? "已选满"
                                  : "立即选课"}
                              </Button>
                            </div>
                          </CardContent>
                        </Card>
                      );
                    })}
                  </div>
                )}
              </TabsContent>

              <TabsContent value="selected">
                <Card className="border-none shadow-sm">
                  <Table>
                    <TableHeader className="bg-muted/50">
                      <TableRow>
                        <TableHead>课程名称</TableHead>
                        <TableHead>时间</TableHead>
                        <TableHead>地点</TableHead>
                        <TableHead>教师</TableHead>
                        <TableHead>学分</TableHead>
                        <TableHead className="text-right">操作</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {studentCourses
                        .filter((c) => c.status === TeachingClassStatus.ACTIVE)
                        .map((item) => (
                          <TableRow key={item.id}>
                            <TableCell className="font-medium">
                              {item.course.name}
                              <Badge
                                variant="outline"
                                className="ml-2 text-[10px]"
                              >
                                已入选
                              </Badge>
                            </TableCell>
                            <TableCell className="text-sm">
                              {item.timeSchedule}
                            </TableCell>
                            <TableCell>{item.classroom}</TableCell>
                            <TableCell>{item.teacherName}</TableCell>
                            <TableCell>{item.course.credit}</TableCell>
                            <TableCell className="text-right">
                              <AlertDialog>
                                <AlertDialogTrigger asChild>
                                  <Button
                                    variant="secondary"
                                    size="sm"
                                    className="text-red-600 hover:text-red-700 hover:bg-red-50"
                                  >
                                    <IconMinus className="h-4 w-4 mr-1" /> 退选
                                  </Button>
                                </AlertDialogTrigger>
                                <AlertDialogContent>
                                  <AlertDialogHeader>
                                    <AlertDialogTitle>
                                      确认退选课程？
                                    </AlertDialogTitle>
                                    <AlertDialogDescription>
                                      您正在申请退选《{item.course.name}
                                      》。退选后，该名额将立即释放给其他同学。
                                    </AlertDialogDescription>
                                  </AlertDialogHeader>
                                  <AlertDialogFooter>
                                    <AlertDialogCancel>取消</AlertDialogCancel>
                                    <AlertDialogAction
                                      className="bg-red-600 hover:bg-red-700"
                                      onClick={(e) => {
                                        e.preventDefault();
                                        handleDropCourse(item.id);
                                      }}
                                    >
                                      确认释放名额
                                    </AlertDialogAction>
                                  </AlertDialogFooter>
                                </AlertDialogContent>
                              </AlertDialog>
                            </TableCell>
                          </TableRow>
                        ))}
                      {studentCourses.filter(
                        (c) => c.status === TeachingClassStatus.ACTIVE,
                      ).length === 0 && (
                        <TableRow>
                          <TableCell
                            colSpan={6}
                            className="text-center py-10 text-muted-foreground"
                          >
                            暂无已选定的课程，快去“可选课程库”看看吧
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </Card>
              </TabsContent>
            </Tabs>
          </div>
        </div>
      </div>
    </div>
  );
}
