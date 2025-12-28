package com.example.GradeSystemBackend.domain.teachingclass;

import com.example.GradeSystemBackend.domain.course.Course;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.teacher.Teacher;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "student_teaching_class",
        joinColumns = @JoinColumn(name = "teaching_class_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> students = new HashSet<>();

    @Column(length = 50)
    private String classroom;

    @Column(length = 100)
    private String timeSchedule;

    @Column
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TeachingClassStatus status;

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

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    public void addStudent(Student student) {
        this.students.add(student);
        student.getTeachingClasses().add(this);
    }

    public void removeStudent(Student student) {
        this.students.remove(student);
        student.getTeachingClasses().remove(this);
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getTimeSchedule() {
        return timeSchedule;
    }

    public void setTimeSchedule(String timeSchedule) {
        this.timeSchedule = timeSchedule;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public TeachingClassStatus getStatus() {
        return status;
    }

    public void setStatus(TeachingClassStatus status) {
        this.status = status;
    }

    public int getEnrolledCount() {
        return students != null ? students.size() : 0;
    }
}
