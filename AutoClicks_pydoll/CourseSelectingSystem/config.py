from pydoll.browser.options import ChromiumOptions

USERNAME = "your_username"
PASSWORD = "your_password"
INTERVAL = 10
MAX_RETRY = 1000
SERVER_KEY = None  # 填写你的Server酱SCKEY以启用微信通知功能
COURSE_SELECTION_URL = "https://my.cqu.edu.cn/enroll/CourseStuSelectionList"


def get_chromium_options() -> ChromiumOptions:
    """获取 Chromium 浏览器选项"""
    options = ChromiumOptions()
    # 禁用不必要的功能
    options.add_argument("--disable-extensions")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-background-networking")
    options.add_argument("--disable-sync")
    options.add_argument("--disable-translate")
    # options.add_argument("--headless=new")
    options.add_argument("--disable-notifications")
    # 禁用图像以实现更快的加载
    options.add_argument("--blink-settings=imagesEnabled=false")
    # 网络优化
    options.add_argument("--disable-features=NetworkPrediction")
    options.add_argument("--dns-prefetch-disable")
    # 代理配置（可选）
    options.add_argument("--proxy-server=127.0.0.1:7890")
    # root 用户需要添加的参数
    # options.add_argument("--no-sandbox")

    return options


class TargetCourses:
    """目标课程关键词"""

    target_list = [
        {"name": "边缘计算", "id": "CST31220", "teachers": ["汪成亮"]},
    ]


# 元素选择器
class LoginSelectors:
    """登录页面元素选择器"""

    username_input = {"tag_name": "input", "name": "username"}
    password_input = {"tag_name": "input", "type": "password"}
    login_button = {
        "tag_name": "button",
        "type": "submit",
        "class_name": "login-button ant-btn",
    }


class CourseSelectors:
    """选课页面元素选择器"""

    # 界面加载完成标志元素
    flag = {
        "tag_name": "span",
        "class_name": "ant-table-column-title",
    }
    data_raw = {"tag_name": "tr", "class_name": "ant-table-row ant-table-row-level-0"}


class SidebarSelectors:
    """侧边栏元素选择器"""

    sidebar_flag_css = "div.ant-drawer-body tbody.ant-table-tbody"
    sidebar_inputbox = {"tag_name": "input", "type": "checkbox"}
    close_button_css = "div.drawer-close-wrap-right svg"
    data_raw_css = "div.ant-drawer-body tbody.ant-table-tbody tr.ant-table-row.ant-table-row-level-0"


class SelectionSelectors:
    """选课确认元素选择器"""

    select_button = {"type": "button", "class_name": "ant-btn ant-btn-primary"}
    select_button_css = "div.ant-drawer-body button"
    confirm_button_css = ".ant-modal button.ant-btn.ant-btn-primary"
    forbidden_flag = {"tag_name": "div", "class_name": "ant-alert ant-alert-error"}
