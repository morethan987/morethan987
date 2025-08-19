from mcts_tree import MCTS
from node import QuestionNode
from model_calls import ModelCalls
import json
from setup_logger import logger


num_iterations = 4
qa_pair = json.load(open("qa.json", "r"))
mcts = MCTS()
client = ModelCalls()
root_node = QuestionNode(qa_pair["question"], None, client)

for _ in range(num_iterations):
    mcts.do_iteration(root_node, qa_pair["ground_truth"])

best_next_node = mcts.choose(root_node)
logger.info(f"Best next node: ")
logger.info(
    best_next_node.history_output + best_next_node.prompt + best_next_node.output
)
