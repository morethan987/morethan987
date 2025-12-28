package com.example.GradeSystemBackend.config;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理数据库并发控制相关的异常
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理乐观锁异常
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(
            OptimisticLockingFailureException ex, WebRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.CONFLICT.value());
        errorDetails.put("error", "CONCURRENT_MODIFICATION");
        errorDetails.put("message", "数据已被其他用户修改，请刷新后重试");
        errorDetails.put("details", ex.getMessage());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        errorDetails.put("suggestion", "请刷新页面获取最新数据后重新提交");

        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    /**
     * 处理悲观锁异常
     */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handlePessimisticLockingFailure(
            PessimisticLockingFailureException ex, WebRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.LOCKED.value());
        errorDetails.put("error", "RESOURCE_LOCKED");
        errorDetails.put("message", "资源正在被其他用户使用，请稍后重试");
        errorDetails.put("details", ex.getMessage());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        errorDetails.put("suggestion", "请稍等片刻后重新尝试操作");

        return new ResponseEntity<>(errorDetails, HttpStatus.LOCKED);
    }

    /**
     * 处理数据完整性约束异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "DATA_INTEGRITY_VIOLATION");
        errorDetails.put("message", "数据完整性约束违反");
        errorDetails.put("details", extractUserFriendlyMessage(ex.getMessage()));
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理并发相关的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        // 检查是否是并发相关的异常
        if (isConcurrencyRelated(ex)) {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("timestamp", LocalDateTime.now());
            errorDetails.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            errorDetails.put("error", "CONCURRENCY_ISSUE");
            errorDetails.put("message", "系统繁忙，请稍后重试");
            errorDetails.put("details", ex.getMessage());
            errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
            errorDetails.put("suggestion", "请稍等片刻后重新尝试操作");

            return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
        }

        // 对于其他运行时异常，重新抛出让其他处理器处理
        throw ex;
    }

    /**
     * 提取用户友好的错误信息
     */
    private String extractUserFriendlyMessage(String originalMessage) {
        if (originalMessage == null) {
            return "数据操作失败";
        }

        // 处理常见的约束违反情况
        if (originalMessage.contains("duplicate key")) {
            return "数据重复，该记录已存在";
        } else if (originalMessage.contains("foreign key")) {
            return "关联数据不存在或已被删除";
        } else if (originalMessage.contains("not null")) {
            return "必填字段不能为空";
        } else if (originalMessage.contains("check constraint")) {
            return "数据不符合业务规则要求";
        }

        return "数据完整性约束违反";
    }

    /**
     * 判断异常是否与并发相关
     */
    private boolean isConcurrencyRelated(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }

        return message.toLowerCase().contains("concurrent") ||
               message.toLowerCase().contains("deadlock") ||
               message.toLowerCase().contains("lock") ||
               message.toLowerCase().contains("timeout") ||
               message.toLowerCase().contains("connection") ||
               ex.getCause() instanceof OptimisticLockingFailureException ||
               ex.getCause() instanceof PessimisticLockingFailureException;
    }
}
