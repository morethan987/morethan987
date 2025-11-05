package com.example.model.user;

import com.example.model.BaseModel;
import java.util.List;
import java.util.Map;

public class Admin extends BaseModel {

    private Map<String, List<String>> studentData;
    private Map<String, List<String>> teacherData;
    private Map<String, List<String>> adminData;

    public Admin() {
        studentData = super.readFile("data/admin.csv");
        teacherData = super.readFile("data/teacher.csv");
        adminData = super.readFile("data/admin.csv");
    }

    public Integer getIndxexById(String aid) {
        return adminData.get("aid").indexOf(aid);
    }

    public String getPasswordByIndex(Integer index) {
        return adminData.get("password").get(index);
    }
}
