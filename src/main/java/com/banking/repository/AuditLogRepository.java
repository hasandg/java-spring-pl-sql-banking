package com.banking.repository;

import com.banking.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByAccountNumberOrderByTimestampDesc(String accountNumber);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByOperationAndTimestampBetween(String operation, LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByAccountNumberAndTimestampBetweenAndSuccessOrderByTimestamp(String accountNumber, LocalDateTime start, LocalDateTime end, Boolean success);
    List<AuditLog> findByAccountNumberAndTimestampBeforeAndSuccessOrderByTimestamp(String accountNumber, LocalDateTime before, Boolean success);
    List<AuditLog> findByAccountNumberAndTimestampBetweenOrderByTimestamp(String accountNumber, LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByTransactionId(Long transactionId);
    List<AuditLog> findByAccountNumberAndOperationAndTimestampBetween(String accountNumber, String operation, LocalDateTime start, LocalDateTime end);
} 