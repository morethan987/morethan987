package com.example.GradeSystemBackend.domain;

import com.example.GradeSystemBackend.enums.TeacherStatus;
import com.example.GradeSystemBackend.enums.TeacherTitle;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 20)
    private String employeeCode;

    @Column(nullable = true, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TeacherTitle title;

    @Column(nullable = true, length = 100)
    private String specialization;

    @Column(nullable = true)
    private LocalDateTime hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeacherStatus status = TeacherStatus.ACTIVE;

    @Column(nullable = true)
    private Double salary;

    @Column(nullable = true)
    private Double workload;

    @Column(nullable = true)
    private Integer maxCourses;

    @Column(nullable = true, length = 200)
    private String office;

    @Column(nullable = true, length = 20)
    private String officePhone;

    @Column(nullable = true, length = 100)
    private String officeHours;

    @Column(nullable = true, length = 1000)
    private String qualifications;

    @Column(nullable = true, length = 2000)
    private String researchInterests;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public Teacher() {}

    public Teacher(User user, String employeeCode) {
        this.user = user;
        this.employeeCode = employeeCode;
    }

    public Teacher(
        User user,
        String employeeCode,
        String department,
        TeacherTitle title
    ) {
        this.user = user;
        this.employeeCode = employeeCode;
        this.department = department;
        this.title = title;
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

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public TeacherTitle getTitle() {
        return title;
    }

    public void setTitle(TeacherTitle title) {
        this.title = title;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public TeacherStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherStatus status) {
        this.status = status;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Double getWorkload() {
        return workload;
    }

    public void setWorkload(Double workload) {
        this.workload = workload;
    }

    public Integer getMaxCourses() {
        return maxCourses;
    }

    public void setMaxCourses(Integer maxCourses) {
        this.maxCourses = maxCourses;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getOfficeHours() {
        return officeHours;
    }

    public void setOfficeHours(String officeHours) {
        this.officeHours = officeHours;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public String getResearchInterests() {
        return researchInterests;
    }

    public void setResearchInterests(String researchInterests) {
        this.researchInterests = researchInterests;
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
    public Integer getYearsOfService() {
        if (hireDate == null) {
            return null;
        }
        return LocalDateTime.now().getYear() - hireDate.getYear();
    }

    public boolean isEligibleForPromotion() {
        Integer yearsOfService = getYearsOfService();
        if (yearsOfService == null || title == null) {
            return false;
        }

        // 基本晋升规则示例
        switch (title) {
            case ASSISTANT_PROFESSOR:
                return yearsOfService >= 6;
            case ASSOCIATE_PROFESSOR:
                return yearsOfService >= 12;
            case PROFESSOR:
                return false; // 已是最高职称
            default:
                return yearsOfService >= 3;
        }
    }

    public boolean isOverloaded() {
        return maxCourses != null && workload != null && workload > maxCourses;
    }

    public boolean canTeachMoreCourses() {
        return maxCourses != null && workload != null && workload < maxCourses;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return (
            "Teacher{" +
            "id=" +
            id +
            ", employeeCode='" +
            employeeCode +
            '\'' +
            ", department='" +
            department +
            '\'' +
            ", title=" +
            title +
            ", status=" +
            status +
            '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return employeeCode.equals(teacher.employeeCode);
    }

    @Override
    public int hashCode() {
        return employeeCode.hashCode();
    }
}
