"""
基本使用示例：展示如何使用重构后的选课系统

这个示例展示了三种使用方式：
1. 使用默认实现（与原始系统行为相同）
2. 使用自定义监控逻辑
3. 完全自定义的监控和处理逻辑
"""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import logging
from typing import Any, Dict, List

from core.course_selection_system import CourseSelectionSystem
from core.implementations.default_course_handler import DefaultCourseHandler
from core.implementations.default_course_monitor import DefaultCourseMonitor
from core.interfaces.interface import CourseHandlerInterface, CourseMonitorInterface
from core.models.config import (
    LoginConfig,
    MonitoringConfig,
    NotificationConfig,
    SystemConfig,
    WebDriverConfig,
)
from core.services.course_monitor_service import CourseMonitorService
from core.services.course_selection_service import CourseSelectionService
from utils.config_loader import ConfigLoader


def example_1_default_implementation():
    """示例1：使用默认实现（相当于原始系统）"""
    print("=== 示例1：使用默认实现 ===")

    # 加载配置
    config = ConfigLoader.load_from_file("config/config.yaml")

    # 创建默认的监控和处理实现
    monitor = DefaultCourseMonitor()
    handler = DefaultCourseHandler()

    # 创建系统实例
    system = CourseSelectionSystem(config, monitor, handler)

    # 启动系统（这与原始系统行为完全相同）
    # system.start()  # 注释掉避免实际运行
    print("系统已配置完成，可以调用 system.start() 启动")


class CustomCourseMonitor(CourseMonitorInterface):
    """自定义课程监控实现示例"""

    def __init__(self, priority_courses: List[str]):
        self.priority_courses = priority_courses  # 优先监控的课程
        self.monitor_service = CourseMonitorService()
        self.logger = logging.getLogger(__name__)

    def monitor_courses(self, sb, config) -> List[Dict[str, Any]]:
        """自定义监控逻辑：优先检查重要课程"""
        available_courses = []

        # 首先检查优先课程
        for course_name in self.priority_courses:
            try:
                course = self.monitor_service.check_course_availability(sb, course_name)
                if course.status.value == "未选满":  # 课程可用
                    available_courses.append(
                        {
                            "type": "priority",
                            "course_name": course_name,
                            "course": course,
                            "priority": True,
                        }
                    )
                    self.logger.info(f"Priority course '{course_name}' is available!")
            except Exception as e:
                self.logger.error(f"Error checking priority course {course_name}: {e}")

        # 如果没有优先课程可用，再检查其他课程
        if not available_courses:
            for course_config in config.monitoring.courses:
                course_name = (
                    course_config.get("name")
                    if isinstance(course_config, dict)
                    else course_config
                )
                if course_name and course_name not in self.priority_courses:
                    try:
                        course = self.monitor_service.check_course_availability(
                            sb, course_name
                        )
                        if course.status.value == "未选满":
                            available_courses.append(
                                {
                                    "type": "general",
                                    "course_name": course_name,
                                    "course": course,
                                    "priority": False,
                                }
                            )
                    except Exception as e:
                        self.logger.error(f"Error checking course {course_name}: {e}")

        return available_courses


class CustomCourseHandler(CourseHandlerInterface):
    """自定义课程处理实现示例"""

    def __init__(self, max_courses: int = 2):
        self.max_courses = max_courses  # 最多选择的课程数量
        self.selected_courses = []  # 已选课程列表
        self.selection_service = CourseSelectionService()
        self.logger = logging.getLogger(__name__)

    def handle_available_courses(
        self, sb, available_courses: List[Dict[str, Any]], config
    ) -> None:
        """自定义处理逻辑：限制选课数量，优先选择重要课程"""

        if len(self.selected_courses) >= self.max_courses:
            self.logger.info(
                f"Already selected {self.max_courses} courses, skipping selection"
            )
            return

        # 按优先级排序
        available_courses.sort(key=lambda x: x.get("priority", False), reverse=True)

        for course_info in available_courses:
            if len(self.selected_courses) >= self.max_courses:
                break

            course_name = course_info["course_name"]

            # 检查是否已经选过这门课
            if course_name in self.selected_courses:
                continue

            try:
                success = self.selection_service.select_course_general(sb, course_name)
                if success:
                    self.selected_courses.append(course_name)
                    self.logger.info(
                        f"Successfully selected course: {course_name} ({len(self.selected_courses)}/{self.max_courses})"
                    )

                    # 发送自定义通知
                    priority_msg = " [PRIORITY]" if course_info.get("priority") else ""
                    print(f"🎉 选课成功: {course_name}{priority_msg}")
                else:
                    self.logger.warning(f"Failed to select course: {course_name}")
            except Exception as e:
                self.logger.error(f"Error selecting course {course_name}: {e}")


def example_2_custom_monitor():
    """示例2：使用自定义监控逻辑"""
    print("\n=== 示例2：使用自定义监控逻辑 ===")

    config = ConfigLoader.load_from_file("config/config.yaml")

    # 创建自定义监控器，设置优先课程
    priority_courses = ["高等数学", "线性代数"]  # 这些课程优先监控
    custom_monitor = CustomCourseMonitor(priority_courses)

    # 使用默认处理器
    handler = DefaultCourseHandler()

    # 创建系统实例
    system = CourseSelectionSystem(config, custom_monitor, handler)

    print(f"系统已配置完成，优先监控课程: {priority_courses}")
    print("可以调用 system.start() 启动")


def example_3_fully_custom():
    """示例3：完全自定义的监控和处理逻辑"""
    print("\n=== 示例3：完全自定义实现 ===")

    config = ConfigLoader.load_from_file("config/config.yaml")

    # 自定义监控器和处理器
    priority_courses = ["高等数学", "线性代数", "概率论"]
    custom_monitor = CustomCourseMonitor(priority_courses)
    custom_handler = CustomCourseHandler(max_courses=2)  # 最多选2门课

    # 创建系统实例
    system = CourseSelectionSystem(config, custom_monitor, custom_handler)

    print("系统已配置完成:")
    print(f"- 优先课程: {priority_courses}")
    print("- 最大选课数量: 2")
    print("- 会优先选择重要课程")
    print("可以调用 system.start() 启动")


def example_4_service_composition():
    """示例4：直接使用底层服务组装逻辑"""
    print("\n=== 示例4：使用底层服务组装逻辑 ===")

    # 这展示了如何不使用系统类，直接组装底层服务
    class SimpleMonitor(CourseMonitorInterface):
        def __init__(self):
            self.monitor_service = CourseMonitorService()

        def monitor_courses(self, sb, config):
            # 只检查第一个配置的课程
            if config.monitoring.courses:
                course_name = config.monitoring.courses[0]
                if isinstance(course_name, dict):
                    course_name = course_name.get("name", "")

                course = self.monitor_service.check_course_availability(sb, course_name)
                if course.status.value == "未选满":
                    return [{"course_name": course_name, "course": course}]
            return []

    class SimpleHandler(CourseHandlerInterface):
        def __init__(self):
            self.selection_service = CourseSelectionService()

        def handle_available_courses(self, sb, available_courses, config):
            # 简单选择第一个可用课程
            if available_courses:
                course_name = available_courses[0]["course_name"]
                success = self.selection_service.select_course_general(sb, course_name)
                print(f"选课结果: {course_name} -> {'成功' if success else '失败'}")

    config = ConfigLoader.load_from_file("config/config.yaml")
    simple_monitor = SimpleMonitor()
    simple_handler = SimpleHandler()

    system = CourseSelectionSystem(config, simple_monitor, simple_handler)
    print("简单系统已配置完成，只会选择第一个配置的课程")


if __name__ == "__main__":
    # 运行所有示例
    example_1_default_implementation()
    example_2_custom_monitor()
    example_3_fully_custom()
    example_4_service_composition()

    print("\n" + "=" * 60)
    print("总结:")
    print("1. 默认实现保持了原有系统的所有功能")
    print("2. 可以通过继承接口类实现自定义监控和处理逻辑")
    print("3. 底层服务可以灵活组合使用")
    print("4. 系统现在更加模块化和可扩展")
