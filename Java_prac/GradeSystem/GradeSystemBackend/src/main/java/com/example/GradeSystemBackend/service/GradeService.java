package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.grade.Grade;
import com.example.GradeSystemBackend.dto.CourseDTO;
import com.example.GradeSystemBackend.dto.GradeDTO;
import com.example.GradeSystemBackend.dto.StudentDTO;
import com.example.GradeSystemBackend.repository.GradeRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    /**
     * 获取某学生当前的总GPA
     * @param studentId
     * @return {@link Double} GPA值，若无成绩则返回null
     */
    public Double getStudentGpa(UUID studentId) {
        Double totalCredits = gradeRepository.calculateStudentTotalCredits(
            studentId
        );
        Double weightedGpa = gradeRepository.calculateStudentWeightedGpa(
            studentId
        );

        if (totalCredits > 0) {
            return weightedGpa / totalCredits;
        } else {
            return null;
        }
    }

    /**
     * 获取某学生当前总分
     * @param studentId
     * @return
     */
    public Double getStudentScore(UUID studentId) {
        Double totalCredits = gradeRepository.calculateStudentTotalCredits(
            studentId
        );
        Double weightedScore = gradeRepository.calculateStudentWeightedScore(
            studentId
        );

        if (weightedScore > 0) {
            return totalCredits / weightedScore;
        } else {
            return null;
        }
    }

    /**
     * 获取某学生某学期的平均GPA
     * @param studentId
     * @param semester
     * @return {@link Double} GPA值，若无成绩则返回null
     */
    public Double getStudentGpaBySemester(UUID studentId, String semester) {
        Double totalCredits =
            gradeRepository.calculateStudentTotalCreditsBySemester(
                studentId,
                semester
            );
        Double weightedGpa =
            gradeRepository.calculateStudentWeightedGpaBySemester(
                studentId,
                semester
            );

        if (totalCredits > 0) {
            return weightedGpa / totalCredits;
        } else {
            return null;
        }
    }

    /**
     * 计算某学生某学期的平均分
     * @param studentId
     * @param semester
     * @return
     */
    public Double getStudentScoreBySemester(UUID studentId, String semester) {
        Double totalCredits =
            gradeRepository.calculateStudentTotalCreditsBySemester(
                studentId,
                semester
            );
        Double weightedScore =
            gradeRepository.calculateStudentWeightedScoreBySemester(
                studentId,
                semester
            );

        if (weightedScore > 0) {
            return totalCredits / weightedScore;
        } else {
            return null;
        }
    }

    /**
     * 计算某学生某学期之前的学期的总分
     * @param studentId
     * @param semester
     * @return
     */
    public Double getStudentScoreBeforeSemester(
        UUID studentId,
        String semester
    ) {
        Double totalCredits =
            gradeRepository.calculateStudentTotalCreditsBeforeSemester(
                studentId,
                semester
            );
        Double weightedScore =
            gradeRepository.calculateStudentWeightedScoreBeforeSemester(
                studentId,
                semester
            );

        if (weightedScore > 0) {
            return totalCredits / weightedScore;
        } else {
            return null;
        }
    }

    /**
     * 获取某学生某学期之前的平均GPA
     * @param studentId
     * @param semester
     * @return {@link Double} GPA值，若无成绩则返回null
     */
    public Double getStudentGpaBeforeSemester(UUID studentId, String semester) {
        Double totalCredits =
            gradeRepository.calculateStudentTotalCreditsBeforeSemester(
                studentId,
                semester
            );
        Double weightedGpa =
            gradeRepository.calculateStudentWeightedGpaBeforeSemester(
                studentId,
                semester
            );

        if (totalCredits > 0) {
            return weightedGpa / totalCredits;
        } else {
            return null;
        }
    }

    /**
     * 获取学生成绩列表
     */
    public List<GradeDTO> getStudentGrades(
        UUID studentId,
        String semester,
        String courseType
    ) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);

        return grades
            .stream()
            .filter(
                grade ->
                    semester == null ||
                    grade.getCourse().getSemester().toString().equals(semester)
            )
            .filter(
                grade ->
                    courseType == null ||
                    grade.getCourse().getCourseType().name().equals(courseType)
            )
            .map(this::convertToGradeDTO)
            .collect(Collectors.toList());
    }

    /**
     * 获取学生成绩统计信息
     */
    public Map<String, Object> getStudentGradeStats(UUID studentId) {
        Map<String, Object> stats = new HashMap<>();

        Double totalCredits = gradeRepository.calculateStudentTotalCredits(
            studentId
        );
        Double averageGPA = getStudentGpa(studentId);
        Double averageScore = getStudentScore(studentId);
        Long passedCourses = gradeRepository.countPassedCoursesByStudentId(
            studentId
        );

        stats.put("totalCredits", totalCredits != null ? totalCredits : 0.0);
        stats.put("averageGPA", averageGPA != null ? averageGPA : 0.0);
        stats.put("averageScore", averageScore != null ? averageScore : 0.0);
        stats.put("passedCourses", passedCourses != null ? passedCourses : 0L);

        return stats;
    }

    /**
     * 获取学生指定学期的成绩统计
     */
    public Map<String, Object> getStudentSemesterStats(
        UUID studentId,
        String semester
    ) {
        Map<String, Object> stats = new HashMap<>();

        Double totalCredits =
            gradeRepository.calculateStudentTotalCreditsBySemester(
                studentId,
                semester
            );
        Double averageGPA = getStudentGpaBySemester(studentId, semester);
        Double averageScore = getStudentScoreBySemester(studentId, semester);

        stats.put("totalCredits", totalCredits != null ? totalCredits : 0.0);
        stats.put("averageGPA", averageGPA != null ? averageGPA : 0.0);
        stats.put("averageScore", averageScore != null ? averageScore : 0.0);

        return stats;
    }

    /**
     * 获取学生所有学期列表
     */
    public List<String> getStudentSemesters(UUID studentId) {
        return gradeRepository.findDistinctSemestersByStudentId(studentId);
    }

    /**
     * 根据ID获取成绩
     */
    public GradeDTO getGradeById(UUID gradeId) {
        Grade grade = gradeRepository.findById(gradeId).orElse(null);
        if (grade != null) {
            return convertToGradeDTO(grade);
        }
        return null;
    }

    /**
     * 更新成绩
     */
    public GradeDTO updateGrade(UUID gradeId, GradeDTO gradeDTO) {
        Grade grade = gradeRepository.findById(gradeId).orElse(null);
        if (grade != null) {
            // 更新成绩字段
            grade.setUsualScore(gradeDTO.getUsualScore());
            grade.setMidScore(gradeDTO.getMidtermScore());
            grade.setExperimentScore(gradeDTO.getExperimentScore());
            grade.setFinalExamScore(gradeDTO.getFinalExamScore());
            // finalScore 和 gpa 会通过 @PrePersist/@PreUpdate 自动计算

            Grade savedGrade = gradeRepository.save(grade);
            return convertToGradeDTO(savedGrade);
        }
        return null;
    }

    /**
     * 将Grade实体转换为DTO
     */
    private GradeDTO convertToGradeDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());

        // 转换Student信息
        if (grade.getStudent() != null) {
            StudentDTO studentDTO = new StudentDTO();
            studentDTO.setId(grade.getStudent().getId());
            studentDTO.setStudentCode(grade.getStudent().getStudentCode());
            studentDTO.setMajor(grade.getStudent().getMajor());
            studentDTO.setClassName(grade.getStudent().getClassName());
            studentDTO.setEnrollmentYear(
                grade.getStudent().getEnrollmentYear()
            );
            studentDTO.setCurrentSemester(
                grade.getStudent().getCurrentSemester()
            );
            studentDTO.setStatus(grade.getStudent().getStatus());
            studentDTO.setTotalCredits(grade.getStudent().getTotalCredits());
            studentDTO.setCreatedAt(grade.getStudent().getCreatedAt());
            studentDTO.setUpdatedAt(grade.getStudent().getUpdatedAt());

            dto.setStudent(studentDTO);
        }

        // 转换Course信息
        if (grade.getCourse() != null) {
            CourseDTO courseDTO = new CourseDTO();
            courseDTO.setId(grade.getCourse().getId());
            courseDTO.setName(grade.getCourse().getName());
            courseDTO.setDescription(grade.getCourse().getDescription());
            courseDTO.setCredit(grade.getCourse().getCredit());
            courseDTO.setSemester(grade.getCourse().getSemester());
            courseDTO.setCourseType(grade.getCourse().getCourseType());
            dto.setCourse(courseDTO);
        }

        // 设置成绩信息
        dto.setUsualScore(grade.getUsualScore());
        dto.setMidtermScore(grade.getMidScore());
        dto.setFinalExamScore(grade.getFinalExamScore());
        dto.setExperimentScore(grade.getExperimentScore());
        dto.setFinalScore(grade.getFinalScore());
        dto.setGpa(grade.getGpa());

        return dto;
    }
}
