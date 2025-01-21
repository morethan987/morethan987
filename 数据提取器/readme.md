# 数据提取器

本项目通过 Python 脚本加载 .mat 文件并进行数据提取，最终将结果保存到 Excel 文件中。提供了一个图形用户界面（GUI）来方便用户操作。

## 主要功能
- 加载 .mat 文件并选择特定变量
- 通过灵活的行列规则提取数据
- 实时预览提取结果
- 将提取的数据保存为 Excel 文件
- 自动去除空行和空列

## 目录结构
- `main.py`：程序入口，包含数据加载、规则定义、提取和保存逻辑
- `gui.py`：图形用户界面实现
- `data/`：存放需要处理的 .mat 文件
- `output/`：存放输出的 Excel 文件

## 使用说明
1. 运行程序后会出现图形界面
2. 点击"浏览"按钮选择 .mat 文件
3. 输入要提取的变量名
4. 在"规则配置"部分输入行规则和列规则
5. 点击"添加规则"将规则应用到数据
6. 在"预览"区域查看提取结果
7. 点击"导出到Excel"保存结果

## 规则输入格式
1. 单个索引: 输入一个数字，如 '5' 或 '-1'
2. 索引列表: 输入以逗号分隔的数字，如 '1,3,5'
3. 范围规则: 输入三个以冒号分隔的数字(起始:结束:步长)，如 '0:-1:60'

## 相关依赖
- numpy：最新版即可
- scipy：最新版即可
- pandas：最新版即可
- tkinter：最新版即可

---

# Data Extractor

This project uses a Python script to load .mat files, extract data, and save the results to an Excel file. It provides a graphical user interface (GUI) for easy operation.

## Main Features
- Load .mat files and select specific variables
- Extract data using flexible row and column rules
- Real-time preview of extraction results
- Save extracted data as Excel files
- Automatically remove empty rows and columns

## Directory Structure
- `main.py`: The entry point of the program, containing the logic for data loading, rule definition, extraction, and saving.
- `gui.py`: Implementation of the graphical user interface
- `data/`: Folder containing the .mat files to be processed.
- `output/`: Folder where the output Excel files will be saved.

## Usage Instructions
1. Run the program to launch the GUI
2. Click "Browse" to select a .mat file
3. Enter the variable name to extract
4. Input row and column rules in the "Rule Configuration" section
5. Click "Add Rule" to apply the rules to the data
6. View extraction results in the "Preview" area
7. Click "Export to Excel" to save the results

## Rule Input Format
1. Single index: Enter a number, e.g. '5' or '-1'
2. Index list: Enter comma-separated numbers, e.g. '1,3,5'
3. Range rule: Enter three colon-separated numbers (start:end:step), e.g. '0:-1:60'

## Dependencies
- numpy: Latest version
- scipy: Latest version
- pandas: Latest version
- tkinter: Latest version
