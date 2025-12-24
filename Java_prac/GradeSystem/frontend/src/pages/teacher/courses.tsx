// 教师的课程界面，能够查看自己负责的教学班
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { IconBook, IconUsers, IconClock } from "@tabler/icons-react";

// 模拟教学班数据
interface TeachingClass {
  id: string;
  className: string;
  courseName: string;
  courseType: string;
  credit: number;
  studentCount: number;
  semester: string;
  schedule: string;
  location: string;
}

const mockTeachingClasses: TeachingClass[] = [
  {
    id: "TC001",
    className: "计算机科学与技术1班",
    courseName: "数据结构与算法",
    courseType: "REQUIRED",
    credit: 4,
    studentCount: 45,
    semester: "2024-2025-1",
    schedule: "周一 1-2节, 周三 3-4节",
    location: "A楼301",
  },
  {
    id: "TC002",
    className: "软件工程1班",
    courseName: "数据库系统原理",
    courseType: "REQUIRED",
    credit: 3,
    studentCount: 38,
    semester: "2024-2025-1",
    schedule: "周二 5-6节, 周四 7-8节",
    location: "B楼205",
  },
  {
    id: "TC003",
    className: "计算机科学与技术2班",
    courseName: "计算机网络",
    courseType: "PROFESSIONAL",
    credit: 3,
    studentCount: 42,
    semester: "2024-2025-1",
    schedule: "周一 3-4节, 周五 1-2节",
    location: "A楼402",
  },
  {
    id: "TC004",
    className: "人工智能班",
    courseName: "机器学习导论",
    courseType: "ELECTIVE",
    credit: 2,
    studentCount: 28,
    semester: "2024-2025-1",
    schedule: "周三 7-8节",
    location: "C楼101",
  },
];

// 课程类型映射
const courseTypeMap: Record<string, string> = {
  REQUIRED: "必修课",
  ELECTIVE: "选修课",
  LIMITED_ELECTIVE: "限选课",
  GENERAL: "通识课",
  PROFESSIONAL: "专业课",
};

export function TeacherCourses() {
  return (
    <div className="flex flex-col gap-6 p-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">我的教学班</h1>
          <p className="text-muted-foreground">查看和管理您负责的教学班级</p>
        </div>
        <Badge variant="secondary" className="text-sm">
          共 {mockTeachingClasses.length} 个教学班
        </Badge>
      </div>

      {/* 统计卡片 */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>教学班总数</CardDescription>
            <CardTitle className="text-2xl">
              {mockTeachingClasses.length}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <IconBook className="size-4" />
              <span>本学期</span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>授课学生</CardDescription>
            <CardTitle className="text-2xl">
              {mockTeachingClasses.reduce((sum, c) => sum + c.studentCount, 0)}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <IconUsers className="size-4" />
              <span>总人数</span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>总学时</CardDescription>
            <CardTitle className="text-2xl">
              {mockTeachingClasses.reduce((sum, c) => sum + c.credit, 0)}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <IconClock className="size-4" />
              <span>学分</span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>课程类型</CardDescription>
            <CardTitle className="text-2xl">3</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-1 flex-wrap">
              {Array.from(
                new Set(mockTeachingClasses.map((c) => c.courseType)),
              ).map((type) => (
                <Badge key={type} variant="outline" className="text-xs">
                  {courseTypeMap[type] || type}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 教学班列表 */}
      <Card>
        <CardHeader>
          <CardTitle>教学班列表</CardTitle>
          <CardDescription>2024-2025学年 第一学期</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>课程名称</TableHead>
                <TableHead>教学班</TableHead>
                <TableHead>课程类型</TableHead>
                <TableHead>学分</TableHead>
                <TableHead>学生人数</TableHead>
                <TableHead>上课时间</TableHead>
                <TableHead>上课地点</TableHead>
                <TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {mockTeachingClasses.map((classInfo) => (
                <TableRow key={classInfo.id}>
                  <TableCell className="font-medium">
                    {classInfo.courseName}
                  </TableCell>
                  <TableCell>{classInfo.className}</TableCell>
                  <TableCell>
                    <Badge variant="secondary">
                      {courseTypeMap[classInfo.courseType] ||
                        classInfo.courseType}
                    </Badge>
                  </TableCell>
                  <TableCell>{classInfo.credit}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1">
                      <IconUsers className="size-3 text-muted-foreground" />
                      {classInfo.studentCount}
                    </div>
                  </TableCell>
                  <TableCell>{classInfo.schedule}</TableCell>
                  <TableCell>{classInfo.location}</TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm">
                        查看成绩
                      </Button>
                      <Button variant="outline" size="sm">
                        登记成绩
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
