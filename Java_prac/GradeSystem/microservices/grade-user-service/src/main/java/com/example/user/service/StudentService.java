package com.example.user.service;

import com.example.user.domain.Student;
import com.example.user.domain.StudentStatus;
import com.example.user.dto.StudentDTO;
import com.example.user.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public long getTotalStudents() {
        return studentRepository.count();
    }

    public StudentDTO getStudentByUserId(UUID userId) {
        return studentRepository
            .findByUserId(userId)
            .map(StudentDTO::new)
            .orElseThrow(() -> new RuntimeException("Student not found with user id: " + userId));
    }

    public StudentDTO getStudentById(UUID studentId) {
        return studentRepository
            .findById(studentId)
            .map(StudentDTO::new)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
    }

    public StudentDTO getStudentByCode(String studentCode) {
        return studentRepository
            .findByStudentCode(studentCode)
            .map(StudentDTO::new)
            .orElseThrow(() -> new RuntimeException("Student not found with code: " + studentCode));
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAllOrderByStudentCodeAsc()
            .stream()
            .map(StudentDTO::new)
            .collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByStatus(StudentStatus status) {
        return studentRepository.findByStatus(status)
            .stream()
            .map(StudentDTO::new)
            .collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByMajor(String major) {
        return studentRepository.findByMajor(major)
            .stream()
            .map(StudentDTO::new)
            .collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByClassName(String className) {
        return studentRepository.findByClassName(className)
            .stream()
            .map(StudentDTO::new)
            .collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByEnrollmentYear(Integer year) {
        return studentRepository.findByEnrollmentYear(year)
            .stream()
            .map(StudentDTO::new)
            .collect(Collectors.toList());
    }

    public List<StudentDTO> getActiveStudents() {
        return studentRepository.findActiveStudents()
            .stream()
            .map(StudentDTO::new)
            .collect(Collectors.toList());
    }

    public StudentDTO createStudent(UUID userId, StudentDTO request) {
        if (studentRepository.existsByUserId(userId)) {
            throw new RuntimeException("Student already exists for user id: " + userId);
        }
        
        if (request.getStudentCode() != null && studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new RuntimeException("Student code already exists: " + request.getStudentCode());
        }

        Student student = new Student();
        student.setUserId(userId);
        student.setStudentCode(request.getStudentCode());
        student.setMajor(request.getMajor());
        student.setClassName(request.getClassName());
        student.setEnrollmentYear(request.getEnrollmentYear());
        student.setCurrentSemester(request.getCurrentSemester() != null ? request.getCurrentSemester() : 1);
        student.setStatus(request.getStatus() != null ? request.getStatus() : StudentStatus.ENROLLED);
        student.setTotalCredits(request.getTotalCredits() != null ? request.getTotalCredits() : 0.0);
        student.setAdvisor(request.getAdvisor());
        student.setExpectedGraduationDate(request.getExpectedGraduationDate());
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());

        studentRepository.save(student);
        return new StudentDTO(student);
    }

    public StudentDTO updateStudent(UUID studentId, StudentDTO request) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        if (request.getMajor() != null) student.setMajor(request.getMajor());
        if (request.getClassName() != null) student.setClassName(request.getClassName());
        if (request.getCurrentSemester() != null) student.setCurrentSemester(request.getCurrentSemester());
        if (request.getStatus() != null) student.setStatus(request.getStatus());
        if (request.getTotalCredits() != null) student.setTotalCredits(request.getTotalCredits());
        if (request.getAdvisor() != null) student.setAdvisor(request.getAdvisor());
        if (request.getExpectedGraduationDate() != null) student.setExpectedGraduationDate(request.getExpectedGraduationDate());

        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);
        return new StudentDTO(student);
    }

    public StudentDTO updateStudentStatus(UUID studentId, StudentStatus newStatus) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        student.setStatus(newStatus);
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);
        return new StudentDTO(student);
    }

    public void deleteStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        studentRepository.delete(student);
    }

    public boolean existsByUserId(UUID userId) {
        return studentRepository.existsByUserId(userId);
    }

    public boolean existsByStudentCode(String studentCode) {
        return studentRepository.existsByStudentCode(studentCode);
    }

    public long countByStatus(StudentStatus status) {
        return studentRepository.countByStatus(status);
    }

    public long countByMajor(String major) {
        return studentRepository.countByMajor(major);
    }
}
