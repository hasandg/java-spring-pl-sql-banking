package com.banking.controller;

import com.banking.dto.AccountDto;
import com.banking.dto.TransactionDto;
import com.banking.service.AccountService;
import com.banking.service.TransactionService;
import com.banking.mapper.AccountMapper;
import com.banking.mapper.TransactionMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banking")
@Validated
@Deprecated
public class BankingController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    public BankingController(AccountService accountService,
                           TransactionService transactionService,
                           AccountMapper accountMapper,
                           TransactionMapper transactionMapper) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.accountMapper = accountMapper;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping("/accounts")
    @Deprecated
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto accountDto) {
        var account = accountMapper.toEntity(accountDto);
        var createdAccount = accountService.createAccount(account);
        return ResponseEntity.ok(accountMapper.toDto(createdAccount));
    }

    @GetMapping("/accounts/{accountNumber}")
    @Deprecated
    public ResponseEntity<AccountDto> getAccount(@PathVariable @NotBlank String accountNumber) {
        var account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @PostMapping("/accounts/{accountNumber}/deposit")
    @Deprecated
    public ResponseEntity<TransactionDto> deposit(
            @PathVariable @NotBlank String accountNumber,
            @RequestParam @Positive BigDecimal amount,
            @RequestParam(required = false) String description) {
        var transaction = transactionService.deposit(accountNumber, amount, description);
        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }

    @PostMapping("/accounts/{accountNumber}/withdraw")
    @Deprecated
    public ResponseEntity<TransactionDto> withdraw(
            @PathVariable @NotBlank String accountNumber,
            @RequestParam @Positive BigDecimal amount,
            @RequestParam(required = false) String description) {
        var transaction = transactionService.withdraw(accountNumber, amount, description);
        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }

    @PostMapping("/transfer")
    @Deprecated
    public ResponseEntity<TransactionDto> transfer(
            @RequestParam @NotBlank String fromAccountNumber,
            @RequestParam @NotBlank String toAccountNumber,
            @RequestParam @Positive BigDecimal amount,
            @RequestParam(required = false) String description) {
        var transaction = transactionService.transfer(fromAccountNumber, toAccountNumber, amount, description);
        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }

    @GetMapping("/accounts/{accountNumber}/transactions")
    @Deprecated
    public ResponseEntity<List<TransactionDto>> getAccountTransactions(@PathVariable @NotBlank String accountNumber) {
        var transactions = transactionService.getAccountTransactions(accountNumber);
        return ResponseEntity.ok(transactionMapper.toDtoList(transactions));
    }
} 