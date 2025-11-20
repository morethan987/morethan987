import logging
import time
from typing import List, Optional

from core.exceptions.exceptions import (
    CourseNotAvailableException,
    ElementNotFoundException,
)
from core.models.course import Course, Teacher
from core.services.element_locator_service import ElementLocatorService
from core.strategies.selection_strategies import SelectionStrategy, StrategyFactory


class CourseSelectionService:
    """Service for executing course selection"""

    def __init__(self):
        self.element_locator = ElementLocatorService()
        self.logger = logging.getLogger(__name__)

    def select_course_general(self, sb, course_name: str) -> bool:
        """
        Select course using general strategy (first available)

        Args:
            sb: SeleniumBase driver instance
            course_name: Name of the course to select

        Returns:
            bool: True if selection successful
        """
        try:
            # Click course link
            link = self.element_locator.find_course_link(sb, course_name)
            sb.driver.execute_script("arguments[0].scrollIntoView();", link)
            time.sleep(0.5)
            link.click()

            self.logger.info(f"Clicked course link for: {course_name}")

            # Wait for sidebar and find first available checkbox
            sb.wait_for_element(".ant-drawer-content-wrapper", timeout=10)
            sb.wait_for_element("div.ant-drawer-body table tbody tr", timeout=10)
            time.sleep(0.5)

            checkboxes = sb.find_elements(
                "css selector",
                'div.ant-drawer-body table input.ant-checkbox-input[type="checkbox"]',
            )

            checkbox_clicked = False
            for checkbox in checkboxes:
                if checkbox.is_enabled():
                    sb.driver.execute_script("arguments[0].click();", checkbox)
                    self.logger.info(f"Clicked checkbox for course: {course_name}")
                    checkbox_clicked = True
                    break

            if not checkbox_clicked:
                self.logger.warning(
                    f"No available checkbox found for course: {course_name}"
                )
                return False

            # Click selection button and confirm
            return self._complete_selection_process(sb, course_name)

        except Exception as e:
            self.logger.error(f"Error selecting course {course_name}: {e}")
            return False

    def select_course_with_teacher(
        self, sb, course_name: str, teacher_name: str
    ) -> bool:
        """
        Select course with specific teacher preference

        Args:
            sb: SeleniumBase driver instance
            course_name: Name of the course to select
            teacher_name: Preferred teacher name

        Returns:
            bool: True if selection successful
        """
        try:
            # Click course link
            link = self.element_locator.find_course_link(sb, course_name)
            sb.driver.execute_script("arguments[0].scrollIntoView();", link)
            time.sleep(0.5)
            # Scroll up a bit to avoid header obstruction
            sb.driver.execute_script("window.scrollBy(0, -100);")
            time.sleep(0.2)
            link.click()

            self.logger.info(f"Clicked course link for: {course_name}")

            # Find teacher row
            row, is_full, checkbox = self.element_locator.find_teacher_row(
                sb, teacher_name
            )

            if row is None:
                self.logger.warning(
                    f"Teacher {teacher_name} not found for course {course_name}"
                )
                self.element_locator.close_sidebar(sb)
                return False

            if is_full:
                self.logger.info(
                    f"Course {course_name} with teacher {teacher_name} is full"
                )
                self.element_locator.close_sidebar(sb)
                return False

            if checkbox is None:
                self.logger.warning(f"No available checkbox for teacher {teacher_name}")
                self.element_locator.close_sidebar(sb)
                return False

            # Click checkbox
            sb.driver.execute_script("arguments[0].click();", checkbox)
            self.logger.info(
                f"Clicked checkbox for course {course_name}, teacher {teacher_name}"
            )

            # Complete selection process
            return self._complete_selection_process(sb, course_name, teacher_name)

        except Exception as e:
            self.logger.error(
                f"Error selecting course {course_name} with teacher {teacher_name}: {e}"
            )
            return False

    def _complete_selection_process(
        self, sb, course_name: str, teacher_name: str = ""
    ) -> bool:
        """Complete the selection process by clicking selection and confirm buttons"""
        try:
            # Click selection button
            selection_btn = self.element_locator.find_selection_button(sb)
            if selection_btn:
                sb.driver.execute_script(
                    "arguments[0].scrollIntoView();", selection_btn
                )
                time.sleep(0.2)
                selection_btn.click()

                course_info = f"{course_name}" + (
                    f" (Teacher: {teacher_name})" if teacher_name else ""
                )
                self.logger.info(f"Clicked selection button for: {course_info}")
            else:
                self.logger.error(
                    f"Selection button not found for course: {course_name}"
                )
                return False

            # Click confirm button
            time.sleep(0.5)
            confirm_btn = self.element_locator.find_confirm_button(sb)
            if confirm_btn:
                sb.driver.execute_script("arguments[0].scrollIntoView();", confirm_btn)
                time.sleep(0.2)
                confirm_btn.click()

                course_info = f"{course_name}" + (
                    f" (Teacher: {teacher_name})" if teacher_name else ""
                )
                self.logger.info(f"Clicked confirm button for: {course_info}")
                return True
            else:
                self.logger.error(f"Confirm button not found for course: {course_name}")
                return False

        except Exception as e:
            course_info = f"{course_name}" + (
                f" (Teacher: {teacher_name})" if teacher_name else ""
            )
            self.logger.error(
                f"Error completing selection process for {course_info}: {e}"
            )
            return False
