package com.example.model.user;

import com.example.model.BaseModel;
import java.util.HashMap;
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

    public boolean flush() {
        return super.writeFile("data/admin.csv", adminData);
    }

    public Map<String, String> getPersonalInfoById(String id) {
        Map<String, String> res = new HashMap<>();
        Integer index = getIndxexById(id);
        res.put("学号", adminData.get("aid").get(index));
        res.put("姓名", adminData.get("name").get(index));
        res.put("性别", adminData.get("gender").get(index));
        res.put("年龄", adminData.get("age").get(index));
        return res;
    }
}
