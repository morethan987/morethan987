"""浏览器检测和配置工具模块"""

import os
import sys

from seleniumbase import SB


def get_driver_path():
    """获取 EdgeDriver 的路径（兼容打包后的程序）

    Returns:
        str: EdgeDriver 的完整路径
    """
    if getattr(sys, "frozen", False):
        base_path = sys._MEIPASS  # type: ignore[attr-defined]
    else:
        base_path = os.path.dirname(os.path.abspath(__file__))

    return os.path.join(base_path, "edgedriver", "msedgedriver.exe")


def detect_available_browser():
    """检测系统上可用的浏览器

    Returns:
        str or None: 可用的浏览器名称，或 None 表示使用默认浏览器
    """
    browsers = ["chrome", "edge", "firefox"]

    for browser in browsers:
        try:
            print(f"🔍 检测 {browser.upper()} 浏览器...")
            with SB(browser=browser, headed=False):
                print(f"✅ 找到 {browser.upper()} 浏览器")
                return browser
        except Exception:
            print(f"❌ {browser.upper()} 不可用")
            continue

    print("未找到可用的浏览器，将尝试使用默认浏览器")
    return None
