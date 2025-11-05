package com.example.model.user;

import java.util.List;
import java.util.Map;

/**
 * Teacher class representing a teacher entity.
 * This class will read/write data from/to the 'teacher.txt' file.
 */
public class Teacher extends BaseUser {

    private Map<String, List<String>> dataMap;

    public Teacher() {
        dataMap = super.readFile("data/teacher.cvs");
    }
}
