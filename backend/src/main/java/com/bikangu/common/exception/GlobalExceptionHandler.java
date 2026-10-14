package com.bikangu.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BikanguException.class)
    public ResponseEntity<ErrorResponse> handleBikanguException(BikanguException ex, HttpServletRequest request) {
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getHttpStatus())
                .error(HttpStatus.valueOf(ex.getHttpStatus()).getReasonPhrase())
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .details(errors)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(path, message);
        });

        log.warn("Constraint violation: {}", errors);
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .code("VALIDATION_ERROR")
                .message("Constraint validation failed")
                .details(errors)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation", ex);
        String message = "Data integrity violation";
        if (ex.getMessage() != null && ex.getMessage().contains("duplicate key")) {
            message = "A record with this value already exists";
        }
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .code("DATA_INTEGRITY_VIOLATION")
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Invalid request body: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .code("INVALID_REQUEST_BODY")
                .message("Invalid or malformed request body")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .code("TYPE_MISMATCH")
                .message("Invalid parameter type: " + ex.getName())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .code("INVALID_CREDENTIALS")
                .message("Invalid email or password")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .code("ACCESS_DENIED")
                .message("You do not have permission to access this resource")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String code,
            String message,
            Map<String, String> details,
            String path
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Instant timestamp;
            private int status;
            private String error;
            private String code;
            private String message;
            private Map<String, String> details;
            private String path;

            public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
            public Builder status(int status) { this.status = status; return this; }
            public Builder error(String error) { this.error = error; return this; }
            public Builder code(String code) { this.code = code; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder details(Map<String, String> details) { this.details = details; return this; }
            public Builder path(String path) { this.path = path; return this; }

            public ErrorResponse build() {
                return new ErrorResponse(timestamp, status, error, code, message, details, path);
            }
        }
    }
}