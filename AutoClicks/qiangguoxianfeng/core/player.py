"""强国先锋自动播放器核心模块"""

from time import sleep, time

from config import SELECTORS
from selenium.common.exceptions import (
    StaleElementReferenceException,
    TimeoutException,
)
from utils import SmartWaiter, retry_on_failure


class QiangGuoPlayer:
    """强国先锋自动播放器 - 增强版"""

    def __init__(self, sb):
        """初始化播放器

        Args:
            sb: SeleniumBase 实例
        """
        self.sb = sb
        self.waiter = SmartWaiter(sb)
        self.selectors = SELECTORS

    @retry_on_failure(max_attempts=3, delay=1)
    def safe_click(self, selector, parent=None, wait_time=5):
        """安全的点击操作，带等待和重试

        Args:
            selector: 元素选择器
            parent: 父元素（可选）
            wait_time: 等待时间（秒）

        Returns:
            bool: 点击是否成功
        """
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
        """安全获取文本

        Args:
            selector: 元素选择器
            parent: 父元素（可选）

        Returns:
            str: 元素文本内容
        """
        if parent:
            return self.sb.get_text(selector, parent)
        return self.sb.get_text(selector)

    @retry_on_failure(max_attempts=2, delay=1)
    def safe_find_elements(self, selector):
        """安全查找元素列表

        Args:
            selector: 元素选择器

        Returns:
            list: 元素列表
        """
        if not self.waiter.wait_for_element(selector, timeout=8):
            print(f"⚠️  未找到元素列表: {selector[:50]}")
            return []
        return self.sb.find_elements(selector)

    def check_incomplete_item(self, items):
        """检查未完成的学习项目（目录一级别）

        Args:
            items: 项目元素列表

        Returns:
            element: 第一个未完成的项目，或 None
        """
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
        """检查未完成的视频项目（目录二级别）

        Args:
            items: 视频元素列表

        Returns:
            element: 第一个未完成的视频，或 None
        """
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
        """检查当前页面是否播放完毕

        Returns:
            bool: 页面是否完成
        """
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
        """检查所有项目是否完成

        Args:
            items: 项目元素列表

        Returns:
            bool: 是否全部完成
        """
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
        """等待视频播放完成（最多30分钟）

        Args:
            max_wait_time: 最大等待时间（秒）

        Returns:
            bool: 视频是否完成播放
        """
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
