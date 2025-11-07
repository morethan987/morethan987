package com.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeachingClass extends BaseModel {

    private Map<String, List<String>> teachingClassData;
    private Map<String, List<String>> student_teachingClass; // 桥接表

    public TeachingClass() {
        teachingClassData = super.readFile("data/teaching_class.csv");
        student_teachingClass = super.readFile("data/stu_teachingclass.csv");
    }

    public String addTeachingClass(String cid, String tid, String term_idx) {
        UUID tcid = UUID.randomUUID();
        teachingClassData.get("tcid").add(tcid.toString());
        teachingClassData.get("cid").add(cid);
        teachingClassData.get("term_idx").add(term_idx);
        return "Teaching class added successfully.";
    }

    public Integer getStudentCount(String tcid) {
        List<String> tcidList = student_teachingClass.get("tcid");
        int count = 0;
        for (String id : tcidList) {
            if (id.equals(tcid)) {
                count++;
            }
        }
        return count;
    }

    public List<String> getTeachingClassIdsByStudentId(String sid) {
        List<String> tcids = new ArrayList<>();
        List<String> sidList = student_teachingClass.get("sid");
        List<String> tcidList = student_teachingClass.get("tcid");
        for (int i = 0; i < sidList.size(); i++) {
            if (sidList.get(i).equals(sid)) {
                tcids.add(tcidList.get(i));
            }
        }
        return tcids;
    }

    public String getCourseIdByTeachingClassId(String tcid) {
        List<String> tcidList = teachingClassData.get("tcid");
        List<String> cidList = teachingClassData.get("cid");
        for (int i = 0; i < tcidList.size(); i++) {
            if (tcidList.get(i).equals(tcid)) {
                return cidList.get(i);
            }
        }
        return null;
    }

    public String getClassNameById(String tcid) {
        List<String> tcidList = teachingClassData.get("tcid");
        List<String> classNameList = teachingClassData.get("name");
        for (int i = 0; i < tcidList.size(); i++) {
            if (tcidList.get(i).equals(tcid)) {
                return classNameList.get(i);
            }
        }
        return null;
    }

    public String getTeacherIdByTeachingClassId(String tcid) {
        List<String> tcidList = teachingClassData.get("tcid");
        List<String> tidList = teachingClassData.get("tid");
        for (int i = 0; i < tcidList.size(); i++) {
            if (tcidList.get(i).equals(tcid)) {
                return tidList.get(i);
            }
        }
        return null;
    }

    /**
     * 获取所有教学班信息
     * @return 包含所有教学班信息的Map
     */
    public Map<String, List<String>> getAllTeachingClasses() {
        return this.teachingClassData;
    }

    /**
     * 将学生添加进教学班（选课）
     * @param sid 学生ID
     * @param tcid 教学班ID
     * @return 如果添加成功返回 true, 否则返回 false
     */
    public boolean addStudentToClass(String sid, String tcid) {
        if (sid == null || tcid == null || sid.isEmpty() || tcid.isEmpty()) {
            return false;
        }
        try {
            // 向学生-教学班桥接表中添加新记录
            student_teachingClass.get("sid").add(sid);
            student_teachingClass.get("tcid").add(tcid);
            return true;
        } catch (Exception e) {
            System.err.println("添加学生到教学班失败: " + e.getMessage());
            return false;
        }
    }

    public boolean flush() {
        return (
            super.writeFile("data/teaching_class.csv", teachingClassData) &&
            super.writeFile("data/stu_teachingclass.csv", student_teachingClass)
        );
    }

    public boolean isTeachingClassExist(String tcid) {
        List<String> tcidList = teachingClassData.get("tcid");
        return tcidList.contains(tcid);
    }

    /**
     * 根据教学班ID获取学生ID列表
     * @param tcid 教学班ID
     * @return 包含该班所有学生ID的列表
     */
    public List<String> getStudentIdsByTeachingClassId(String tcid) {
        List<String> studentIds = new ArrayList<>();
        List<String> tcidList = student_teachingClass.get("tcid");
        List<String> sidList = student_teachingClass.get("sid");
        for (int i = 0; i < tcidList.size(); i++) {
            if (tcidList.get(i).equals(tcid)) {
                studentIds.add(sidList.get(i));
            }
        }
        return studentIds;
    }

    public Map<String, List<String>> getTeachingClassesByTeacherId(String tid) {
        Map<String, List<String>> result = new java.util.HashMap<>();
        List<String> tcidList = teachingClassData.get("tcid");
        List<String> cidList = teachingClassData.get("cid");
        List<String> termIdxList = teachingClassData.get("term_idx");
        List<String> nameList = teachingClassData.get("name");

        result.put("教学班号", new ArrayList<>());
        result.put("课程ID", new ArrayList<>());
        result.put("开课学期", new ArrayList<>());
        result.put("教学班名称", new ArrayList<>());

        for (int i = 0; i < tcidList.size(); i++) {
            if (teachingClassData.get("tid").get(i).equals(tid)) {
                result.get("教学班号").add(tcidList.get(i));
                result.get("课程ID").add(cidList.get(i));
                result.get("开课学期").add(termIdxList.get(i));
                result.get("教学班名称").add(nameList.get(i));
            }
        }
        return result;
    }
}
