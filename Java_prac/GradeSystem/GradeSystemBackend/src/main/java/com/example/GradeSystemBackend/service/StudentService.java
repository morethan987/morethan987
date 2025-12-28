package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.dto.*;
import com.example.GradeSystemBackend.repository.StudentRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
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
            .orElseThrow(() ->
                new RuntimeException(
                    "Student not found with user id: " + userId
                )
            );
    }
}
