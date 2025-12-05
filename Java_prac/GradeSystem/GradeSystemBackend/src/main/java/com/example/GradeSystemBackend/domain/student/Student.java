package com.example.GradeSystemBackend.domain.student;

import com.example.GradeSystemBackend.domain.auth.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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

    @Column(nullable = true)
    private Integer enrollmentYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status = StudentStatus.ENROLLED;

    @Column(nullable = true, precision = 3)
    private Double gpa;

    @Column(nullable = true)
    private Integer totalCredits = 0;

    @Column(nullable = true)
    private Integer completedCredits = 0;

    @Column(nullable = true, length = 100)
    private String advisor;

    @Column(nullable = true)
    private LocalDateTime expectedGraduationDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

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

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public Integer getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Integer totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Integer getCompletedCredits() {
        return completedCredits;
    }

    public void setCompletedCredits(Integer completedCredits) {
        this.completedCredits = completedCredits;
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

    // Utility methods
    public Double getCreditsProgress() {
        if (totalCredits == null || totalCredits == 0) {
            return 0.0;
        }
        return ((double) completedCredits / totalCredits) * 100;
    }

    public boolean isGraduationEligible() {
        return (
            status == StudentStatus.ENROLLED &&
            completedCredits != null &&
            totalCredits != null &&
            completedCredits >= totalCredits &&
            gpa != null &&
            gpa >= 2.0
        ); // 假设最低毕业GPA为2.0
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
