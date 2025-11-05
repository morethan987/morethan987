package com.example.model.user;

import java.util.List;
import java.util.Map;

public class Admin extends BaseUser {

    private Map<String, List<String>> dataMap;

    public Admin() {
        dataMap = super.readFile("data/admin.cvs");
    }
}
