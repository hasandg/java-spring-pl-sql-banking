package com.banking.service.impl;

import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.entity.TransactionStatus;
import com.banking.entity.TransactionType;
import com.banking.exception.InsufficientFundsException;
import com.banking.factory.TransactionFactory;
import com.banking.repository.TransactionRepository;
import com.banking.service.AccountService;
import com.banking.service.AuditService;
import com.banking.service.TransactionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final TransactionFactory transactionFactory;
    private final AuditService auditService;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                 AccountService accountService,
                                 TransactionFactory transactionFactory,
                                 AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.transactionFactory = transactionFactory;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"accounts", "transactions"}, key = "#accountNumber")
    public Transaction deposit(String accountNumber, BigDecimal amount, String description) {
        try {
            Account account = accountService.getAccount(accountNumber);
            BigDecimal newBalance = account.getBalance().add(amount);
            accountService.updateAccountBalance(accountNumber, newBalance);

            Transaction transaction = transactionFactory.createTransaction(account, TransactionType.DEPOSIT, amount, description, TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            auditService.logSuccess("DEPOSIT", accountNumber, "Deposited " + amount);
            return savedTransaction;
        } catch (Exception e) {
            auditService.logFailure("DEPOSIT", accountNumber, "Failed to deposit " + amount + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"accounts", "transactions"}, key = "#accountNumber")
    public Transaction withdraw(String accountNumber, BigDecimal amount, String description) {
        try {
            Account account = accountService.getAccount(accountNumber);
            
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient funds");
            }

            BigDecimal newBalance = account.getBalance().subtract(amount);
            accountService.updateAccountBalance(accountNumber, newBalance);

            Transaction transaction = transactionFactory.createTransaction(account, TransactionType.WITHDRAWAL, amount, description, TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            auditService.logSuccess("WITHDRAWAL", accountNumber, "Withdrew " + amount);
            return savedTransaction;
        } catch (Exception e) {
            auditService.logFailure("WITHDRAWAL", accountNumber, "Failed to withdraw " + amount + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"accounts", "transactions"}, allEntries = true)
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        try {
            Account fromAccount = accountService.getAccount(fromAccountNumber);
            Account toAccount = accountService.getAccount(toAccountNumber);

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient funds");
            }

            BigDecimal fromNewBalance = fromAccount.getBalance().subtract(amount);
            BigDecimal toNewBalance = toAccount.getBalance().add(amount);
            
            accountService.updateAccountBalance(fromAccountNumber, fromNewBalance);
            accountService.updateAccountBalance(toAccountNumber, toNewBalance);

            Transaction transaction = transactionFactory.createTransaction(fromAccount, TransactionType.TRANSFER, amount, description, TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            auditService.logSuccess("TRANSFER", fromAccountNumber, "Transferred " + amount + " to " + toAccountNumber);
            return savedTransaction;
        } catch (Exception e) {
            auditService.logFailure("TRANSFER", fromAccountNumber, "Failed to transfer " + amount + " to " + toAccountNumber + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Cacheable(value = "transactions", key = "#accountNumber")
    public List<Transaction> getAccountTransactions(String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return transactionRepository.findByAccountIdOrderByTransactionDateDesc(account.getId());
    }
} 