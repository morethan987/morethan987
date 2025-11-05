package com.example.model.user;

import com.example.model.BaseModel;
import java.util.List;
import java.util.Map;

/**
 * Student class representing a student entity.
 * This class will read/write data from/to the 'student.txt' file.
 */
public class Student extends BaseModel {

    private Map<String, List<String>> studentData;

    public Student() {
        studentData = super.readFile("data/student.cvs");
    }

    public String addStudent(String sid, String password) {
        studentData.get("sid").add(sid);
        studentData.get("password").add(password);
        return "Student added successfully.";
    }

    public String flush() {
        super.writeFile("data/student.cvs", studentData);
        return "Data flushed successfully.";
    }

    public String getNameByid(String sid) {
        List<String> sids = studentData.get("sid");
        int index = sids.indexOf(sid);
        if (index != -1) {
            return studentData.get("name").get(index);
        }
        return null;
    }
}
