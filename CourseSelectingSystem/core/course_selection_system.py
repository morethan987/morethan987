import time
from typing import List, Dict, Any, Optional
from core.models.config import SystemConfig
from core.interfaces.interface import CourseMonitorInterface, CourseHandlerInterface
from core.services.web_driver_service import WebDriverService
from core.services.login_service import LoginService
from core.services.notification_service import NotificationManager
from core.task_scheduler import TaskScheduler
from core.exceptions.exceptions import LoginFailedException
import logging


class CourseSelectionSystem:
    """重构后的主系统类，支持依赖注入的监控和处理逻辑"""

    def __init__(self, 
                 config: SystemConfig,
                 course_monitor: CourseMonitorInterface,
                 course_handler: CourseHandlerInterface):
        """
        初始化选课系统
        
        Args:
            config: 系统配置
            course_monitor: 课程监控实现
            course_handler: 课程处理实现
        """
        self.config = config
        self.course_monitor = course_monitor
        self.course_handler = course_handler
        self.logger = logging.getLogger(__name__)

        # Initialize core services
        self.webdriver_service = WebDriverService(config.webdriver)
        self.login_service = LoginService(config.login)
        self.notification_manager = NotificationManager(config.notifications)
        self.task_scheduler = TaskScheduler(config)

        self.sb = None

    def start(self):
        """启动选课系统"""
        try:
            self.logger.info("Starting Course Selection System")
            self.logger.info(f"Monitoring {len(self.config.monitoring.courses)} general courses")
            self.logger.info(f"Monitoring {len(self.config.monitoring.course_teacher_pairs)} specific course-teacher pairs")

            with self.webdriver_service.get_driver() as sb:
                self.sb = sb

                # Initial login
                self._perform_login()

                # Start monitoring task with injected implementations
                self.task_scheduler.start_monitoring(
                    monitor_func=self._monitor_courses_wrapper,
                    selection_func=self._handle_available_courses_wrapper
                )

        except KeyboardInterrupt:
            self.logger.info("System stopped by user")
        except Exception as e:
            self.logger.error(f"System error: {e}")
            self.notification_manager.notify_error(str(e))
        finally:
            self.logger.info("Course Selection System shutdown")

    def stop(self):
        """停止选课系统"""
        self.task_scheduler.stop()

    def _perform_login(self):
        """执行登录操作"""
        max_retries = self.config.login.max_retries
        retry_count = 0

        while retry_count < max_retries:
            try:
                self.login_service.login(self.sb)
                self.logger.info("Login successful")
                return
            except LoginFailedException as e:
                retry_count += 1
                if retry_count >= max_retries:
                    raise e
                self.logger.warning(f"Login failed, attempt {retry_count}/{max_retries}")
                time.sleep(2)

    def _monitor_courses_wrapper(self) -> List[Dict[str, Any]]:
        """监控课程的包装器，调用注入的监控实现"""
        try:
            return self.course_monitor.monitor_courses(self.sb, self.config)
        except Exception as e:
            self.logger.error(f"Error during monitoring: {e}")
            # Try to recover by refreshing and re-login if necessary
            try:
                self.sb.refresh()
                time.sleep(2)
            except:
                # If refresh fails, try re-login
                self._perform_login()
            return []

    def _handle_available_courses_wrapper(self, available_courses: List[Dict[str, Any]]):
        """处理可用课程的包装器，调用注入的处理实现"""
        try:
            self.course_handler.handle_available_courses(self.sb, available_courses, self.config)
        except Exception as e:
            self.logger.error(f"Error handling available courses: {e}")
