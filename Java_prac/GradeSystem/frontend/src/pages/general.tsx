import { ChartAreaInteractive } from "@/components/chart-area-interactive";
import { SectionCards } from "@/components/section-cards";
import { DataTable } from "@/components/data-table";
import data from "./data.json";
import { TrendDirection } from "@/types/card-data";
import { useAuthContext } from "@/contexts/auth-context";

const sampleCardsData = [
  {
    id: "gpa-stats",
    title: "当前绩点 (GPA)",
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

export function GeneralData() {
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
            <ChartAreaInteractive />
          </div>
          <DataTable data={data} />
        </div>
      </div>
    </div>
  );
}
