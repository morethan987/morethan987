package com.example.user.domain;

/**
 * 教师职称枚举
 * 定义教师在学校系统中的各种职称等级
 */
public enum TeacherTitle {
    /**
     * 教授 - 最高职称
     */
    PROFESSOR("教授"),

    /**
     * 副教授 - 高级职称
     */
    ASSOCIATE_PROFESSOR("副教授"),

    /**
     * 助理教授 - 中级职称
     */
    ASSISTANT_PROFESSOR("助理教授"),

    /**
     * 讲师 - 基础职称
     */
    LECTURER("讲师"),

    /**
     * 助教 - 初级职称
     */
    TEACHING_ASSISTANT("助教"),

    /**
     * 研究教授 - 专门从事研究的教授
     */
    RESEARCH_PROFESSOR("研究教授"),

    /**
     * 临床教授 - 医学院等专业的临床教授
     */
    CLINICAL_PROFESSOR("临床教授"),

    /**
     * 兼职教授 - 外聘教授
     */
    ADJUNCT_PROFESSOR("兼职教授"),

    /**
     * 荣誉教授 - 退休或特聘的荣誉职称
     */
    EMERITUS_PROFESSOR("荣誉教授"),

    /**
     * 客座教授 - 短期聘请的教授
     */
    VISITING_PROFESSOR("客座教授"),

    /**
     * 首席教授 - 特殊荣誉职称
     */
    DISTINGUISHED_PROFESSOR("首席教授");

    private final String description;

    TeacherTitle(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取职称等级（数字越高职称越高）
     */
    public int getLevel() {
        switch (this) {
            case DISTINGUISHED_PROFESSOR:
                return 11;
            case PROFESSOR:
            case EMERITUS_PROFESSOR:
                return 10;
            case RESEARCH_PROFESSOR:
            case CLINICAL_PROFESSOR:
                return 9;
            case ASSOCIATE_PROFESSOR:
                return 8;
            case VISITING_PROFESSOR:
                return 7;
            case ASSISTANT_PROFESSOR:
                return 6;
            case ADJUNCT_PROFESSOR:
                return 5;
            case LECTURER:
                return 4;
            case TEACHING_ASSISTANT:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * 检查是否为教授级别
     */
    public boolean isProfessorLevel() {
        return this == PROFESSOR || this == RESEARCH_PROFESSOR || this == CLINICAL_PROFESSOR ||
               this == EMERITUS_PROFESSOR || this == VISITING_PROFESSOR || this == ADJUNCT_PROFESSOR ||
               this == DISTINGUISHED_PROFESSOR;
    }

    /**
     * 检查是否为正式编制教师
     */
    public boolean isPermanent() {
        return this == PROFESSOR || this == ASSOCIATE_PROFESSOR || this == ASSISTANT_PROFESSOR ||
               this == LECTURER || this == TEACHING_ASSISTANT || this == RESEARCH_PROFESSOR ||
               this == CLINICAL_PROFESSOR || this == DISTINGUISHED_PROFESSOR;
    }

    /**
     * 检查是否为临时或访问教师
     */
    public boolean isTemporary() {
        return this == VISITING_PROFESSOR || this == ADJUNCT_PROFESSOR || this == EMERITUS_PROFESSOR;
    }

    /**
     * 检查是否有独立授课资格
     */
    public boolean canTeachIndependently() {
        return this != TEACHING_ASSISTANT;
    }

    /**
     * 检查是否有研究生指导资格
     */
    public boolean canSuperviseDoctorate() {
        return isProfessorLevel() && this != ADJUNCT_PROFESSOR;
    }

    /**
     * 检查是否有硕士生指导资格
     */
    public boolean canSuperviseMaster() {
        return getLevel() >= 6;
    }

    /**
     * 获取下一个可晋升的职称
     */
    public TeacherTitle getNextTitle() {
        switch (this) {
            case TEACHING_ASSISTANT:
                return LECTURER;
            case LECTURER:
                return ASSISTANT_PROFESSOR;
            case ASSISTANT_PROFESSOR:
                return ASSOCIATE_PROFESSOR;
            case ASSOCIATE_PROFESSOR:
                return PROFESSOR;
            case PROFESSOR:
                return DISTINGUISHED_PROFESSOR;
            default:
                return null;
        }
    }

    /**
     * 比较职称高低
     */
    public boolean isHigherThan(TeacherTitle other) {
        return this.getLevel() > other.getLevel();
    }

    /**
     * 比较职称高低
     */
    public boolean isLowerThan(TeacherTitle other) {
        return this.getLevel() < other.getLevel();
    }

    @Override
    public String toString() {
        return description;
    }
}
