package com.example.academic.domain;

public enum EnrollmentStatus {
    ENROLLED("已选课"),
    DROPPED("已退课"),
    COMPLETED("已完成"),
    FAILED("不通过"),
    PENDING("待审核");

    private final String description;

    EnrollmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
