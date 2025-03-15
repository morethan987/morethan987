---
title: Python小技巧
weight: -15
draft: false
description: Python中的一些小技巧
slug: pytips
tags:
  - Python
series:
  - 技术流程
series_order: 2
date: 2024-08-10
authors:
  - Morethan
---

## 创建虚拟环境
### 创建
一些常规的代码例子如下👇

```sh
# 创建虚拟环境
python -m venv your_env_name

# 指定python版本创建虚拟环境，如果你的python是默认安装路径
python -m venv your_env_name --python=python3.11

# python是自定义的安装路径
D:\Python\Python311\python.exe -m venv your_env_name
```

下面有一些可选参数用于创建自定义的虚拟环境：

| 参数名                   | 含义                                     |
| ------------------------ | --------------------------------------- |
| `--system-site-packages` | 创建的虚拟环境将包含全局Python环境中的包，这可以避免重复安装一些常用的包|
| `--clear`                | 如果指定的虚拟环境目录已经存在，这会清除目录中的所有内容，然后重新创建虚拟环境 |
| `--version`              | 用于确认虚拟环境中 Python 的版本                    |

> [!NOTE] Title
> 所有的参数说明都可以通过运行 `python -m venv -h` 来获得；不用到处查文档了~😆


### 激活
默认情况下，虚拟环境处于非激活状态。在“your_env_name/Scripts/”目录下将有一个名为“activate”的文件，用命令行运行即可。

```sh
# 激活虚拟环境
your_env_name/Scripts/activate
```
