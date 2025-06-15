package com.banking.mapper;

import com.banking.dto.TransactionDto;
import com.banking.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return new TransactionDto(
            transaction.getId(),
            transaction.getAccount().getAccountNumber(),
            transaction.getTransactionType(),
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getTransactionDate(),
            transaction.getStatus()
        );
    }

    public List<TransactionDto> toDtoList(List<Transaction> transactions) {
        return transactions.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
} 