package com.banking.service;

import com.banking.config.BankingProperties;
import com.banking.entity.AuditLog;
import com.banking.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
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

    public void logSuccess(String operation, String accountNumber, String details) {
        logOperation(operation, accountNumber, "system", details, true);
    }

    public void logFailure(String operation, String accountNumber, String details) {
        logOperation(operation, accountNumber, "system", details, false);
    }
} 