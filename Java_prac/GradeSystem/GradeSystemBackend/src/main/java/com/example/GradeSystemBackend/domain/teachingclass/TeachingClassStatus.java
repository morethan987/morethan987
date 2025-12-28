package com.example.GradeSystemBackend.domain.teachingclass;

/**
 * 教学班状态枚举
 * 定义教学班在系统中的各种状态
 */
public enum TeachingClassStatus {
    /**
     * 计划中 - 教学班已创建但尚未开始
     */
    PLANNED("计划中"),

    /**
     * 开放选课 - 学生可以选择此教学班
     */
    OPEN_FOR_ENROLLMENT("开放选课"),

    /**
     * 选课结束 - 停止选课，准备开课
     */
    ENROLLMENT_CLOSED("选课结束"),

    /**
     * 进行中 - 教学班正在进行中
     */
    ACTIVE("进行中"),

    /**
     * 已完成 - 教学班已结束
     */
    COMPLETED("已完成"),

    /**
     * 已取消 - 教学班被取消（如人数不足）
     */
    CANCELLED("已取消"),

    /**
     * 暂停 - 教学班暂时停止（如教师请假）
     */
    SUSPENDED("暂停"),

    /**
     * 合并 - 教学班与其他班级合并
     */
    MERGED("合并");

    private final String description;

    TeachingClassStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否可以选课
     */
    public boolean canEnroll() {
        return this == OPEN_FOR_ENROLLMENT;
    }

    /**
     * 检查是否正在进行教学
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 检查是否已结束（不再进行）
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == MERGED;
    }

    /**
     * 检查是否可以录入成绩
     */
    public boolean canInputGrades() {
        return this == ACTIVE || this == COMPLETED;
    }

    /**
     * 检查是否可以修改班级信息
     */
    public boolean canModify() {
        return (
            this == PLANNED ||
            this == OPEN_FOR_ENROLLMENT ||
            this == ENROLLMENT_CLOSED
        );
    }

    /**
     * 检查学生是否可以退课
     */
    public boolean canWithdraw() {
        return this == OPEN_FOR_ENROLLMENT || this == ENROLLMENT_CLOSED;
    }

    @Override
    public String toString() {
        return description;
    }
}
