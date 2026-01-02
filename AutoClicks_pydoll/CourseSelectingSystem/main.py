import asyncio

from .config import (
    COURSE_SELECTION_URL,
    INTERVAL,
    MAX_RETRY,
    PASSWORD,
    SERVER_KEY,
    USERNAME,
    CourseSelectors,
    LoginSelectors,
    SelectionSelectors,
    SidebarSelectors,
    TargetCourses,
    get_chromium_options,
)
from .notification import sc_send
from pydoll.browser.chromium import Chrome
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
        timeout=40,
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
    await tab.query(
        SidebarSelectors.sidebar_flag_css,
        timeout=10,
    )
    close_button = await tab.query(SidebarSelectors.close_button_css, timeout=10)
    print("侧边栏加载完成，开始提取教师信息...")

    rows = await tab.query(SidebarSelectors.data_raw_css, find_all=True, timeout=10)
    print(f"提取到{len(rows)}名教师，开始匹配教师姓名...")

    select_flag = False  # 标记是否成功选课
    for row in rows:
        cells = await row.find(tag_name="td", find_all=True, timeout=10)
        teacher_name = await cells[3].text
        print(f"检测到教师：{teacher_name}，开始与目标教师匹配...")
        for target in TargetCourses.target_list:
            if teacher_name in target["teachers"]:
                print(
                    f"找到目标课程：{target['name']}，教师：{teacher_name}，开始选课..."
                )

                # 检查课程是否可选
                if await _is_available(row):
                    # 先定位复选框并点击
                    input_box = await row.find(
                        tag_name=SidebarSelectors.sidebar_inputbox["tag_name"],
                        type=SidebarSelectors.sidebar_inputbox["type"],
                        timeout=10,
                    )
                    await input_box.click()

                    # 确认选课
                    await confirm_selection(tab)
                    select_flag = True
                else:
                    print("课程已满或已选，无法选择该课程。")

    # 如果选课成功，侧边栏会自动关闭，无需点击关闭按钮
    if not select_flag:
        await close_button.click()
    print("侧边栏已关闭，继续下一个课程...")


async def _is_available(row: WebElement) -> bool:
    full = await row.find(tag_name="span", class_name="text-error", raise_exc=False)
    already_selected = await row.find(
        tag_name="span", class_name="text-success", raise_exc=False
    )
    if full or already_selected:
        print("检测到课程已满或已选")
        return False
    return True


async def confirm_selection(tab: Tab):
    select_button = await tab.query(
        SelectionSelectors.select_button_css, timeout=5, raise_exc=False
    )
    if not select_button:
        print("未找到选课按钮，目前可能不是选课时间段")
        return
    await select_button.click()
    print("选课按钮已点击，等待确认对话框...")

    confirm_button = await tab.query(
        SelectionSelectors.confirm_button_css, timeout=5, raise_exc=False
    )
    if not confirm_button:
        print("未找到确认按钮，选课可能未成功")
        return
    await confirm_button.click()
    print("确认按钮已点击，选课成功...")

    # 发送通知
    if SERVER_KEY:
        result = sc_send(SERVER_KEY, "选课成功通知", "成功选择了一门课程！")
        print("已发送选课成功通知，返回结果：", result)


@retry(
    max_retries=MAX_RETRY,
    exceptions=[WaitElementTimeout, NetworkError, PageLoadTimeout],
)
async def main():
    options = get_chromium_options()

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
