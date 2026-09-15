package com.webliix.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.webliix.shared.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(
            BusinessException ex) {

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

        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message(errorMsg)
                        .build());
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {

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

        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Uploaded file size exceeds the maximum allowed limit of 50MB. Please upload a smaller file.")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(
            Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred")
                        .build());
    }
}
