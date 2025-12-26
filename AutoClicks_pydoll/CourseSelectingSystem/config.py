COURSE_SELECTION_URL = "https://my.cqu.edu.cn/enroll/CourseStuSelectionList"
USERNAME = "your_username"
PASSWORD = "your_password"
INTERVAL = 10
SERVER_KEY = "your_server_key_here"


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


class TargetCourses:
    """目标课程关键词"""

    target_list = [
        {"name": "边缘计算", "id": "CST31220", "teachers": ["汪成亮"]},
    ]


class SidebarSelectors:
    """侧边栏元素选择器"""

    sidebar_flag_css = "div.ant-drawer-body tbody.ant-table-tbody"
    sidebar_inputbox = {"tag_name": "input", "type": "checkbox"}
    close_button_css = "div.drawer-close-wrap-right svg"
    data_raw_css = "div.ant-drawer-body tbody.ant-table-tbody tr.ant-table-row.ant-table-row-level-0"


class SelectionSelectors:
    """选课确认元素选择器"""

    select_button_css = "div.ant-drawer-body button.ant-btn-primary"
    confirm_button_css = ".ant-modal button.ant-btn-primary"
    forbidden_flag = {"tag_name": "div", "class_name": "ant-alert ant-alert-error"}
