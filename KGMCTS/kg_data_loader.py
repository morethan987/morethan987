"""
知识图谱数据加载器：负责加载和管理所有KG相关数据
"""

import logging
from typing import Dict, List, Set, Tuple
from collections import defaultdict

from setup_logger import setup_logger


class KGDataLoader:
    """知识图谱数据加载器"""

    def __init__(self,
                 entity2name_path: str,
                 relation2id_path: str,
                 entity2description_path: str,
                 train_kg_path: str):
        """
        初始化数据加载器

        Args:
            entity2name_path: 实体到名称映射文件
            relation2id_path: 关系到ID映射文件
            entity2description_path: 实体描述文件
            train_kg_path: 训练知识图谱文件
        """
        self.logger = setup_logger(self.__class__.__name__)

        # 加载实体到名称映射
        self.entity2name = self._load_entity2name(entity2name_path)
        self.logger.info(f"Loaded {len(self.entity2name)} entity names")

        # 加载关系到ID映射
        self.relation2id = self._load_relation2id(relation2id_path)
        self.logger.info(f"Loaded {len(self.relation2id)} relations")

        # 加载实体描述
        self.entity2description = self._load_entity2description(entity2description_path)
        self.logger.info(f"Loaded {len(self.entity2description)} entity descriptions")

        # 加载知识图谱三元组
        self.kg_triplets = self._load_kg_triplets(train_kg_path)
        self.logger.info(f"Loaded {len(self.kg_triplets)} KG triplets")

        # 构建邻接关系索引
        self._build_adjacency_index()
        self.logger.info("Built adjacency index")

    def _load_entity2name(self, file_path: str) -> Dict[str, str]:
        """加载实体到名称映射"""
        entity2name = {}
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                for line in f:
                    line = line.strip()
                    if line:
                        parts = line.split('\t', 1)
                        if len(parts) == 2:
                            entity_id, name = parts
                            entity2name[entity_id] = name
        except Exception as e:
            self.logger.error(f"Error loading entity2name from {file_path}: {e}")
            raise
        return entity2name

    def _load_relation2id(self, file_path: str) -> Dict[str, str]:
        """加载关系到ID映射"""
        relation2id = {}
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                for line in f:
                    line = line.strip()
                    if line:
                        parts = line.split('\t', 1)
                        if len(parts) == 2:
                            relation, rel_id = parts
                            relation2id[relation] = rel_id
        except Exception as e:
            self.logger.error(f"Error loading relation2id from {file_path}: {e}")
            raise
        return relation2id

    def _load_entity2description(self, file_path: str) -> Dict[str, str]:
        """加载实体描述"""
        entity2description = {}
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                for line in f:
                    line = line.strip()
                    if line:
                        parts = line.split('\t', 1)
                        if len(parts) == 2:
                            entity_id, description = parts
                            entity2description[entity_id] = description
        except Exception as e:
            self.logger.error(f"Error loading entity2description from {file_path}: {e}")
            raise
        return entity2description

    def _load_kg_triplets(self, file_path: str) -> Set[Tuple[str, str, str]]:
        """加载知识图谱三元组"""
        kg_triplets = set()
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                for line in f:
                    line = line.strip()
                    if line:
                        parts = line.split('\t')
                        if len(parts) == 3:
                            head, relation, tail = parts
                            kg_triplets.add((head, relation, tail))
        except Exception as e:
            self.logger.error(f"Error loading KG triplets from {file_path}: {e}")
            raise
        return kg_triplets

    def _build_adjacency_index(self):
        """构建邻接关系索引，用于快速查找一跳、多跳邻居"""
        # 出边索引: entity -> [(relation, target_entity), ...]
        self.outgoing_edges = defaultdict(list)

        # 入边索引: entity -> [(relation, source_entity), ...]
        self.incoming_edges = defaultdict(list)

        # 关系索引: relation -> [(head, tail), ...]
        self.relation_pairs = defaultdict(list)

        for head, relation, tail in self.kg_triplets:
            self.outgoing_edges[head].append((relation, tail))
            self.incoming_edges[tail].append((relation, head))
            self.relation_pairs[relation].append((head, tail))

    def get_one_hop_neighbors(self, entity: str) -> Set[str]:
        """获取实体的一跳邻居"""
        neighbors = set()

        # 出边邻居
        for _, target in self.outgoing_edges[entity]:
            neighbors.add(target)

        # 入边邻居
        for _, source in self.incoming_edges[entity]:
            neighbors.add(source)

        return neighbors

    def get_two_hop_neighbors(self, entity: str) -> Set[str]:
        """获取实体的二跳邻居"""
        one_hop = self.get_one_hop_neighbors(entity)
        two_hop = set()

        for neighbor in one_hop:
            two_hop.update(self.get_one_hop_neighbors(neighbor))

        # 去除自身和一跳邻居
        two_hop.discard(entity)
        two_hop -= one_hop

        return two_hop

    def get_entities_with_relation(self, relation: str, position: str) -> Set[str]:
        """
        获取与指定关系相关的实体

        Args:
            relation: 关系类型
            position: 'head' 或 'tail'

        Returns:
            实体集合
        """
        entities = set()

        if relation in self.relation_pairs:
            for head, tail in self.relation_pairs[relation]:
                if position == 'head':
                    entities.add(head)
                elif position == 'tail':
                    entities.add(tail)

        return entities

    def get_entity_name(self, entity_id: str) -> str:
        """获取实体名称"""
        return self.entity2name.get(entity_id, entity_id)

    def get_entity_description(self, entity_id: str) -> str:
        """获取实体描述"""
        return self.entity2description.get(entity_id, "")

    def triplet_exists(self, head: str, relation: str, tail: str) -> bool:
        """检查三元组是否在原始KG中存在"""
        return (head, relation, tail) in self.kg_triplets
