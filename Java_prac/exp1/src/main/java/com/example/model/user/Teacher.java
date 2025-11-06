package com.example.model.user;

import com.example.model.BaseModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Teacher class representing a teacher entity.
 * This class will read/write data from/to the 'teacher.txt' file.
 */
public class Teacher extends BaseModel {

    private Map<String, List<String>> teacherData;

    public Teacher() {
        teacherData = super.readFile("data/teacher.csv");
    }

    public String addTeacher(String tid, String password) {
        teacherData.get("tid").add(tid);
        teacherData.get("password").add(password);
        return "Teacher added successfully.";
    }

    public boolean flush() {
        return super.writeFile("data/teacher.csv", teacherData);
    }

    public Integer getIndxexById(String tid) {
        return teacherData.get("tid").indexOf(tid);
    }

    public String getPasswordByIndex(Integer index) {
        return teacherData.get("password").get(index);
    }

    public Map<String, String> getPersonalInfoById(String id) {
        Map<String, String> res = new HashMap<>();
        Integer index = getIndxexById(id);
        res.put("编号", teacherData.get("tid").get(index));
        res.put("姓名", teacherData.get("name").get(index));
        res.put("性别", teacherData.get("gender").get(index));
        res.put("年龄", teacherData.get("age").get(index));
        return res;
    }

    public String getTeacherNameById(String tid) {
        Integer index = getIndxexById(tid);
        if (index == -1) {
            return null;
        }
        return teacherData.get("name").get(index);
    }

    public String[] updateInfo(String sid, Map<String, String> updates) {
        String[] res = new String[2];
        int index = teacherData.get("tid").indexOf(sid);
        if (index == -1) {
            res[0] = "false";
            res[1] = "Teacher ID not found.";
            return res;
        }
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (teacherData.containsKey(key)) {
                // 空字符串表示不更新
                if (!value.isEmpty()) {
                    teacherData.get(key).set(index, value);
                }
            }
        }
        res[0] = "true";
        res[1] = "Teacher information updated successfully.";
        return res;
    }
}
