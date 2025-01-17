import numpy as np
from scipy.io import loadmat
import pandas as pd
import os

def load_mat_file(filename):
    """加载.mat文件"""
    file_path = os.path.join("./data", filename)
    return loadmat(file_path)

class SelectionRule:
    """选择规则类，用于定义数据选择的方式"""
    
    @staticmethod
    def by_indices(indices):
        """通过指定索引列表选择"""
        return indices
    
    @staticmethod
    def by_range(start=0, end=None, step=1):
        """通过范围规则选择，支持负数索引"""
        if end is None:
            end = float('inf')  # 保证range支持到最大值
        return range(start, end, step)
    
    @staticmethod
    def by_single(index):
        """选择单个索引，支持负数索引"""
        return [index] if isinstance(index, int) else index

class DataSelector:
    """数据选择器，用于组合行列选择规则"""
    
    def __init__(self, data):
        self.data = data
    
    def _normalize_index(self, index, dim_size):
        """处理负数索引"""
        if isinstance(index, (list, range)):
            return [self._normalize_single_idx(i, dim_size) for i in index]
        return self._normalize_single_idx(index, dim_size)
    
    def _normalize_single_idx(self, index, dim_size):
        """单一索引处理"""
        if index < 0:
            index = dim_size + index
        return max(0, min(index, dim_size - 1))
    
    def _normalize_indices(self, indices, dim_size):
        """改进的索引处理逻辑"""
        if indices is None:
            return slice(None)
        
        if isinstance(indices, range):
            # 处理range对象中的负数索引
            start = indices.start if indices.start >= 0 else dim_size + indices.start
            stop = indices.stop if indices.stop >= 0 else dim_size + indices.stop
            return range(max(0, start), max(0, stop), indices.step)
        
        if isinstance(indices, (list, np.ndarray)):
            return [self._normalize_single_idx(i, dim_size) for i in indices]
        
        if isinstance(indices, int):
            return [self._normalize_single_idx(indices, dim_size)]
        
        return slice(None)
    
    def select(self, row_selector=None, col_selector=None):
        """根据行列选择器提取数据，支持负数索引"""
        rows = self._normalize_indices(row_selector, self.data.shape[0])
        cols = self._normalize_indices(col_selector, self.data.shape[1])
        
        result = self.data[rows if isinstance(rows, slice) else np.array(rows)]
        if result.ndim == 1:
            result = result[:, np.newaxis]
        
        if cols is not None and not isinstance(cols, slice):
            result = result[:, np.array(cols)]
        
        return result

class Workflow:
    def __init__(self, mat_data):
        self.data = mat_data
        self.selector = DataSelector(mat_data)
        self.extracted_data = []
        print(f"原始数据形状: {self.data.shape}")

    def add_extraction(self, row_rule=None, col_rule=None):
        """改进的提取操作，直接向提取结果中添加数据"""
        try:
            result = self.selector.select(row_rule, col_rule)
        except IndexError as e:
            print(f"索引错误: {e}")
            return self

        rows = self.selector._normalize_indices(row_rule, self.data.shape[0])
        cols = self.selector._normalize_indices(col_rule, self.data.shape[1])
        
        print(f"处理后的行索引: {list(rows)}")
        print(f"处理后的列索引: {list(cols)}")
        print(f"提取的数据形状: {result.shape}")
        
        self.extracted_data.append((rows, cols, result))
        return self

    def get_merged_data(self):
        """将所有提取的数据合并到一个数组中，去除空行和空列"""
        merged_data = []
        row_idx_set = set()  # 使用集合来跟踪行索引
        col_idx_set = set()  # 使用集合来跟踪列索引
        
        for rows, cols, result in self.extracted_data:
            if result.ndim == 1:
                result = result[:, np.newaxis]  # 处理一维数据

            # 过滤NaN和0
            valid_indices = np.where(~np.isnan(result) & (result != 0))
            for i, j in zip(*valid_indices):
                merged_data.append((rows[i], cols[j], result[i, j]))
                row_idx_set.add(rows[i])
                col_idx_set.add(cols[j])

        # 根据索引集计算合并后的最大行列数
        max_row = max(row_idx_set) + 1 if row_idx_set else 0
        max_col = max(col_idx_set) + 1 if col_idx_set else 0

        result_matrix = np.full((max_row, max_col), np.nan)
        for row, col, value in merged_data:
            result_matrix[row, col] = value

        print(f"合并后的数据形状: {result_matrix.shape}")
        print(f"非空值数量: {np.count_nonzero(~np.isnan(result_matrix))}")

        return result_matrix

def save_to_excel(data, output_filename):
    """保存数据到Excel，支持去除空行和空列"""
    output_dir = "./output"
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    df = pd.DataFrame(data)
    df = df.loc[~df.isnull().all(axis=1), ~df.isnull().all(axis=0)]  # 一次性删除全NaN的行列
    
    name, ext = os.path.splitext(output_filename)
    if not ext:
        ext = '.xlsx'
    
    counter = 1
    final_filename = f"{name}{ext}"
    while os.path.exists(os.path.join(output_dir, final_filename)):
        final_filename = f"{name}_{counter}{ext}"
        counter += 1
    
    try:
        output_path = os.path.join(output_dir, final_filename)
        df.to_excel(output_path, index=False)
        print(f"数据已成功保存到: {output_path}")
    except Exception as e:
        print(f"保存文件时发生错误: {str(e)}")
        raise

def main():
    filename = "x.mat" # 数据的文件名
    mat_data = load_mat_file(filename)
    data = mat_data['x'] # 数据内存储的变量名，一般都与文件名相同；不确定先在MATLAB中查看
    
    workflow = Workflow(data)
    
    # 示例
    workflow.add_extraction(
        row_rule=SelectionRule.by_indices([0]), # 使用列表进行选中，这里可以选择多行，也可以只选一行
        col_rule=SelectionRule.by_range(0, -1, 60) # 支持负数索引，从第0列到最后一列，间隔为60
    ).add_extraction(
        row_rule=SelectionRule.by_range(1, -1, 50),
        col_rule=SelectionRule.by_range(0, -1, 60)
    ).add_extraction(
        row_rule=SelectionRule.by_single(-1), # 选中单行（只能选一行），最后一行
        col_rule=SelectionRule.by_range(0, -1, 60)
    )
    
    merged_data = workflow.get_merged_data()
    
    # 检查数据是否为空
    if np.all(np.isnan(merged_data)):
        print("错误: 生成的数据全为NaN")
    else:
        print("数据提取成功")
        
    save_to_excel(merged_data, "merged_output.xlsx")

if __name__ == "__main__":
    main()
