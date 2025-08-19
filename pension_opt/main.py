import numpy as np
from scipy.optimize import minimize
from utils import get_year_diff
from predict_savg import predict_savg
from datetime import datetime


class Object:
    def __init__(self, start_date: str, birth_date: str, N: int, savg_year: list, R_previous: list):
        self.start_date = datetime.strptime(start_date, "%Y%m")
        self.birth_date = datetime.strptime(birth_date, "%Y%m")
        self.target_months = N
        self.savg_year = savg_year          # 年度均值序列
        self.R_previous = R_previous        # 已缴年度比例

        # 个人账户养老金计发月数映射表（40~70岁）
        self.months = [233, 230, 226, 223, 220, 216, 212, 207, 204, 199, 195, 190, 185, 180, 175, 170, 164, 158, 152, 145, 139, 132, 125, 117, 109, 101, 93, 84, 75, 65, 56]
        self.month_map = {40 + i: self.months[i] for i in range(len(self.months))}

        # 先把年度 savg 展开到月份
        self.extend_savg()

    def extend_savg(self):
        # 起始年按起始月份补齐到年底
        self.savg = [self.savg_year[0]] * (13 - self.start_date.month)
        # 中间完整年度
        for y in self.savg_year[1:-1]:
            self.savg += [y] * 12
        # 最后一年补到目标月份数
        remain = self.target_months - len(self.savg)
        if remain > 0:
            self.savg += [self.savg_year[-1]] * remain

    def extend_R(self, R_post):
        # R_post 是“后续各年”的年度比例
        R_year = list(self.R_previous) + list(R_post)
        R = [R_year[0]] * (13 - self.start_date.month)
        for y in R_year[1:-1]:
            R += [y] * 12
        remain = self.target_months - len(R)
        if remain > 0:
            R += [R_year[-1]] * remain
        return R

    def total_cost(self, R):
        # 养老金总支出
        cost = sum(self.savg[i] * R[i] * 0.2 for i in range(self.target_months)) * (1 + 0.005) # 0.5%的通胀率
        return cost

    def get_month_count(self):
        total_months = self.start_date.year * 12 + self.start_date.month + self.target_months
        month_diff = total_months - (self.birth_date.year * 12 + self.birth_date.month)
        age = month_diff // 12
        return self.month_map[age]

    def final_salary(self, R):
        # 你的公式保持不变
        return (self.savg[-1] / 2400.0) * (self.target_months + sum(R)) + 0.08 * sum(self.savg[i] * R[i] for i in range(self.target_months)) / self.get_month_count()

    def return_month(self, R_post):
        R_full = self.extend_R(R_post)
        return self.total_cost(R_full)/self.final_salary(R_full)
    
    def obj_func(self, R_post):
        return_months_baseline = 120
        salary_baseline = 2500
        return 0.5 * self.return_month(R_post)/return_months_baseline - 0.5 * self.final_salary(self.extend_R(R_post))/salary_baseline

if __name__ == "__main__":
    # 固定参数
    start_date = "201801"   # 开始缴纳年月
    birth_date = "197805"   # 出生年月
    N = 220                 # 目标缴费月份数

    # 先前已缴年度 savg（年均值）
    savg_previous = [7125, 6381.66, 5470, 5818.33, 6595, 6863.33, 7265, 7265]
    # 先前已缴年度比例
    R_previous = [np.float64(0.6)] * len(savg_previous)

    # 预测补全年份（年度粒度）
    year_diff = get_year_diff(start_date, N)
    savg_year = savg_previous + predict_savg(year_diff - len(savg_previous))

    # 初始化与优化设置
    obj = Object(start_date, birth_date, N, savg_year, R_previous)
    dim = year_diff - len(savg_previous)

    # 初始点与边界（年度比例）
    x0 = np.full(dim, 1)
    bounds = [(0.6, 0.8)] * dim

    # 选择 L-BFGS-B：仅 box 约束、目标平滑、维度可到几十
    res = minimize(obj.obj_func, x0=x0, method="L-BFGS-B", bounds=bounds, options={"maxiter": 2000, "ftol": 1e-9})
    print(res)
    print("Optimal R (yearly):", res.x)
    final_R = obj.extend_R(res.x)
    # R_post = np.concatenate((np.full(dim//3, 1.3) , np.full(dim//3, 0.8), np.full(dim - 2*dim//3, 0.6)))
    # print(R_post)
    # final_R = obj.extend_R(R_post)
    final_cost = obj.total_cost(final_R)
    print("Final salary: ", obj.final_salary(final_R))
    print("Total cost: ", final_cost)
    print("Monthly average cost: ", final_cost/N)
    print("Return months: ", obj.return_month(res.x))
