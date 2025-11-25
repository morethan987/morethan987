"""智能等待工具模块"""

from time import sleep, time

from selenium.common.exceptions import TimeoutException


class SmartWaiter:
    """智能等待工具类"""

    def __init__(self, sb, default_timeout=10):
        """初始化智能等待器

        Args:
            sb: SeleniumBase 实例
            default_timeout: 默认超时时间（秒）
        """
        self.sb = sb
        self.default_timeout = default_timeout

    def wait_for_element(self, selector, timeout=None, by_xpath=True):
        """等待元素出现

        Args:
            selector: 元素选择器
            timeout: 超时时间（秒），默认使用 default_timeout
            by_xpath: 是否使用 XPath 选择器

        Returns:
            bool: 元素是否成功出现
        """
        timeout = timeout or self.default_timeout
        try:
            if by_xpath:
                self.sb.wait_for_element_visible(selector, timeout=timeout)
            else:
                self.sb.wait_for_element_visible(
                    selector, timeout=timeout, by="css selector"
                )
            return True
        except TimeoutException:
            print(f"⏱️  等待超时: {selector[:50]}...")
            return False

    def wait_for_element_disappear(self, selector, timeout=None):
        """等待元素消失

        Args:
            selector: 元素选择器
            timeout: 超时时间（秒）

        Returns:
            bool: 元素是否成功消失
        """
        timeout = timeout or self.default_timeout
        start_time = time()
        while time() - start_time < timeout:
            try:
                if not self.sb.is_element_visible(selector):
                    return True
            except Exception:
                return True
            sleep(0.5)
        return False

    def wait_for_condition(self, condition_func, timeout=None, check_interval=0.5):
        """等待自定义条件满足

        Args:
            condition_func: 条件函数，返回 True 表示条件满足
            timeout: 超时时间（秒）
            check_interval: 检查间隔（秒）

        Returns:
            bool: 条件是否在超时前满足
        """
        timeout = timeout or self.default_timeout
        start_time = time()
        while time() - start_time < timeout:
            try:
                if condition_func():
                    return True
            except Exception:
                pass
            sleep(check_interval)
        return False
