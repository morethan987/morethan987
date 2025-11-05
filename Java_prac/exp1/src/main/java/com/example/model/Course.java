package com.example.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Course extends BaseModel {

    private Map<String, List<String>> courseData;

    public Course() {
        this.courseData = super.readFile("data/course.cvs");
    }

    public String getCourseNameById(String courseId) {
        List<String> courseIds = courseData.get("courseId");
        int index = courseIds.indexOf(courseId);
        if (index != -1) {
            return courseData.get("courseName").get(index);
        }
        return null;
    }

    public String addCourse(String courseName) {
        UUID cid = UUID.randomUUID();
        courseData.get("cid").add(cid.toString());
        courseData.get("name").add(courseName);
        return "Course added successfully.";
    }

    public String flush() {
        super.writeFile("data/course.cvs", courseData);
        return "Course data flushed successfully.";
    }
}
