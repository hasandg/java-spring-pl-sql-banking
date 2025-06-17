package com.banking.service;

import com.banking.config.BankingProperties;
import com.banking.entity.AuditLog;
import com.banking.entity.Transaction;
import com.banking.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final BankingProperties bankingProperties;

    public AuditService(AuditLogRepository auditLogRepository, BankingProperties bankingProperties) {
        this.auditLogRepository = auditLogRepository;
        this.bankingProperties = bankingProperties;
    }

    public void logOperation(String operation, String accountNumber, String userId, String details, boolean success) {
        if (!bankingProperties.isEnableAuditLog()) {
            return;
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setOperation(operation);
        auditLog.setAccountNumber(accountNumber);
        auditLog.setUserId(userId);
        auditLog.setDetails(details);
        auditLog.setSuccess(success);

        auditLogRepository.save(auditLog);
    }

    public void logTransactionOperation(String operation, String accountNumber, Long transactionId, 
                                      BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter,
                                      String toAccountNumber, boolean success, String errorMessage) {
        if (!bankingProperties.isEnableAuditLog()) {
            return;
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setOperation(operation);
        auditLog.setAccountNumber(accountNumber);
        auditLog.setUserId("system");
        auditLog.setTransactionId(transactionId);
        auditLog.setAmount(amount);
        auditLog.setBalanceBefore(balanceBefore);
        auditLog.setBalanceAfter(balanceAfter);
        auditLog.setToAccountNumber(toAccountNumber);
        auditLog.setSuccess(success);
        auditLog.setErrorMessage(errorMessage);
        
        String details = String.format("Operation: %s, Amount: %s, Balance: %s -> %s", 
                                      operation, amount, balanceBefore, balanceAfter);
        if (toAccountNumber != null) {
            details += ", To Account: " + toAccountNumber;
        }
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }

    public void logSuccess(String operation, String accountNumber, String details) {
        logOperation(operation, accountNumber, "system", details, true);
    }

    public void logFailure(String operation, String accountNumber, String details) {
        logOperation(operation, accountNumber, "system", details, false);
    }

    public List<AuditLog> findMissingTransactions(String accountNumber, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Searching for missing transactions for account: {} between {} and {}", 
                accountNumber, startTime, endTime);
        
        return auditLogRepository.findByAccountNumberAndTimestampBetweenAndSuccessOrderByTimestamp(
                accountNumber, startTime, endTime, true);
    }

    public BigDecimal reconstructAccountBalance(String accountNumber, LocalDateTime asOfDate) {
        log.info("Reconstructing balance for account: {} as of {}", accountNumber, asOfDate);
        
        List<AuditLog> operations = auditLogRepository.findByAccountNumberAndTimestampBeforeAndSuccessOrderByTimestamp(
                accountNumber, asOfDate, true);
        
        if (operations.isEmpty()) {
            log.warn("No audit records found for account: {} before {}", accountNumber, asOfDate);
            return BigDecimal.ZERO;
        }
        
        AuditLog lastOperation = operations.get(operations.size() - 1);
        BigDecimal reconstructedBalance = lastOperation.getBalanceAfter();
        
        log.info("Reconstructed balance for account: {} as of {} is: {}", 
                accountNumber, asOfDate, reconstructedBalance);
        
        return reconstructedBalance != null ? reconstructedBalance : BigDecimal.ZERO;
    }

    public List<AuditLog> findDiscrepancies(String accountNumber, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Searching for discrepancies in account: {} between {} and {}", 
                accountNumber, startTime, endTime);
        
        return auditLogRepository.findByAccountNumberAndTimestampBetweenOrderByTimestamp(
                accountNumber, startTime, endTime);
    }

    public void logDataRecoveryOperation(String accountNumber, String recoveryDetails, boolean success) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperation("DATA_RECOVERY");
        auditLog.setAccountNumber(accountNumber);
        auditLog.setUserId("system");
        auditLog.setDetails(recoveryDetails);
        auditLog.setSuccess(success);
        
        auditLogRepository.save(auditLog);
        
        log.info("Data recovery operation logged for account: {}, Success: {}", accountNumber, success);
    }

    public List<AuditLog> getOperationHistory(String accountNumber, int limit) {
        List<AuditLog> allLogs = auditLogRepository.findByAccountNumberOrderByTimestampDesc(accountNumber);
        return allLogs.stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    public AuditLogRepository getAuditLogRepository() {
        return auditLogRepository;
    }
} 