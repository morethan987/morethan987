import asyncio

from config import (
    COURSE_SELECTION_URL,
    INTERVAL,
    PASSWORD,
    USERNAME,
    CourseSelectors,
    LoginSelectors,
    TargetCourses,
)
from pydoll.browser.chromium import Chrome
from pydoll.browser.options import ChromiumOptions
from pydoll.browser.tab import Tab
from pydoll.decorators import retry
from pydoll.elements.web_element import WebElement
from pydoll.exceptions import NetworkError, PageLoadTimeout, WaitElementTimeout


async def login(tab: Tab):
    await tab.go_to(COURSE_SELECTION_URL, timeout=50)
    print("页面加载完成，开始执行脚本...")

    # 登录页面元素定位
    username_input = await tab.find(
        tag_name=LoginSelectors.username_input["tag_name"],
        name=LoginSelectors.username_input["name"],
        timeout=10,
    )
    password_input = await tab.find(
        tag_name=LoginSelectors.password_input["tag_name"],
        type=LoginSelectors.password_input["type"],
        timeout=10,
    )
    login_button = await tab.find(
        tag_name=LoginSelectors.login_button["tag_name"],
        type=LoginSelectors.login_button["type"],
        class_name=LoginSelectors.login_button["class_name"],
        timeout=10,
    )
    print("登录页面元素定位完成，准备输入用户名和密码...")
    await asyncio.sleep(3)

    # 输入用户名和密码并点击登录按钮
    await username_input.type_text(USERNAME, interval=0.05)
    await password_input.type_text(PASSWORD, interval=0.05)
    await login_button.click()
    print("登录信息提交，等待跳转到选课页面...")


async def wait_for_course_page(tab: Tab):
    await tab.find(
        tag_name=CourseSelectors.flag["tag_name"],
        class_name=CourseSelectors.flag["class_name"],
        timeout=20,
    )
    print("选课页面加载完成，开始查找课程数据...")


async def find_target_courses(tab: Tab) -> list[WebElement]:
    course_list = await tab.find(
        tag_name=CourseSelectors.data_raw["tag_name"],
        class_name=CourseSelectors.data_raw["class_name"],
        find_all=True,
        timeout=10,
    )

    all_courses = []
    for course in course_list:
        link = await course.find(tag_name="a")
        id = await course.find(tag_name="div")
        all_courses.append((link, id))

    # 定位目标课程
    target_courses_links = []
    for link, id in all_courses:
        course_name = link.get_attribute("title")
        for target in TargetCourses.target_list:
            if course_name == target["name"] and await id.text == target["id"]:
                target_courses_links.append(link)

    print("设定的课程数量：", len(TargetCourses.target_list))
    print("找到的课程数量：", len(target_courses_links))
    return target_courses_links


async def select_course(tab: Tab, course_link: WebElement):
    await course_link.click()
    # 检测侧边栏加载
    await tab.find(
        tag_name=TargetCourses.sidebar_flag["tag_name"],
        type=TargetCourses.sidebar_flag["type"],
        timeout=10,
    )
    close_button = await tab.query(TargetCourses.close_button_css, timeout=10)
    print("侧边栏加载完成，开始提取教师信息...")

    raws = await tab.query(TargetCourses.data_raw_css, find_all=True, timeout=10)
    print(f"提取到{len(raws)}名教师，开始匹配教师姓名...")
    for raw in raws:
        cells = await raw.find(tag_name="td", find_all=True, timeout=10)
        teacher_name = await cells[3].text
        print(f"检测到教师：{teacher_name}，开始与目标教师匹配...")
        for target in TargetCourses.target_list:
            if teacher_name in target["teachers"]:
                print(
                    f"找到目标课程：{target['name']}，教师：{teacher_name}，开始选课..."
                )
                input_box = await raw.find(
                    tag_name=TargetCourses.sidebar_inputbox["tag_name"],
                    type=TargetCourses.sidebar_inputbox["type"],
                    timeout=10,
                )
                await input_box.click()
    await close_button.click()
    print("侧边栏已关闭，继续下一个课程...")


@retry(max_retries=5, exceptions=[WaitElementTimeout, NetworkError, PageLoadTimeout])
async def main():
    options = ChromiumOptions()
    # 禁用不必要的功能
    options.add_argument("--disable-extensions")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-background-networking")
    options.add_argument("--disable-sync")
    options.add_argument("--disable-translate")
    options.add_argument("--headless=new")
    options.add_argument("--disable-notifications")
    options.add_argument("--proxy-server=127.0.0.1:7890")
    # 禁用图像以实现更快的加载
    options.add_argument("--blink-settings=imagesEnabled=false")
    # 网络优化
    options.add_argument("--disable-features=NetworkPrediction")
    options.add_argument("--dns-prefetch-disable")

    async with Chrome(options=options) as browser:
        # 启动浏览器并打开新标签页
        tab = await browser.start()

        # 登录选课系统
        await login(tab)

        while True:
            # 选课界面定位数据元素
            await wait_for_course_page(tab)
            target_courses_links = await find_target_courses(tab)

            # 循环找到的课程
            for target_course_link in target_courses_links:
                await select_course(tab, target_course_link)

            await asyncio.sleep(INTERVAL)
            await tab.refresh()


if __name__ == "__main__":
    asyncio.run(main())
