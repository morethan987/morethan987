package com.example.controller;

import com.example.model.Course;
import com.example.model.Grade;
import com.example.model.TeachingClass;
import com.example.model.user.Student;
import com.example.model.user.Teacher;
import com.example.view.BaseView;
import com.example.view.StudentView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentController extends BaseUserController {

    private final Student studentData = new Student();
    private final Teacher teacherData = new Teacher();
    private final Grade gradeData = new Grade();
    private final Course courseData = new Course();
    private final TeachingClass teachingClassData = new TeachingClass();

    public StudentController(
        String userId,
        String token,
        AuthController authController
    ) {
        super(userId, token, authController);
    }

    @Override
    protected Map<String, String> initCodeMap() {
        return Map.of(
            "show_init",
            "显示主菜单",
            "clear_screen",
            "清屏",
            "show_personal_info",
            "查看个人信息",
            "update_personal_info",
            "修改个人信息",
            "view_grades",
            "查看成绩",
            "view_course",
            "查看已选课程",
            "select_course",
            "选课",
            "exit",
            "退出"
        );
    }

    @Override
    protected boolean flushData() {
        return (
            studentData.flush() &&
            gradeData.flush() &&
            teachingClassData.flush() &&
            courseData.flush()
        );
    }

    @Override
    protected BaseView createUserView() {
        return new StudentView(codeMap);
    }

    @Override
    protected void showPersonalInfo() {
        userView.showMessage("=== 学生个人信息 ===");
        Map<String, String> info = studentData.getPersonalInfoById(userId);
        userView.showPersonalInfo(info);
    }

    @Override
    protected void updatePersonalInfo() {
        // 显示当前个人信息
        showPersonalInfo();

        // 获取用户输入
        Map<String, String> updates = new HashMap<>();
        String newName = userView.readInput("请输入新的姓名（留空则不修改）: ");
        updates.put("name", newName);
        String newGender = userView.readInput(
            "请输入新的性别（留空则不修改）: "
        );
        updates.put("gender", newGender);
        String newAge = userView.readInput("请输入新的年龄（留空则不修改）: ");
        updates.put("age", newAge);
        String newPassword = userView.readInput(
            "请输入新的密码（留空则不修改）: "
        );
        updates.put("password", newPassword);

        String[] res = studentData.updateInfo(userId, updates);

        if (res[0].equals("false")) {
            userView.showMessage("个人信息修改失败: " + res[1]);
            return;
        }
        userView.showMessage("个人信息修改成功！");
    }

    @Override
    protected void handleCustomOperation(String operationCode) {
        switch (operationCode) {
            case "view_grades":
                viewGrades();
                break;
            case "view_course":
                viewSelectedCourses();
                break;
            case "select_course":
                selectCourse();
                break;
            default:
                userView.showMessage("无效的操作码: " + operationCode);
                break;
        }
    }

    /**
     * 查看成绩
     */
    private void viewGrades() {
        userView.showMessage("=== 成绩查询 ===");
        Map<String, List<String>> grades = gradeData.getGradesByStudentId(
            userId
        );

        if (grades.isEmpty() || grades.get("课程ID").isEmpty()) {
            userView.showMessage("未查询到您的成绩信息。");
        } else {
            // 直接替换课程ID列为课程名称
            List<String> courseNames = new ArrayList<>();
            for (String courseId : grades.get("课程ID")) {
                courseNames.add(courseData.getCourseNameById(courseId));
            }
            grades.put("课程名称", courseNames);
            grades.remove("课程ID");

            // 创建顺序列表
            int numCourses = grades.get("课程名称").size();
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < numCourses; i++) {
                order.add(i);
            }

            userView.showSortedData(grades, order);
        }
    }

    /**
     * 查看已选课程
     * 显示内容： 课程名称/教学班名称/教师姓名
     */
    private void viewSelectedCourses() {
        userView.showMessage("=== 已选课程查询 ===");
        Map<String, List<String>> courses = new HashMap<>();
        courses.put("课程名称", new ArrayList<>());
        courses.put("教学班名称", new ArrayList<>());
        courses.put("教师姓名", new ArrayList<>());
        List<String> tcids = teachingClassData.getTeachingClassIdsByStudentId(
            userId
        );
        for (String tcid : tcids) {
            String cid = teachingClassData.getCourseIdByTeachingClassId(tcid);
            String courseName = courseData.getCourseNameById(cid);
            String className = teachingClassData.getClassNameById(tcid);
            String tid = teachingClassData.getTeacherIdByTeachingClassId(tcid);
            String teacherName = teacherData.getTeacherNameById(tid);

            courses.get("课程名称").add(courseName);
            courses.get("教学班名称").add(className);
            courses.get("教师姓名").add(teacherName);
        }

        // 创建顺序列表
        int numCourses = courses.get("课程名称").size();
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            order.add(i);
        }
        userView.showSortedData(courses, order);
    }

    /**
     * 选课
     */
    private void selectCourse() {
        userView.showMessage("=== 选课系统 ===");

        // 1. 获取所有开设的教学班
        Map<String, List<String>> allTeachingClasses =
            teachingClassData.getAllTeachingClasses();
        if (allTeachingClasses.get("tcid").isEmpty()) {
            userView.showMessage("当前没有可供选择的课程。");
            return;
        }

        // 2. 获取学生已选的教学班ID，用于过滤
        List<String> selectedTcIds =
            teachingClassData.getTeachingClassIdsByStudentId(userId);

        // 3. 准备并筛选可显示的课程列表
        Map<String, List<String>> availableCourses = new HashMap<>();
        availableCourses.put("课程名称", new ArrayList<>());
        availableCourses.put("教学班名称", new ArrayList<>());
        availableCourses.put("教师姓名", new ArrayList<>());
        // availableCourses.put("已选人数", new ArrayList<>()); // (可选)

        // 用于将用户输入的选择序号映射回教学班ID
        List<String> availableTcIdList = new ArrayList<>();

        List<String> allTcIds = allTeachingClasses.get("tcid");
        for (String tcid : allTcIds) {
            // 如果学生已经选过此课程，则跳过
            if (selectedTcIds.contains(tcid)) {
                continue;
            }

            // 将这个可选的教学班ID添加到列表中
            availableTcIdList.add(tcid);

            // 获取课程、教师等详细信息用于显示
            String cid = teachingClassData.getCourseIdByTeachingClassId(tcid);
            String courseName = courseData.getCourseNameById(cid);
            String className = teachingClassData.getClassNameById(tcid);
            String tid = teachingClassData.getTeacherIdByTeachingClassId(tcid);
            String teacherName = teacherData.getTeacherNameById(tid);
            // Integer studentCount = teachingClassData.getStudentCount(tcid); // (可选)

            availableCourses.get("课程名称").add(courseName);
            availableCourses.get("教学班名称").add(className);
            availableCourses.get("教师姓名").add(teacherName);
            // availableCourses.get("已选人数").add(studentCount.toString()); // (可选)
        }

        // 如果没有新课程可选
        if (availableTcIdList.isEmpty()) {
            userView.showMessage("没有新的课程可供选择。");
            return;
        }

        // 4. 显示可选课程列表
        userView.showMessage("--- 可选课程列表 ---");
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < availableTcIdList.size(); i++) {
            order.add(i);
        }
        userView.showSortedData(availableCourses, order);

        // 5. 获取用户输入
        try {
            String choiceInput = userView.readInput(
                "请输入要选择的课程对应的行号（从1开始），或输入 'q' 退出: "
            );
            if (choiceInput.equalsIgnoreCase("q")) {
                userView.showMessage("已退出选课。");
                return;
            }

            int choiceIndex = Integer.parseInt(choiceInput) - 1;

            // 6. 处理用户选择
            if (choiceIndex >= 0 && choiceIndex < availableTcIdList.size()) {
                String tcidToSelect = availableTcIdList.get(choiceIndex);
                boolean success = teachingClassData.addStudentToClass(
                    userId,
                    tcidToSelect
                );

                if (success) {
                    userView.showMessage("选课成功！");
                } else {
                    userView.showMessage("选课失败，请重试。");
                }
            } else {
                userView.showMessage("无效的选择，请输入列表中的有效行号。");
            }
        } catch (NumberFormatException e) {
            userView.showMessage("输入无效，请输入数字行号。");
        }
    }
}
