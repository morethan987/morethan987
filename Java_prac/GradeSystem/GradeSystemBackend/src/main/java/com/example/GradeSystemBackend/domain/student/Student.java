package com.example.GradeSystemBackend.domain.student;

import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 20)
    private String studentCode;

    @Column(nullable = true, length = 100)
    private String major;

    @Column(nullable = true, length = 50)
    private String className;

    @Column(nullable = false)
    private Integer enrollmentYear = 2023;

    @Column(nullable = false)
    private Integer currentSemester = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status = StudentStatus.ENROLLED;

    @Column(nullable = true)
    private Double totalCredits = 128.0;

    @Column(nullable = true, length = 100)
    private String advisor;

    @Column(nullable = true)
    private LocalDateTime expectedGraduationDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany(mappedBy = "students", fetch = FetchType.EAGER)
    private Set<TeachingClass> teachingClasses;

    // Constructors
    public Student() {}

    public Student(User user, String studentCode) {
        this.user = user;
        this.studentCode = studentCode;
    }

    public Student(
        User user,
        String studentCode,
        String major,
        String className,
        Integer enrollmentYear
    ) {
        this.user = user;
        this.studentCode = studentCode;
        this.major = major;
        this.className = className;
        this.enrollmentYear = enrollmentYear;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getEnrollmentYear() {
        return enrollmentYear;
    }

    public void setEnrollmentYear(Integer enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    public Integer getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(Integer currentSemester) {
        this.currentSemester = currentSemester;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public String getAdvisor() {
        return advisor;
    }

    public void setAdvisor(String advisor) {
        this.advisor = advisor;
    }

    public LocalDateTime getExpectedGraduationDate() {
        return expectedGraduationDate;
    }

    public void setExpectedGraduationDate(
        LocalDateTime expectedGraduationDate
    ) {
        this.expectedGraduationDate = expectedGraduationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<TeachingClass> getTeachingClasses() {
        return teachingClasses;
    }

    public void setTeachingClasses(Set<TeachingClass> teachingClasses) {
        this.teachingClasses = teachingClasses;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return (
            "Student{" +
            "id=" +
            id +
            ", studentCode='" +
            studentCode +
            '\'' +
            ", major='" +
            major +
            '\'' +
            ", className='" +
            className +
            '\'' +
            ", enrollmentYear=" +
            enrollmentYear +
            ", status=" +
            status +
            '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentCode.equals(student.studentCode);
    }

    @Override
    public int hashCode() {
        return studentCode.hashCode();
    }
}
