"""
搜索节点定义：定义MCTS中使用的各种节点类型
每个节点代表搜索空间中的一个状态，包含候选实体的子集
"""

import random
import logging
from abc import ABC, abstractmethod
from typing import Set, List, Tuple, Optional

from kg_data_loader import KGDataLoader
from triplet_classifier import TripletClassifier
from model_calls import ModelCalls
from setup_logger import setup_logger


class SearchNode(ABC):
    """搜索节点抽象基类"""

    def __init__(self,
                 sparse_entity: str,
                 position: str,
                 relation: str,
                 candidate_entities: Set[str],
                 data_loader: KGDataLoader,
                 triplet_classifier: TripletClassifier,
                 leaf_threshold: int,
                 parent: Optional['SearchNode'] = None):
        """
        初始化搜索节点

        Args:
            sparse_entity: 稀疏实体ID
            position: 实体在三元组中的位置 ('head' 或 'tail')
            relation: 关系类型
            candidate_entities: 候选目标实体集合
            data_loader: 数据加载器
            triplet_classifier: 三元组分类器
            leaf_threshold: 叶子节点阈值
            parent: 父节点
        """
        self.sparse_entity = sparse_entity
        self.position = position
        self.relation = relation
        self.candidate_entities = candidate_entities
        self.data_loader = data_loader
        self.triplet_classifier = triplet_classifier
        self.leaf_threshold = leaf_threshold
        self.parent = parent
        self.children = None
        self.logger = setup_logger(f"{self.__class__.__name__}")

    @abstractmethod
    def find_children(self) -> Set['SearchNode']:
        """查找子节点"""
        pass

    @abstractmethod
    def find_random_child(self) -> Optional['SearchNode']:
        """随机选择一个子节点"""
        pass

    @abstractmethod
    def expand(self):
        """扩展节点，生成子节点"""
        pass

    def is_terminal(self) -> bool:
        """判断是否为终端节点（候选实体数量小于阈值）"""
        return len(self.candidate_entities) <= self.leaf_threshold

    def evaluate_candidates(self) -> Tuple[List[Tuple[str, str, str]], int]:
        """
        评估候选实体，返回正确的三元组

        Returns:
            (正确的三元组列表, 使用的分类器调用次数)
        """
        correct_triplets = []
        budget_used = 0

        for entity in self.candidate_entities:
            # 构造三元组
            if self.position == 'head':
                triplet = (self.sparse_entity, self.relation, entity)
            else:  # position == 'tail'
                triplet = (entity, self.relation, self.sparse_entity)

            # 跳过已存在的三元组
            if self.data_loader.triplet_exists(*triplet):
                continue

            # 使用分类器判断
            if self.triplet_classifier.classify_triplet(*triplet):
                correct_triplets.append(triplet)

            budget_used += 1

        self.logger.debug(f"Evaluated {len(self.candidate_entities)} candidates, "
                         f"found {len(correct_triplets)} correct triplets")

        return correct_triplets, budget_used


class SearchRootNode(SearchNode):
    """搜索根节点"""

    def find_children(self) -> Set[SearchNode]:
        """根节点的子节点是各种过滤策略节点"""
        if self.children is None:
            return set()
        return self.children

    def find_random_child(self) -> Optional[SearchNode]:
        """随机选择一个子节点"""
        if self.children:
            return random.choice(list(self.children))
        return None

    def expand(self):
        """扩展根节点，生成不同的过滤策略子节点"""
        if self.children is not None:
            return

        self.children = set()

        # 1. 基于知识图谱邻居的过滤节点
        self.children.add(NeighborFilterNode(
            sparse_entity=self.sparse_entity,
            position=self.position,
            relation=self.relation,
            candidate_entities=self.candidate_entities,
            data_loader=self.data_loader,
            triplet_classifier=self.triplet_classifier,
            leaf_threshold=self.leaf_threshold,
            parent=self
        ))

        # 2. 基于关系类型的过滤节点
        self.children.add(RelationFilterNode(
            sparse_entity=self.sparse_entity,
            position=self.position,
            relation=self.relation,
            candidate_entities=self.candidate_entities,
            data_loader=self.data_loader,
            triplet_classifier=self.triplet_classifier,
            leaf_threshold=self.leaf_threshold,
            parent=self
        ))

        # 3. 基于LLM语义的过滤节点
        self.children.add(SemanticFilterNode(
            sparse_entity=self.sparse_entity,
            position=self.position,
            relation=self.relation,
            candidate_entities=self.candidate_entities,
            data_loader=self.data_loader,
            triplet_classifier=self.triplet_classifier,
            leaf_threshold=self.leaf_threshold,
            parent=self
        ))


class NeighborFilterNode(SearchNode):
    """基于邻居关系的过滤节点"""

    def find_children(self) -> Set[SearchNode]:
        if self.children is None:
            return set()
        return self.children

    def find_random_child(self) -> Optional[SearchNode]:
        if self.children:
            return random.choice(list(self.children))
        return None

    def expand(self):
        """根据邻居关系划分候选实体"""
        if self.children is not None:
            return

        self.children = set()

        # 获取稀疏实体的一跳和二跳邻居
        one_hop_neighbors = self.data_loader.get_one_hop_neighbors(self.sparse_entity)
        two_hop_neighbors = self.data_loader.get_two_hop_neighbors(self.sparse_entity)

        # 根据与稀疏实体的距离关系划分候选实体
        one_hop_candidates = self.candidate_entities & one_hop_neighbors
        two_hop_candidates = self.candidate_entities & two_hop_neighbors
        other_candidates = self.candidate_entities - one_hop_neighbors - two_hop_neighbors

        # 创建子节点
        if one_hop_candidates:
            self.children.add(LeafNode(
                sparse_entity=self.sparse_entity,
                position=self.position,
                relation=self.relation,
                candidate_entities=one_hop_candidates,
                data_loader=self.data_loader,
                triplet_classifier=self.triplet_classifier,
                leaf_threshold=self.leaf_threshold,
                parent=self,
                node_type="one_hop"
            ))

        if two_hop_candidates:
            self.children.add(LeafNode(
                sparse_entity=self.sparse_entity,
                position=self.position,
                relation=self.relation,
                candidate_entities=two_hop_candidates,
                data_loader=self.data_loader,
                triplet_classifier=self.triplet_classifier,
                leaf_threshold=self.leaf_threshold,
                parent=self,
                node_type="two_hop"
            ))

        if other_candidates:
            # 如果其他候选实体过多，进一步随机划分
            if len(other_candidates) > self.leaf_threshold * 3:
                # 随机划分为多个子集
                candidates_list = list(other_candidates)
                random.shuffle(candidates_list)

                chunk_size = len(candidates_list) // 3
                for i in range(3):
                    start_idx = i * chunk_size
                    end_idx = len(candidates_list) if i == 2 else (i + 1) * chunk_size
                    chunk = set(candidates_list[start_idx:end_idx])

                    if chunk:
                        self.children.add(LeafNode(
                            sparse_entity=self.sparse_entity,
                            position=self.position,
                            relation=self.relation,
                            candidate_entities=chunk,
                            data_loader=self.data_loader,
                            triplet_classifier=self.triplet_classifier,
                            leaf_threshold=self.leaf_threshold,
                            parent=self,
                            node_type=f"other_chunk_{i}"
                        ))
            else:
                self.children.add(LeafNode(
                    sparse_entity=self.sparse_entity,
                    position=self.position,
                    relation=self.relation,
                    candidate_entities=other_candidates,
                    data_loader=self.data_loader,
                    triplet_classifier=self.triplet_classifier,
                    leaf_threshold=self.leaf_threshold,
                    parent=self,
                    node_type="other"
                ))


class RelationFilterNode(SearchNode):
    """基于关系类型的过滤节点"""

    def find_children(self) -> Set[SearchNode]:
        if self.children is None:
            return set()
        return self.children

    def find_random_child(self) -> Optional[SearchNode]:
        if self.children:
            return random.choice(list(self.children))
        return None

    def expand(self):
        """根据候选实体在其他关系中的出现频率划分"""
        if self.children is not None:
            return

        self.children = set()

        # 获取与当前关系相关的实体（在其他三元组中出现过该关系的实体）
        relation_entities = self.data_loader.get_entities_with_relation(
            self.relation, self.position
        )

        # 划分候选实体
        related_candidates = self.candidate_entities & relation_entities
        unrelated_candidates = self.candidate_entities - relation_entities

        if related_candidates:
            self.children.add(LeafNode(
                sparse_entity=self.sparse_entity,
                position=self.position,
                relation=self.relation,
                candidate_entities=related_candidates,
                data_loader=self.data_loader,
                triplet_classifier=self.triplet_classifier,
                leaf_threshold=self.leaf_threshold,
                parent=self,
                node_type="relation_related"
            ))

        if unrelated_candidates:
            # 如果无关实体过多，随机划分
            if len(unrelated_candidates) > self.leaf_threshold * 2:
                candidates_list = list(unrelated_candidates)
                random.shuffle(candidates_list)

                mid_point = len(candidates_list) // 2
                chunk1 = set(candidates_list[:mid_point])
                chunk2 = set(candidates_list[mid_point:])

                for i, chunk in enumerate([chunk1, chunk2]):
                    if chunk:
                        self.children.add(LeafNode(
                            sparse_entity=self.sparse_entity,
                            position=self.position,
                            relation=self.relation,
                            candidate_entities=chunk,
                            data_loader=self.data_loader,
                            triplet_classifier=self.triplet_classifier,
                            leaf_threshold=self.leaf_threshold,
                            parent=self,
                            node_type=f"relation_unrelated_{i}"
                        ))
            else:
                self.children.add(LeafNode(
                    sparse_entity=self.sparse_entity,
                    position=self.position,
                    relation=self.relation,
                    candidate_entities=unrelated_candidates,
                    data_loader=self.data_loader,
                    triplet_classifier=self.triplet_classifier,
                    leaf_threshold=self.leaf_threshold,
                    parent=self,
                    node_type="relation_unrelated"
                ))


class SemanticFilterNode(SearchNode):
    """基于LLM语义理解的过滤节点"""

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.llm_client = ModelCalls()

    def find_children(self) -> Set[SearchNode]:
        if self.children is None:
            return set()
        return self.children

    def find_random_child(self) -> Optional[SearchNode]:
        if self.children:
            return random.choice(list(self.children))
        return None

    def expand(self):
        """使用LLM进行语义过滤"""
        if self.children is not None:
            return

        self.children = set()

        # 如果候选实体较少，直接作为叶子节点
        if len(self.candidate_entities) <= self.leaf_threshold * 2:
            self.children.add(LeafNode(
                sparse_entity=self.sparse_entity,
                position=self.position,
                relation=self.relation,
                candidate_entities=self.candidate_entities,
                data_loader=self.data_loader,
                triplet_classifier=self.triplet_classifier,
                leaf_threshold=self.leaf_threshold,
                parent=self,
                node_type="semantic_all"
            ))
            return

        # 使用LLM进行语义分析和分组
        try:
            semantic_groups = self._semantic_grouping()

            for i, group in enumerate(semantic_groups):
                if group:
                    self.children.add(LeafNode(
                        sparse_entity=self.sparse_entity,
                        position=self.position,
                        relation=self.relation,
                        candidate_entities=group,
                        data_loader=self.data_loader,
                        triplet_classifier=self.triplet_classifier,
                        leaf_threshold=self.leaf_threshold,
                        parent=self,
                        node_type=f"semantic_group_{i}"
                    ))

        except Exception as e:
            self.logger.warning(f"LLM semantic grouping failed: {e}, using random split")
            # 如果LLM调用失败，回退到随机划分
            self._random_split()

    def _semantic_grouping(self) -> List[Set[str]]:
        """使用LLM进行语义分组"""
        # 获取实体描述信息
        sparse_name = self.data_loader.get_entity_name(self.sparse_entity)
        sparse_desc = self.data_loader.get_entity_description(self.sparse_entity)

        # 随机采样一些候选实体进行分析（避免输入过长）
        sample_size = min(20, len(self.candidate_entities))
        sample_entities = random.sample(list(self.candidate_entities), sample_size)

        # 构造LLM输入
        prompt = self._build_semantic_prompt(sparse_name, sparse_desc, sample_entities)

        # 调用LLM
        response = self.llm_client.get_output(prompt)

        # 解析响应并分组
        groups = self._parse_semantic_response(response, sample_entities)

        # 为未采样的实体随机分配组
        remaining_entities = self.candidate_entities - set(sample_entities)
        if remaining_entities and groups:
            # 将剩余实体随机分配到现有组中
            remaining_list = list(remaining_entities)
            random.shuffle(remaining_list)

            group_count = len(groups)
            for i, entity in enumerate(remaining_list):
                groups[i % group_count].add(entity)

        return groups

    def _build_semantic_prompt(self, sparse_name: str, sparse_desc: str, sample_entities: List[str]) -> str:
        """构造语义分析的LLM提示"""
        entity_info = []
        for entity in sample_entities:
            name = self.data_loader.get_entity_name(entity)
            desc = self.data_loader.get_entity_description(entity)
            entity_info.append(f"- {entity}: {name} ({desc[:100]}...)" if len(desc) > 100 else f"- {entity}: {name} ({desc})")

        prompt = f"""
Given a sparse entity and a relation, please group the following candidate entities into 2-3 semantic groups based on their relevance and semantic similarity.

Sparse Entity: {self.sparse_entity} - {sparse_name}
Description: {sparse_desc}
Relation: {self.relation}
Position: {self.position}

Candidate Entities:
{chr(10).join(entity_info)}

Please group these entities into 2-3 groups and respond in the following format:
Group 1: entity1, entity2, entity3
Group 2: entity4, entity5, entity6
Group 3: entity7, entity8, entity9

Base your grouping on semantic similarity and relevance to the sparse entity and relation.
"""
        return prompt

    def _parse_semantic_response(self, response: str, sample_entities: List[str]) -> List[Set[str]]:
        """解析LLM的语义分组响应"""
        groups = []

        try:
            lines = response.strip().split('\n')
            for line in lines:
                if line.startswith('Group'):
                    # 提取组中的实体
                    if ':' in line:
                        entities_str = line.split(':', 1)[1].strip()
                        entities = [e.strip() for e in entities_str.split(',')]
                        # 只保留有效的实体ID
                        valid_entities = set(e for e in entities if e in sample_entities)
                        if valid_entities:
                            groups.append(valid_entities)

        except Exception as e:
            self.logger.warning(f"Failed to parse semantic response: {e}")

        # 如果解析失败或没有有效分组，随机分组
        if not groups:
            groups = self._random_grouping(sample_entities)

        return groups

    def _random_grouping(self, entities: List[str]) -> List[Set[str]]:
        """随机分组"""
        random.shuffle(entities)
        group_size = len(entities) // 2

        group1 = set(entities[:group_size])
        group2 = set(entities[group_size:])

        return [group1, group2]

    def _random_split(self):
        """随机划分候选实体"""
        candidates_list = list(self.candidate_entities)
        random.shuffle(candidates_list)

        mid_point = len(candidates_list) // 2
        chunk1 = set(candidates_list[:mid_point])
        chunk2 = set(candidates_list[mid_point:])

        for i, chunk in enumerate([chunk1, chunk2]):
            if chunk:
                self.children.add(LeafNode(
                    sparse_entity=self.sparse_entity,
                    position=self.position,
                    relation=self.relation,
                    candidate_entities=chunk,
                    data_loader=self.data_loader,
                    triplet_classifier=self.triplet_classifier,
                    leaf_threshold=self.leaf_threshold,
                    parent=self,
                    node_type=f"random_split_{i}"
                ))


class LeafNode(SearchNode):
    """叶子节点：候选实体数量较少，可以直接进行分类器评估"""

    def __init__(self, *args, node_type: str = "leaf", **kwargs):
        super().__init__(*args, **kwargs)
        self.node_type = node_type

    def find_children(self) -> Set[SearchNode]:
        return set()  # 叶子节点没有子节点

    def find_random_child(self) -> Optional[SearchNode]:
        return None  # 叶子节点没有子节点

    def expand(self):
        """叶子节点不需要扩展"""
        self.children = set()

    def is_terminal(self) -> bool:
        """叶子节点始终是终端节点"""
        return True
