import logging
from typing import Optional, Tuple

from core.exceptions.exceptions import ElementNotFoundException
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from selenium.webdriver.common.by import By


class ElementLocatorService:
    """Service for locating web elements"""

    def __init__(self):
        self.logger = logging.getLogger(__name__)

    def find_course_link(self, sb, course_name: str):
        """Find course link by name"""
        try:
            link = sb.find_element(f'//a[@title="{course_name}"]')
            return link
        except NoSuchElementException as e:
            raise ElementNotFoundException(
                f"Course link not found for: {course_name}"
            ) from e

    def find_course_status(self, sb, course_name: str) -> str:
        """Get course status"""
        try:
            tr = sb.find_element(f'//tr[td/a[@title="{course_name}"]]')
            status_elements = tr.find_elements(
                "xpath", './/span[contains(@class,"u-tag-text")]'
            )
            if status_elements:
                return status_elements[0].text.strip()
            return "未选满"
        except NoSuchElementException:
            return "未知"

    def find_teacher_row(
        self, sb, teacher_name: str
    ) -> Tuple[Optional[any], bool, Optional[any]]:
        """
        Find teacher row in course detail sidebar

        Returns:
            Tuple[row_element, is_full, checkbox_element]
        """
        try:
            # Wait for sidebar to appear
            sb.wait_for_element(".ant-drawer-content-wrapper", timeout=10)
            sb.wait_for_element("div.ant-drawer-body table tbody tr", timeout=10)

            rows = sb.find_elements(
                "css selector", "div.ant-drawer-body table tbody tr"
            )

            for row in rows:
                tds = row.find_elements(
                    By.CSS_SELECTOR, "td.ant-table-row-cell-break-word"
                )

                # Check if this row contains the teacher
                if any(teacher_name in td.text.strip() for td in tds):
                    # Check if full
                    full_tags = row.find_elements(By.CSS_SELECTOR, "span.text-error")
                    is_full = any("容量已满" in tag.text for tag in full_tags)

                    # Find checkbox
                    checkboxes = row.find_elements(
                        By.CSS_SELECTOR, 'input.ant-checkbox-input[type="checkbox"]'
                    )
                    checkbox = None
                    for cb in checkboxes:
                        if cb.is_enabled():
                            checkbox = cb
                            break

                    return row, is_full, checkbox

            return None, None, None

        except (NoSuchElementException, TimeoutException) as e:
            self.logger.error(f"Error finding teacher row for {teacher_name}: {e}")
            return None, None, None

    def find_selection_button(self, sb):
        """Find course selection button"""
        try:
            sb.wait_for_element("div.ant-drawer-body button.ant-btn-primary", timeout=5)
            buttons = sb.find_elements(
                "css selector", "div.ant-drawer-body button.ant-btn-primary"
            )

            for btn in buttons:
                if btn.is_enabled() and "选课" in btn.text.replace(" ", ""):
                    return btn
            return None
        except TimeoutException:
            return None

    def find_confirm_button(self, sb):
        """Find confirmation button in modal"""
        try:
            sb.wait_for_element(".ant-modal-content", timeout=8)
            modals = sb.find_elements("css selector", ".ant-modal")

            if not modals:
                return None

            modal = modals[-1]  # Get latest modal
            confirm_buttons = modal.find_elements(
                By.CSS_SELECTOR, "button.ant-btn-primary"
            )

            for btn in confirm_buttons:
                if btn.is_enabled() and "确认" in btn.text.replace(" ", ""):
                    return btn
            return None
        except TimeoutException:
            return None

    def close_sidebar(self, sb):
        """Close the course detail sidebar"""
        try:
            close_btn = sb.find_element("css selector", "i.anticon.anticon-close")
            sb.driver.execute_script("arguments[0].scrollIntoView();", close_btn)
            close_btn.click()
            return True
        except Exception as e:
            self.logger.error(f"Error closing sidebar: {e}")
            return False
