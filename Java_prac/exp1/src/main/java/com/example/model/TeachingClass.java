package com.example.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeachingClass extends BaseModel {

    private Map<String, List<String>> teachingClassData;

    public TeachingClass() {
        teachingClassData = super.readFile("data/teaching_class.cvs");
    }

    public String addTeachingClass(String cid, String tid, String term_idx) {
        UUID tcid = UUID.randomUUID();
        teachingClassData.get("tcid").add(tcid.toString());
        teachingClassData.get("cid").add(cid);
        teachingClassData.get("term_idx").add(term_idx);
        return "Teaching class added successfully.";
    }

    public String flush() {
        super.writeFile("data/teaching_class.cvs", teachingClassData);
        return "Data flushed successfully.";
    }
}
