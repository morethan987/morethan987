package com.example.model.user;

import java.util.List;
import java.util.Map;

/**
 * Student class representing a student entity.
 * This class will read/write data from/to the 'student.txt' file.
 */
public class Student extends BaseUser {

    private Map<String, List<String>> dataMap;

    public Student() {
        dataMap = super.readFile("data/student.cvs");
    }

    public String addStudent(String sid, String password) {
        dataMap.get("sid").add(sid);
        dataMap.get("password").add(password);
        super.writeFile("data/student.cvs", dataMap);
        return "Student added successfully.";
    }

    public String getNameByid(String sid) {
        List<String> sids = dataMap.get("sid");
        int index = sids.indexOf(sid);
        if (index != -1) {
            return dataMap.get("name").get(index);
        }
        return null;
    }
}
