from abc import ABC, abstractmethod
import random
import re
from model_calls import ModelCalls
from setup_logger import logger
import logging


class Node(ABC):
    @abstractmethod
    def find_children(self):
        return set()

    @abstractmethod
    def find_random_child(self):
        return None
    
    @abstractmethod
    def expand(self):
        return None

    @abstractmethod
    def is_terminal(self):
        return True

    @abstractmethod
    def reward(self):
        return 0

class LLMNode(Node):
    prompt = "LLM: "

    def __init__(self, history_output, parent, client:ModelCalls):
        self.history_output = history_output
        self.client = client
        self.parent = parent
        self.children = None
        self.output = self.init_output(self.history_output)
        logger.info(f"+++++++++Output {self.prompt}+++++++++\n {self.output}")
        self.current_history = self.init_current_history(self.history_output)


    def find_children(self):
        return self.children
    
    def find_random_child(self):
        return random.choice([SillyNode, FunnyNode, SmartNode])(
            self.current_history, parent=self, client=self.client
        )
    
    def expand(self):
        if not self.children:
            self.children = [
                SillyNode(self.current_history, parent=self, client=self.client),
                FunnyNode(self.current_history, parent=self, client=self.client),
                SmartNode(self.current_history, parent=self, client=self.client),
            ]
        return
    
    def is_terminal(self):
        pattern = r"Final answer:\s*(-?\d+(\.\d+)?)" # TODO 正则匹配机制太脆弱
        match = re.search(pattern, self.output, re.IGNORECASE)
        return match

    def reward(self, truth):
        assert self.is_terminal(), f"reward called on non-terminal node {self}"
        match = re.search( # TODO 正则匹配机制太脆弱
            r"Final answer:\s*(-?\d+(\.\d+)?)", self.output, re.IGNORECASE
        )
        prediction = float(match.group(1))
        logger.info(f"----------------Solving path:\n {self.current_history}")
        if prediction == truth:
            return 1
        return 0

    def init_output(self, history_output):
        return self.client.get_output(history_output + self.prompt)
    
    def init_current_history(self, history_output):
        return history_output + self.prompt + self.output


class QuestionNode(LLMNode):
    def init_output(self, history_output):
        return history_output
    
    def init_current_history(self, history_output):
        return history_output

class SillyNode(LLMNode):
    prompt = "Silly Man:"

class SmartNode(LLMNode):
    prompt = "Smart Man:"

class FunnyNode(LLMNode):
    prompt = "Funny Man:"
