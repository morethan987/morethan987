package com.example.user.service;

import com.example.user.domain.Teacher;
import com.example.user.domain.TeacherStatus;
import com.example.user.domain.TeacherTitle;
import com.example.user.dto.TeacherDTO;
import com.example.user.repository.TeacherRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    public long getTotalTeachers() {
        return teacherRepository.count();
    }

    public TeacherDTO getTeacherByUserId(UUID userId) {
        return teacherRepository
            .findByUserId(userId)
            .map(TeacherDTO::new)
            .orElseThrow(() -> new RuntimeException("Teacher not found with user id: " + userId));
    }

    public TeacherDTO getTeacherById(UUID teacherId) {
        return teacherRepository
            .findById(teacherId)
            .map(TeacherDTO::new)
            .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
    }

    public TeacherDTO getTeacherByEmployeeCode(String employeeCode) {
        return teacherRepository
            .findByEmployeeCode(employeeCode)
            .map(TeacherDTO::new)
            .orElseThrow(() -> new RuntimeException("Teacher not found with employee code: " + employeeCode));
    }

    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAllOrderByEmployeeCodeAsc()
            .stream()
            .map(TeacherDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeacherDTO> getTeachersByDepartment(String department) {
        return teacherRepository.findByDepartment(department)
            .stream()
            .map(TeacherDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeacherDTO> getTeachersByTitle(TeacherTitle title) {
        return teacherRepository.findByTitle(title)
            .stream()
            .map(TeacherDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeacherDTO> getTeachersByStatus(TeacherStatus status) {
        return teacherRepository.findByStatus(status)
            .stream()
            .map(TeacherDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeacherDTO> getActiveTeachers() {
        return teacherRepository.findActiveTeachers()
            .stream()
            .map(TeacherDTO::new)
            .collect(Collectors.toList());
    }

    public List<TeacherDTO> getAvailableTeachers() {
        return teacherRepository.findTeachersCanTeachMore()
            .stream()
            .map(TeacherDTO::new)
            .collect(Collectors.toList());
    }

    public TeacherDTO createTeacher(UUID userId, TeacherDTO request) {
        if (teacherRepository.existsByUserId(userId)) {
            throw new RuntimeException("Teacher already exists for user id: " + userId);
        }

        if (request.getEmployeeCode() != null && teacherRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new RuntimeException("Employee code already exists: " + request.getEmployeeCode());
        }

        Teacher teacher = new Teacher();
        teacher.setUserId(userId);
        teacher.setEmployeeCode(request.getEmployeeCode());
        teacher.setDepartment(request.getDepartment());
        teacher.setTitle(request.getTitle() != null ? request.getTitle() : TeacherTitle.LECTURER);
        teacher.setSpecialization(request.getSpecialization());
        teacher.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDateTime.now());
        teacher.setStatus(request.getStatus() != null ? request.getStatus() : TeacherStatus.ACTIVE);
        teacher.setWorkload(request.getWorkload() != null ? request.getWorkload() : 0.0);
        teacher.setMaxCourses(request.getMaxCourses() != null ? request.getMaxCourses() : 4);
        teacher.setOffice(request.getOffice());
        teacher.setOfficePhone(request.getOfficePhone());
        teacher.setOfficeHours(request.getOfficeHours());
        teacher.setQualifications(request.getQualifications());
        teacher.setResearchInterests(request.getResearchInterests());
        teacher.setCreatedAt(LocalDateTime.now());
        teacher.setUpdatedAt(LocalDateTime.now());

        teacherRepository.save(teacher);
        return new TeacherDTO(teacher);
    }

    public TeacherDTO updateTeacher(UUID teacherId, TeacherDTO request) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        if (request.getDepartment() != null) teacher.setDepartment(request.getDepartment());
        if (request.getTitle() != null) teacher.setTitle(request.getTitle());
        if (request.getSpecialization() != null) teacher.setSpecialization(request.getSpecialization());
        if (request.getStatus() != null) teacher.setStatus(request.getStatus());
        if (request.getWorkload() != null) teacher.setWorkload(request.getWorkload());
        if (request.getMaxCourses() != null) teacher.setMaxCourses(request.getMaxCourses());
        if (request.getOffice() != null) teacher.setOffice(request.getOffice());
        if (request.getOfficePhone() != null) teacher.setOfficePhone(request.getOfficePhone());
        if (request.getOfficeHours() != null) teacher.setOfficeHours(request.getOfficeHours());
        if (request.getQualifications() != null) teacher.setQualifications(request.getQualifications());
        if (request.getResearchInterests() != null) teacher.setResearchInterests(request.getResearchInterests());

        teacher.setUpdatedAt(LocalDateTime.now());
        teacherRepository.save(teacher);
        return new TeacherDTO(teacher);
    }

    public TeacherDTO updateTeacherStatus(UUID teacherId, TeacherStatus newStatus) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        teacher.setStatus(newStatus);
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherRepository.save(teacher);
        return new TeacherDTO(teacher);
    }

    public TeacherDTO updateTeacherWorkload(UUID teacherId, Double newWorkload) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        teacher.setWorkload(newWorkload);
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherRepository.save(teacher);
        return new TeacherDTO(teacher);
    }

    public void deleteTeacher(UUID teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        teacherRepository.delete(teacher);
    }

    public boolean existsByUserId(UUID userId) {
        return teacherRepository.existsByUserId(userId);
    }

    public boolean existsByEmployeeCode(String employeeCode) {
        return teacherRepository.existsByEmployeeCode(employeeCode);
    }

    public long countByDepartment(String department) {
        return teacherRepository.countByDepartment(department);
    }

    public long countByStatus(TeacherStatus status) {
        return teacherRepository.countByStatus(status);
    }
}
