# gpt-academic代码分析插件

## 概述
这个小项目是基于[gpt_academic](https://github.com/binary-husky/gpt_academic)的一个自定义插件，主要由插件[解析一个Matlab项目](https://github.com/binary-husky/gpt_academic/blob/master/crazy_functions/SourceCode_Analyse.py)改动而来。

## 主要功能
主要功能是生成一份代码项目的描述，囊括了主要的代码逻辑，避免在论文撰写过程中出现描述与代码实际不一致的情况。由于语言模型对于这种严谨的学术论文的编写能力有所欠缺，因此仅作为分析工具，不作为最终内容的生产者。

## 使用方式
1. 用`crazy_functional.py`文件覆盖[gpt_academic](https://github.com/binary-husky/gpt_academic)项目根目录下的同名文件
2. 在`crazy_functional`目录下粘贴`MATLAB项目转论文级描述.py`文件
3. 运行项目根目录下的`main.py`文件

---

# gpt-academic Code Analysis Plugin

## Overview
This small project is a customized plugin based on [gpt_academic](https://github.com/binary-husky/gpt_academic), mainly modified from the plugin [Analyze a MATLAB Project](https://github.com/binary-husky/gpt_academic/blob/master/crazy_functions/SourceCode_Analyse.py).

## Main Features
The primary function of this plugin is to generate a description of a code project, covering the main logic of the code. This helps to avoid discrepancies between the description and the actual code when writing academic papers. Since language models may lack precision when it comes to writing rigorous academic papers, this tool is intended only as an analysis aid, not as the final content generator.

## Usage Instructions
1. Replace the `crazy_functional.py` file in the root directory of the [gpt_academic](https://github.com/binary-husky/gpt_academic) project with the provided one.
2. Paste the `MATLAB_Project_to_Paper_Level_Description.py` file into the `crazy_functional` directory.
3. Run the `main.py` file located in the root directory of the project.
