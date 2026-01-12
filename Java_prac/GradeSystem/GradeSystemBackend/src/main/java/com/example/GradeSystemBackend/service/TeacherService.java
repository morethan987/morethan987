package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.grade.Grade;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.domain.student.Student;
import com.example.GradeSystemBackend.domain.teachingclass.TeachingClass;
import com.example.GradeSystemBackend.dto.BatchGradeUpdateDTO;
import com.example.GradeSystemBackend.dto.DistributionDataDTO;
import com.example.GradeSystemBackend.dto.StudentGradeInputDTO;
import com.example.GradeSystemBackend.dto.TeacherDTO;
import com.example.GradeSystemBackend.dto.TeachingClassWithStatsDTO;
import com.example.GradeSystemBackend.repository.GradeRepository;
import com.example.GradeSystemBackend.repository.StudentRepository;
import com.example.GradeSystemBackend.repository.TeacherRepository;
import com.example.GradeSystemBackend.repository.TeachingClassRepository;
import com.example.GradeSystemBackend.repository.UserProfileRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherService {

    @Autowired
    private TeachingClassRepository teachingClassRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    public TeacherDTO getTeacherByUserId(UUID userId) {
        return teacherRepository
            .findByUserId(userId)
            .map(TeacherDTO::new)
            .orElseThrow(() -> new RuntimeException("教师不存在: " + userId));
    }

    public List<TeachingClassWithStatsDTO> getTeachingClasses(UUID teacherId) {
        List<TeachingClass> teachingClasses =
            teachingClassRepository.findByTeacherId(teacherId);

        return teachingClasses
            .stream()
            .map(tc -> {
                TeachingClassWithStatsDTO dto = new TeachingClassWithStatsDTO();
                dto.setId(tc.getId());
                dto.setClassName(tc.getName());
                dto.setCourseName(
                    tc.getCourse() != null ? tc.getCourse().getName() : null
                );
                dto.setCourseType(
                    tc.getCourse() != null
                        ? tc.getCourse().getCourseType()
                        : null
                );
                dto.setCredit(
                    tc.getCourse() != null ? tc.getCourse().getCredit() : null
                );
                dto.setStudentCount(tc.getEnrolledCount());
                dto.setSemester(
                    tc.getCourse() != null
                        ? String.valueOf(tc.getCourse().getSemester())
                        : null
                );
                dto.setSchedule(tc.getTimeSchedule());
                dto.setLocation(tc.getClassroom());
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<StudentGradeInputDTO> getStudentsInTeachingClass(
        UUID teachingClassId
    ) {
        TeachingClass teachingClass = teachingClassRepository
            .findById(teachingClassId)
            .orElseThrow(() ->
                new RuntimeException("教学班不存在: " + teachingClassId)
            );

        List<Student> students = studentRepository.findByTeachingClassId(
            teachingClassId
        );
        UUID courseId =
            teachingClass.getCourse() != null
                ? teachingClass.getCourse().getId()
                : null;

        return students
            .stream()
            .map(student -> {
                StudentGradeInputDTO dto = new StudentGradeInputDTO();
                dto.setStudentCode(student.getStudentCode());
                dto.setName(getStudentRealName(student));
                dto.setClassName(student.getClassName());

                if (courseId != null) {
                    Optional<Grade> gradeOpt =
                        gradeRepository.findByStudentAndCourse(
                            student,
                            teachingClass.getCourse()
                        );
                    if (gradeOpt.isPresent()) {
                        Grade grade = gradeOpt.get();
                        dto.setId(grade.getId());
                        dto.setUsualScore(grade.getUsualScore());
                        dto.setMidtermScore(grade.getMidScore());
                        dto.setFinalExamScore(grade.getFinalExamScore());
                        dto.setExperimentScore(grade.getExperimentScore());
                        dto.setFinalScore(grade.getFinalScore());
                        dto.setGpa(grade.getGpa());
                        dto.setVersion(grade.getVersion());
                    }
                }
                dto.setIsModified(false);
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<DistributionDataDTO> getGradeDistribution(
        UUID teachingClassId
    ) {
        TeachingClass teachingClass = teachingClassRepository
            .findById(teachingClassId)
            .orElseThrow(() ->
                new RuntimeException("教学班不存在: " + teachingClassId)
            );

        if (teachingClass.getCourse() == null) {
            return new ArrayList<>();
        }

        List<Grade> grades = gradeRepository.findByCourseId(
            teachingClass.getCourse().getId()
        );

        List<Student> students = studentRepository.findByTeachingClassId(
            teachingClassId
        );
        List<UUID> studentIds = students
            .stream()
            .map(Student::getId)
            .collect(Collectors.toList());

        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("90-100", 0);
        distribution.put("80-89", 0);
        distribution.put("70-79", 0);
        distribution.put("60-69", 0);
        distribution.put("0-59", 0);

        for (Grade grade : grades) {
            if (
                grade.getStudent() != null &&
                studentIds.contains(grade.getStudent().getId()) &&
                grade.getFinalScore() != null
            ) {
                Double score = grade.getFinalScore();
                if (score >= 90) {
                    distribution.merge("90-100", 1, Integer::sum);
                } else if (score >= 80) {
                    distribution.merge("80-89", 1, Integer::sum);
                } else if (score >= 70) {
                    distribution.merge("70-79", 1, Integer::sum);
                } else if (score >= 60) {
                    distribution.merge("60-69", 1, Integer::sum);
                } else {
                    distribution.merge("0-59", 1, Integer::sum);
                }
            }
        }

        return distribution
            .entrySet()
            .stream()
            .map(entry ->
                new DistributionDataDTO(entry.getKey(), entry.getValue())
            )
            .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BatchGradeUpdateDTO.Response batchUpdateGrades(
        UUID teachingClassId,
        List<StudentGradeInputDTO> grades
    ) {
        TeachingClass teachingClass = teachingClassRepository
            .findById(teachingClassId)
            .orElseThrow(() ->
                new RuntimeException("教学班不存在: " + teachingClassId)
            );

        if (teachingClass.getCourse() == null) {
            return BatchGradeUpdateDTO.Response.failure(
                List.of("教学班没有关联课程")
            );
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (StudentGradeInputDTO gradeInput : grades) {
            try {
                if (gradeInput.getId() == null) {
                    continue;
                }

                Grade grade = gradeRepository
                    .findById(gradeInput.getId())
                    .orElse(null);

                if (grade == null) {
                    errors.add("成绩记录不存在: " + gradeInput.getId());
                    continue;
                }

                if (
                    gradeInput.getVersion() != null &&
                    !gradeInput.getVersion().equals(grade.getVersion())
                ) {
                    errors.add(
                        "学生 " +
                            gradeInput.getStudentCode() +
                            " 的成绩已被其他用户修改，请刷新后重试"
                    );
                    continue;
                }

                grade.setUsualScore(gradeInput.getUsualScore());
                grade.setMidScore(gradeInput.getMidtermScore());
                grade.setExperimentScore(gradeInput.getExperimentScore());
                grade.setFinalExamScore(gradeInput.getFinalExamScore());

                gradeRepository.save(grade);
                successCount++;
            } catch (OptimisticLockingFailureException e) {
                errors.add(
                    "学生 " +
                        gradeInput.getStudentCode() +
                        " 的成绩更新发生并发冲突"
                );
            } catch (Exception e) {
                errors.add(
                    "学生 " +
                        gradeInput.getStudentCode() +
                        " 的成绩更新失败: " +
                        e.getMessage()
                );
            }
        }

        if (errors.isEmpty()) {
            return BatchGradeUpdateDTO.Response.success(successCount);
        } else if (successCount > 0) {
            return BatchGradeUpdateDTO.Response.partial(successCount, errors);
        } else {
            return BatchGradeUpdateDTO.Response.failure(errors);
        }
    }

    public byte[] exportGrades(UUID teachingClassId) throws IOException {
        teachingClassRepository
            .findById(teachingClassId)
            .orElseThrow(() ->
                new RuntimeException("教学班不存在: " + teachingClassId)
            );

        List<StudentGradeInputDTO> students = getStudentsInTeachingClass(
            teachingClassId
        );

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("成绩表");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "学号",
                "姓名",
                "班级",
                "平时成绩",
                "期中成绩",
                "实验成绩",
                "期末成绩",
                "总评成绩",
                "绩点",
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (StudentGradeInputDTO student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentCode());
                row
                    .createCell(1)
                    .setCellValue(
                        student.getName() != null ? student.getName() : ""
                    );
                row
                    .createCell(2)
                    .setCellValue(
                        student.getClassName() != null
                            ? student.getClassName()
                            : ""
                    );
                setCellValue(row.createCell(3), student.getUsualScore());
                setCellValue(row.createCell(4), student.getMidtermScore());
                setCellValue(row.createCell(5), student.getExperimentScore());
                setCellValue(row.createCell(6), student.getFinalExamScore());
                setCellValue(row.createCell(7), student.getFinalScore());
                setCellValue(row.createCell(8), student.getGpa());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BatchGradeUpdateDTO.Response importGrades(
        UUID teachingClassId,
        MultipartFile file
    ) throws IOException {
        TeachingClass teachingClass = teachingClassRepository
            .findById(teachingClassId)
            .orElseThrow(() ->
                new RuntimeException("教学班不存在: " + teachingClassId)
            );

        if (teachingClass.getCourse() == null) {
            return BatchGradeUpdateDTO.Response.failure(
                List.of("教学班没有关联课程")
            );
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                try {
                    String studentCode = getCellStringValue(row.getCell(0));
                    if (studentCode == null || studentCode.trim().isEmpty()) {
                        continue;
                    }

                    Optional<Student> studentOpt =
                        studentRepository.findByStudentCode(studentCode.trim());
                    if (studentOpt.isEmpty()) {
                        errors.add("学号 " + studentCode + " 不存在");
                        continue;
                    }

                    Student student = studentOpt.get();

                    Optional<Grade> gradeOpt =
                        gradeRepository.findByStudentAndCourse(
                            student,
                            teachingClass.getCourse()
                        );

                    if (gradeOpt.isEmpty()) {
                        errors.add(
                            "学号 " + studentCode + " 在该课程没有成绩记录"
                        );
                        continue;
                    }

                    Grade grade = gradeOpt.get();

                    Double usualScore = getCellDoubleValue(row.getCell(3));
                    Double midtermScore = getCellDoubleValue(row.getCell(4));
                    Double experimentScore = getCellDoubleValue(row.getCell(5));
                    Double finalExamScore = getCellDoubleValue(row.getCell(6));

                    grade.setUsualScore(usualScore);
                    grade.setMidScore(midtermScore);
                    grade.setExperimentScore(experimentScore);
                    grade.setFinalExamScore(finalExamScore);

                    gradeRepository.save(grade);
                    successCount++;
                } catch (Exception e) {
                    errors.add(
                        "第 " + (rowNum + 1) + " 行处理失败: " + e.getMessage()
                    );
                }
            }
        }

        if (errors.isEmpty()) {
            return BatchGradeUpdateDTO.Response.success(successCount);
        } else if (successCount > 0) {
            return BatchGradeUpdateDTO.Response.partial(successCount, errors);
        } else {
            return BatchGradeUpdateDTO.Response.failure(errors);
        }
    }

    private String getStudentRealName(Student student) {
        if (student.getUser() == null) {
            return null;
        }
        return userProfileRepository
            .findByUserId(student.getUser().getId())
            .map(UserProfile::getRealName)
            .orElse(null);
    }

    private void setCellValue(Cell cell, Double value) {
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }

    private Double getCellDoubleValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
