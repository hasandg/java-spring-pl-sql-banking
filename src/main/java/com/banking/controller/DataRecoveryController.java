package com.banking.controller;

import com.banking.entity.AuditLog;
import com.banking.service.DataRecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/data-recovery")
@RequiredArgsConstructor
@Slf4j
public class DataRecoveryController {

    private final DataRecoveryService dataRecoveryService;

    @GetMapping("/missing-transactions/{accountNumber}")
    public ResponseEntity<List<AuditLog>> detectMissingTransactions(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Detecting missing transactions for account: {} between {} and {}", 
                accountNumber, startTime, endTime);
        
        List<AuditLog> missingTransactions = dataRecoveryService.detectMissingTransactions(
                accountNumber, startTime, endTime);
        
        return ResponseEntity.ok(missingTransactions);
    }

    @GetMapping("/validate-balance/{accountNumber}")
    public ResponseEntity<Map<String, Object>> validateAccountBalance(@PathVariable String accountNumber) {
        log.info("Validating balance for account: {}", accountNumber);
        
        boolean isValid = dataRecoveryService.validateAccountBalance(accountNumber);
        
        Map<String, Object> response = Map.of(
                "accountNumber", accountNumber,
                "balanceValid", isValid,
                "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recover-transaction/{auditLogId}")
    public ResponseEntity<Map<String, Object>> recoverMissingTransaction(@PathVariable Long auditLogId) {
        log.info("Attempting to recover transaction from audit log: {}", auditLogId);
        
        boolean recovered = dataRecoveryService.recoverMissingTransaction(auditLogId);
        
        Map<String, Object> response = Map.of(
                "auditLogId", auditLogId,
                "recovered", recovered,
                "timestamp", LocalDateTime.now(),
                "message", recovered ? "Transaction recovered successfully" : "Manual intervention required"
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/integrity-report/{accountNumber}")
    public ResponseEntity<List<AuditLog>> generateDataIntegrityReport(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Generating data integrity report for account: {} between {} and {}", 
                accountNumber, startTime, endTime);
        
        List<AuditLog> report = dataRecoveryService.generateDataIntegrityReport(
                accountNumber, startTime, endTime);
        
        return ResponseEntity.ok(report);
    }

    @PostMapping("/integrity-check/{accountNumber}")
    public ResponseEntity<Map<String, String>> performDataIntegrityCheck(@PathVariable String accountNumber) {
        log.info("Performing comprehensive data integrity check for account: {}", accountNumber);
        
        dataRecoveryService.performDataIntegrityCheck(accountNumber);
        
        Map<String, String> response = Map.of(
                "accountNumber", accountNumber,
                "status", "Data integrity check completed",
                "timestamp", LocalDateTime.now().toString(),
                "message", "Check audit logs for detailed results"
        );
        
        return ResponseEntity.ok(response);
    }
} 