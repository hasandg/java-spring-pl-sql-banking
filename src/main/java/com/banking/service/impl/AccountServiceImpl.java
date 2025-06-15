package com.banking.service.impl;

import com.banking.entity.Account;
import com.banking.exception.AccountNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.service.AccountService;
import com.banking.service.AuditService;
import com.banking.util.AccountNumberGenerator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AuditService auditService;

    public AccountServiceImpl(AccountRepository accountRepository,
                             AccountNumberGenerator accountNumberGenerator,
                             AuditService auditService) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "accounts", key = "#result.accountNumber")
    public Account createAccount(Account account) {
        try {
            account.setAccountNumber(accountNumberGenerator.generateAccountNumber());
            Account savedAccount = accountRepository.save(account);
            auditService.logSuccess("CREATE_ACCOUNT", savedAccount.getAccountNumber(), "Account created successfully");
            return savedAccount;
        } catch (Exception e) {
            auditService.logFailure("CREATE_ACCOUNT", account.getAccountNumber(), "Failed to create account: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Cacheable(value = "accounts", key = "#accountNumber")
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    @Override
    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public Account updateAccountBalance(String accountNumber, BigDecimal newBalance) {
        Account account = getAccount(accountNumber);
        account.setBalance(newBalance);
        return accountRepository.save(account);
    }
} 