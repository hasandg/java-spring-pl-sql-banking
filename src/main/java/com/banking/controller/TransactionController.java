package com.banking.controller;

import com.banking.dto.TransactionRequestDto;
import com.banking.dto.TransactionResponseDto;
import com.banking.entity.Transaction;
import com.banking.mapper.TransactionMapper;
import com.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Validated
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDto> deposit(@Valid @RequestBody TransactionRequestDto requestDto) {
        log.info("Processing deposit request for account: {}, amount: {}", 
                requestDto.getAccountNumber(), requestDto.getAmount());
        
        Transaction transaction = transactionService.deposit(
                requestDto.getAccountNumber(), 
                requestDto.getAmount(), 
                requestDto.getDescription()
        );
        
        TransactionResponseDto responseDto = transactionMapper.toResponseDto(transaction);
        log.info("Deposit completed successfully. Transaction ID: {}", transaction.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDto> withdraw(@Valid @RequestBody TransactionRequestDto requestDto) {
        log.info("Processing withdrawal request for account: {}, amount: {}", 
                requestDto.getAccountNumber(), requestDto.getAmount());
        
        Transaction transaction = transactionService.withdraw(
                requestDto.getAccountNumber(), 
                requestDto.getAmount(),
                requestDto.getDescription()
        );
        
        TransactionResponseDto responseDto = transactionMapper.toResponseDto(transaction);
        log.info("Withdrawal completed successfully. Transaction ID: {}", transaction.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transfer(@Valid @RequestBody TransactionRequestDto requestDto) {
        log.info("Processing transfer request from: {} to: {}, amount: {}", 
                requestDto.getAccountNumber(), requestDto.getToAccountNumber(), requestDto.getAmount());
        
        if (requestDto.getToAccountNumber() == null || requestDto.getToAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination account number is required for transfer");
        }
        
        Transaction transaction = transactionService.transfer(
                requestDto.getAccountNumber(), 
                requestDto.getToAccountNumber(),
                requestDto.getAmount(),
                requestDto.getDescription()
        );
        
        TransactionResponseDto responseDto = transactionMapper.toResponseDto(transaction);
        log.info("Transfer completed successfully. Transaction ID: {}", transaction.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByAccount(
            @PathVariable @NotBlank String accountNumber) {
        log.info("Retrieving transactions for account: {}", accountNumber);
        
        List<Transaction> transactions = transactionService.getAccountTransactions(accountNumber);
        List<TransactionResponseDto> responseDtos = transactionMapper.toResponseDtoList(transactions);
        
        return ResponseEntity.ok(responseDtos);
    }
} 