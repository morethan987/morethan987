package com.example.model.user;

import com.example.model.BaseModel;
import java.util.List;
import java.util.Map;

public class Admin extends BaseModel {

    private Map<String, List<String>> studentDataMap;
    private Map<String, List<String>> teacherDataMap;
    private Map<String, List<String>> adminDataMap;

    public Admin() {
        studentDataMap = super.readFile("data/admin.cvs");
        teacherDataMap = super.readFile("data/teacher.cvs");
        adminDataMap = super.readFile("data/admin.cvs");
    }
}
