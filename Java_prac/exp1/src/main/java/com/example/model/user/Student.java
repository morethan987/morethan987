package com.example.model.user;

import com.example.model.BaseModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student class representing a student entity.
 * This class will read/write data from/to the 'student.txt' file.
 */
public class Student extends BaseModel {

    private Map<String, List<String>> studentData;

    public Student() {
        studentData = super.readFile("data/student.csv");
    }

    public String addStudent(String sid, String password) {
        studentData.get("sid").add(sid);
        studentData.get("password").add(password);
        return "Student added successfully.";
    }

    public boolean flush() {
        return super.writeFile("data/student.csv", studentData);
    }

    public String updateStudent(String sid, Map<String, String> updates) {
        int index = studentData.get("sid").indexOf(sid);
        if (index == -1) {
            return "Student not found.";
        }
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (studentData.containsKey(key)) {
                studentData.get(key).set(index, value);
            }
        }
        return "Student updated successfully.";
    }

    public Integer getIndxexById(String sid) {
        return studentData.get("sid").indexOf(sid);
    }

    public String getPasswordByIndex(Integer index) {
        return studentData.get("password").get(index);
    }

    public Map<String, String> getPersonalInfoById(String id) {
        Map<String, String> res = new HashMap<>();
        Integer index = getIndxexById(id);
        res.put("学号", studentData.get("sid").get(index));
        res.put("姓名", studentData.get("name").get(index));
        res.put("性别", studentData.get("gender").get(index));
        res.put("年龄", studentData.get("age").get(index));
        return res;
    }
}
