package com.example.GradeSystemBackend.domain.teachingclass;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.teacher.Teacher;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "teaching_class")
public class TeachingClass {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = true)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // getters / setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
