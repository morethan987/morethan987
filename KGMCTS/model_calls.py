"""
LLM模型调用：用于语义分析和实体分组的LLM接口
"""

import os
import time
import logging
import httpx
from openai import OpenAI
from dotenv import load_dotenv
from typing import Optional

from setup_logger import setup_logger

# 加载环境变量
load_dotenv()


class ModelCalls:
    """LLM模型调用客户端"""

    def __init__(self,
                 api_key: Optional[str] = None,
                 base_url: Optional[str] = None,
                 model_name: Optional[str] = None):
        """
        初始化LLM客户端

        Args:
            api_key: API密钥
            base_url: API基础URL
            model_name: 模型名称
        """
        self.logger = setup_logger(self.__class__.__name__)

        # 从环境变量或参数获取配置
        self.api_key = api_key or os.getenv("DEEPSEEK_API_KEY")
        self.base_url = base_url or os.getenv("DEEPSEEK_BASE_URL")
        self.model_name = model_name or os.getenv("DEEPSEEK_MODEL_NAME", "deepseek-chat")

        # 初始化OpenAI客户端
        try:
            self.client = OpenAI(
                api_key=self.api_key,
                base_url=self.base_url,
                http_client=httpx.Client(trust_env=False)
            )
            self.logger.info(f"LLM client initialized with model: {self.model_name}")
        except Exception as e:
            self.logger.error(f"Failed to initialize LLM client: {e}")
            raise

        # 调用统计
        self.call_count = 0
        self.total_tokens = 0

    def get_output(self, prompt: str,
                   temperature: float = 0.0,
                   max_tokens: Optional[int] = None,
                   max_retries: int = 3,
                   retry_delay: float = 2.0) -> str:
        """
        获取LLM输出

        Args:
            prompt: 输入提示
            temperature: 生成温度
            max_tokens: 最大token数
            max_retries: 最大重试次数
            retry_delay: 重试延迟（秒）

        Returns:
            LLM生成的文本
        """
        attempt = 0
        while attempt < max_retries:
            try:
                # 准备请求
                messages = [
                    {
                        "role": "user",
                        "content": prompt
                    }
                ]

                # 准备参数
                kwargs = {
                    "messages": messages,
                    "model": self.model_name,
                    "temperature": temperature
                }

                if max_tokens:
                    kwargs["max_tokens"] = max_tokens

                # 调用API
                self.logger.debug(f"Calling LLM with prompt length: {len(prompt)}")
                response = self.client.chat.completions.create(**kwargs)

                # 提取响应
                content = response.choices[0].message.content

                # 更新统计信息
                self.call_count += 1
                if hasattr(response, 'usage') and response.usage:
                    self.total_tokens += response.usage.total_tokens

                self.logger.debug(f"LLM call successful, response length: {len(content)}")
                return content

            except Exception as e:
                attempt += 1
                self.logger.warning(f"LLM call attempt {attempt} failed: {e}")

                if attempt < max_retries:
                    self.logger.info(f"Retrying in {retry_delay} seconds...")
                    time.sleep(retry_delay)
                else:
                    self.logger.error("Max retries reached for LLM call")
                    raise

        return ""

    def get_semantic_similarity(self, entity1: str, entity2: str,
                               entity1_desc: str = "", entity2_desc: str = "",
                               relation: str = "") -> float:
        """
        获取两个实体的语义相似度评分

        Args:
            entity1: 实体1
            entity2: 实体2
            entity1_desc: 实体1描述
            entity2_desc: 实体2描述
            relation: 相关关系

        Returns:
            相似度评分 (0-1)
        """
        prompt = f"""
Please rate the semantic similarity between the following two entities on a scale of 0.0 to 1.0, where 1.0 means very similar and 0.0 means completely unrelated.

Entity 1: {entity1}
Description: {entity1_desc}

Entity 2: {entity2}
Description: {entity2_desc}

Context Relation: {relation}

Please respond with only a number between 0.0 and 1.0.
"""

        try:
            response = self.get_output(prompt, temperature=0.0, max_tokens=10)
            # 尝试解析数字
            score = float(response.strip())
            return max(0.0, min(1.0, score))  # 确保在有效范围内
        except (ValueError, TypeError):
            self.logger.warning(f"Failed to parse similarity score: {response}")
            return 0.5  # 默认中等相似度

    def classify_entity_relevance(self, sparse_entity: str, sparse_desc: str,
                                candidate_entity: str, candidate_desc: str,
                                relation: str, position: str) -> str:
        """
        分类候选实体与稀疏实体在给定关系下的相关性

        Args:
            sparse_entity: 稀疏实体
            sparse_desc: 稀疏实体描述
            candidate_entity: 候选实体
            candidate_desc: 候选实体描述
            relation: 关系
            position: 位置 ('head' or 'tail')

        Returns:
            相关性类别 ('high', 'medium', 'low')
        """
        prompt = f"""
Given a sparse entity and a candidate entity, please classify the relevance of forming a triplet with the given relation.

Sparse Entity: {sparse_entity}
Description: {sparse_desc}

Candidate Entity: {candidate_entity}
Description: {candidate_desc}

Relation: {relation}
Position: The sparse entity is the {position} in the triplet.

Please classify the relevance as one of: high, medium, low

Respond with only one word: high, medium, or low.
"""

        try:
            response = self.get_output(prompt, temperature=0.0, max_tokens=5)
            relevance = response.strip().lower()

            if relevance in ['high', 'medium', 'low']:
                return relevance
            else:
                return 'medium'  # 默认中等相关性

        except Exception as e:
            self.logger.warning(f"Failed to classify entity relevance: {e}")
            return 'medium'

    def group_entities_semantically(self, entities: list,
                                  sparse_entity: str, sparse_desc: str,
                                  relation: str, position: str,
                                  num_groups: int = 3) -> dict:
        """
        将实体列表按语义相似性分组

        Args:
            entities: 实体列表
            sparse_entity: 稀疏实体
            sparse_desc: 稀疏实体描述
            relation: 关系
            position: 位置
            num_groups: 分组数量

        Returns:
            分组结果字典 {group_id: [entity_list]}
        """
        if len(entities) <= num_groups:
            # 实体数量少于分组数，每个实体一组
            return {i: [entities[i]] for i in range(len(entities))}

        prompt = f"""
Please group the following entities into {num_groups} semantic groups based on their relevance to the sparse entity and relation.

Sparse Entity: {sparse_entity}
Description: {sparse_desc}
Relation: {relation}
Position: {position}

Entities to group:
{chr(10).join([f"- {entity}" for entity in entities])}

Please respond in the format:
Group 0: entity1, entity2, entity3
Group 1: entity4, entity5, entity6
Group 2: entity7, entity8, entity9

Make sure each entity appears exactly once.
"""

        try:
            response = self.get_output(prompt, temperature=0.0)
            groups = self._parse_grouping_response(response, entities, num_groups)
            return groups

        except Exception as e:
            self.logger.warning(f"Failed to group entities semantically: {e}")
            # 回退到随机分组
            return self._random_grouping(entities, num_groups)

    def _parse_grouping_response(self, response: str, entities: list, num_groups: int) -> dict:
        """解析分组响应"""
        groups = {}
        used_entities = set()

        lines = response.strip().split('\n')
        for line in lines:
            if line.startswith('Group'):
                try:
                    # 提取组ID和实体列表
                    parts = line.split(':', 1)
                    if len(parts) == 2:
                        group_id = int(parts[0].replace('Group', '').strip())
                        entities_str = parts[1].strip()

                        # 解析实体列表
                        group_entities = []
                        for entity in entities_str.split(','):
                            entity = entity.strip()
                            if entity in entities and entity not in used_entities:
                                group_entities.append(entity)
                                used_entities.add(entity)

                        if group_entities:
                            groups[group_id] = group_entities

                except (ValueError, IndexError):
                    continue

        # 处理未分组的实体
        remaining_entities = [e for e in entities if e not in used_entities]
        if remaining_entities:
            # 将剩余实体分配到现有组或创建新组
            group_ids = list(groups.keys())
            if not group_ids:
                groups[0] = remaining_entities
            else:
                for i, entity in enumerate(remaining_entities):
                    group_id = group_ids[i % len(group_ids)]
                    groups[group_id].append(entity)

        # 确保有有效的分组
        if not groups:
            return self._random_grouping(entities, num_groups)

        return groups

    def _random_grouping(self, entities: list, num_groups: int) -> dict:
        """随机分组"""
        import random

        entities_copy = entities.copy()
        random.shuffle(entities_copy)

        groups = {}
        group_size = len(entities_copy) // num_groups

        for i in range(num_groups):
            start_idx = i * group_size
            if i == num_groups - 1:  # 最后一组包含所有剩余实体
                end_idx = len(entities_copy)
            else:
                end_idx = (i + 1) * group_size

            groups[i] = entities_copy[start_idx:end_idx]

        return groups

    def get_statistics(self) -> dict:
        """获取调用统计信息"""
        return {
            "call_count": self.call_count,
            "total_tokens": self.total_tokens
        }

    def reset_statistics(self):
        """重置统计信息"""
        self.call_count = 0
        self.total_tokens = 0
