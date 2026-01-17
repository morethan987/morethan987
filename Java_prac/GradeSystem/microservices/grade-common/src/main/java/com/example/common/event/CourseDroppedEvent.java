package com.example.common.event;

import java.util.UUID;

public class CourseDroppedEvent extends BaseEvent {
    
    private UUID studentId;
    private UUID teachingClassId;
    private String studentCode;

    public CourseDroppedEvent() {
        super("COURSE_DROPPED", "academic-service");
    }

    public CourseDroppedEvent(UUID studentId, UUID teachingClassId, String studentCode) {
        this();
        this.studentId = studentId;
        this.teachingClassId = teachingClassId;
        this.studentCode = studentCode;
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
}
