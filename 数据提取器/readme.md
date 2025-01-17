# 数据提取器

本项目通过 Python 脚本来加载 .mat 文件并进行数据提取，最终将结果保存到 Excel 文件中。

## 目录结构
- `main.py`：程序入口，包含数据加载、规则定义、提取和保存逻辑
- `data/`：存放需要处理的 .mat 文件
- `output/`：存放输出的 Excel 文件

## 相关依赖
- numpy：最新版即可
- scipy：最新版即可
- pandas：最新版即可
- os：最新版即可

## 使用说明
1. 将需要处理的 .mat 文件放置在 `./data` 目录下，文件名应与脚本中的变量名相对应。
2. 修改`main.py`中的`main`函数的相关代码，匹配你需要的逻辑，然后运行 `main.py`。
3. 程序中提供了多种数据选择方法：
   - **by_indices**：根据索引列表进行选择
   - **by_range**：以范围和步长的方式选择行或列
   - **by_single**：选择单个行或列
4. 在提取完成后，脚本会将合并处理后的数据保存到 `output` 目录下的 Excel 文件中，若文件重名则自动顺延。

## 示例命令
```bash
python main.py
```

在命令行执行该命令后，若一切正常，终端会显示数据处理过程和结果保存路径。

---

# Data Extractor

This project uses a Python script to load .mat files, extract data, and save the results to an Excel file.

## Directory Structure
- `main.py`: The entry point of the program, containing the logic for data loading, rule definition, extraction, and saving.
- `data/`: Folder containing the .mat files to be processed.
- `output/`: Folder where the output Excel files will be saved.

## Dependencies
- numpy: Latest version
- scipy: Latest version
- pandas: Latest version
- os: Latest version

## Usage Instructions
1. Place the .mat files you want to process in the `./data` folder. The filenames should match the corresponding variable names in the script.
2. Modify the relevant code in the `main` function of `main.py` file to match your required logic.Then run the `main.py` file.
3. The script provides multiple data selection methods:
   - **by_indices**: Selects data based on a list of indices.
   - **by_range**: Selects rows or columns based on a specified range and step size.
   - **by_single**: Selects a single row or column.
4. After extraction, the script will save the processed data to an Excel file in the `output` folder. If a file with the same name already exists, it will automatically increment the name to avoid overwriting.

## Example Command
```bash
python main.py
```

Running this command from the terminal will trigger the data processing, and the terminal will display the process and the path to the saved result.