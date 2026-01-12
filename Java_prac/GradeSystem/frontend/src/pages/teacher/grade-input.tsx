import { useState, useEffect, useRef, useCallback } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
import {
  IconCheck,
  IconX,
  IconRefresh,
  IconFileDownload,
  IconUpload,
} from "@tabler/icons-react";
import { useAuthContext } from "@/contexts/auth-context";
import { useTeacher } from "@/hooks/use-teacher";
import { teacherApi } from "@/api/v1/modules/teacher";
import type {
  TeachingClassWithStats,
  StudentGradeInput,
} from "@/types/teaching-class";

interface GradeWeights {
  usual: number;
  midterm: number;
  final: number;
  experiment: number;
}

const defaultWeights: GradeWeights = {
  usual: 0.2,
  midterm: 0.2,
  final: 0.4,
  experiment: 0.2,
};

const calculateFinalScore = (
  usual: number,
  midterm: number,
  final: number,
  experiment: number,
  weights: GradeWeights,
): number => {
  return (
    usual * weights.usual +
    midterm * weights.midterm +
    final * weights.final +
    experiment * weights.experiment
  );
};

const calculateGPA = (score: number): number => {
  if (score >= 90) return 4.0;
  if (score >= 85) return 3.7;
  if (score >= 80) return 3.3;
  if (score >= 75) return 3.0;
  if (score >= 70) return 2.7;
  if (score >= 65) return 2.3;
  if (score >= 60) return 2.0;
  return 1.0;
};

const validateScore = (value: string): boolean => {
  if (value === "") return true;
  const num = parseFloat(value);
  return !isNaN(num) && num >= 0 && num <= 100;
};

interface LocalGradeInput extends StudentGradeInput {
  localUsualScore: string;
  localMidtermScore: string;
  localFinalExamScore: string;
  localExperimentScore: string;
  localIsModified: boolean;
}

export function TeacherGradeInput() {
  const { user } = useAuthContext();
  const {
    teachingClasses,
    students,
    loading,
    error,
    getTeachingClasses,
    getStudentsInTeachingClass,
    batchUpdateGrades,
    exportGrades,
    importGrades,
  } = useTeacher();

  const [teacherId, setTeacherId] = useState<string | null>(null);
  const [selectedClassId, setSelectedClassId] = useState<string>("");
  const [localGrades, setLocalGrades] = useState<LocalGradeInput[]>([]);
  const [weights, setWeights] = useState<GradeWeights>(defaultWeights);
  const [initError, setInitError] = useState<string | null>(null);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

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
    }
  }, [selectedClassId, getStudentsInTeachingClass]);

  useEffect(() => {
    const mapped: LocalGradeInput[] = students.map((s) => ({
      ...s,
      localUsualScore: s.usualScore?.toString() ?? "",
      localMidtermScore: s.midtermScore?.toString() ?? "",
      localFinalExamScore: s.finalExamScore?.toString() ?? "",
      localExperimentScore: s.experimentScore?.toString() ?? "",
      localIsModified: false,
    }));
    setLocalGrades(mapped);
  }, [students]);

  const handleClassChange = (classId: string) => {
    setSelectedClassId(classId);
    setSaveMessage(null);
  };

  const handleScoreChange = (
    id: string | undefined,
    field:
      | "localUsualScore"
      | "localMidtermScore"
      | "localFinalExamScore"
      | "localExperimentScore",
    value: string,
  ) => {
    if (!id) return;
    setLocalGrades((prev) =>
      prev.map((grade) => {
        if (grade.id === id) {
          const updated = { ...grade, [field]: value, localIsModified: true };

          const usual = parseFloat(updated.localUsualScore) || 0;
          const midterm = parseFloat(updated.localMidtermScore) || 0;
          const final = parseFloat(updated.localFinalExamScore) || 0;
          const experiment = parseFloat(updated.localExperimentScore) || 0;

          const hasAllScores =
            updated.localUsualScore !== "" &&
            updated.localMidtermScore !== "" &&
            updated.localFinalExamScore !== "" &&
            updated.localExperimentScore !== "";

          if (hasAllScores) {
            updated.finalScore = calculateFinalScore(
              usual,
              midterm,
              final,
              experiment,
              weights,
            );
            updated.gpa = calculateGPA(updated.finalScore);
          } else {
            updated.finalScore = null;
            updated.gpa = null;
          }

          return updated;
        }
        return grade;
      }),
    );
  };

  const handleResetSingle = (id: string | undefined) => {
    if (!id) return;
    const original = students.find((s) => s.id === id);
    if (!original) return;

    setLocalGrades((prev) =>
      prev.map((grade) =>
        grade.id === id
          ? {
              ...grade,
              localUsualScore: original.usualScore?.toString() ?? "",
              localMidtermScore: original.midtermScore?.toString() ?? "",
              localFinalExamScore: original.finalExamScore?.toString() ?? "",
              localExperimentScore: original.experimentScore?.toString() ?? "",
              finalScore: original.finalScore,
              gpa: original.gpa,
              localIsModified: false,
            }
          : grade,
      ),
    );
  };

  const handleSaveAll = useCallback(async () => {
    const modifiedGrades = localGrades
      .filter((g) => g.localIsModified && g.id)
      .map((g) => ({
        id: g.id,
        studentCode: g.studentCode,
        name: g.name,
        className: g.className,
        usualScore: g.localUsualScore ? parseFloat(g.localUsualScore) : null,
        midtermScore: g.localMidtermScore
          ? parseFloat(g.localMidtermScore)
          : null,
        finalExamScore: g.localFinalExamScore
          ? parseFloat(g.localFinalExamScore)
          : null,
        experimentScore: g.localExperimentScore
          ? parseFloat(g.localExperimentScore)
          : null,
        version: g.version,
      }));

    if (modifiedGrades.length === 0) {
      setSaveMessage("没有需要保存的修改");
      return;
    }

    const result = await batchUpdateGrades(selectedClassId, modifiedGrades);
    if (result) {
      if (result.success) {
        setSaveMessage(`成功更新 ${result.updatedCount} 条成绩`);
        setLocalGrades((prev) =>
          prev.map((g) => ({ ...g, localIsModified: false })),
        );
        await getStudentsInTeachingClass(selectedClassId);
      } else {
        setSaveMessage(`更新失败: ${result.errors?.join(", ") || "未知错误"}`);
      }
    }
  }, [
    localGrades,
    selectedClassId,
    batchUpdateGrades,
    getStudentsInTeachingClass,
  ]);

  const handleExport = useCallback(async () => {
    if (!selectedClassId) return;
    await exportGrades(selectedClassId);
  }, [selectedClassId, exportGrades]);

  const handleImport = useCallback(async () => {
    fileInputRef.current?.click();
  }, []);

  const handleFileChange = useCallback(
    async (event: React.ChangeEvent<HTMLInputElement>) => {
      const file = event.target.files?.[0];
      if (!file || !selectedClassId) return;

      const result = await importGrades(selectedClassId, file);
      if (result) {
        if (result.success) {
          setSaveMessage(`成功导入 ${result.updatedCount} 条成绩`);
          await getStudentsInTeachingClass(selectedClassId);
        } else {
          setSaveMessage(
            `导入失败: ${result.errors?.join(", ") || "未知错误"}`,
          );
        }
      }
      event.target.value = "";
    },
    [selectedClassId, importGrades, getStudentsInTeachingClass],
  );

  const displayError = error || initError;

  const stats = {
    total: localGrades.length,
    completed: localGrades.filter(
      (g) =>
        g.localUsualScore !== "" &&
        g.localMidtermScore !== "" &&
        g.localFinalExamScore !== "" &&
        g.localExperimentScore !== "",
    ).length,
    modified: localGrades.filter((g) => g.localIsModified).length,
  };

  const getGradeBadge = (score: number | null | undefined) => {
    if (score == null || score === 0)
      return <Badge variant="outline">未录入</Badge>;
    if (score >= 90) return <Badge className="bg-emerald-500">优秀</Badge>;
    if (score >= 80) return <Badge className="bg-blue-500">良好</Badge>;
    if (score >= 70) return <Badge className="bg-yellow-500">中等</Badge>;
    if (score >= 60) return <Badge className="bg-orange-500">及格</Badge>;
    return <Badge variant="destructive">不及格</Badge>;
  };

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

  return (
    <div className="flex flex-col gap-6 p-6">
      <input
        type="file"
        ref={fileInputRef}
        className="hidden"
        accept=".xlsx,.xls"
        onChange={handleFileChange}
      />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">成绩登记</h1>
          <p className="text-muted-foreground">录入和管理学生成绩</p>
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

      {saveMessage && (
        <div
          className={`p-3 rounded-md ${saveMessage.includes("失败") ? "bg-destructive/10 text-destructive" : "bg-emerald-50 text-emerald-700"}`}
        >
          {saveMessage}
        </div>
      )}

      {selectedClassId && (
        <>
          <div className="flex items-center justify-between">
            <div className="flex gap-4">
              <Card className="px-4 py-2">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground">总人数:</span>
                  <span className="font-bold">{stats.total}</span>
                </div>
              </Card>
              <Card className="px-4 py-2">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground">已录入:</span>
                  <span className="font-bold text-emerald-600">
                    {stats.completed}
                  </span>
                </div>
              </Card>
              <Card className="px-4 py-2">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground">待保存:</span>
                  <span className="font-bold text-orange-600">
                    {stats.modified}
                  </span>
                </div>
              </Card>
            </div>
            <div className="flex gap-2">
              <Button variant="outline" onClick={handleImport}>
                <IconUpload className="size-4 mr-1" />
                导入成绩
              </Button>
              <Button variant="outline" onClick={handleExport}>
                <IconFileDownload className="size-4 mr-1" />
                导出成绩
              </Button>
              <Button
                onClick={handleSaveAll}
                disabled={stats.modified === 0 || loading}
              >
                <IconCheck className="size-4 mr-1" />
                保存全部
              </Button>
            </div>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>成绩权重设置</CardTitle>
              <CardDescription>
                设置各项成绩的权重（总和应为100%）
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-4 gap-4">
                <div className="flex flex-col gap-2">
                  <Label htmlFor="usual-weight">平时成绩权重</Label>
                  <Input
                    id="usual-weight"
                    type="number"
                    min="0"
                    max="100"
                    step="5"
                    value={(weights.usual * 100).toFixed(0)}
                    onChange={(e) =>
                      setWeights({
                        ...weights,
                        usual: parseFloat(e.target.value) / 100,
                      })
                    }
                  />
                  <span className="text-xs text-muted-foreground">
                    {(weights.usual * 100).toFixed(0)}%
                  </span>
                </div>
                <div className="flex flex-col gap-2">
                  <Label htmlFor="midterm-weight">期中成绩权重</Label>
                  <Input
                    id="midterm-weight"
                    type="number"
                    min="0"
                    max="100"
                    step="5"
                    value={(weights.midterm * 100).toFixed(0)}
                    onChange={(e) =>
                      setWeights({
                        ...weights,
                        midterm: parseFloat(e.target.value) / 100,
                      })
                    }
                  />
                  <span className="text-xs text-muted-foreground">
                    {(weights.midterm * 100).toFixed(0)}%
                  </span>
                </div>
                <div className="flex flex-col gap-2">
                  <Label htmlFor="final-weight">期末成绩权重</Label>
                  <Input
                    id="final-weight"
                    type="number"
                    min="0"
                    max="100"
                    step="5"
                    value={(weights.final * 100).toFixed(0)}
                    onChange={(e) =>
                      setWeights({
                        ...weights,
                        final: parseFloat(e.target.value) / 100,
                      })
                    }
                  />
                  <span className="text-xs text-muted-foreground">
                    {(weights.final * 100).toFixed(0)}%
                  </span>
                </div>
                <div className="flex flex-col gap-2">
                  <Label htmlFor="experiment-weight">实验成绩权重</Label>
                  <Input
                    id="experiment-weight"
                    type="number"
                    min="0"
                    max="100"
                    step="5"
                    value={(weights.experiment * 100).toFixed(0)}
                    onChange={(e) =>
                      setWeights({
                        ...weights,
                        experiment: parseFloat(e.target.value) / 100,
                      })
                    }
                  />
                  <span className="text-xs text-muted-foreground">
                    {(weights.experiment * 100).toFixed(0)}%
                  </span>
                </div>
              </div>
              <div className="mt-2 text-sm">
                权重总和:{" "}
                <span className="font-bold">
                  {(
                    (weights.usual +
                      weights.midterm +
                      weights.final +
                      weights.experiment) *
                    100
                  ).toFixed(0)}
                  %
                </span>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>学生成绩列表</CardTitle>
              <CardDescription>
                输入成绩范围：0-100，留空表示未录入
              </CardDescription>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="flex justify-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
              ) : localGrades.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  暂无学生数据
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-[100px]">学号</TableHead>
                      <TableHead className="w-[100px]">姓名</TableHead>
                      <TableHead>班级</TableHead>
                      <TableHead className="w-[120px]">平时成绩</TableHead>
                      <TableHead className="w-[120px]">期中成绩</TableHead>
                      <TableHead className="w-[120px]">期末成绩</TableHead>
                      <TableHead className="w-[120px]">实验成绩</TableHead>
                      <TableHead className="w-[100px]">总评成绩</TableHead>
                      <TableHead className="w-20">绩点</TableHead>
                      <TableHead>等级</TableHead>
                      <TableHead className="w-[100px]">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {localGrades.map((grade) => (
                      <TableRow
                        key={grade.id || grade.studentCode}
                        className={
                          grade.localIsModified ? "bg-orange-50/50" : ""
                        }
                      >
                        <TableCell>{grade.studentCode}</TableCell>
                        <TableCell className="font-medium">
                          {grade.name}
                        </TableCell>
                        <TableCell>{grade.className}</TableCell>
                        <TableCell>
                          <Input
                            type="number"
                            min="0"
                            max="100"
                            value={grade.localUsualScore}
                            onChange={(e) =>
                              handleScoreChange(
                                grade.id,
                                "localUsualScore",
                                e.target.value,
                              )
                            }
                            className={
                              !validateScore(grade.localUsualScore)
                                ? "border-destructive"
                                : ""
                            }
                          />
                        </TableCell>
                        <TableCell>
                          <Input
                            type="number"
                            min="0"
                            max="100"
                            value={grade.localMidtermScore}
                            onChange={(e) =>
                              handleScoreChange(
                                grade.id,
                                "localMidtermScore",
                                e.target.value,
                              )
                            }
                            className={
                              !validateScore(grade.localMidtermScore)
                                ? "border-destructive"
                                : ""
                            }
                          />
                        </TableCell>
                        <TableCell>
                          <Input
                            type="number"
                            min="0"
                            max="100"
                            value={grade.localFinalExamScore}
                            onChange={(e) =>
                              handleScoreChange(
                                grade.id,
                                "localFinalExamScore",
                                e.target.value,
                              )
                            }
                            className={
                              !validateScore(grade.localFinalExamScore)
                                ? "border-destructive"
                                : ""
                            }
                          />
                        </TableCell>
                        <TableCell>
                          <Input
                            type="number"
                            min="0"
                            max="100"
                            value={grade.localExperimentScore}
                            onChange={(e) =>
                              handleScoreChange(
                                grade.id,
                                "localExperimentScore",
                                e.target.value,
                              )
                            }
                            className={
                              !validateScore(grade.localExperimentScore)
                                ? "border-destructive"
                                : ""
                            }
                          />
                        </TableCell>
                        <TableCell className="font-bold">
                          {grade.finalScore != null
                            ? grade.finalScore.toFixed(1)
                            : "-"}
                        </TableCell>
                        <TableCell>
                          {grade.gpa != null ? grade.gpa.toFixed(2) : "-"}
                        </TableCell>
                        <TableCell>{getGradeBadge(grade.finalScore)}</TableCell>
                        <TableCell>
                          <div className="flex gap-1">
                            {grade.localIsModified && (
                              <Button
                                variant="ghost"
                                size="icon"
                                className="size-8"
                                onClick={() => handleResetSingle(grade.id)}
                              >
                                <IconX className="size-4 text-destructive" />
                              </Button>
                            )}
                            <Button
                              variant="ghost"
                              size="icon"
                              className="size-8"
                              onClick={() => handleResetSingle(grade.id)}
                            >
                              <IconRefresh className="size-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </>
      )}

      {!selectedClassId && teachingClasses.length > 0 && (
        <div className="flex flex-1 items-center justify-center">
          <p className="text-muted-foreground">请选择一个教学班开始录入成绩</p>
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
