from typing import List, Dict
from core.models.course import Course, CourseStatus
from core.services.element_locator_service import ElementLocatorService
from core.exceptions.exceptions import ElementNotFoundException
import logging


class CourseMonitorService:
    """Service for monitoring course availability"""

    def __init__(self):
        self.element_locator = ElementLocatorService()
        self.logger = logging.getLogger(__name__)

    def check_course_availability(self, sb, course_name: str) -> Course:
        """
        Check if a course is available for selection

        Args:
            sb: SeleniumBase driver instance
            course_name: Name of the course to check

        Returns:
            Course: Course object with current status
        """
        try:
            status_text = self.element_locator.find_course_status(sb, course_name)

            if status_text == "已选满":
                status = CourseStatus.FULL
            elif status_text == "未选满":
                status = CourseStatus.AVAILABLE
            else:
                status = CourseStatus.UNKNOWN

            course = Course(name=course_name, status=status)
            self.logger.debug(f"Course {course_name} status: {status_text}")

            return course

        except Exception as e:
            self.logger.error(f"Error checking course {course_name}: {e}")
            return Course(name=course_name, status=CourseStatus.UNKNOWN)

    def check_multiple_courses(self, sb, course_names: List[str]) -> List[Course]:
        """Check availability for multiple courses"""
        courses = []

        for course_name in course_names:
            try:
                course = self.check_course_availability(sb, course_name)
                courses.append(course)
            except Exception as e:
                self.logger.error(f"Error checking course {course_name}: {e}")
                # Add course with unknown status
                courses.append(Course(name=course_name, status=CourseStatus.UNKNOWN))

        return courses
