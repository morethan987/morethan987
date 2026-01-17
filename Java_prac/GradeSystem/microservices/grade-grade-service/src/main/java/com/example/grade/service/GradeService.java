package com.example.grade.service;

import com.example.grade.client.AcademicServiceClient;
import com.example.grade.client.UserServiceClient;
import com.example.grade.domain.Grade;
import com.example.grade.dto.CourseDTO;
import com.example.grade.dto.GradeCreateDTO;
import com.example.grade.dto.GradeDTO;
import com.example.grade.dto.GradeStatsDTO;
import com.example.grade.dto.GradeUpdateDTO;
import com.example.grade.dto.StudentDTO;
import com.example.grade.repository.GradeRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AcademicServiceClient academicServiceClient;

    public GradeDTO getGradeById(UUID gradeId) {
        return gradeRepository.findById(gradeId)
            .map(this::convertToDTO)
            .orElse(null);
    }

    public GradeDTO getGradeByIdWithDetails(UUID gradeId) {
        return gradeRepository.findById(gradeId)
            .map(this::convertToDTOWithDetails)
            .orElse(null);
    }

    public List<GradeDTO> getGradesByStudentId(UUID studentId) {
        return gradeRepository.findByStudentId(studentId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<GradeDTO> getGradesByStudentIdWithDetails(UUID studentId) {
        return gradeRepository.findByStudentId(studentId)
            .stream()
            .map(this::convertToDTOWithDetails)
            .collect(Collectors.toList());
    }

    public List<GradeDTO> getGradesByCourseId(UUID courseId) {
        return gradeRepository.findByCourseId(courseId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<GradeDTO> getGradesByCourseIdWithDetails(UUID courseId) {
        return gradeRepository.findByCourseId(courseId)
            .stream()
            .map(this::convertToDTOWithDetails)
            .collect(Collectors.toList());
    }

    public Optional<GradeDTO> getGradeByStudentAndCourse(UUID studentId, UUID courseId) {
        return gradeRepository.findByStudentIdAndCourseId(studentId, courseId)
            .map(this::convertToDTO);
    }

    @Transactional
    public GradeDTO createGrade(GradeCreateDTO createDTO) {
        if (gradeRepository.existsByStudentIdAndCourseId(createDTO.getStudentId(), createDTO.getCourseId())) {
            throw new IllegalArgumentException("Grade already exists for this student and course");
        }

        Grade grade = new Grade(createDTO.getCourseId(), createDTO.getStudentId());
        grade.setUsualScore(createDTO.getUsualScore());
        grade.setMidScore(createDTO.getMidScore());
        grade.setExperimentScore(createDTO.getExperimentScore());
        grade.setFinalExamScore(createDTO.getFinalExamScore());

        Grade savedGrade = gradeRepository.save(grade);
        return convertToDTO(savedGrade);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public GradeDTO updateGrade(UUID gradeId, GradeUpdateDTO updateDTO) {
        Grade grade = gradeRepository.findById(gradeId)
            .orElseThrow(() -> new RuntimeException("Grade not found: " + gradeId));

        if (updateDTO.getVersion() != null && !updateDTO.getVersion().equals(grade.getVersion())) {
            throw new OptimisticLockingFailureException(
                "Grade has been modified by another user. Current version: " + 
                grade.getVersion() + ", submitted version: " + updateDTO.getVersion()
            );
        }

        if (updateDTO.getUsualScore() != null) {
            grade.setUsualScore(updateDTO.getUsualScore());
        }
        if (updateDTO.getMidScore() != null) {
            grade.setMidScore(updateDTO.getMidScore());
        }
        if (updateDTO.getExperimentScore() != null) {
            grade.setExperimentScore(updateDTO.getExperimentScore());
        }
        if (updateDTO.getFinalExamScore() != null) {
            grade.setFinalExamScore(updateDTO.getFinalExamScore());
        }

        Grade savedGrade = gradeRepository.save(grade);
        return convertToDTO(savedGrade);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> batchUpdateGrades(List<GradeUpdateDTO> updateDTOs) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new java.util.ArrayList<>();

        for (GradeUpdateDTO updateDTO : updateDTOs) {
            try {
                updateGrade(updateDTO.getId(), updateDTO);
                successCount++;
            } catch (OptimisticLockingFailureException e) {
                failureCount++;
                errors.add("Grade ID " + updateDTO.getId() + " conflict: " + e.getMessage());
            } catch (Exception e) {
                failureCount++;
                errors.add("Grade ID " + updateDTO.getId() + " failed: " + e.getMessage());
            }
        }

        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);
        return result;
    }

    @Transactional
    public void deleteGrade(UUID gradeId) {
        if (!gradeRepository.existsById(gradeId)) {
            throw new RuntimeException("Grade not found: " + gradeId);
        }
        gradeRepository.deleteById(gradeId);
    }

    public Double getStudentAverageGpa(UUID studentId) {
        return gradeRepository.calculateStudentAverageGpa(studentId);
    }

    public GradeStatsDTO getStudentGradeStats(UUID studentId) {
        Map<String, Object> stats = new HashMap<>();

        Double averageGPA = gradeRepository.calculateStudentAverageGpa(studentId);
        Double averageScore = gradeRepository.calculateStudentAverageFinalScore(studentId);
        Long passedCourses = gradeRepository.countPassedCoursesByStudentId(studentId);
        Long totalCourses = gradeRepository.countGradesWithGpaByStudentId(studentId);

        stats.put("averageGPA", averageGPA != null ? averageGPA : 0.0);
        stats.put("averageScore", averageScore != null ? averageScore : 0.0);
        stats.put("passedCourses", passedCourses != null ? passedCourses : 0L);
        stats.put("totalCourses", totalCourses != null ? totalCourses : 0L);
        stats.put("totalCredits", 0.0);

        return new GradeStatsDTO(stats);
    }

    public Double getCoursePassRate(UUID courseId) {
        Long passed = gradeRepository.countPassedInCourse(courseId);
        Long total = gradeRepository.countTotalInCourse(courseId);

        if (total == null || total == 0) {
            return null;
        }

        return (passed != null ? passed : 0.0) * 100.0 / total;
    }

    public Double getCourseAverageScore(UUID courseId) {
        return gradeRepository.calculateCourseAverageFinalScore(courseId);
    }

    public List<GradeDTO> getPassedStudentsInCourse(UUID courseId) {
        return gradeRepository.findPassedStudentsInCourse(courseId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<GradeDTO> getFailedStudentsInCourse(UUID courseId) {
        return gradeRepository.findFailedStudentsInCourse(courseId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<GradeDTO> getCourseGradesRanked(UUID courseId) {
        return gradeRepository.findCourseGradesOrderByFinalScoreDesc(courseId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<GradeDTO> getStudentGradesRanked(UUID studentId) {
        return gradeRepository.findStudentGradesOrderByFinalScoreDesc(studentId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private GradeDTO convertToDTO(Grade grade) {
        return new GradeDTO(grade);
    }

    private GradeDTO convertToDTOWithDetails(Grade grade) {
        GradeDTO dto = new GradeDTO(grade);

        try {
            StudentDTO student = userServiceClient.getStudentById(grade.getStudentId());
            dto.setStudent(student);
        } catch (Exception ignored) {
        }

        try {
            CourseDTO course = academicServiceClient.getCourseById(grade.getCourseId());
            dto.setCourse(course);
        } catch (Exception ignored) {
        }

        return dto;
    }
}
