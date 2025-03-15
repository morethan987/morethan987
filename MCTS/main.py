from mcts_tree import MCTS
from node import QuestionNode
from model_calls import ModelCalls
import json
import logging


logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

console_handler = logging.StreamHandler()
console_handler.setLevel(logging.INFO)

formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
console_handler.setFormatter(formatter)

logger.addHandler(console_handler)

num_iterations = 4
qa_pair = json.load(open("qa.json", "r"))
mcts = MCTS()
client = ModelCalls(model_name="qwen-max-2025-01-25")
root_node = QuestionNode(qa_pair["question"], None, client)

for _ in range(num_iterations):
    mcts.do_iteration(root_node, qa_pair["ground_truth"])

best_next_node = mcts.choose(root_node)
logger.info(f"Best next node: ")
logger.info(
    best_next_node.history_output + best_next_node.prompt + best_next_node.output
)
