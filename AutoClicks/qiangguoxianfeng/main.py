import os
import sys
from functools import wraps
from time import sleep, time

from selenium.common.exceptions import (
    StaleElementReferenceException,
    TimeoutException,
)
from seleniumbase import SB


def retry_on_failure(max_attempts=3, delay=2, exceptions=(Exception,)):
    """重试装饰器"""

    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            last_exception: Exception | None = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except exceptions as e:
                    last_exception = e
                    if attempt < max_attempts:
                        print(
                            f"⚠️  操作失败 (尝试 {attempt}/{max_attempts}): {func.__name__}"
                        )
                        print(f"   错误: {str(e)[:100]}")
                        sleep(delay)
                    else:
                        print(f"❌ 操作最终失败: {func.__name__}")

            assert last_exception is not None
            raise last_exception

        return wrapper

    return decorator


class SmartWaiter:
    """智能等待工具类"""

    def __init__(self, sb, default_timeout=10):
        self.sb = sb
        self.default_timeout = default_timeout

    def wait_for_element(self, selector, timeout=None, by_xpath=True):
        """等待元素出现"""
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
        """等待元素消失"""
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
        """等待自定义条件满足"""
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


class QiangGuoPlayer:
    """强国先锋自动播放器 - 增强版"""

    def __init__(self, sb):
        self.sb = sb
        self.waiter = SmartWaiter(sb)

        # 选择器配置
        self.selectors = {
            "entry_button": '//*[@id="app"]/div/div[2]/div[2]/div[1]/section/div[2]/div/div/div[1]/div',
            "level1_items": '//*[@id="app"]/div/div[2]/div[2]/div[1]/div[2]/ul/li',
            "level2_items": '//*[@id="app"]/div/div[2]/div[2]/div[2]/div[2]/ul/li',
            "playlist": '//*[@id="app"]/div/div[2]/div/div[1]/section[2]/div[2]/section/div[2]/div/ul/li',
            "play_box": '//*[@id="app"]/div/div[2]/div/div[1]/section[1]/div[2]/div/div',
            "back_button": '//*[@id="app"]/div/div[2]/div/div[1]/section[1]/div',
            "back_to_level1": '//*[@id="app"]/div/div[2]/div[2]/div[1]/div/span[1]/span[1]',
            "popup_buttons": [
                "/html/body/div[5]/div/div/div[3]/button[2]",
                "/html/body/div[5]/div/div/div[3]/button",
                "//button[contains(text(), '确定')]",
                "//button[contains(text(), '关闭')]",
                "//button[contains(@class, 'confirm')]",
            ],
        }

    @retry_on_failure(max_attempts=3, delay=1)
    def safe_click(self, selector, parent=None, wait_time=5):
        """安全的点击操作，带等待和重试"""
        try:
            # 等待元素可见
            if not self.waiter.wait_for_element(selector, timeout=wait_time):
                raise TimeoutException(f"元素未出现: {selector[:50]}")

            # 执行点击
            if parent:
                self.sb.click(selector, parent)
            else:
                self.sb.click(selector)

            # 短暂等待页面响应
            sleep(0.5)
            return True
        except Exception as e:
            print(f"点击失败: {str(e)[:100]}")
            raise

    @retry_on_failure(
        max_attempts=2, delay=1, exceptions=(StaleElementReferenceException,)
    )
    def safe_get_text(self, selector, parent=None):
        """安全获取文本"""
        if parent:
            return self.sb.get_text(selector, parent)
        return self.sb.get_text(selector)

    @retry_on_failure(max_attempts=2, delay=1)
    def safe_find_elements(self, selector):
        """安全查找元素列表"""
        if not self.waiter.wait_for_element(selector, timeout=8):
            print(f"⚠️  未找到元素列表: {selector[:50]}")
            return []
        return self.sb.find_elements(selector)

    def check_incomplete_item(self, items):
        """检查未完成的学习项目（目录一级别）"""
        for item in items:
            try:
                progress_text = self.safe_get_text(
                    "./div[2]/div/div/section[1]/div[1]/p[1]", item
                )
                current = int(progress_text[-3])
                total = int(progress_text[-1])
                if current < total:
                    return item
            except Exception as e:
                print(f"检查进度时出错: {e}")
                continue
        return None

    def check_incomplete_video(self, items):
        """检查未完成的视频项目（目录二级别）"""
        for item in items:
            try:
                progress_node = self.sb.find_element("./div[2]/div[2]/div[1]", item)
                num_element = self.sb.find_element(".num", progress_node)
                if self.safe_get_text(num_element) != "100.00%":
                    return item
            except Exception as e:
                print(f"检查视频进度时出错: {e}")
                continue
        return None

    def handle_popups(self):
        """处理各种弹窗 - 增强版"""
        handled = False

        # 尝试所有可能的弹窗选择器
        for selector in self.selectors["popup_buttons"]:
            try:
                if self.sb.is_element_visible(selector):
                    self.sb.click(selector)
                    print("✓ 已关闭弹窗")
                    handled = True
                    sleep(0.5)
            except Exception:
                continue

        # 处理播放按钮
        try:
            play_box = self.selectors["play_box"]
            if self.sb.is_element_visible(play_box):
                class_name = self.sb.get_attribute(play_box, "class")
                if "isShowBtn" in class_name:
                    self.sb.click(play_box)
                    print("✓ 已恢复播放")
                    handled = True
        except Exception:
            pass

        if not handled:
            print("未检测到需要处理的弹窗")

    def is_page_complete(self):
        """检查当前页面是否播放完毕"""
        try:
            # 先尝试关闭可能的弹窗
            self.handle_popups()

            # 等待播放列表加载
            if not self.waiter.wait_for_element(self.selectors["playlist"], timeout=5):
                print("⚠️  播放列表未加载")
                return False

            items = self.sb.find_elements(self.selectors["playlist"])

            if items:
                last_item_class = items[-1].get_attribute("class")
                if "success" in last_item_class:
                    print("✓ 当前页面视频已全部播放完毕！")
                    return True
            return False
        except Exception as e:
            print(f"检查播放状态时出错: {e}")
            return False

    def is_all_complete(self, items):
        """检查所有项目是否完成"""
        if not items:
            return False

        try:
            last_item = items[-1]
            progress_text = self.safe_get_text(
                "./div[2]/div/div/section[1]/div[1]/p[1]", last_item
            )
            current = int(progress_text[-3])
            total = int(progress_text[-1])
            return current >= total
        except Exception as e:
            print(f"检查完成状态时出错: {e}")
            return False

    def wait_for_video_complete(self, max_wait_time=1800):
        """等待视频播放完成（最多30分钟）"""
        print("正在播放视频，请稍候...")
        start_time = time()
        check_count = 0

        while time() - start_time < max_wait_time:
            check_count += 1

            # 每次检查前处理弹窗
            self.handle_popups()

            # 检查是否完成
            if self.is_page_complete():
                return True

            # 每30秒打印一次状态
            if check_count % 6 == 0:
                elapsed = int(time() - start_time)
                print(f"   已等待 {elapsed} 秒...")

            sleep(5)

        print("⚠️  视频播放超时")
        return False

    @retry_on_failure(max_attempts=2, delay=3)
    def play_videos(self):
        """主播放逻辑 - 增强版"""
        print("\n开始自动播放...")

        # 进入学习界面（目录一）
        print("📂 进入学习目录...")
        self.safe_click(self.selectors["entry_button"], wait_time=10)
        sleep(2)

        iteration = 0
        while True:
            iteration += 1
            print(f"\n🔄 第 {iteration} 轮检查...")

            # 获取一级目录列表
            level1_items = self.safe_find_elements(self.selectors["level1_items"])

            if not level1_items:
                print("❌ 未找到学习项目列表")
                break

            # 检查是否全部完成
            if self.is_all_complete(level1_items):
                print("\n" + "=" * 50)
                print("🎉 所有视频已经播放完毕！感谢使用！")
                print("📋 您可以在浏览器界面里查看进度是否完成")
                print("🔄 如未完成，请再次尝试运行程序！")
                print("💻 源代码请见GitHub：morethan987")
                print("=" * 50)
                break

            # 找到未完成的项目
            incomplete_item = self.check_incomplete_item(level1_items)

            if incomplete_item:
                print("📖 找到未完成的章节，进入...")

                # 进入二级目录
                next_button = "./div[2]/div/div/section[2]/div"
                self.safe_click(next_button, incomplete_item)
                sleep(2)

                # 切换到新窗口
                self.sb.switch_to_window(-1)
                sleep(1)

                # 获取二级目录列表
                level2_items = self.safe_find_elements(self.selectors["level2_items"])

                if not level2_items:
                    print("⚠️  二级目录为空，返回上一级")
                    self.sb.switch_to_window(-2)
                    continue

                incomplete_video = self.check_incomplete_video(level2_items)

                if incomplete_video:
                    print("🎬 开始播放视频...")

                    # 点击播放视频
                    play_button = "./div[2]/div[2]/div[2]"
                    self.safe_click(play_button, incomplete_video)
                    self.sb.switch_to_window(-1)
                    sleep(3)

                    # 等待视频播放完成
                    if self.wait_for_video_complete():
                        # 播放完成，返回上一级
                        self.handle_popups()
                        print("⬅️  返回上一级...")
                        self.safe_click(self.selectors["back_button"])
                        self.sb.switch_to_window(-1)
                        sleep(5)
                    else:
                        print("⚠️  视频未完成，继续下一个")

                # 返回一级目录
                print("⬅️  返回主目录...")
                self.safe_click(self.selectors["back_to_level1"])
                self.sb.switch_to_window(-1)
                sleep(5)
            else:
                print("⚠️  未找到未完成项目，可能已全部完成")
                break


def get_driver_path():
    """获取 EdgeDriver 的路径（兼容打包后的程序）"""
    if getattr(sys, "frozen", False):
        base_path = sys._MEIPASS  # type: ignore[attr-defined]
    else:
        base_path = os.path.dirname(os.path.abspath(__file__))

    return os.path.join(base_path, "edgedriver", "msedgedriver.exe")


def detect_available_browser():
    """检测系统上可用的浏览器"""
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


def main():
    """主函数"""
    print("=" * 60)
    print("🎓 欢迎来到'强国先锋'自动播放程序！(增强版)")
    print("=" * 60)
    print("\n📢 使用说明：")
    print("1. 目前程序只能手动登录")
    print("2. 浏览器窗口打开后会有40秒的时间来登录")
    print("3. 完成登录后请不要有多余的点击操作")
    print("4. 程序将自动播放所有未完成的视频")
    print("5. 新增：智能等待和自动重试机制\n")

    # 自动检测可用浏览器
    detected_browser = detect_available_browser()

    start = input("👉 是否开始运行？(yes/no): ").strip().lower()

    if start != "yes":
        print("程序已取消")
        return

    browser_config = {"browser": detected_browser} if detected_browser else {}

    # 使用 SeleniumBase 启动浏览器
    try:
        with SB(
            **browser_config,
            headed=True,
            undetectable=True,
            chromium_arg="proxy-server=127.0.0.1:7890",
        ) as sb:
            print("\n🌐 正在打开浏览器...")
            sb.open("https://cqu.qiangguoxianfeng.com/")

            print("⏳ 等待40秒供您登录，请尽快完成登录...")
            sleep(40)

            # 创建播放器实例并开始播放
            player = QiangGuoPlayer(sb)
            player.play_videos()

            print("\n✅ 程序执行完毕！浏览器将保持打开状态供您检查。")
            input("按回车键关闭程序...")
    except Exception as e:
        print(f"\n❌ 程序执行出错: {e}")
        print("💡 建议：检查网络连接或稍后重试")
        input("按回车键退出...")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  程序被用户中断")
    except Exception as e:
        print(f"\n❌ 程序出错: {e}")
        import traceback

        traceback.print_exc()
        input("按回车键退出...")
