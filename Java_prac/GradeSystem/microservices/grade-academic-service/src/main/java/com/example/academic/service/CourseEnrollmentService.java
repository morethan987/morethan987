package com.example.academic.service;

import com.example.academic.domain.CourseEnrollment;
import com.example.academic.domain.EnrollmentStatus;
import com.example.academic.domain.TeachingClass;
import com.example.academic.dto.CourseEnrollmentDTO;
import com.example.academic.dto.EnrollmentRequestDTO;
import com.example.academic.dto.EnrollmentResponseDTO;
import com.example.academic.repository.CourseEnrollmentRepository;
import com.example.academic.repository.TeachingClassRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseEnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(CourseEnrollmentService.class);

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    public CourseEnrollmentDTO getEnrollmentById(UUID enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
            .map(CourseEnrollmentDTO::new)
            .orElseThrow(() -> new RuntimeException("Enrollment not found: " + enrollmentId));
    }

    public List<CourseEnrollmentDTO> getEnrollmentsByStudent(UUID studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
            .map(CourseEnrollmentDTO::new)
            .collect(Collectors.toList());
    }

    public List<CourseEnrollmentDTO> getActiveEnrollmentsByStudent(UUID studentId) {
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ENROLLED).stream()
            .map(CourseEnrollmentDTO::new)
            .collect(Collectors.toList());
    }

    public List<CourseEnrollmentDTO> getEnrollmentsByTeachingClass(UUID teachingClassId) {
        return enrollmentRepository.findByTeachingClassId(teachingClassId).stream()
            .map(CourseEnrollmentDTO::new)
            .collect(Collectors.toList());
    }

    public List<CourseEnrollmentDTO> getEnrollmentsByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status).stream()
            .map(CourseEnrollmentDTO::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentResponseDTO enrollStudent(EnrollmentRequestDTO request) {
        UUID studentId = request.getStudentId();
        UUID teachingClassId = request.getTeachingClassId();

        TeachingClass tc = teachingClassRepository.findById(teachingClassId)
            .orElseThrow(() -> new RuntimeException("Teaching class not found: " + teachingClassId));

        if (!tc.getStatus().canEnroll()) {
            return EnrollmentResponseDTO.failure(studentId, teachingClassId, 
                "Teaching class is not open for enrollment. Status: " + tc.getStatus());
        }

        if (!tc.hasCapacity()) {
            return EnrollmentResponseDTO.failure(studentId, teachingClassId, 
                "Teaching class is full. Capacity: " + tc.getCapacity());
        }

        if (enrollmentRepository.existsByStudentIdAndTeachingClassId(studentId, teachingClassId)) {
            return EnrollmentResponseDTO.failure(studentId, teachingClassId, 
                "Student is already enrolled in this teaching class");
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setTeachingClass(tc);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now());

        tc.incrementEnrolledCount();
        teachingClassRepository.save(tc);
        CourseEnrollment saved = enrollmentRepository.save(enrollment);

        String courseName = tc.getCourse() != null ? tc.getCourse().getName() : null;
        String courseCode = tc.getCourse() != null ? tc.getCourse().getCourseCode() : null;

        log.info("Student {} enrolled in teaching class {} (course: {})", 
            studentId, teachingClassId, courseCode);

        return EnrollmentResponseDTO.success(
            saved.getId(),
            studentId,
            teachingClassId,
            courseName,
            courseCode,
            tc.getName(),
            saved.getEnrolledAt()
        );
    }

    @Transactional
    public EnrollmentResponseDTO dropCourse(UUID studentId, UUID teachingClassId) {
        CourseEnrollment enrollment = enrollmentRepository.findByStudentIdAndTeachingClassId(studentId, teachingClassId)
            .orElseThrow(() -> new RuntimeException("Enrollment not found for student " + studentId 
                + " in class " + teachingClassId));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            return EnrollmentResponseDTO.failure(studentId, teachingClassId, 
                "Cannot drop course. Current status: " + enrollment.getStatus());
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment.setDroppedAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        TeachingClass tc = enrollment.getTeachingClass();
        tc.decrementEnrolledCount();
        teachingClassRepository.save(tc);

        log.info("Student {} dropped from teaching class {}", studentId, teachingClassId);

        String courseName = tc.getCourse() != null ? tc.getCourse().getName() : null;
        String courseCode = tc.getCourse() != null ? tc.getCourse().getCourseCode() : null;

        EnrollmentResponseDTO response = new EnrollmentResponseDTO();
        response.setEnrollmentId(enrollment.getId());
        response.setStudentId(studentId);
        response.setTeachingClassId(teachingClassId);
        response.setCourseName(courseName);
        response.setCourseCode(courseCode);
        response.setStatus("SUCCESS");
        response.setMessage("Successfully dropped from course");
        return response;
    }

    @Transactional
    public CourseEnrollmentDTO updateEnrollmentStatus(UUID enrollmentId, EnrollmentStatus status) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new RuntimeException("Enrollment not found: " + enrollmentId));

        EnrollmentStatus oldStatus = enrollment.getStatus();
        enrollment.setStatus(status);

        if (status == EnrollmentStatus.COMPLETED) {
            enrollment.setCompletedAt(LocalDateTime.now());
        } else if (status == EnrollmentStatus.DROPPED) {
            enrollment.setDroppedAt(LocalDateTime.now());
        }

        if (oldStatus == EnrollmentStatus.ENROLLED && status != EnrollmentStatus.ENROLLED) {
            TeachingClass tc = enrollment.getTeachingClass();
            tc.decrementEnrolledCount();
            teachingClassRepository.save(tc);
        }

        CourseEnrollment saved = enrollmentRepository.save(enrollment);
        return new CourseEnrollmentDTO(saved);
    }

    public long countEnrollmentsByStudent(UUID studentId) {
        return enrollmentRepository.countByStudentId(studentId);
    }

    public long countActiveEnrollmentsByStudent(UUID studentId) {
        return enrollmentRepository.countActiveEnrollmentsByStudentId(studentId);
    }

    public long countEnrollmentsByTeachingClass(UUID teachingClassId) {
        return enrollmentRepository.countByTeachingClassId(teachingClassId);
    }

    public boolean isStudentEnrolled(UUID studentId, UUID teachingClassId) {
        return enrollmentRepository.findByStudentIdAndTeachingClassId(studentId, teachingClassId)
            .map(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
            .orElse(false);
    }
}
