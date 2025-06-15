package com.banking.service.impl;

import com.banking.config.DistributedLockTemplate;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.entity.TransactionStatus;
import com.banking.entity.TransactionType;
import com.banking.event.TransactionCompletedEvent;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.BankingOperationException;
import com.banking.exception.InsufficientFundsException;
import com.banking.factory.TransactionFactory;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.service.AuditService;
import com.banking.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@Qualifier("enterpriseTransactionService")
public class EnterpriseTransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionFactory transactionFactory;
    private final AuditService auditService;
    private final DistributedLockTemplate distributedLockTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public EnterpriseTransactionServiceImpl(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            TransactionFactory transactionFactory,
            AuditService auditService,
            DistributedLockTemplate distributedLockTemplate,
            ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionFactory = transactionFactory;
        this.auditService = auditService;
        this.distributedLockTemplate = distributedLockTemplate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public Transaction deposit(String accountNumber, BigDecimal amount, String description) {
        log.info("Starting deposit operation for account: {}, amount: {}", accountNumber, amount);
        
        return distributedLockTemplate.execute(
            "account:" + accountNumber,
            Duration.ofSeconds(30),
            () -> {
                try {
                    validateDepositAmount(amount);
                    
                    Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                        .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
                    
                    BigDecimal newBalance = account.getBalance().add(amount);
                    account.setBalance(newBalance);
                    Account savedAccount = accountRepository.save(account);
                    
                    Transaction transaction = transactionFactory.createTransaction(
                        savedAccount, TransactionType.DEPOSIT, amount, description, TransactionStatus.COMPLETED
                    );
                    Transaction savedTransaction = transactionRepository.save(transaction);
                    
                    auditService.logSuccess("DEPOSIT", accountNumber, 
                        String.format("Deposited %s, new balance: %s", amount, newBalance));
                    
                    eventPublisher.publishEvent(new TransactionCompletedEvent(
                        this, accountNumber, "DEPOSIT", amount, savedTransaction.getId()
                    ));
                    
                    log.info("Deposit completed successfully for account: {}", accountNumber);
                    return savedTransaction;
                    
                } catch (Exception e) {
                    auditService.logFailure("DEPOSIT", accountNumber, 
                        "Failed to deposit " + amount + ": " + e.getMessage());
                    log.error("Deposit failed for account: {}", accountNumber, e);
                    throw e;
                }
            }
        );
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public Transaction withdraw(String accountNumber, BigDecimal amount, String description) {
        log.info("Starting withdrawal operation for account: {}, amount: {}", accountNumber, amount);
        
        return distributedLockTemplate.execute(
            "account:" + accountNumber,
            Duration.ofSeconds(30),
            () -> {
                try {
                    validateWithdrawalAmount(amount);
                    
                    Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                        .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
                    
                    if (account.getBalance().compareTo(amount) < 0) {
                        throw new InsufficientFundsException(
                            String.format("Insufficient funds. Available: %s, Requested: %s", 
                                account.getBalance(), amount)
                        );
                    }
                    
                    BigDecimal newBalance = account.getBalance().subtract(amount);
                    account.setBalance(newBalance);
                    Account savedAccount = accountRepository.save(account);
                    
                    Transaction transaction = transactionFactory.createTransaction(
                        savedAccount, TransactionType.WITHDRAWAL, amount, description, TransactionStatus.COMPLETED
                    );
                    Transaction savedTransaction = transactionRepository.save(transaction);
                    
                    auditService.logSuccess("WITHDRAWAL", accountNumber, 
                        String.format("Withdrew %s, new balance: %s", amount, newBalance));
                    
                    eventPublisher.publishEvent(new TransactionCompletedEvent(
                        this, accountNumber, "WITHDRAWAL", amount, savedTransaction.getId()
                    ));
                    
                    log.info("Withdrawal completed successfully for account: {}", accountNumber);
                    return savedTransaction;
                    
                } catch (Exception e) {
                    auditService.logFailure("WITHDRAWAL", accountNumber, 
                        "Failed to withdraw " + amount + ": " + e.getMessage());
                    log.error("Withdrawal failed for account: {}", accountNumber, e);
                    throw e;
                }
            }
        );
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        log.info("Starting transfer operation from: {} to: {}, amount: {}", fromAccountNumber, toAccountNumber, amount);
        
        String lockKey1 = "account:" + fromAccountNumber;
        String lockKey2 = "account:" + toAccountNumber;
        String combinedLockKey = fromAccountNumber.compareTo(toAccountNumber) < 0 ? 
            lockKey1 + ":" + lockKey2 : lockKey2 + ":" + lockKey1;
        
        return distributedLockTemplate.execute(
            combinedLockKey,
            Duration.ofSeconds(45),
            () -> {
                try {
                    validateTransferAmount(amount);
                    
                    if (fromAccountNumber.equals(toAccountNumber)) {
                        throw new BankingOperationException("Cannot transfer to the same account");
                    }
                    
                    Account fromAccount = accountRepository.findByAccountNumberForUpdate(fromAccountNumber)
                        .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + fromAccountNumber));
                    
                    Account toAccount = accountRepository.findByAccountNumberForUpdate(toAccountNumber)
                        .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + toAccountNumber));
                    
                    if (fromAccount.getBalance().compareTo(amount) < 0) {
                        throw new InsufficientFundsException(
                            String.format("Insufficient funds in source account. Available: %s, Requested: %s", 
                                fromAccount.getBalance(), amount)
                        );
                    }
                    
                    BigDecimal fromNewBalance = fromAccount.getBalance().subtract(amount);
                    BigDecimal toNewBalance = toAccount.getBalance().add(amount);
                    
                    fromAccount.setBalance(fromNewBalance);
                    toAccount.setBalance(toNewBalance);
                    
                    accountRepository.save(fromAccount);
                    accountRepository.save(toAccount);
                    
                    Transaction transaction = transactionFactory.createTransaction(
                        fromAccount, TransactionType.TRANSFER, amount, 
                        String.format("Transfer to %s: %s", toAccountNumber, description), 
                        TransactionStatus.COMPLETED
                    );
                    Transaction savedTransaction = transactionRepository.save(transaction);
                    
                    auditService.logSuccess("TRANSFER", fromAccountNumber, 
                        String.format("Transferred %s to %s, new balance: %s", amount, toAccountNumber, fromNewBalance));
                    
                    eventPublisher.publishEvent(new TransactionCompletedEvent(
                        this, fromAccountNumber, "TRANSFER", amount, savedTransaction.getId()
                    ));
                    
                    eventPublisher.publishEvent(new TransactionCompletedEvent(
                        this, toAccountNumber, "TRANSFER_RECEIVED", amount, savedTransaction.getId()
                    ));
                    
                    log.info("Transfer completed successfully from: {} to: {}", fromAccountNumber, toAccountNumber);
                    return savedTransaction;
                    
                } catch (Exception e) {
                    auditService.logFailure("TRANSFER", fromAccountNumber, 
                        String.format("Failed to transfer %s to %s: %s", amount, toAccountNumber, e.getMessage()));
                    log.error("Transfer failed from: {} to: {}", fromAccountNumber, toAccountNumber, e);
                    throw e;
                }
            }
        );
    }

    @Override
    public List<Transaction> getAccountTransactions(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        return transactionRepository.findByAccountIdOrderByTransactionDateDesc(account.getId());
    }

    private void validateDepositAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingOperationException("Deposit amount must be positive");
        }
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            throw new BankingOperationException("Deposit amount exceeds maximum limit");
        }
    }

    private void validateWithdrawalAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingOperationException("Withdrawal amount must be positive");
        }
        if (amount.compareTo(new BigDecimal("50000")) > 0) {
            throw new BankingOperationException("Withdrawal amount exceeds daily limit");
        }
    }

    private void validateTransferAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingOperationException("Transfer amount must be positive");
        }
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new BankingOperationException("Transfer amount exceeds maximum limit");
        }
    }
} 