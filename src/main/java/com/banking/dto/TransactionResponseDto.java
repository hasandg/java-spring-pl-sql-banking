package com.banking.dto;

import com.banking.entity.TransactionStatus;
import com.banking.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    
    private Long id;
    private String accountNumber;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;
    
    private TransactionStatus status;
} 