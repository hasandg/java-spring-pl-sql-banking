package com.banking.exception;

import com.banking.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class BankingExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        String path = extractPath(request);
        
        log.warn("Account not found - CorrelationId: {}, Path: {}, Message: {}",
                correlationId, path, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "ACCOUNT_NOT_FOUND",
            HttpStatus.NOT_FOUND.value(),
            path
        );
        error.setCorrelationId(correlationId);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        String path = extractPath(request);
        
        log.warn("Insufficient funds - CorrelationId: {}, Path: {}, Message: {}",
                correlationId, path, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "INSUFFICIENT_FUNDS",
            HttpStatus.BAD_REQUEST.value(),
            path
        );
        error.setCorrelationId(correlationId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BankingOperationException.class)
    public ResponseEntity<ErrorResponse> handleBankingOperation(BankingOperationException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        String path = extractPath(request);
        
        log.error("Banking operation failed - CorrelationId: {}, Path: {}, Message: {}",
                correlationId, path, ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "BANKING_OPERATION_FAILED",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            path
        );
        error.setCorrelationId(correlationId);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        String path = extractPath(request);
        
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Validation failed");
        
        log.warn("Validation failed - CorrelationId: {}, Path: {}, Errors: {}",
                correlationId, path, message);
            
        ErrorResponse error = new ErrorResponse(
            message,
            "VALIDATION_ERROR",
            HttpStatus.BAD_REQUEST.value(),
            path
        );
        error.setCorrelationId(correlationId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        String path = extractPath(request);
        
        log.warn("Constraint violation - CorrelationId: {}, Path: {}, Message: {}",
                correlationId, path, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "CONSTRAINT_VIOLATION",
            HttpStatus.BAD_REQUEST.value(),
            path
        );
        error.setCorrelationId(correlationId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        String path = extractPath(request);
        
        log.error("Unexpected error occurred - CorrelationId: {}, Path: {}, Exception: {}",
                correlationId, path, ex.getClass().getSimpleName(), ex);
        
        ErrorResponse error = new ErrorResponse(
            "An unexpected error occurred. Please contact support with correlation ID: " + correlationId,
            "INTERNAL_SERVER_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            path
        );
        error.setCorrelationId(correlationId);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
} 