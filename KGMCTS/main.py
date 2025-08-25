"""
主程序入口：为稀疏结点进行知识图谱增强
通过MCTS搜索在有限预算内找到尽可能多的正确三元组
"""

import json
import logging
from pathlib import Path
from typing import Dict, List, Tuple

from kg_enhancer import KGEnhancer
from setup_logger import setup_logger


def load_sparse_entities_config(config_path: str) -> Dict[str, List[Tuple[str, str]]]:
    """
    加载稀疏实体配置文件

    Args:
        config_path: 配置文件路径

    Returns:
        字典格式: {entity: [(position, relation), ...], ...}
        position: 'head' 或 'tail'
        relation: 关系类型
    """
    with open(config_path, 'r', encoding='utf-8') as f:
        return json.load(f)


def main():
    # 设置日志
    logger = setup_logger("main")

    # 配置参数
    config = {
        "sparse_entities_config": "sparse_entities.json",
        "entity2name_path": "entity2name.txt",
        "relation2id_path": "relation2id.txt",
        "entity2description_path": "entity2description.txt",
        "train_kg_path": "train.txt",
        "output_path": "discovered_triplets.txt",
        "budget_per_entity": 1000,  # 每个稀疏实体的判别预算
        "mcts_iterations": 50,      # MCTS迭代次数
        "leaf_threshold": 10,       # 叶子结点候选实体阈值
        "exploration_weight": 1.0   # UCT探索权重
    }

    try:
        # 检查必需文件是否存在
        required_files = [
            config["sparse_entities_config"],
            config["entity2name_path"],
            config["relation2id_path"],
            config["entity2description_path"],
            config["train_kg_path"]
        ]

        for file_path in required_files:
            if not Path(file_path).exists():
                logger.error(f"Required file not found: {file_path}")
                return

        # 加载稀疏实体配置
        logger.info("Loading sparse entities configuration...")
        sparse_entities = load_sparse_entities_config(config["sparse_entities_config"])
        logger.info(f"Loaded {len(sparse_entities)} sparse entities")

        # 初始化知识图谱增强器
        logger.info("Initializing KG enhancer...")
        enhancer = KGEnhancer(
            entity2name_path=config["entity2name_path"],
            relation2id_path=config["relation2id_path"],
            entity2description_path=config["entity2description_path"],
            train_kg_path=config["train_kg_path"],
            budget_per_entity=config["budget_per_entity"],
            mcts_iterations=config["mcts_iterations"],
            leaf_threshold=config["leaf_threshold"],
            exploration_weight=config["exploration_weight"]
        )

        # 为每个稀疏实体进行增强
        all_discovered_triplets = []

        for entity_idx, (entity, position_relations) in enumerate(sparse_entities.items()):
            logger.info(f"\n{'='*50}")
            logger.info(f"Processing sparse entity {entity_idx + 1}/{len(sparse_entities)}: {entity}")
            logger.info(f"Position-relation pairs: {len(position_relations)}")

            # TODO: 目前是对每一个位置-关系对单独进行MCTS搜索，应该把关系对的选择也放到MCTS中
            for pos_rel_idx, (position, relation) in enumerate(position_relations):
                logger.info(f"\nProcessing pair {pos_rel_idx + 1}/{len(position_relations)}: "
                           f"position={position}, relation={relation}")

                # 使用MCTS搜索该实体-位置-关系组合的正确三元组
                discovered = enhancer.enhance_entity_relation(entity, position, relation)

                all_discovered_triplets.extend(discovered)
                logger.info(f"Discovered {len(discovered)} valid triplets for {entity}-{position}-{relation}")

        # 保存结果
        logger.info(f"\nSaving {len(all_discovered_triplets)} discovered triplets to {config['output_path']}")
        with open(config["output_path"], 'w', encoding='utf-8') as f:
            for head, rel, tail in all_discovered_triplets:
                f.write(f"({head}\t{rel}\t{tail})\n")

        logger.info("Knowledge graph enhancement completed successfully!")

    except Exception as e:
        logger.error(f"Error in main execution: {e}")
        raise


if __name__ == "__main__":
    main()
