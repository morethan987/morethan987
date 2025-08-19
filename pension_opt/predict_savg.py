from typing import List


def predict_savg(steps: int, data: List[float] = None) -> List[float]:
    """
    预测全市上年度在岗职工月平均工资, 暂时使用最基础的线性模型

    Input:

    steps: int - 预测的步数
    data: List[float] - 历史数据

    Output:
    output: List[float] - 预测数据列表
    """
    start = 7265 # 初始值
    k = 100 # 年增长
    return [start + (i + 1) * k for i in range(steps)]

