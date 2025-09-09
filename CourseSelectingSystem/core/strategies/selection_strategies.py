from abc import ABC, abstractmethod
from typing import List, Optional
from core.models.course import Course, Teacher


class SelectionStrategy(ABC):
    """Abstract base class for course selection strategies"""

    @abstractmethod
    def select(self, course: Course, available_teachers: List[Teacher]) -> Optional[Teacher]:
        """Select a teacher from available options"""
        pass


class FirstAvailableStrategy(SelectionStrategy):
    """Select the first available teacher"""

    def select(self, course: Course, available_teachers: List[Teacher]) -> Optional[Teacher]:
        if not available_teachers:
            return None
        return available_teachers[0]


class PreferredTeacherStrategy(SelectionStrategy):
    """Select a specific preferred teacher if available"""

    def __init__(self, preferred_teacher: str):
        self.preferred_teacher = preferred_teacher

    def select(self, course: Course, available_teachers: List[Teacher]) -> Optional[Teacher]:
        # First try to find preferred teacher
        for teacher in available_teachers:
            if teacher.name == self.preferred_teacher and not teacher.is_full:
                return teacher

        # Fallback to first available if preferred not found
        return available_teachers[0] if available_teachers else None


class CustomStrategy(SelectionStrategy):
    """Custom selection strategy with user-defined logic"""

    def __init__(self, selection_function):
        self.selection_function = selection_function

    def select(self, course: Course, available_teachers: List[Teacher]) -> Optional[Teacher]:
        return self.selection_function(course, available_teachers)


class StrategyFactory:
    """Factory for creating selection strategies"""

    @staticmethod
    def create_strategy(strategy_type: str, **kwargs) -> SelectionStrategy:
        if strategy_type == "first_available":
            return FirstAvailableStrategy()
        elif strategy_type == "preferred_teacher":
            return PreferredTeacherStrategy(kwargs.get("preferred_teacher", ""))
        elif strategy_type == "custom":
            return CustomStrategy(kwargs.get("selection_function"))
        else:
            raise ValueError(f"Unknown strategy type: {strategy_type}")
