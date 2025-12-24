import { GenericAreaChart } from "@/components/chart-area-interactive";
import { SectionCards } from "@/components/section-cards";
import { DataTable } from "@/components/data-table";
import data from "./data.json";
import { TrendDirection } from "@/types/card-data";
import { useAuthContext } from "@/contexts/auth-context";

const sampleCardsData = [
  {
    id: "gpa-stats",
    title: "当前绩点",
    value: "3.85",
    trend: {
      direction: TrendDirection.UP,
      value: "+0.12",
      isVisible: true,
    },
    footer: {
      status: "表现优异",
      description: "专业排名前 5%",
    },
  },
  {
    id: "credits-progress",
    title: "修读学分",
    value: "86 / 120",
    trend: {
      direction: TrendDirection.UP,
      value: "+12",
      isVisible: true,
    },
    footer: {
      status: "本学期新增 12 学分",
      description: "毕业进度 71%",
    },
  },
  {
    id: "current-courses",
    title: "本学期课程",
    value: "6",
    trend: {
      direction: TrendDirection.NEUTRAL,
      value: "0",
      isVisible: false,
    },
    footer: {
      status: "学习状态",
      description: "包含 2 门必修课",
    },
  },
  {
    id: "weighted-score",
    title: "加权平均分",
    value: "89.2",
    trend: {
      direction: TrendDirection.DOWN,
      value: "-1.5",
      isVisible: true,
    },
    footer: {
      status: "较上学期轻微下滑",
      description: "基于所有已修课程",
    },
  },
];

const sampleChartData = {
  title: "学业成绩趋势",
  description: "个人绩点与班级平均分对比",
  xAxisKey: "semester",
  series: [
    { key: "gpa", label: "个人 GPA" },
    { key: "class_avg", label: "班级平均" },
  ],
  data: [
    { semester: "2022 秋", gpa: 3.2, class_avg: 3.0 },
    { semester: "2023 春", gpa: 3.5, class_avg: 3.1 },
    { semester: "2023 秋", gpa: 3.4, class_avg: 3.2 },
    { semester: "2024 春", gpa: 3.8, class_avg: 3.3 },
    { semester: "2024 秋", gpa: 3.9, class_avg: 3.2 },
  ],
};

export function GeneralView() {
  {
    /* TODO 根据用户角色进行响应,使用use去获取数据，这里只是渲染*/
  }
  const { user } = useAuthContext();
  return (
    <div className="flex flex-1 flex-col">
      <div className="@container/main flex flex-1 flex-col gap-2">
        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
          <SectionCards cardsData={sampleCardsData} />
          <div className="px-4 lg:px-6">
            <GenericAreaChart chartData={sampleChartData} />
          </div>
          <DataTable data={data} />
        </div>
      </div>
    </div>
  );
}
