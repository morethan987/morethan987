COURSE_SELECTION_URL = "https://my.cqu.edu.cn/enroll/CourseStuSelectionList"
USERNAME = "511402200508020013"
PASSWORD = "mo123456789"
INTERVAL = 10


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

    target_list = [{"name": "经济学原理", "id": "SSG00004", "teachers": ["文争为"]}]
    sidebar_flag = sidebar_inputbox = {"tag_name": "input", "type": "checkbox"}
    close_button_css = "div.drawer-close-wrap-right svg"
    data_raw_css = "div.ant-drawer-body tbody.ant-table-tbody tr.ant-table-row.ant-table-row-level-0"
