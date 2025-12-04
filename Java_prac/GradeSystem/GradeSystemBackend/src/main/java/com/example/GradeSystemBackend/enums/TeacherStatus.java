package com.example.GradeSystemBackend.enums;

/**
 * 教师状态枚举
 * 定义教师在学校系统中的各种工作状态
 */
public enum TeacherStatus {
    /**
     * 在职 - 正常工作状态
     */
    ACTIVE("在职"),

    /**
     * 休假 - 临时离岗但保留职位
     */
    ON_LEAVE("休假"),

    /**
     * 停薪留职 - 暂停工作但保留职位
     */
    UNPAID_LEAVE("停薪留职"),

    /**
     * 退休 - 正常退休
     */
    RETIRED("退休"),

    /**
     * 辞职 - 主动离职
     */
    RESIGNED("辞职"),

    /**
     * 解雇 - 被学校解雇
     */
    TERMINATED("解雇"),

    /**
     * 调动 - 调到其他部门或学校
     */
    TRANSFERRED("调动"),

    /**
     * 临时聘用 - 短期合同教师
     */
    TEMPORARY("临时聘用"),

    /**
     * 兼职 - 非全职教师
     */
    PART_TIME("兼职"),

    /**
     * 访问学者 - 临时来校的访问教师
     */
    VISITING("访问学者"),

    /**
     * 暂停 - 因违纪等原因暂停工作
     */
    SUSPENDED("暂停");

    private final String description;

    TeacherStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否为活跃状态（可以正常教学）
     */
    public boolean isActive() {
        return this == ACTIVE || this == TEMPORARY || this == PART_TIME || this == VISITING;
    }

    /**
     * 检查是否已离职
     */
    public boolean isLeft() {
        return this == RETIRED || this == RESIGNED || this == TERMINATED || this == TRANSFERRED;
    }

    /**
     * 检查是否为临时状态
     */
    public boolean isTemporary() {
        return this == ON_LEAVE || this == UNPAID_LEAVE || this == SUSPENDED ||
               this == TEMPORARY || this == VISITING;
    }

    /**
     * 检查是否可以分配教学任务
     */
    public boolean canTeach() {
        return this == ACTIVE || this == TEMPORARY || this == PART_TIME || this == VISITING;
    }

    /**
     * 检查是否有工资
     */
    public boolean hasSalary() {
        return this == ACTIVE || this == ON_LEAVE || this == TEMPORARY ||
               this == PART_TIME || this == VISITING;
    }

    @Override
    public String toString() {
        return description;
    }
}
