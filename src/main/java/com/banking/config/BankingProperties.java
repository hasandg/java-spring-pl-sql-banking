package com.banking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "banking")
public class BankingProperties {
    
    private BigDecimal maxTransactionAmount = new BigDecimal("10000.00");
    private int accountNumberLength = 12;
    private boolean enableAuditLog = true;
    private int maxDailyTransactions = 100;

    public BigDecimal getMaxTransactionAmount() {
        return maxTransactionAmount;
    }

    public void setMaxTransactionAmount(BigDecimal maxTransactionAmount) {
        this.maxTransactionAmount = maxTransactionAmount;
    }

    public int getAccountNumberLength() {
        return accountNumberLength;
    }

    public void setAccountNumberLength(int accountNumberLength) {
        this.accountNumberLength = accountNumberLength;
    }

    public boolean isEnableAuditLog() {
        return enableAuditLog;
    }

    public void setEnableAuditLog(boolean enableAuditLog) {
        this.enableAuditLog = enableAuditLog;
    }

    public int getMaxDailyTransactions() {
        return maxDailyTransactions;
    }

    public void setMaxDailyTransactions(int maxDailyTransactions) {
        this.maxDailyTransactions = maxDailyTransactions;
    }
} 