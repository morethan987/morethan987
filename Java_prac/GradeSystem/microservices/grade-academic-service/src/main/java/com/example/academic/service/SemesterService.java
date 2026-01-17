package com.example.academic.service;

import com.example.academic.domain.Semester;
import com.example.academic.dto.SemesterDTO;
import com.example.academic.repository.SemesterRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    public SemesterDTO getSemesterById(UUID semesterId) {
        return semesterRepository.findById(semesterId)
            .map(SemesterDTO::new)
            .orElseThrow(() -> new RuntimeException("Semester not found: " + semesterId));
    }

    public SemesterDTO getCurrentSemester() {
        return semesterRepository.findFirstByIsCurrentTrue()
            .map(SemesterDTO::new)
            .orElseThrow(() -> new RuntimeException("No current semester configured"));
    }

    public List<SemesterDTO> getAllSemesters() {
        return semesterRepository.findAll().stream()
            .map(SemesterDTO::new)
            .collect(Collectors.toList());
    }

    public List<SemesterDTO> getSemestersByAcademicYear(String academicYear) {
        return semesterRepository.findByAcademicYear(academicYear).stream()
            .map(SemesterDTO::new)
            .collect(Collectors.toList());
    }

    public List<SemesterDTO> getActiveSemesters() {
        LocalDate today = LocalDate.now();
        return semesterRepository.findAll().stream()
            .filter(s -> s.getStartDate() != null && s.getEndDate() != null 
                && !today.isBefore(s.getStartDate()) && !today.isAfter(s.getEndDate()))
            .map(SemesterDTO::new)
            .collect(Collectors.toList());
    }

    public List<SemesterDTO> getSemestersWithOpenEnrollment() {
        return semesterRepository.findSemestersWithOpenEnrollment(LocalDate.now()).stream()
            .map(SemesterDTO::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public SemesterDTO createSemester(SemesterDTO request) {
        if (semesterRepository.existsByAcademicYearAndSemesterNumber(
                request.getAcademicYear(), request.getSemesterNumber())) {
            throw new RuntimeException("Semester already exists for academic year " 
                + request.getAcademicYear() + " semester " + request.getSemesterNumber());
        }

        Semester semester = new Semester();
        semester.setName(request.getName());
        semester.setAcademicYear(request.getAcademicYear());
        semester.setSemesterNumber(request.getSemesterNumber());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setEnrollmentStartDate(request.getEnrollmentStartDate());
        semester.setEnrollmentEndDate(request.getEnrollmentEndDate());
        semester.setGradeSubmissionDeadline(request.getGradeSubmissionDeadline());
        semester.setIsCurrent(false);

        Semester saved = semesterRepository.save(semester);
        return new SemesterDTO(saved);
    }

    @Transactional
    public SemesterDTO updateSemester(UUID semesterId, SemesterDTO request) {
        Semester semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new RuntimeException("Semester not found: " + semesterId));

        if (request.getName() != null) semester.setName(request.getName());
        if (request.getStartDate() != null) semester.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) semester.setEndDate(request.getEndDate());
        if (request.getEnrollmentStartDate() != null) semester.setEnrollmentStartDate(request.getEnrollmentStartDate());
        if (request.getEnrollmentEndDate() != null) semester.setEnrollmentEndDate(request.getEnrollmentEndDate());
        if (request.getGradeSubmissionDeadline() != null) semester.setGradeSubmissionDeadline(request.getGradeSubmissionDeadline());

        Semester saved = semesterRepository.save(semester);
        return new SemesterDTO(saved);
    }

    @Transactional
    public SemesterDTO setCurrentSemester(UUID semesterId) {
        semesterRepository.clearCurrentSemester();

        Semester semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new RuntimeException("Semester not found: " + semesterId));

        semester.setIsCurrent(true);
        Semester saved = semesterRepository.save(semester);
        return new SemesterDTO(saved);
    }

    @Transactional
    public void deleteSemester(UUID semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new RuntimeException("Semester not found: " + semesterId));

        if (semester.getIsCurrent()) {
            throw new RuntimeException("Cannot delete current semester");
        }

        semesterRepository.deleteById(semesterId);
    }

    public long getTotalSemesters() {
        return semesterRepository.count();
    }

    public boolean isEnrollmentOpen() {
        return semesterRepository.findFirstByIsCurrentTrue()
            .map(Semester::isEnrollmentOpen)
            .orElse(false);
    }
}
