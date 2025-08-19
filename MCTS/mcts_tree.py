from collections import defaultdict
from node import Node, LLMNode, SmartNode, SillyNode, FunnyNode, QuestionNode
import math
from typing import Union, TypeAlias
import logging
from logging import getLogger
from setup_logger import logger

# logger = getLogger(__name__)
# logger.setLevel("INFO")
# console_handler = logging.StreamHandler()
# formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
# console_handler.setFormatter(formatter)
# logger.addHandler(console_handler)

# 定义一个类型别名来统一所有Node类型
NodeType: TypeAlias = Union[Node, LLMNode, SmartNode, SillyNode, FunnyNode, QuestionNode]


class MCTS:
    def __init__(self, exploration_weight=1):
        self.Q = defaultdict(int)
        self.N = defaultdict(int)
        self.explored = list()
        self.exploration_weight = exploration_weight

    def do_iteration(self, node:NodeType, ans=None):
        logger.info("===== Start MCTS Iteration =====")
        logger.info("Step 1: Perform Selection")
        path = self._select(node)
        leaf = path[-1]
        logger.info(f"Selected leaf node type: {leaf.__class__.__name__}")
        logger.info("Step 2: Perform Expansion")
        self._expand(leaf)
        logger.info("Step 3: Perform Rollout")
        reward = self._rollout(leaf, ans)
        logger.info("Step 4: Perform Backpropagation")
        self._backpropagate(path, reward)
        logger.info("===== End MCTS Iteration =====")


    def choose(self, node:NodeType):
        if node.is_terminal():
            raise RuntimeError(f"choose called on terminal node {node}")
        
        if node not in self.explored:
            return node.find_random_child()
        
        return max(node.find_children(), key=self._get_avg_score)
        
    def _get_avg_score(self, node:NodeType):
        return float('-inf') if self.N[node]==0 else (self.Q[node] / self.N[node])

    def _select(self, node:NodeType):
        path = []
        while True:
            path.append(node)
            if node not in self.explored or node.is_terminal():
                return path
            unexplored = node.find_children() - self.explored
            if unexplored:
                n = unexplored.pop()
                path.append(n)
                return path
            node = self._uct_select(node)

    def _uct_select(self, node:NodeType):
        children = node.find_children()
        # 断言这个结点的所有子节点都被探索过
        assert all(n in self.explored for n in children)
        # 选取uct最大的
        return max(children, key=self._get_uct)
        
    def _get_uct(self, node:NodeType):
        assert self.N[node] != 0
        return self._get_avg_score(node) + self.exploration_weight * math.sqrt(math.log(self.N[node.parent])/self.N[node])
    
    def _expand(self, node:NodeType):
        if node in self.explored:
            return
        node.expand()

    def _rollout(self, node:NodeType, ans):
        while True:
            if node.is_terminal():
                return node.reward(ans)
            
            node = node.find_random_child() # 快速随机生成路径

    def _backpropagate(self, path, reward):
        for node in reversed(path):
            self.N[node] += 1
            self.Q[node] += reward
