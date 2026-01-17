package com.example.common.event;

import java.util.UUID;

public class CourseEnrollmentEvent extends BaseEvent {
    
    private UUID studentId;
    private UUID teachingClassId;
    private String studentCode;
    private String courseName;

    public CourseEnrollmentEvent() {
        super("COURSE_ENROLLED", "academic-service");
    }

    public CourseEnrollmentEvent(UUID studentId, UUID teachingClassId, String studentCode, String courseName) {
        this();
        this.studentId = studentId;
        this.teachingClassId = teachingClassId;
        this.studentCode = studentCode;
        this.courseName = courseName;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getTeachingClassId() {
        return teachingClassId;
    }

    public void setTeachingClassId(UUID teachingClassId) {
        this.teachingClassId = teachingClassId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
