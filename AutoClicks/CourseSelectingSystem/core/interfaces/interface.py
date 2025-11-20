from abc import ABC, abstractmethod
from typing import List, Dict, Any
from core.models.course import Course


class CourseMonitorInterface(ABC):
    """抽象的课程监控接口"""

    @abstractmethod
    def monitor_courses(self, sb, config) -> List[Dict[str, Any]]:
        """
        监控课程可用性
        
        Args:
            sb: SeleniumBase driver instance
            config: 系统配置
            
        Returns:
            List[Dict[str, Any]]: 可用课程列表
        """
        pass


class CourseHandlerInterface(ABC):
    """抽象的课程处理接口"""

    @abstractmethod
    def handle_available_courses(self, sb, available_courses: List[Dict[str, Any]], config) -> None:
        """
        处理可用的课程
        
        Args:
            sb: SeleniumBase driver instance
            available_courses: 可用课程列表
            config: 系统配置
        """
        pass