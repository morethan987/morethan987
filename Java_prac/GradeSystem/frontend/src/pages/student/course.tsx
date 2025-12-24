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
import { Input } from "@/components/ui/input";
import {
  IconSearch,
  IconBook2,
  IconClock,
  IconUsers,
  IconCalendarEvent,
  IconMapPin,
} from "@tabler/icons-react";
import type { Course, CourseType } from "@/types/course";
import { CourseTypeDescriptions } from "@/types/course";
import { SectionCards } from "@/components/section-cards";
import { TrendDirection } from "@/types/card-data";

// 扩展课程接口，增加教学班信息
interface ExtendedCourse extends Course {
  teacher: string;
  classroom: string;
  time: string;
  capacity: number;
  enrolled: number;
  status: "ongoing" | "completed" | "upcoming";
  semester_name: string;
}

// 样例数据
const sampleCourses: ExtendedCourse[] = [
  {
    id: "cs001",
    name: "数据结构与算法",
    description:
      "介绍基本数据结构和算法设计分析方法，培养学生的程序设计能力和算法思维。",
    credit: 4,
    semester: 5,
    courseType: "REQUIRED",
    teacher: "李教授",
    classroom: "A101",
    time: "周一 1-2节, 周三 3-4节",
    capacity: 60,
    enrolled: 58,
    status: "ongoing",
    semester_name: "2024秋",
  },
  {
    id: "cs002",
    name: "Web开发技术",
    description:
      "学习现代Web开发技术，包括前端框架、后端API设计、数据库操作等。",
    credit: 2,
    semester: 5,
    courseType: "ELECTIVE",
    teacher: "王老师",
    classroom: "B203",
    time: "周二 5-6节",
    capacity: 40,
    enrolled: 35,
    status: "ongoing",
    semester_name: "2024秋",
  },
  {
    id: "cs003",
    name: "软件工程",
    description: "软件开发生命周期管理，项目管理，团队协作等软件工程核心概念。",
    credit: 3,
    semester: 5,
    courseType: "REQUIRED",
    teacher: "张教授",
    classroom: "C301",
    time: "周四 1-3节",
    capacity: 50,
    enrolled: 48,
    status: "ongoing",
    semester_name: "2024秋",
  },
  {
    id: "cs004",
    name: "操作系统",
    description: "操作系统原理与实现，进程管理、内存管理、文件系统等核心概念。",
    credit: 3,
    semester: 4,
    courseType: "REQUIRED",
    teacher: "陈教授",
    classroom: "A205",
    time: "周一 3-4节, 周五 1节",
    capacity: 55,
    enrolled: 55,
    status: "completed",
    semester_name: "2024春",
  },
  {
    id: "cs005",
    name: "计算机网络",
    description: "计算机网络基础知识、网络协议、网络安全等内容的学习。",
    credit: 3,
    semester: 4,
    courseType: "REQUIRED",
    teacher: "刘老师",
    classroom: "B105",
    time: "周二 1-2节, 周四 5节",
    capacity: 60,
    enrolled: 57,
    status: "completed",
    semester_name: "2024春",
  },
  {
    id: "cs006",
    name: "机器学习基础",
    description: "机器学习算法基础，深度学习入门，人工智能应用实践。",
    credit: 3,
    semester: 6,
    courseType: "ELECTIVE",
    teacher: "赵教授",
    classroom: "D401",
    time: "周三 1-3节",
    capacity: 30,
    enrolled: 0,
    status: "upcoming",
    semester_name: "2025春",
  },
];

export function CourseView() {
  const [selectedSemester, setSelectedSemester] = useState("全部学期");
  const [selectedCourseType, setSelectedCourseType] = useState("全部类型");
  const [selectedStatus, setSelectedStatus] = useState("全部状态");
  const [searchQuery, setSearchQuery] = useState("");

  // 筛选课程
  const filteredCourses = sampleCourses.filter((course) => {
    const matchesSemester =
      selectedSemester === "全部学期" ||
      course.semester_name === selectedSemester;
    const matchesType =
      selectedCourseType === "全部类型" ||
      course.courseType === selectedCourseType;
    const matchesStatus =
      selectedStatus === "全部状态" || course.status === selectedStatus;
    const matchesSearch =
      course.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      course.teacher.toLowerCase().includes(searchQuery.toLowerCase());

    return matchesSemester && matchesType && matchesStatus && matchesSearch;
  });

  // 统计数据
  const ongoingCourses = sampleCourses.filter(
    (c) => c.status === "ongoing",
  ).length;
  const completedCourses = sampleCourses.filter(
    (c) => c.status === "completed",
  ).length;
  const totalCredits = sampleCourses
    .filter((c) => c.status !== "upcoming")
    .reduce((sum, c) => sum + c.credit, 0);
  const avgClassSize =
    sampleCourses
      .filter((c) => c.status === "ongoing")
      .reduce((sum, c) => sum + c.enrolled, 0) / ongoingCourses || 0;

  const statsCardsData = [
    {
      id: "ongoing-courses",
      title: "本学期课程",
      value: `${ongoingCourses}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: {
        status: "正在进行",
        description: `共${totalCredits}学分`,
      },
    },
    {
      id: "completed-courses",
      title: "已完成课程",
      value: `${completedCourses}`,
      trend: {
        direction: TrendDirection.UP,
        value: "+2",
        isVisible: true,
      },
      footer: {
        status: "较上学期增加",
        description: "学习进度良好",
      },
    },
    {
      id: "total-credits",
      title: "累计学分",
      value: `${totalCredits}`,
      trend: {
        direction: TrendDirection.UP,
        value: "+5",
        isVisible: true,
      },
      footer: {
        status: "本学期新增",
        description: `还需${120 - totalCredits}学分毕业`,
      },
    },
    {
      id: "class-size",
      title: "平均班容量",
      value: `${avgClassSize.toFixed(0)}`,
      trend: {
        direction: TrendDirection.NEUTRAL,
        value: "0",
        isVisible: false,
      },
      footer: {
        status: "人/班",
        description: "课堂规模适中",
      },
    },
  ];

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "ongoing":
        return <Badge className="bg-green-100 text-green-800">进行中</Badge>;
      case "completed":
        return <Badge variant="secondary">已完成</Badge>;
      case "upcoming":
        return <Badge className="bg-blue-100 text-blue-800">即将开始</Badge>;
      default:
        return <Badge variant="outline">未知</Badge>;
    }
  };

  const getEnrollmentStatus = (enrolled: number, capacity: number) => {
    const ratio = enrolled / capacity;
    if (ratio >= 0.9) return "text-red-600";
    if (ratio >= 0.7) return "text-yellow-600";
    return "text-green-600";
  };

  return (
    <div className="flex flex-1 flex-col">
      <div className="@container/main flex flex-1 flex-col gap-2">
        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
          {/* 统计卡片 */}
          <SectionCards cardsData={statsCardsData} />

          {/* 主要内容 */}
          <div className="px-4 lg:px-6">
            <Tabs defaultValue="current" className="w-full">
              <div className="flex items-center justify-between mb-4">
                <TabsList>
                  <TabsTrigger value="current">当前课程</TabsTrigger>
                  <TabsTrigger value="history">历史课程</TabsTrigger>
                  <TabsTrigger value="schedule">课程表</TabsTrigger>
                </TabsList>
              </div>

              <TabsContent value="current" className="space-y-4">
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
                    value={selectedSemester}
                    onValueChange={setSelectedSemester}
                  >
                    <SelectTrigger className="w-40">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="全部学期">全部学期</SelectItem>
                      <SelectItem value="2024秋">2024秋</SelectItem>
                      <SelectItem value="2024春">2024春</SelectItem>
                      <SelectItem value="2025春">2025春</SelectItem>
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
                        ([key, value]) => (
                          <SelectItem key={key} value={key}>
                            {value}
                          </SelectItem>
                        ),
                      )}
                    </SelectContent>
                  </Select>

                  <Select
                    value={selectedStatus}
                    onValueChange={setSelectedStatus}
                  >
                    <SelectTrigger className="w-32">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="全部状态">全部状态</SelectItem>
                      <SelectItem value="ongoing">进行中</SelectItem>
                      <SelectItem value="completed">已完成</SelectItem>
                      <SelectItem value="upcoming">即将开始</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {/* 课程列表 */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {filteredCourses.map((course) => (
                    <Card
                      key={course.id}
                      className="hover:shadow-md transition-shadow bg-linear-to-t from-primary/5 to-card bg-card border-border shadow-sm"
                    >
                      <CardHeader className="pb-3">
                        <div className="flex items-start justify-between">
                          <CardTitle className="text-lg line-clamp-1">
                            {course.name}
                          </CardTitle>
                          {getStatusBadge(course.status)}
                        </div>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
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
                      </CardHeader>
                      <CardContent className="space-y-3">
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {course.description}
                        </p>

                        <div className="space-y-2 text-sm">
                          <div className="flex items-center gap-2">
                            <IconUsers className="h-4 w-4 text-muted-foreground" />
                            <span>授课教师: {course.teacher}</span>
                          </div>

                          <div className="flex items-center gap-2">
                            <IconMapPin className="h-4 w-4 text-muted-foreground" />
                            <span>教室: {course.classroom}</span>
                          </div>

                          <div className="flex items-center gap-2">
                            <IconClock className="h-4 w-4 text-muted-foreground" />
                            <span className="line-clamp-1">{course.time}</span>
                          </div>

                          <div className="flex items-center gap-2">
                            <IconCalendarEvent className="h-4 w-4 text-muted-foreground" />
                            <span>学期: {course.semester_name}</span>
                          </div>

                          {course.status === "ongoing" && (
                            <div className="flex items-center justify-between pt-2 border-t">
                              <span className="text-xs text-muted-foreground">
                                班级人数
                              </span>
                              <span
                                className={`text-sm font-medium ${getEnrollmentStatus(course.enrolled, course.capacity)}`}
                              >
                                {course.enrolled}/{course.capacity}
                              </span>
                            </div>
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

              <TabsContent value="history" className="space-y-4">
                <Card className="bg-card border-border shadow-sm">
                  <CardHeader>
                    <CardTitle>已完成课程</CardTitle>
                    <CardDescription>
                      查看历史学期的课程学习记录
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>课程名称</TableHead>
                          <TableHead>学期</TableHead>
                          <TableHead>学分</TableHead>
                          <TableHead>授课教师</TableHead>
                          <TableHead>课程类型</TableHead>
                          <TableHead>状态</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {sampleCourses
                          .filter((c) => c.status === "completed")
                          .map((course) => (
                            <TableRow key={course.id}>
                              <TableCell className="font-medium">
                                {course.name}
                              </TableCell>
                              <TableCell>{course.semester_name}</TableCell>
                              <TableCell>{course.credit}</TableCell>
                              <TableCell>{course.teacher}</TableCell>
                              <TableCell>
                                <Badge variant="outline">
                                  {
                                    CourseTypeDescriptions[
                                      course.courseType as CourseType
                                    ]
                                  }
                                </Badge>
                              </TableCell>
                              <TableCell>
                                {getStatusBadge(course.status)}
                              </TableCell>
                            </TableRow>
                          ))}
                      </TableBody>
                    </Table>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="schedule" className="space-y-4">
                <Card className="bg-card border-border shadow-sm">
                  <CardHeader>
                    <CardTitle>本周课程表</CardTitle>
                    <CardDescription>
                      2024秋季学期 第12周 (12月2日-12月8日)
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-6 gap-4 text-center">
                      <div className="font-medium">时间</div>
                      {["周一", "周二", "周三", "周四", "周五"].map((day) => (
                        <div key={day} className="font-medium">
                          {day}
                        </div>
                      ))}

                      {[1, 2, 3, 4, 5, 6].map((period) => (
                        <>
                          <div
                            key={`time-${period}`}
                            className="py-4 text-sm text-muted-foreground"
                          >
                            第{period}节
                          </div>
                          {["周一", "周二", "周三", "周四", "周五"].map(
                            (day) => {
                              const course = sampleCourses.find(
                                (c) =>
                                  c.status === "ongoing" &&
                                  c.time.includes(day) &&
                                  c.time.includes(`${period}`),
                              );
                              return (
                                <div key={`${day}-${period}`} className="py-2">
                                  {course ? (
                                    <div className="bg-primary/20 dark:bg-primary/30 border border-primary/20 dark:border-primary/40 rounded-lg p-2 text-xs">
                                      <div className="font-medium line-clamp-1">
                                        {course.name}
                                      </div>
                                      <div className="text-muted-foreground">
                                        {course.classroom}
                                      </div>
                                      <div className="text-muted-foreground">
                                        {course.teacher}
                                      </div>
                                    </div>
                                  ) : (
                                    <div className="h-16 border-2 border-dashed border-muted rounded-lg"></div>
                                  )}
                                </div>
                              );
                            },
                          )}
                        </>
                      ))}
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
