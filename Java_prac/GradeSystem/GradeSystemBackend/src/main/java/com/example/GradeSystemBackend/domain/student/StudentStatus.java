package com.example.GradeSystemBackend.domain.student;

/**
 * 学生状态枚举
 * 定义学生在学校系统中的各种状态
 */
public enum StudentStatus {
    /**
     * 在读 - 正常在校学习状态
     */
    ENROLLED("在读"),

    /**
     * 休学 - 暂时离校但保留学籍
     */
    SUSPENDED("休学"),

    /**
     * 退学 - 主动退出学校
     */
    WITHDRAWN("退学"),

    /**
     * 毕业 - 完成学业正常毕业
     */
    GRADUATED("毕业"),

    /**
     * 转学 - 转到其他学校
     */
    TRANSFERRED("转学"),

    /**
     * 开除 - 被学校开除
     */
    EXPELLED("开除"),

    /**
     * 延期毕业 - 延长学习时间
     */
    DEFERRED("延期毕业"),

    /**
     * 交换生 - 临时在本校学习的交换学生
     */
    EXCHANGE("交换生");

    private final String description;

    StudentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否为活跃状态（可以正常学习）
     */
    public boolean isActive() {
        return this == ENROLLED || this == EXCHANGE || this == DEFERRED;
    }

    /**
     * 检查是否已离校
     */
    public boolean isLeft() {
        return (
            this == WITHDRAWN ||
            this == GRADUATED ||
            this == TRANSFERRED ||
            this == EXPELLED
        );
    }

    /**
     * 检查是否为临时状态
     */
    public boolean isTemporary() {
        return this == SUSPENDED || this == EXCHANGE || this == DEFERRED;
    }

    @Override
    public String toString() {
        return description;
    }
}
