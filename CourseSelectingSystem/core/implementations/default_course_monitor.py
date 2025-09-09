import time
from typing import List, Dict, Any
from core.interfaces.interface import CourseMonitorInterface
from core.models.course import Course, CourseStatus
from core.services.course_monitor_service import CourseMonitorService
from core.services.notification_service import NotificationManager
import logging


class DefaultCourseMonitor(CourseMonitorInterface):
    """默认的课程监控实现，包含原始的监控逻辑"""

    def __init__(self):
        self.monitor_service = CourseMonitorService()
        self.logger = logging.getLogger(__name__)
        self.last_course_states = {}  # Track course states for change detection

    def monitor_courses(self, sb, config) -> List[Dict[str, Any]]:
        """
        监控所有配置的课程并返回可用的课程
        
        Returns:
            List of available courses with their details
        """
        available_courses = []

        try:
            # Refresh page to get latest data
            sb.refresh()
            time.sleep(1)

            # Monitor general courses
            for course_config in config.monitoring.courses:
                course_name = course_config.get('name') if isinstance(course_config, dict) else course_config
                if course_name:
                    available_courses.extend(self._check_general_course(sb, course_name, course_config, config))

            # Monitor course-teacher pairs
            for pair_config in config.monitoring.course_teacher_pairs:
                course_name = pair_config.get('course_name')
                teacher_name = pair_config.get('teacher_name')
                if course_name and teacher_name:
                    available_courses.extend(self._check_course_teacher_pair(sb, course_name, teacher_name, pair_config, config))

        except Exception as e:
            self.logger.error(f"Error during monitoring: {e}")
            # Try to recover by refreshing and re-login if necessary
            try:
                sb.refresh()
                time.sleep(2)
            except:
                # If refresh fails, will be handled by the main system
                pass

        return available_courses

    def _check_general_course(self, sb, course_name: str, course_config: Any, config) -> List[Dict[str, Any]]:
        """Check availability of a general course"""
        available = []

        try:
            course = self.monitor_service.check_course_availability(sb, course_name)

            # Check if status changed
            previous_status = self.last_course_states.get(course_name)
            self.last_course_states[course_name] = course.status

            if course.status == CourseStatus.AVAILABLE:
                # Only notify if status changed from FULL to AVAILABLE
                if previous_status == CourseStatus.FULL:
                    notification_manager = NotificationManager(config.notifications)
                    notification_manager.notify_course_available(course)
                    self.logger.info(f"Course '{course_name}' became available!")

                available.append({
                    'type': 'general',
                    'course_name': course_name,
                    'course': course,
                    'config': course_config
                })
            elif previous_status == CourseStatus.AVAILABLE and course.status == CourseStatus.FULL:
                self.logger.info(f"Course '{course_name}' is now full")

        except Exception as e:
            self.logger.error(f"Error checking general course {course_name}: {e}")

        return available

    def _check_course_teacher_pair(self, sb, course_name: str, teacher_name: str, pair_config: Dict[str, Any], config) -> List[Dict[str, Any]]:
        """Check availability of a specific course-teacher pair"""
        available = []

        try:
            # Create a unique key for this course-teacher pair
            pair_key = f"{course_name}:{teacher_name}"

            # Click course link to check teacher availability
            link = self.monitor_service.element_locator.find_course_link(sb, course_name)
            if link:
                sb.driver.execute_script("arguments[0].scrollIntoView();", link)
                time.sleep(0.5)
                sb.driver.execute_script("window.scrollBy(0, -100);")
                time.sleep(0.2)
                link.click()

                # Check teacher availability
                row, is_full, checkbox = self.monitor_service.element_locator.find_teacher_row(sb, teacher_name)
                self.logger.debug(f"Checked teacher '{teacher_name}' for course '{course_name}': is_full={is_full}, row_found={row is not None}, checkbox_found={checkbox is not None}")

                # Track previous state
                previous_full = self.last_course_states.get(pair_key, True)
                self.last_course_states[pair_key] = is_full

                if row is not None and not is_full and checkbox is not None:
                    # Only notify if status changed from full to available
                    if previous_full:
                        message = f"Course '{course_name}' with teacher '{teacher_name}' is now available!"
                        self.logger.info(message)
                        notification_manager = NotificationManager(config.notifications)
                        notification_manager.services[0].send_notification(message)

                    available.append({
                        'type': 'teacher_specific',
                        'course_name': course_name,
                        'teacher_name': teacher_name,
                        'config': pair_config
                    })

                # Close sidebar
                self.monitor_service.element_locator.close_sidebar(sb)

        except Exception as e:
            self.logger.error(f"Error checking course-teacher pair {course_name}:{teacher_name}: {e}")

        return available