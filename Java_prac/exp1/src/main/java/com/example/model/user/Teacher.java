package com.example.model.user;

import com.example.model.BaseModel;
import java.util.List;
import java.util.Map;

/**
 * Teacher class representing a teacher entity.
 * This class will read/write data from/to the 'teacher.txt' file.
 */
public class Teacher extends BaseModel {

    private Map<String, List<String>> teacherData;

    public Teacher() {
        teacherData = super.readFile("data/teacher.cvs");
    }

    public String addTeacher(String tid, String password) {
        teacherData.get("tid").add(tid);
        teacherData.get("password").add(password);
        return "Teacher added successfully.";
    }

    public String flush() {
        super.writeFile("data/teacher.cvs", teacherData);
        return "Data flushed successfully.";
    }
}
