# 知识图谱稀疏结点增强 (KG Sparse Node Enhancement)

基于Monte Carlo Tree Search (MCTS) 的知识图谱稀疏结点增强系统，用于在有限预算内为稀疏实体发现尽可能多的正确三元组关系。

## 项目概述

本项目旨在解决知识图谱中稀疏结点的增强问题。通过MCTS搜索算法，在候选实体空间中高效地搜索正确的三元组，同时控制高成本的分类器调用次数。

### 核心特性

- **分层搜索策略**: 使用多种过滤策略（邻居关系、关系类型、语义相似性）对搜索空间进行分层划分
- **预算控制**: 严格控制昂贵的三元组分类器调用次数
- **多样化节点类型**: 支持基于KG结构、关系类型和LLM语义理解的多种搜索策略
- **高效的MCTS实现**: 使用UCT策略平衡探索与开发

## 系统架构

```
├── main.py                    # 主程序入口
├── kg_enhancer.py            # 知识图谱增强器
├── kg_data_loader.py         # 数据加载器
├── mcts_tree.py              # MCTS搜索树实现
├── node.py                   # 搜索节点定义
├── triplet_classifier.py     # 三元组分类器
├── model_calls.py            # LLM模型调用
├── setup_logger.py           # 日志配置
├── sparse_entities.json      # 稀疏实体配置
└── requirements.txt          # 项目依赖
```

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 配置环境变量

创建 `.env` 文件并配置LLM API：

```bash
DEEPSEEK_API_KEY=your_api_key_here
DEEPSEEK_BASE_URL=https://api.deepseek.com
DEEPSEEK_MODEL_NAME=deepseek-chat
```

### 3. 准备数据文件

确保以下数据文件存在于项目根目录：

- `entity2name.txt`: 实体ID到名称的映射
- `relation2id.txt`: 关系到ID的映射
- `entity2description.txt`: 实体描述信息
- `train.txt`: 原始知识图谱三元组
- `sparse_entities.json`: 稀疏实体配置

### 4. 配置稀疏实体

编辑 `sparse_entities.json` 文件，格式如下：

```json
{
  "entity_id": [
    ["position", "relation_type"],
    ["head", "nationality"],
    ["tail", "born_in"]
  ]
}
```

### 5. 运行程序

```bash
python main.py
```

## 配置参数

主要配置参数（在 `main.py` 中修改）：

- `budget_per_entity`: 每个稀疏实体的分类器调用预算 (默认: 1000)
- `mcts_iterations`: MCTS迭代次数 (默认: 50)
- `leaf_threshold`: 叶子节点候选实体阈值 (默认: 10)
- `exploration_weight`: UCT探索权重 (默认: 1.0)

## 搜索策略

### 1. 邻居过滤 (NeighborFilterNode)
基于实体在知识图谱中的邻居关系进行过滤：
- 一跳邻居：与稀疏实体直接相连的实体
- 二跳邻居：通过一个中间实体连接的实体
- 其他实体：与稀疏实体无直接图连接的实体

### 2. 关系过滤 (RelationFilterNode)
基于候选实体在指定关系中的历史出现情况：
- 相关实体：在其他三元组中出现过该关系的实体
- 无关实体：未在该关系中出现过的实体

### 3. 语义过滤 (SemanticFilterNode)
使用LLM进行语义理解和分组：
- 调用LLM分析实体描述和语义相似性
- 将候选实体按语义相关性分组
- 支持批量处理和智能回退策略

## 输出格式

程序会生成 `discovered_triplets.txt` 文件，包含所有发现的正确三元组：

```
(entity1,relation1,entity2)
(entity3,relation2,entity4)
...
```

## 扩展和自定义

### 1. 添加新的搜索策略

继承 `SearchNode` 类并实现以下方法：

```python
class CustomFilterNode(SearchNode):
    def find_children(self) -> Set[SearchNode]:
        # 实现子节点生成逻辑
        pass

    def expand(self):
        # 实现节点扩展逻辑
        pass
```

### 2. 替换三元组分类器

继承 `TripletClassifier` 类并实现真实的模型调用：

```python
class MyTripletClassifier(TripletClassifier):
    def classify_triplet(self, head: str, relation: str, tail: str) -> bool:
        # 实现真实的分类器调用
        return your_model.predict(head, relation, tail)
```

### 3. 自定义数据加载器

修改 `KGDataLoader` 类以支持不同的数据格式：

```python
def _load_custom_format(self, file_path: str):
    # 实现自定义数据格式加载
    pass
```

## 性能优化建议

1. **批量处理**: 如果分类器支持批量调用，修改 `TripletClassifier.batch_classify()` 方法
2. **缓存机制**: 为频繁查询的实体信息添加缓存
3. **并行处理**: 为多个稀疏实体的处理添加多进程支持
4. **预过滤**: 在MCTS搜索前使用快速启发式方法预过滤明显错误的候选

## 日志配置

项目使用分级日志系统：

- `DEBUG`: 详细的调试信息
- `INFO`: 一般进度信息
- `WARNING`: 警告信息
- `ERROR`: 错误信息

修改 `main.py` 中的日志级别：

```python
logger = setup_logger("main", level="DEBUG")  # 设置为DEBUG级别
```

## 故障排除

### 常见问题

1. **API调用失败**: 检查 `.env` 文件中的API配置
2. **内存不足**: 减少 `mcts_iterations` 或 `budget_per_entity` 参数
3. **数据格式错误**: 确保数据文件格式符合要求
4. **分类器超时**: 增加 `retry_delay` 参数或减少并发数

### 调试技巧

1. 启用DEBUG日志查看详细执行信息
2. 减少数据规模进行小规模测试
3. 使用模拟分类器验证MCTS逻辑
4. 检查中间结果文件确认数据加载正确

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系信息

如有问题或建议，请通过以下方式联系：

- 提交 Issue: [GitHub Issues](https://github.com/yourusername/kg-sparse-enhancement/issues)
- 邮件联系: your.email@example.com

## 更新日志

### v1.0.0 (当前版本)
- 初始版本发布
- 实现基础MCTS搜索框架
- 支持三种主要搜索策略
- 完整的日志和配置系统
