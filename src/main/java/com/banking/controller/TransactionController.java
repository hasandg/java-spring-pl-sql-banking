package com.banking.controller;

import com.banking.dto.TransactionDto;
import com.banking.entity.Transaction;
import com.banking.mapper.TransactionMapper;
import com.banking.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Validated
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionDto> deposit(
            @RequestParam @NotBlank String accountNumber,
            @RequestParam @Positive BigDecimal amount,
            @RequestParam(required = false) String description) {
        Transaction transaction = transactionService.deposit(accountNumber, amount, description);
        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDto> withdraw(
            @RequestParam @NotBlank String accountNumber,
            @RequestParam @Positive BigDecimal amount,
            @RequestParam(required = false) String description) {
        Transaction transaction = transactionService.withdraw(accountNumber, amount, description);
        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> transfer(
            @RequestParam @NotBlank String fromAccountNumber,
            @RequestParam @NotBlank String toAccountNumber,
            @RequestParam @Positive BigDecimal amount,
            @RequestParam(required = false) String description) {
        Transaction transaction = transactionService.transfer(fromAccountNumber, toAccountNumber, amount, description);
        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionDto>> getAccountTransactions(@PathVariable @NotBlank String accountNumber) {
        List<Transaction> transactions = transactionService.getAccountTransactions(accountNumber);
        return ResponseEntity.ok(transactionMapper.toDtoList(transactions));
    }
} 