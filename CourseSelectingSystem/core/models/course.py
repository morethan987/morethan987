from dataclasses import dataclass
from typing import List, Optional
from enum import Enum


class CourseStatus(Enum):
    AVAILABLE = "未选满"
    FULL = "已选满"
    UNKNOWN = "未知"


@dataclass
class Teacher:
    name: str
    section_id: Optional[str] = None
    is_full: bool = False


@dataclass
class Course:
    name: str
    code: Optional[str] = None
    status: CourseStatus = CourseStatus.UNKNOWN
    capacity: int = 0
    enrolled: int = 0
    teachers: List[Teacher] = None

    def __post_init__(self):
        if self.teachers is None:
            self.teachers = []


@dataclass
class SelectionTask:
    course_name: str
    preferred_teacher: Optional[str] = None
    priority: int = 1
    strategy: str = "first_available"
    max_retries: int = 3
