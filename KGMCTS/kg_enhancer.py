"""
知识图谱增强器：使用MCTS为稀疏结点搜索正确的三元组
"""

import logging
from typing import List, Set, Dict, Tuple, Optional
import random

from kg_data_loader import KGDataLoader
from mcts_tree import MCTS
from node import SearchRootNode
from triplet_classifier import TripletClassifier
from setup_logger import setup_logger


class KGEnhancer:
    """知识图谱增强器：为稀疏结点搜索新的三元组关系"""

    def __init__(self,
                 entity2name_path: str,
                 relation2id_path: str,
                 entity2description_path: str,
                 train_kg_path: str,
                 budget_per_entity: int = 1000,
                 mcts_iterations: int = 50,
                 leaf_threshold: int = 10,
                 exploration_weight: float = 1.0):
        """
        初始化知识图谱增强器

        Args:
            entity2name_path: 实体到名称映射文件路径
            relation2id_path: 关系到ID映射文件路径
            entity2description_path: 实体描述文件路径
            train_kg_path: 训练知识图谱文件路径
            budget_per_entity: 每个实体的分类器调用预算
            mcts_iterations: MCTS迭代次数
            leaf_threshold: 叶子结点候选实体数量阈值
            exploration_weight: UCT探索权重
        """
        self.logger = setup_logger(self.__class__.__name__)

        # 配置参数
        self.budget_per_entity = budget_per_entity
        self.mcts_iterations = mcts_iterations
        self.leaf_threshold = leaf_threshold
        self.exploration_weight = exploration_weight

        # 初始化数据加载器
        self.logger.info("Loading knowledge graph data...")
        self.data_loader = KGDataLoader(
            entity2name_path=entity2name_path,
            relation2id_path=relation2id_path,
            entity2description_path=entity2description_path,
            train_kg_path=train_kg_path
        )

        # 初始化三元组分类器（高精度但昂贵）
        self.logger.info("Initializing triplet classifier...")
        self.triplet_classifier = TripletClassifier()

        # 初始化MCTS
        self.mcts = MCTS(exploration_weight=exploration_weight)

        self.logger.info("KGEnhancer initialized successfully")

    def enhance_entity_relation(self, sparse_entity: str, position: str, relation: str) -> List[Tuple[str, str, str]]:
        """
        为指定的稀疏实体-位置-关系组合搜索正确的三元组

        Args:
            sparse_entity: 稀疏实体ID
            position: 实体在三元组中的位置 ('head' 或 'tail')
            relation: 关系类型

        Returns:
            发现的正确三元组列表 [(head, relation, tail), ...]
        """
        self.logger.info(f"Starting enhancement for {sparse_entity}-{position}-{relation}")

        # 获取所有候选目标实体（除了稀疏实体本身）
        all_entities = set(self.data_loader.entity2name.keys())
        candidate_entities = all_entities - {sparse_entity}

        self.logger.info(f"Total candidate entities: {len(candidate_entities)}")

        # 创建搜索根节点
        root_node = SearchRootNode(
            sparse_entity=sparse_entity,
            position=position,
            relation=relation,
            candidate_entities=candidate_entities,
            data_loader=self.data_loader,
            triplet_classifier=self.triplet_classifier,
            leaf_threshold=self.leaf_threshold
        )

        # 重置MCTS状态
        self.mcts.reset()

        # 记录已发现的正确三元组
        discovered_triplets = []
        budget_used = 0

        # MCTS搜索循环
        for iteration in range(self.mcts_iterations):
            if budget_used >= self.budget_per_entity:
                self.logger.info(f"Budget exhausted after {iteration} iterations")
                break

            self.logger.info(f"MCTS iteration {iteration + 1}/{self.mcts_iterations}, "
                           f"budget used: {budget_used}/{self.budget_per_entity}")

            # 执行一次MCTS迭代
            triplets_found, budget_increment = self.mcts.do_iteration(root_node)

            # 更新统计信息
            discovered_triplets.extend(triplets_found)
            budget_used += budget_increment

            self.logger.info(f"Iteration {iteration + 1} found {len(triplets_found)} triplets, "
                           f"used {budget_increment} budget")

        # 去重
        discovered_triplets = list(set(discovered_triplets))

        self.logger.info(f"Enhancement completed: found {len(discovered_triplets)} unique triplets, "
                        f"total budget used: {budget_used}")

        return discovered_triplets

    def get_statistics(self) -> Dict:
        """获取增强过程的统计信息"""
        return {
            "total_entities": len(self.data_loader.entity2name),
            "total_relations": len(self.data_loader.relation2id),
            "total_kg_triplets": len(self.data_loader.kg_triplets)
        }
