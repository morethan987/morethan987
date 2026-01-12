package com.example.GradeSystemBackend.dto;

import java.util.List;

public class BatchGradeUpdateDTO {

    public static class Request {
        private List<StudentGradeInputDTO> grades;

        public Request() {}

        public Request(List<StudentGradeInputDTO> grades) {
            this.grades = grades;
        }

        public List<StudentGradeInputDTO> getGrades() {
            return grades;
        }

        public void setGrades(List<StudentGradeInputDTO> grades) {
            this.grades = grades;
        }
    }

    public static class Response {
        private Boolean success;
        private Integer updatedCount;
        private List<String> errors;

        public Response() {}

        public Response(Boolean success, Integer updatedCount, List<String> errors) {
            this.success = success;
            this.updatedCount = updatedCount;
            this.errors = errors;
        }

        public static Response success(Integer updatedCount) {
            return new Response(true, updatedCount, null);
        }

        public static Response failure(List<String> errors) {
            return new Response(false, 0, errors);
        }

        public static Response partial(Integer updatedCount, List<String> errors) {
            return new Response(updatedCount > 0, updatedCount, errors);
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public Integer getUpdatedCount() {
            return updatedCount;
        }

        public void setUpdatedCount(Integer updatedCount) {
            this.updatedCount = updatedCount;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}
