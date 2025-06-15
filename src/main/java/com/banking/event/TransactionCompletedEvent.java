package com.banking.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class TransactionCompletedEvent extends ApplicationEvent {
    
    private final String accountNumber;
    private final String transactionType;
    private final BigDecimal amount;
    private final Long transactionId;
    
    public TransactionCompletedEvent(Object source, String accountNumber, 
                                   String transactionType, BigDecimal amount, Long transactionId) {
        super(source);
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionId = transactionId;
    }
} 