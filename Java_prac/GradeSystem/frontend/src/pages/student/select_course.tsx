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
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
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
} from "@tabler/icons-react";
import type { Course, CourseType } from "@/types/course";
import { CourseTypeDescriptions } from "@/types/course";
import { SectionCards } from "@/components/section-cards";
import { TrendDirection } from "@/types/card-data";

// 选课课程接口
interface SelectableCourse extends Course {
  teacher: string;
  classroom: string;
  time: string;
  capacity: number;
  enrolled: number;
  available: number;
  conflicts: string[];
  prerequisites: string[];
  isSelected: boolean;
  canSelect: boolean;
  selectReason?: string;
}

// 样例数据
const availableCourses: SelectableCourse[] = [
  {
    id: "cs101",
    name: "高级数据结构",
    description: "深入学习高级数据结构：红黑树、B树、图算法等",
    credit: 3,
    semester: 6,
    courseType: "ELECTIVE",
    teacher: "李教授",
    classroom: "A201",
    time: "周二 1-3节",
    capacity: 40,
    enrolled: 32,
    available: 8,
    conflicts: [],
    prerequisites: ["数据结构与算法"],
    isSelected: false,
    canSelect: true,
  },
  {
    id: "cs102",
    name: "机器学习基础",
    description: "机器学习算法基础，包括监督学习、无监督学习等",
    credit: 3,
    semester: 6,
    courseType: "ELECTIVE",
    teacher: "王教授",
    classroom: "B301",
    time: "周四 5-7节",
    capacity: 30,
    enrolled: 28,
    available: 2,
    conflicts: [],
    prerequisites: ["高等数学", "线性代数"],
    isSelected: true,
    canSelect: true,
  },
  {
    id: "cs103",
    name: "分布式系统",
    description: "学习分布式系统架构设计与实现",
    credit: 3,
    semester: 6,
    courseType: "ELECTIVE",
    teacher: "张教授",
    classroom: "C401",
    time: "周三 3-5节",
    capacity: 35,
    enrolled: 35,
    available: 0,
    conflicts: [],
    prerequisites: ["计算机网络", "操作系统"],
    isSelected: false,
    canSelect: false,
    selectReason: "名额已满",
  },
  {
    id: "cs104",
    name: "Web安全技术",
    description: "Web应用安全漏洞分析与防护技术",
    credit: 2,
    semester: 6,
    courseType: "ELECTIVE",
    teacher: "陈老师",
    classroom: "D101",
    time: "周二 1-2节",
    capacity: 25,
    enrolled: 20,
    available: 5,
    conflicts: ["高级数据结构"],
    prerequisites: ["Web开发技术"],
    isSelected: false,
    canSelect: false,
    selectReason: "时间冲突",
  },
  {
    id: "cs105",
    name: "移动应用开发",
    description: "Android和iOS移动应用开发技术",
    credit: 3,
    semester: 6,
    courseType: "ELECTIVE",
    teacher: "刘老师",
    classroom: "E201",
    time: "周五 1-3节",
    capacity: 30,
    enrolled: 18,
    available: 12,
    conflicts: [],
    prerequisites: ["面向对象程序设计"],
    isSelected: false,
    canSelect: true,
  },
  {
    id: "ge001",
    name: "艺术欣赏",
    description: "中外艺术作品鉴赏与文化内涵分析",
    credit: 2,
    semester: 6,
    courseType: "GENERAL",
    teacher: "李老师",
    classroom: "F101",
    time: "周一 7-8节",
    capacity: 60,
    enrolled: 45,
    available: 15,
    conflicts: [],
    prerequisites: [],
    isSelected: true,
    canSelect: true,
  },
];

export function SelectCourseView() {
  const [courses, setCourses] = useState(availableCourses);
  const [selectedCourseType, setSelectedCourseType] = useState("全部类型");
  const [searchQuery, setSearchQuery] = useState("");
  const [filterAvailable, setFilterAvailable] = useState(false);

  // 筛选课程
  const filteredCourses = courses.filter((course) => {
    const matchesType =
      selectedCourseType === "全部类型" ||
      course.courseType === selectedCourseType;
    const matchesSearch =
      course.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      course.teacher.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesAvailable = !filterAvailable || course.available > 0;

    return matchesType && matchesSearch && matchesAvailable;
  });

  // 统计数据
  const selectedCourses = courses.filter((c) => c.isSelected);
  const selectedCredits = selectedCourses.reduce((sum, c) => sum + c.credit, 0);
  const requiredCourses = selectedCourses.filter(
    (c) => c.courseType === "REQUIRED",
  ).length;
  const electiveCourses = selectedCourses.filter(
    (c) => c.courseType === "ELECTIVE",
  ).length;

  const statsCardsData = [
    {
      id: "selected-courses",
      title: "已选课程",
      value: `${selectedCourses.length}`,
      trend: {
        direction: TrendDirection.UP,
        value: "+2",
        isVisible: true,
      },
      footer: {
        status: "本次新增",
        description: "选课进行中",
      },
    },
    {
      id: "selected-credits",
      title: "已选学分",
      value: `${selectedCredits}`,
      trend: {
        direction: TrendDirection.UP,
        value: "+5",
        isVisible: true,
      },
      footer: {
        status: "学分累计",
        description: `建议总学分20-25`,
      },
    },
    {
      id: "required-courses",
      title: "必修课程",
      value: `${requiredCourses}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: {
        status: "门必修课",
        description: "按计划进行",
      },
    },
    {
      id: "elective-courses",
      title: "选修课程",
      value: `${electiveCourses}`,
      trend: {
        direction: TrendDirection.UP,
        value: "+2",
        isVisible: true,
      },
      footer: {
        status: "门选修课",
        description: "丰富专业知识",
      },
    },
  ];

  const handleSelectCourse = (courseId: string) => {
    setCourses((prev) =>
      prev.map((course) =>
        course.id === courseId
          ? { ...course, isSelected: !course.isSelected }
          : course,
      ),
    );
  };

  const getAvailabilityColor = (available: number, capacity: number) => {
    const ratio = available / capacity;
    if (ratio <= 0) return "text-red-600";
    if (ratio <= 0.2) return "text-orange-600";
    return "text-green-600";
  };

  const getSelectButtonProps = (course: SelectableCourse) => {
    if (!course.canSelect) {
      return {
        disabled: true,
        variant: "secondary" as const,
        children: (
          <div className="flex items-center gap-2">
            <IconX className="h-4 w-4" />
            不可选
          </div>
        ),
      };
    }

    if (course.isSelected) {
      return {
        disabled: false,
        variant: "destructive" as const,
        children: (
          <div className="flex items-center gap-2">
            <IconMinus className="h-4 w-4" />
            退选
          </div>
        ),
      };
    }

    return {
      disabled: false,
      variant: "default" as const,
      children: (
        <div className="flex items-center gap-2">
          <IconPlus className="h-4 w-4" />
          选课
        </div>
      ),
    };
  };

  return (
    <div className="flex flex-1 flex-col">
      <div className="@container/main flex flex-1 flex-col gap-2">
        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
          {/* 统计卡片 */}
          <SectionCards cardsData={statsCardsData} />

          {/* 选课提醒 */}
          <div className="px-4 lg:px-6">
            <Card className="border-orange-200 bg-orange-50 dark:bg-orange-900/20 dark:border-orange-800">
              <CardContent className="flex items-center gap-3 p-4">
                <IconAlertCircle className="h-5 w-5 text-orange-600" />
                <div className="flex-1">
                  <p className="text-sm font-medium text-orange-800 dark:text-orange-200">
                    选课提醒
                  </p>
                  <p className="text-sm text-orange-700 dark:text-orange-300">
                    选课时间：2024年12月1日 08:00 - 2024年12月15日 18:00 |
                    本学期建议选择 20-25 学分课程
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* 主要内容 */}
          <div className="px-4 lg:px-6">
            <Tabs defaultValue="available" className="w-full">
              <div className="flex items-center justify-between mb-4">
                <TabsList>
                  <TabsTrigger value="available">可选课程</TabsTrigger>
                  <TabsTrigger value="selected">已选课程</TabsTrigger>
                  <TabsTrigger value="cart">选课购物车</TabsTrigger>
                </TabsList>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm">
                    培养方案
                  </Button>
                  <Button size="sm">提交选课</Button>
                </div>
              </div>

              <TabsContent value="available" className="space-y-4">
                {/* 搜索和筛选 */}
                <div className="flex flex-col sm:flex-row gap-4">
                  <div className="relative flex-1">
                    <IconSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
                    <Input
                      placeholder="搜索课程名称或教师..."
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
                      {Object.entries(CourseTypeDescriptions).map(
                        ([key, value]) => (
                          <SelectItem key={key} value={key}>
                            {value}
                          </SelectItem>
                        ),
                      )}
                    </SelectContent>
                  </Select>

                  <Button
                    variant={filterAvailable ? "default" : "outline"}
                    onClick={() => setFilterAvailable(!filterAvailable)}
                  >
                    仅显示可选
                  </Button>
                </div>

                {/* 课程列表 */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {filteredCourses.map((course) => (
                    <Card
                      key={course.id}
                      className={`hover:shadow-md transition-shadow bg-linear-to-t from-primary/5 to-card bg-card border-border shadow-sm ${course.isSelected ? "ring-2 ring-primary" : ""}`}
                    >
                      <CardHeader className="pb-3">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <CardTitle className="text-lg flex items-center gap-2">
                              {course.name}
                              {course.isSelected && (
                                <Badge className="bg-green-100 text-green-800 text-xs">
                                  <IconCheck className="h-3 w-3 mr-1" />
                                  已选
                                </Badge>
                              )}
                            </CardTitle>
                            <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                              <div className="flex items-center gap-1">
                                <IconBook2 className="h-4 w-4" />
                                <span>{course.credit}学分</span>
                              </div>
                              <Badge variant="outline" className="text-xs">
                                {
                                  CourseTypeDescriptions[
                                    course.courseType as CourseType
                                  ]
                                }
                              </Badge>
                            </div>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {course.description}
                        </p>

                        <div className="grid grid-cols-2 gap-3 text-sm">
                          <div className="flex items-center gap-2">
                            <IconUsers className="h-4 w-4 text-muted-foreground" />
                            <span>教师: {course.teacher}</span>
                          </div>

                          <div className="flex items-center gap-2">
                            <IconMapPin className="h-4 w-4 text-muted-foreground" />
                            <span>教室: {course.classroom}</span>
                          </div>

                          <div className="flex items-center gap-2 col-span-2">
                            <IconClock className="h-4 w-4 text-muted-foreground" />
                            <span>{course.time}</span>
                          </div>
                        </div>

                        {/* 选课状态 */}
                        <div className="space-y-2 pt-2 border-t">
                          <div className="flex justify-between items-center text-sm">
                            <span>课程容量:</span>
                            <span
                              className={getAvailabilityColor(
                                course.available,
                                course.capacity,
                              )}
                            >
                              <strong>{course.available}</strong>/
                              {course.capacity} 可选
                            </span>
                          </div>

                          {/* 先修课程 */}
                          {course.prerequisites.length > 0 && (
                            <div className="text-sm">
                              <span className="text-muted-foreground">
                                先修课程:{" "}
                              </span>
                              <div className="flex flex-wrap gap-1 mt-1">
                                {course.prerequisites.map((prereq, index) => (
                                  <Badge
                                    key={index}
                                    variant="outline"
                                    className="text-xs"
                                  >
                                    {prereq}
                                  </Badge>
                                ))}
                              </div>
                            </div>
                          )}

                          {/* 时间冲突提醒 */}
                          {course.conflicts.length > 0 && (
                            <div className="text-sm text-orange-600">
                              <span className="flex items-center gap-1">
                                <IconAlertCircle className="h-3 w-3" />
                                与以下课程时间冲突:
                              </span>
                              <div className="flex flex-wrap gap-1 mt-1">
                                {course.conflicts.map((conflict, index) => (
                                  <Badge
                                    key={index}
                                    variant="destructive"
                                    className="text-xs"
                                  >
                                    {conflict}
                                  </Badge>
                                ))}
                              </div>
                            </div>
                          )}

                          {/* 不可选原因 */}
                          {!course.canSelect && course.selectReason && (
                            <div className="text-sm text-red-600">
                              <span className="flex items-center gap-1">
                                <IconX className="h-3 w-3" />
                                {course.selectReason}
                              </span>
                            </div>
                          )}
                        </div>

                        {/* 操作按钮 */}
                        <div className="flex justify-end pt-2">
                          {course.isSelected ? (
                            <AlertDialog>
                              <AlertDialogTrigger asChild>
                                <Button {...getSelectButtonProps(course)} />
                              </AlertDialogTrigger>
                              <AlertDialogContent>
                                <AlertDialogHeader>
                                  <AlertDialogTitle>退选课程</AlertDialogTitle>
                                  <AlertDialogDescription>
                                    确定要退选《{course.name}
                                    》吗？退选后需要重新选课。
                                  </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                  <AlertDialogCancel>取消</AlertDialogCancel>
                                  <AlertDialogAction
                                    onClick={() =>
                                      handleSelectCourse(course.id)
                                    }
                                    className="bg-red-700 hover:bg-red-900"
                                  >
                                    确认退选
                                  </AlertDialogAction>
                                </AlertDialogFooter>
                              </AlertDialogContent>
                            </AlertDialog>
                          ) : (
                            <Button
                              {...getSelectButtonProps(course)}
                              onClick={() =>
                                course.canSelect &&
                                handleSelectCourse(course.id)
                              }
                            />
                          )}
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>

                {filteredCourses.length === 0 && (
                  <Card className="bg-card border-border shadow-sm">
                    <CardContent className="flex flex-col items-center justify-center py-12">
                      <IconBook2 className="h-12 w-12 text-muted-foreground mb-4" />
                      <p className="text-muted-foreground text-center">
                        没有找到符合条件的课程
                      </p>
                    </CardContent>
                  </Card>
                )}
              </TabsContent>

              <TabsContent value="selected" className="space-y-4">
                <Card className="bg-card border-border shadow-sm">
                  <CardHeader>
                    <CardTitle>已选课程清单</CardTitle>
                    <CardDescription>
                      本学期已选择的课程列表，总计 {selectedCredits} 学分
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    {selectedCourses.length > 0 ? (
                      <div className="space-y-4">
                        {selectedCourses.map((course) => (
                          <div
                            key={course.id}
                            className="flex items-center justify-between p-4 border rounded-lg bg-muted/30 dark:bg-muted/50 border-border"
                          >
                            <div className="flex-1">
                              <div className="flex items-center gap-3">
                                <h4 className="font-medium">{course.name}</h4>
                                <Badge variant="outline">
                                  {
                                    CourseTypeDescriptions[
                                      course.courseType as CourseType
                                    ]
                                  }
                                </Badge>
                                <Badge className="bg-green-100 text-green-800">
                                  {course.credit}学分
                                </Badge>
                              </div>
                              <p className="text-sm text-muted-foreground mt-1">
                                {course.teacher} | {course.time} |{" "}
                                {course.classroom}
                              </p>
                            </div>
                            <Button
                              variant="destructive"
                              size="sm"
                              onClick={() => handleSelectCourse(course.id)}
                            >
                              退选
                            </Button>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="text-center py-8 text-muted-foreground">
                        还没有选择任何课程
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="cart" className="space-y-4">
                <Card className="bg-card border-border shadow-sm">
                  <CardHeader>
                    <CardTitle>选课购物车</CardTitle>
                    <CardDescription>
                      临时保存感兴趣的课程，可以批量操作
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="text-center py-8 text-muted-foreground">
                      购物车功能开发中...
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
