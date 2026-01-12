import { useEffect, useState } from "react";
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
import { useAuthContext } from "@/contexts/auth-context";
import { useTeacher } from "@/hooks/use-teacher";
import { teacherApi } from "@/api/v1/modules/teacher";

const courseTypeMap: Record<string, string> = {
  REQUIRED: "必修课",
  ELECTIVE: "选修课",
  LIMITED_ELECTIVE: "限选课",
  GENERAL: "通识课",
  PROFESSIONAL: "专业课",
};

export function TeacherCourses() {
  const { user } = useAuthContext();
  const { teachingClasses, loading, error, getTeachingClasses } = useTeacher();
  const [initError, setInitError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) return;

    const fetchTeachingClasses = async () => {
      try {
        const teacher = await teacherApi.getTeacherByUserId(user.id);
        await getTeachingClasses(teacher.id);
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "获取教学班数据失败";
        setInitError(message);
        console.error("获取教学班数据失败:", err);
      }
    };

    fetchTeachingClasses();
  }, [user, getTeachingClasses]);

  const displayError = error || initError;

  if (loading) {
    return (
      <div className="flex flex-1 items-center justify-center p-6">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p>正在加载教学班数据...</p>
        </div>
      </div>
    );
  }

  if (displayError) {
    return (
      <div className="flex flex-1 items-center justify-center p-6">
        <div className="text-center text-destructive">
          <p>加载失败: {displayError}</p>
        </div>
      </div>
    );
  }

  const totalStudents = teachingClasses.reduce(
    (sum, c) => sum + (c.studentCount || 0),
    0,
  );
  const totalCredits = teachingClasses.reduce(
    (sum, c) => sum + (c.credit || 0),
    0,
  );
  const uniqueCourseTypes = Array.from(
    new Set(teachingClasses.map((c) => c.courseType).filter(Boolean)),
  );

  return (
    <div className="flex flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">我的教学班</h1>
          <p className="text-muted-foreground">查看和管理您负责的教学班级</p>
        </div>
        <Badge variant="secondary" className="text-sm">
          共 {teachingClasses.length} 个教学班
        </Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>教学班总数</CardDescription>
            <CardTitle className="text-2xl">{teachingClasses.length}</CardTitle>
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
            <CardTitle className="text-2xl">{totalStudents}</CardTitle>
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
            <CardTitle className="text-2xl">{totalCredits}</CardTitle>
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
            <CardTitle className="text-2xl">
              {uniqueCourseTypes.length}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-1 flex-wrap">
              {uniqueCourseTypes.map((type) => (
                <Badge key={type} variant="outline" className="text-xs">
                  {courseTypeMap[type!] || type}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>教学班列表</CardTitle>
          <CardDescription>本学期教学班</CardDescription>
        </CardHeader>
        <CardContent>
          {teachingClasses.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              暂无教学班数据
            </div>
          ) : (
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
                </TableRow>
              </TableHeader>
              <TableBody>
                {teachingClasses.map((classInfo) => (
                  <TableRow key={classInfo.id}>
                    <TableCell className="font-medium">
                      {classInfo.courseName}
                    </TableCell>
                    <TableCell>{classInfo.className}</TableCell>
                    <TableCell>
                      <Badge variant="secondary">
                        {courseTypeMap[classInfo.courseType!] ||
                          classInfo.courseType ||
                          "-"}
                      </Badge>
                    </TableCell>
                    <TableCell>{classInfo.credit ?? "-"}</TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <IconUsers className="size-3 text-muted-foreground" />
                        {classInfo.studentCount ?? 0}
                      </div>
                    </TableCell>
                    <TableCell>{classInfo.schedule || "-"}</TableCell>
                    <TableCell>{classInfo.location || "-"}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
