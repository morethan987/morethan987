from abc import ABC, abstractmethod
from typing import List
from core.models.course import Course
from core.models.config import NotificationConfig
import logging


class NotificationService(ABC):
    """Abstract base class for notification services"""

    @abstractmethod
    def send_notification(self, message: str, **kwargs):
        """Send notification with given message"""
        pass


class ConsoleNotificationService(NotificationService):
    """Console-based notification service"""

    def __init__(self):
        self.logger = logging.getLogger(__name__)

    def send_notification(self, message: str, **kwargs):
        """Send notification to console"""
        print(f"🔔 {message}")
        self.logger.info(f"Notification: {message}")


class NotificationManager:
    """Manages multiple notification services"""

    def __init__(self, config: NotificationConfig):
        self.config = config
        self.services: List[NotificationService] = []

        # Initialize enabled notification services
        if config.console:
            self.services.append(ConsoleNotificationService())

    def notify_course_available(self, course: Course):
        """Notify that a course is available"""
        message = f"Course '{course.name}' is now available for selection!"
        for service in self.services:
            service.send_notification(message)

    def notify_course_selected(self, course_name: str, teacher_name: str = None):
        """Notify that a course has been selected"""
        if teacher_name:
            message = f"Successfully selected course '{course_name}' with teacher '{teacher_name}'!"
        else:
            message = f"Successfully selected course '{course_name}'!"

        for service in self.services:
            service.send_notification(message)

    def notify_error(self, error_message: str):
        """Notify about an error"""
        message = f"Error occurred: {error_message}"
        for service in self.services:
            service.send_notification(message)
