package com.banking.service;

import com.banking.entity.Transaction;
import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction deposit(String accountNumber, BigDecimal amount, String description);
    Transaction withdraw(String accountNumber, BigDecimal amount, String description);
    Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description);
    List<Transaction> getAccountTransactions(String accountNumber);
} 