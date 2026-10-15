package com.webliix.shared.exceptions;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import com.webliix.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditService auditService;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(
            ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(
            BusinessException ex) {

        log.warn("Business exception: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Invalid request payload");

        log.warn("Validation failure: {}", errorMsg);
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message(errorMsg)
                        .build());
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {

        log.warn("HTTP message not readable: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Malformed JSON or invalid request format")
                        .build());
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResource(
            org.springframework.web.servlet.resource.NoResourceFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Endpoint or resource not found")
                        .build());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxUploadSize(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex) {

        log.warn("Max upload size exceeded: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Uploaded file size exceeds the maximum allowed limit of 50MB. Please upload a smaller file.")
                        .build());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {

        String requiredPermission = (String) request.getAttribute("REQUIRED_PERMISSION");
        if (requiredPermission == null && ex.getMessage() != null && ex.getMessage().contains("Required permission: ")) {
            requiredPermission = ex.getMessage().substring(ex.getMessage().indexOf("Required permission: ") + "Required permission: ".length()).trim();
        }
        if (requiredPermission == null) {
            requiredPermission = "ACCESS_DENIED";
        }

        try {
            auditService.record(
                    AuditAction.ACCESS_DENIED,
                    AuditModule.AUTH,
                    null,
                    null,
                    null,
                    null,
                    "Denied at " + request.getRequestURI() + " (Required: " + requiredPermission + ")",
                    request
            );
        } catch (Exception auditEx) {
            log.warn("Failed to record access denied audit log: {}", auditEx.getMessage());
        }

        log.warn("Access denied for URI {}: Required permission: {}", request.getRequestURI(), requiredPermission);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.builder()
                        .success(false)
                        .code("FORBIDDEN")
                        .message("Access denied. You do not possess the required permission.")
                        .requiredPermission(requiredPermission)
                        .build());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<?>> handleIllegalArguments(
            RuntimeException ex) {

        log.warn("Illegal argument/state exception: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage() != null ? ex.getMessage() : "Invalid operation or request state")
                        .build());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleDatabaseException(
            DataAccessException ex) {

        log.error("Database error occurred: ", ex);
        String causeMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Database error: " + (causeMsg != null ? causeMsg : "Data access error"))
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(
            Exception ex) {

        log.error("Unhandled server exception occurred: ", ex);
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                message = ex.getCause().getMessage();
            } else {
                message = ex.getClass().getSimpleName();
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(message != null ? message : "An unexpected server error occurred")
                        .build());
    }
}
