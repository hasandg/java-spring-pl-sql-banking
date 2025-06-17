package com.banking.controller;

import com.banking.dto.AccountDto;
import com.banking.dto.AccountCreateRequestDto;
import com.banking.dto.AccountResponseDto;
import com.banking.entity.Account;
import com.banking.mapper.AccountMapper;
import com.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/accounts")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountCreateRequestDto requestDto) {
        log.info("Creating new account with currency: {} and type: {}", 
                requestDto.getCurrency(), requestDto.getAccountType());
        
        Account account = accountMapper.toEntity(requestDto);
        Account createdAccount = accountService.createAccount(account);
        AccountResponseDto responseDto = accountMapper.toResponseDto(createdAccount);
        
        log.info("Account created successfully with number: {}", createdAccount.getAccountNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable @NotBlank String accountNumber) {
        log.info("Retrieving account: {}", accountNumber);
        
        Account account = accountService.getAccount(accountNumber);
        AccountResponseDto responseDto = accountMapper.toResponseDto(account);
        
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/legacy")
    public ResponseEntity<AccountDto> createAccountLegacy(@Valid @RequestBody AccountDto accountDto) {
        log.info("Creating account using legacy endpoint");
        
        Account account = accountMapper.toEntity(accountDto);
        Account createdAccount = accountService.createAccount(account);
        AccountDto responseDto = accountMapper.toDto(createdAccount);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/legacy/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountLegacy(@PathVariable @NotBlank String accountNumber) {
        log.info("Retrieving account using legacy endpoint: {}", accountNumber);
        
        Account account = accountService.getAccount(accountNumber);
        AccountDto responseDto = accountMapper.toDto(account);
        
        return ResponseEntity.ok(responseDto);
    }
} 