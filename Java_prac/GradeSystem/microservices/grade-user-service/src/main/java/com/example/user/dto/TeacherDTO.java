package com.example.user.dto;

import com.example.user.domain.Teacher;
import com.example.user.domain.TeacherStatus;
import com.example.user.domain.TeacherTitle;
import java.time.LocalDateTime;
import java.util.UUID;

public class TeacherDTO {

    private UUID id;
    private UUID userId;
    private String employeeCode;
    private String department;
    private TeacherTitle title;
    private String specialization;
    private LocalDateTime hireDate;
    private TeacherStatus status;
    private Double workload;
    private Integer maxCourses;
    private String office;
    private String officePhone;
    private String officeHours;
    private String qualifications;
    private String researchInterests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TeacherDTO() {}

    public TeacherDTO(Teacher teacher) {
        this.id = teacher.getId();
        this.userId = teacher.getUserId();
        this.employeeCode = teacher.getEmployeeCode();
        this.department = teacher.getDepartment();
        this.title = teacher.getTitle();
        this.specialization = teacher.getSpecialization();
        this.hireDate = teacher.getHireDate();
        this.status = teacher.getStatus();
        this.workload = teacher.getWorkload();
        this.maxCourses = teacher.getMaxCourses();
        this.office = teacher.getOffice();
        this.officePhone = teacher.getOfficePhone();
        this.officeHours = teacher.getOfficeHours();
        this.qualifications = teacher.getQualifications();
        this.researchInterests = teacher.getResearchInterests();
        this.createdAt = teacher.getCreatedAt();
        this.updatedAt = teacher.getUpdatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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
}
