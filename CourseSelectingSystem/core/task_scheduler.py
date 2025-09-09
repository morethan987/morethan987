import time
import threading
from typing import List, Callable, Dict, Any
from abc import ABC, abstractmethod
from core.models.config import SystemConfig
from core.strategies.retry_strategies import RetryStrategy, FixedIntervalRetry
import logging


class TaskScheduler:
    """Main task scheduler for course selection system"""

    def __init__(self, config: SystemConfig):
        self.config = config
        self.logger = logging.getLogger(__name__)
        self.running = False
        self.retry_strategy = FixedIntervalRetry(
            max_retries=config.monitoring.max_retries,
            delay=1.0
        )

    def start_monitoring(self, monitor_func: Callable, selection_func: Callable):
        """
        Start the monitoring and selection process

        Args:
            monitor_func: Function to monitor course availability
            selection_func: Function to select available courses
        """
        self.running = True
        self.logger.info("Starting course monitoring and selection process")

        while self.running:
            try:
                # Execute monitoring with retry
                self.retry_strategy.execute(self._execute_monitoring_cycle, monitor_func, selection_func)

                if self.running:  # Check if still running before sleeping
                    time.sleep(self.config.monitoring.check_interval)

            except KeyboardInterrupt:
                self.logger.info("Received interrupt signal, stopping...")
                self.stop()
                break
            except Exception as e:
                self.logger.error(f"Critical error in monitoring cycle: {e}")
                if self.running:
                    time.sleep(self.config.monitoring.check_interval)

    def _execute_monitoring_cycle(self, monitor_func: Callable, selection_func: Callable):
        """Execute one monitoring cycle"""
        self.logger.debug("Executing monitoring cycle")

        # Monitor courses
        available_courses = monitor_func()

        # Select available courses
        if available_courses:
            selection_func(available_courses)

    def stop(self):
        """Stop the monitoring process"""
        self.running = False
        self.logger.info("Task scheduler stopped")
