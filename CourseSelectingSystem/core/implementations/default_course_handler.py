from typing import List, Dict, Any
from core.interfaces.interface import CourseHandlerInterface
from core.services.course_selection_service import CourseSelectionService
from core.services.notification_service import NotificationManager
import logging


class DefaultCourseHandler(CourseHandlerInterface):
    """默认的课程处理实现，包含原始的选课逻辑"""

    def __init__(self):
        self.selection_service = CourseSelectionService()
        self.logger = logging.getLogger(__name__)

    def handle_available_courses(self, sb, available_courses: List[Dict[str, Any]], config) -> None:
        """处理可用课程，尝试选课"""
        for course_info in available_courses:
            try:
                if course_info['type'] == 'general':
                    self._select_general_course(sb, course_info, config)
                elif course_info['type'] == 'teacher_specific':
                    self._select_course_with_teacher(sb, course_info, config)
            except Exception as e:
                self.logger.error(f"Error handling available course {course_info}: {e}")

    def _select_general_course(self, sb, course_info: Dict[str, Any], config):
        """选择普通课程"""
        course_name = course_info['course_name']

        try:
            success = self.selection_service.select_course_general(sb, course_name)

            notification_manager = NotificationManager(config.notifications)
            if success:
                notification_manager.notify_course_selected(course_name)
                self.logger.info(f"Successfully selected course: {course_name}")
            else:
                self.logger.warning(f"Failed to select course: {course_name}")

        except Exception as e:
            self.logger.error(f"Error selecting general course {course_name}: {e}")

    def _select_course_with_teacher(self, sb, course_info: Dict[str, Any], config):
        """选择指定教师的课程"""
        course_name = course_info['course_name']
        teacher_name = course_info['teacher_name']

        try:
            success = self.selection_service.select_course_with_teacher(sb, course_name, teacher_name)

            notification_manager = NotificationManager(config.notifications)
            if success:
                notification_manager.notify_course_selected(course_name, teacher_name)
                self.logger.info(f"Successfully selected course: {course_name} with teacher: {teacher_name}")
            else:
                self.logger.warning(f"Failed to select course: {course_name} with teacher: {teacher_name}")

        except Exception as e:
            self.logger.error(f"Error selecting course {course_name} with teacher {teacher_name}: {e}")