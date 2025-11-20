from seleniumbase import SB
from selenium.webdriver.chrome.options import Options
from contextlib import contextmanager
from core.models.config import WebDriverConfig
from core.exceptions.exceptions import NetworkException
import logging


class WebDriverService:
    """Service for managing WebDriver instances"""

    def __init__(self, config: WebDriverConfig):
        self.config = config
        self.logger = logging.getLogger(__name__)

    @contextmanager
    def get_driver(self):
        """Context manager for WebDriver"""
        try:
            # SB() 返回一个上下文管理器，通过 with 语句进入
            # 并将创建的实例命名为 sb
            with SB(
                uc=True,
                test=False,
                headless=self.config.headless
            ) as sb:
                # yield 出真正的 SeleniumBase 实例
                yield sb
        except Exception as e:
            self.logger.error(f"WebDriver error: {e}")
            raise NetworkException(f"WebDriver initialization failed: {e}")
