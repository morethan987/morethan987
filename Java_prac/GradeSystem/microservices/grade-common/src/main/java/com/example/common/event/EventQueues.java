package com.example.common.event;

public final class EventQueues {
    
    public static final String COURSE_ENROLLMENT_QUEUE = "grade.course.enrollment";
    public static final String COURSE_DROPPED_QUEUE = "grade.course.dropped";
    public static final String GRADE_UPDATED_QUEUE = "grade.grade.updated";
    
    public static final String EXCHANGE_NAME = "grade.exchange";
    
    public static final String ENROLLMENT_ROUTING_KEY = "course.enrolled";
    public static final String DROPPED_ROUTING_KEY = "course.dropped";
    public static final String GRADE_ROUTING_KEY = "grade.updated";
    
    private EventQueues() {
    }
}
