package com.banking.service;

import com.banking.entity.AuditLog;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DataRecoveryService {

    private final AuditService auditService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DataRecoveryService(AuditService auditService, 
                              AccountRepository accountRepository,
                              TransactionRepository transactionRepository) {
        this.auditService = auditService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<AuditLog> detectMissingTransactions(String accountNumber, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Detecting missing transactions for account: {} between {} and {}", 
                accountNumber, startTime, endTime);
        
        List<AuditLog> auditLogs = auditService.findMissingTransactions(accountNumber, startTime, endTime);
        
        for (AuditLog auditLog : auditLogs) {
            if (auditLog.getTransactionId() != null) {
                Optional<Transaction> transaction = transactionRepository.findById(auditLog.getTransactionId());
                if (transaction.isEmpty()) {
                    log.warn("Missing transaction detected! Audit Log ID: {}, Transaction ID: {}, Account: {}", 
                            auditLog.getId(), auditLog.getTransactionId(), accountNumber);
                    auditService.logDataRecoveryOperation(accountNumber, 
                            "Missing transaction detected: " + auditLog.getTransactionId(), false);
                }
            }
        }
        
        return auditLogs;
    }

    public boolean validateAccountBalance(String accountNumber) {
        log.info("Validating account balance for: {}", accountNumber);
        
        try {
            Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isEmpty()) {
                log.error("Account not found: {}", accountNumber);
                return false;
            }
            
            Account account = accountOpt.get();
            BigDecimal currentBalance = account.getBalance();
            
            BigDecimal reconstructedBalance = auditService.reconstructAccountBalance(accountNumber, LocalDateTime.now());
            
            boolean balancesMatch = currentBalance.compareTo(reconstructedBalance) == 0;
            
            if (!balancesMatch) {
                log.error("Balance discrepancy detected for account: {}. Current: {}, Reconstructed: {}", 
                         accountNumber, currentBalance, reconstructedBalance);
                
                auditService.logDataRecoveryOperation(accountNumber, 
                        String.format("Balance discrepancy: Current=%s, Reconstructed=%s", 
                                     currentBalance, reconstructedBalance), false);
            } else {
                log.info("Balance validation successful for account: {}", accountNumber);
            }
            
            return balancesMatch;
            
        } catch (Exception e) {
            log.error("Error validating balance for account: {}", accountNumber, e);
            auditService.logDataRecoveryOperation(accountNumber, 
                    "Balance validation failed: " + e.getMessage(), false);
            return false;
        }
    }

    @Transactional
    public boolean recoverMissingTransaction(Long auditLogId) {
        log.info("Attempting to recover missing transaction from audit log ID: {}", auditLogId);
        
        try {
            Optional<AuditLog> auditLogOpt = auditService.getAuditLogRepository().findById(auditLogId);
            if (auditLogOpt.isEmpty()) {
                log.error("Audit log not found: {}", auditLogId);
                return false;
            }
            
            AuditLog auditLog = auditLogOpt.get();
            
            if (auditLog.getTransactionId() != null) {
                Optional<Transaction> existingTransaction = transactionRepository.findById(auditLog.getTransactionId());
                if (existingTransaction.isPresent()) {
                    log.info("Transaction already exists, no recovery needed: {}", auditLog.getTransactionId());
                    return true;
                }
            }
            
            log.warn("Transaction recovery requires manual intervention for audit log: {}", auditLogId);
            auditService.logDataRecoveryOperation(auditLog.getAccountNumber(), 
                    "Manual transaction recovery required for audit log: " + auditLogId, false);
            
            return false;
            
        } catch (Exception e) {
            log.error("Error recovering transaction from audit log: {}", auditLogId, e);
            return false;
        }
    }

    public List<AuditLog> generateDataIntegrityReport(String accountNumber, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Generating data integrity report for account: {} between {} and {}", 
                accountNumber, startTime, endTime);
        
        List<AuditLog> discrepancies = auditService.findDiscrepancies(accountNumber, startTime, endTime);
        
        log.info("Found {} potential discrepancies for account: {}", discrepancies.size(), accountNumber);
        
        auditService.logDataRecoveryOperation(accountNumber, 
                String.format("Data integrity report generated: %d items found", discrepancies.size()), true);
        
        return discrepancies;
    }

    public void performDataIntegrityCheck(String accountNumber) {
        log.info("Performing comprehensive data integrity check for account: {}", accountNumber);
        
        boolean balanceValid = validateAccountBalance(accountNumber);
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<AuditLog> missingTransactions = detectMissingTransactions(accountNumber, thirtyDaysAgo, LocalDateTime.now());
        
        List<AuditLog> discrepancies = generateDataIntegrityReport(accountNumber, thirtyDaysAgo, LocalDateTime.now());
        
        String reportSummary = String.format(
                "Data integrity check completed. Balance Valid: %s, Missing Transactions: %d, Discrepancies: %d",
                balanceValid, missingTransactions.size(), discrepancies.size());
        
        auditService.logDataRecoveryOperation(accountNumber, reportSummary, balanceValid);
        
        log.info("Data integrity check completed for account: {}", accountNumber);
    }
} 