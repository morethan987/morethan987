"""
MCTS搜索树：实现Monte Carlo Tree Search算法
用于在候选实体空间中高效搜索正确的三元组
"""

import math
import logging
from collections import defaultdict
from typing import List, Tuple, Set

from node import SearchNode
from setup_logger import setup_logger


class MCTS:
    """Monte Carlo Tree Search实现"""

    def __init__(self, exploration_weight: float = 1.0):
        """
        初始化MCTS

        Args:
            exploration_weight: UCT公式中的探索权重参数
        """
        self.exploration_weight = exploration_weight
        self.logger = setup_logger(self.__class__.__name__)
        self.reset()

    def reset(self):
        """重置MCTS状态"""
        self.Q = defaultdict(float)  # 节点累计奖励
        self.N = defaultdict(int)    # 节点访问次数
        self.explored = set()        # 已探索节点集合

    def do_iteration(self, root_node: SearchNode) -> Tuple[List[Tuple[str, str, str]], int]:
        """
        执行一次完整的MCTS迭代

        Args:
            root_node: 搜索根节点

        Returns:
            (发现的正确三元组列表, 使用的分类器调用次数)
        """
        self.logger.debug("Starting MCTS iteration")

        # Step 1: Selection - 选择到叶子节点的路径
        path = self._select(root_node)
        leaf = path[-1]

        self.logger.debug(f"Selected path length: {len(path)}, leaf type: {leaf.__class__.__name__}")

        # Step 2: Expansion - 扩展叶子节点（如果不是终端节点）
        expanded_nodes = self._expand(leaf)

        # Step 3: Rollout - 在叶子节点或新扩展的节点上进行评估
        rollout_results = []
        total_budget_used = 0

        if leaf.is_terminal():
            # 叶子节点是终端节点，直接评估
            triplets, budget = self._rollout(leaf)
            rollout_results.extend(triplets)
            total_budget_used += budget
            reward = 1.0 if triplets else 0.0

            # Step 4: Backpropagation - 反向传播奖励
            self._backpropagate(path, reward)

        else:
            # 对扩展的子节点进行评估
            for child in expanded_nodes:
                triplets, budget = self._rollout(child)
                rollout_results.extend(triplets)
                total_budget_used += budget

                # 计算奖励（基于发现的正确三元组数量）
                reward = len(triplets) / max(budget, 1)  # 避免除零

                # 反向传播到包含子节点的路径
                child_path = path + [child]
                self._backpropagate(child_path, reward)

        self.logger.debug(f"Iteration completed: found {len(rollout_results)} triplets, "
                         f"used {total_budget_used} budget")

        return rollout_results, total_budget_used

    def _select(self, node: SearchNode) -> List[SearchNode]:
        """
        Selection阶段：从根节点开始，使用UCT策略选择到叶子节点的路径

        Args:
            node: 当前节点

        Returns:
            从根节点到叶子节点的路径
        """
        path = []

        while True:
            path.append(node)

            if node not in self.explored or node.is_terminal():
                # 找到未探索的节点或终端节点
                return path

            # 检查是否有未探索的子节点
            children = node.find_children()
            unexplored = children - self.explored

            if unexplored:
                # 选择一个未探索的子节点
                child = next(iter(unexplored))  # 取第一个未探索的子节点
                path.append(child)
                return path

            # 所有子节点都已探索，使用UCT选择最优子节点
            node = self._uct_select(node)

    def _uct_select(self, node: SearchNode) -> SearchNode:
        """
        使用UCT公式选择最优子节点

        Args:
            node: 父节点

        Returns:
            选中的子节点
        """
        children = node.find_children()

        # 确保所有子节点都被探索过
        assert all(child in self.explored for child in children), \
            "UCT selection called with unexplored children"

        # 使用UCT公式选择
        return max(children, key=self._get_uct_value)

    def _get_uct_value(self, node: SearchNode) -> float:
        """
        计算节点的UCT值

        Args:
            node: 节点

        Returns:
            UCT值
        """
        if self.N[node] == 0:
            return float('inf')  # 未访问的节点具有最高优先级

        exploitation = self.Q[node] / self.N[node]  # 开发项
        exploration = self.exploration_weight * math.sqrt(
            math.log(self.N[node.parent]) / self.N[node]
        )  # 探索项

        return exploitation + exploration

    def _expand(self, node: SearchNode) -> List[SearchNode]:
        """
        Expansion阶段：扩展节点的子节点

        Args:
            node: 要扩展的节点

        Returns:
            新扩展的子节点列表
        """
        if node in self.explored:
            return []  # 节点已经被扩展过

        # 扩展节点
        node.expand()
        self.explored.add(node)

        # 返回所有子节点（可能为空）
        children = node.find_children()
        return list(children)

    def _rollout(self, node: SearchNode) -> Tuple[List[Tuple[str, str, str]], int]:
        """
        Rollout阶段：评估节点质量

        Args:
            node: 要评估的节点

        Returns:
            (发现的正确三元组列表, 使用的分类器调用次数)
        """
        if node.is_terminal():
            # 终端节点：使用分类器评估候选三元组
            return node.evaluate_candidates()
        else:
            # 非终端节点：递归评估一个随机子节点
            random_child = node.find_random_child()
            if random_child:
                return self._rollout(random_child)
            else:
                return [], 0

    def _backpropagate(self, path: List[SearchNode], reward: float):
        """
        Backpropagation阶段：沿路径向上传播奖励

        Args:
            path: 从根到叶子的节点路径
            reward: 奖励值
        """
        for node in reversed(path):
            self.N[node] += 1
            self.Q[node] += reward

    def choose_best_child(self, node: SearchNode) -> SearchNode:
        """
        选择最佳子节点（基于平均奖励）

        Args:
            node: 父节点

        Returns:
            最佳子节点
        """
        if node.is_terminal():
            raise RuntimeError("choose_best_child called on terminal node")

        if node not in self.explored:
            return node.find_random_child()

        children = node.find_children()

        if not children:
            return None

        # 选择平均奖励最高的子节点
        return max(children, key=self._get_average_reward)

    def _get_average_reward(self, node: SearchNode) -> float:
        """
        获取节点的平均奖励

        Args:
            node: 节点

        Returns:
            平均奖励值
        """
        return float('-inf') if self.N[node] == 0 else (self.Q[node] / self.N[node])

    def get_statistics(self) -> dict:
        """获取MCTS统计信息"""
        return {
            "total_nodes_explored": len(self.explored),
            "total_visits": sum(self.N.values()),
            "total_reward": sum(self.Q.values())
        }
