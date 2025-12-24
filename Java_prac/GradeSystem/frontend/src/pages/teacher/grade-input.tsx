// 学生成绩登记界面
import { useState } from "react";
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
} from "@tabler/icons-react";

// 模拟学生成绩数据（用于编辑）
interface StudentGradeInput {
  id: string;
  studentCode: string;
  name: string;
  className: string;
  usualScore: string;
  midtermScore: string;
  finalExamScore: string;
  experimentScore: string;
  finalScore: number;
  gpa: number;
  isModified: boolean;
}

const mockGradesInput: StudentGradeInput[] = [
  {
    id: "1",
    studentCode: "2021001",
    name: "张三",
    className: "计算机科学与技术1班",
    usualScore: "85",
    midtermScore: "88",
    finalExamScore: "90",
    experimentScore: "92",
    finalScore: 88.5,
    gpa: 3.85,
    isModified: false,
  },
  {
    id: "2",
    studentCode: "2021002",
    name: "李四",
    className: "计算机科学与技术1班",
    usualScore: "78",
    midtermScore: "82",
    finalExamScore: "85",
    experimentScore: "80",
    finalScore: 81.5,
    gpa: 3.35,
    isModified: false,
  },
  {
    id: "3",
    studentCode: "2021003",
    name: "王五",
    className: "计算机科学与技术1班",
    usualScore: "92",
    midtermScore: "90",
    finalExamScore: "95",
    experimentScore: "88",
    finalScore: 91.5,
    gpa: 4.0,
    isModified: false,
  },
  {
    id: "4",
    studentCode: "2021004",
    name: "赵六",
    className: "计算机科学与技术1班",
    usualScore: "",
    midtermScore: "",
    finalExamScore: "",
    experimentScore: "",
    finalScore: 0,
    gpa: 0,
    isModified: false,
  },
  {
    id: "5",
    studentCode: "2021005",
    name: "钱七",
    className: "计算机科学与技术1班",
    usualScore: "",
    midtermScore: "",
    finalExamScore: "",
    experimentScore: "",
    finalScore: 0,
    gpa: 0,
    isModified: false,
  },
  {
    id: "6",
    studentCode: "2021006",
    name: "孙八",
    className: "计算机科学与技术1班",
    usualScore: "75",
    midtermScore: "78",
    finalExamScore: "80",
    experimentScore: "76",
    finalScore: 77.5,
    gpa: 2.85,
    isModified: false,
  },
  {
    id: "7",
    studentCode: "2021007",
    name: "周九",
    className: "计算机科学与技术1班",
    usualScore: "",
    midtermScore: "",
    finalExamScore: "",
    experimentScore: "",
    finalScore: 0,
    gpa: 0,
    isModified: false,
  },
  {
    id: "8",
    studentCode: "2021008",
    name: "吴十",
    className: "计算机科学与技术1班",
    usualScore: "",
    midtermScore: "",
    finalExamScore: "",
    experimentScore: "",
    finalScore: 0,
    gpa: 0,
    isModified: false,
  },
];

// 成绩权重配置
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

// 计算总评成绩和绩点
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

// 验证成绩输入
const validateScore = (value: string): boolean => {
  if (value === "") return true;
  const num = parseFloat(value);
  return !isNaN(num) && num >= 0 && num <= 100;
};

export function TeacherGradeInput() {
  const [selectedClass, setSelectedClass] = useState<string>("TC001");
  const [grades, setGrades] = useState<StudentGradeInput[]>(mockGradesInput);
  const [weights, setWeights] = useState<GradeWeights>(defaultWeights);

  // 处理成绩输入变化
  const handleScoreChange = (
    id: string,
    field: "usualScore" | "midtermScore" | "finalExamScore" | "experimentScore",
    value: string,
  ) => {
    setGrades((prev) =>
      prev.map((grade) => {
        if (grade.id === id) {
          const updated = { ...grade, [field]: value, isModified: true };

          // 计算总评成绩
          const usual = parseFloat(updated.usualScore) || 0;
          const midterm = parseFloat(updated.midtermScore) || 0;
          const final = parseFloat(updated.finalExamScore) || 0;
          const experiment = parseFloat(updated.experimentScore) || 0;

          // 检查是否所有成绩都已填写
          const hasAllScores =
            updated.usualScore !== "" &&
            updated.midtermScore !== "" &&
            updated.finalExamScore !== "" &&
            updated.experimentScore !== "";

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
            updated.finalScore = 0;
            updated.gpa = 0;
          }

          return updated;
        }
        return grade;
      }),
    );
  };

  // 保存单个学生成绩
  const handleSaveSingle = (id: string) => {
    setGrades((prev) =>
      prev.map((grade) =>
        grade.id === id ? { ...grade, isModified: false } : grade,
      ),
    );
  };

  // 重置单个学生成绩
  const handleResetSingle = (id: string) => {
    setGrades((prev) =>
      prev.map((grade) =>
        grade.id === id
          ? {
              ...grade,
              usualScore: "",
              midtermScore: "",
              finalExamScore: "",
              experimentScore: "",
              finalScore: 0,
              gpa: 0,
              isModified: false,
            }
          : grade,
      ),
    );
  };

  // 批量保存
  const handleSaveAll = () => {
    setGrades((prev) => prev.map((grade) => ({ ...grade, isModified: false })));
  };

  // 导入成绩模板
  const handleImportTemplate = () => {
    // TODO: 实现导入功能
  };

  // 导出成绩
  const handleExport = () => {
    // TODO: 实现导出功能
  };

  // 计算统计
  const stats = {
    total: grades.length,
    completed: grades.filter(
      (g) =>
        g.usualScore !== "" &&
        g.midtermScore !== "" &&
        g.finalExamScore !== "" &&
        g.experimentScore !== "",
    ).length,
    modified: grades.filter((g) => g.isModified).length,
  };

  // 获取成绩等级样式
  const getGradeBadge = (score: number) => {
    if (score === 0) return <Badge variant="outline">未录入</Badge>;
    if (score >= 90) return <Badge className="bg-emerald-500">优秀</Badge>;
    if (score >= 80) return <Badge className="bg-blue-500">良好</Badge>;
    if (score >= 70) return <Badge className="bg-yellow-500">中等</Badge>;
    if (score >= 60) return <Badge className="bg-orange-500">及格</Badge>;
    return <Badge variant="destructive">不及格</Badge>;
  };

  return (
    <div className="flex flex-col gap-6 p-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">成绩登记</h1>
          <p className="text-muted-foreground">录入和管理学生成绩</p>
        </div>
        <div className="flex items-center gap-2">
          <Select value={selectedClass} onValueChange={setSelectedClass}>
            <SelectTrigger className="w-[200px]">
              <SelectValue placeholder="选择教学班" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="TC001">数据结构与算法</SelectItem>
              <SelectItem value="TC002">数据库系统原理</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* 统计卡片和操作按钮 */}
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
          <Button variant="outline" onClick={handleImportTemplate}>
            导入模板
          </Button>
          <Button variant="outline" onClick={handleExport}>
            导出成绩
          </Button>
          <Button onClick={handleSaveAll} disabled={stats.modified === 0}>
            <IconFileDownload className="size-4" />
            保存全部
          </Button>
        </div>
      </div>

      {/* 成绩权重设置 */}
      <Card>
        <CardHeader>
          <CardTitle>成绩权重设置</CardTitle>
          <CardDescription>设置各项成绩的权重（总和应为100%）</CardDescription>
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

      {/* 成绩录入表格 */}
      <Card>
        <CardHeader>
          <CardTitle>学生成绩列表</CardTitle>
          <CardDescription>输入成绩范围：0-100，留空表示未录入</CardDescription>
        </CardHeader>
        <CardContent>
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
                <TableHead className="w-[150px]">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {grades.map((grade) => (
                <TableRow
                  key={grade.id}
                  className={grade.isModified ? "bg-orange-50/50" : ""}
                >
                  <TableCell>{grade.studentCode}</TableCell>
                  <TableCell className="font-medium">{grade.name}</TableCell>
                  <TableCell>{grade.className}</TableCell>
                  <TableCell>
                    <Input
                      type="number"
                      min="0"
                      max="100"
                      value={grade.usualScore}
                      onChange={(e) =>
                        handleScoreChange(
                          grade.id,
                          "usualScore",
                          e.target.value,
                        )
                      }
                      className={
                        !validateScore(grade.usualScore)
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
                      value={grade.midtermScore}
                      onChange={(e) =>
                        handleScoreChange(
                          grade.id,
                          "midtermScore",
                          e.target.value,
                        )
                      }
                      className={
                        !validateScore(grade.midtermScore)
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
                      value={grade.finalExamScore}
                      onChange={(e) =>
                        handleScoreChange(
                          grade.id,
                          "finalExamScore",
                          e.target.value,
                        )
                      }
                      className={
                        !validateScore(grade.finalExamScore)
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
                      value={grade.experimentScore}
                      onChange={(e) =>
                        handleScoreChange(
                          grade.id,
                          "experimentScore",
                          e.target.value,
                        )
                      }
                      className={
                        !validateScore(grade.experimentScore)
                          ? "border-destructive"
                          : ""
                      }
                    />
                  </TableCell>
                  <TableCell className="font-bold">
                    {grade.finalScore > 0 ? grade.finalScore.toFixed(1) : "-"}
                  </TableCell>
                  <TableCell>
                    {grade.gpa > 0 ? grade.gpa.toFixed(2) : "-"}
                  </TableCell>
                  <TableCell>{getGradeBadge(grade.finalScore)}</TableCell>
                  <TableCell>
                    <div className="flex gap-1">
                      {grade.isModified && (
                        <>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="size-8"
                            onClick={() => handleSaveSingle(grade.id)}
                          >
                            <IconCheck className="size-4 text-emerald-600" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="size-8"
                            onClick={() => handleResetSingle(grade.id)}
                          >
                            <IconX className="size-4 text-destructive" />
                          </Button>
                        </>
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
        </CardContent>
      </Card>
    </div>
  );
}
