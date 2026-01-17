package com.example.academic.domain;

public enum TeachingClassStatus {
    PLANNED("planned"),
    OPEN_FOR_ENROLLMENT("open_for_enrollment"),
    ENROLLMENT_CLOSED("enrollment_closed"),
    ACTIVE("active"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    SUSPENDED("suspended"),
    MERGED("merged");

    private final String description;

    TeachingClassStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canEnroll() {
        return this == OPEN_FOR_ENROLLMENT;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == MERGED;
    }

    public boolean canInputGrades() {
        return this == ACTIVE || this == COMPLETED;
    }

    public boolean canModify() {
        return this == PLANNED || this == OPEN_FOR_ENROLLMENT || this == ENROLLMENT_CLOSED;
    }

    public boolean canWithdraw() {
        return this == OPEN_FOR_ENROLLMENT || this == ENROLLMENT_CLOSED;
    }

    @Override
    public String toString() {
        return description;
    }
}
