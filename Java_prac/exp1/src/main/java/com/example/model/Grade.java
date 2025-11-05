package com.example.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Grade extends BaseModel {

    private Map<String, List<String>> gradeData;

    public Grade() {
        gradeData = super.readFile("data/grade.cvs");
    }

    public String addGrade(String sid, String cid) {
        UUID gid = UUID.randomUUID();
        gradeData.get("gid").add(gid.toString());
        gradeData.get("sid").add(sid);
        gradeData.get("cid").add(cid);
        return "Grade added successfully.";
    }
}
