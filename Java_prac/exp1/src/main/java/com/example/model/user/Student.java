package com.example.model.user;

/**
 * Student class representing a student entity.
 * This class will read/write data from/to the 'student.txt' file.
 */
public class Student extends BaseUser {

    public String getName(String sid) {
        return "Student Name for ID: " + sid;
    }
}
