package com.example.academic.dto;

import java.util.UUID;

public class EnrollmentRequestDTO {

    private UUID studentId;
    private UUID teachingClassId;

    public EnrollmentRequestDTO() {}

    public EnrollmentRequestDTO(UUID studentId, UUID teachingClassId) {
        this.studentId = studentId;
        this.teachingClassId = teachingClassId;
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
}
