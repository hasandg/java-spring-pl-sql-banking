package com.banking.service;

import com.banking.config.DistributedLockTemplate;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.entity.TransactionStatus;
import com.banking.entity.TransactionType;
import com.banking.event.TransactionCompletedEvent;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientFundsException;
import com.banking.factory.TransactionFactory;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.service.impl.EnterpriseTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseTransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionFactory transactionFactory;

    @Mock
    private AuditService auditService;

    @Mock
    private DistributedLockTemplate distributedLockTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EnterpriseTransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new EnterpriseTransactionServiceImpl(
            accountRepository,
            transactionRepository,
            transactionFactory,
            auditService,
            distributedLockTemplate,
            eventPublisher
        );
    }

    @Test
    void deposit_WithValidAmount_ShouldSucceed() {
        String accountNumber = "123456789012";
        BigDecimal amount = new BigDecimal("500.00");
        String description = "Test deposit";
        
        Account account = createTestAccount(accountNumber, new BigDecimal("1000.00"));
        Transaction transaction = createTestTransaction(TransactionType.DEPOSIT, amount);

        when(distributedLockTemplate.execute(anyString(), any(Duration.class), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Transaction> supplier = invocation.getArgument(2);
                return supplier.get();
            });

        when(accountRepository.findByAccountNumberForUpdate(accountNumber))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionFactory.createTransaction(any(), eq(TransactionType.DEPOSIT), eq(amount), 
            eq(description), eq(TransactionStatus.COMPLETED))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.deposit(accountNumber, amount, description);

        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), account.getBalance());
        verify(auditService).logSuccess(eq("DEPOSIT"), eq(accountNumber), anyString());
        verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
    }

    @Test
    void deposit_WithAccountNotFound_ShouldThrowException() {
        String accountNumber = "123456789012";
        BigDecimal amount = new BigDecimal("500.00");

        when(distributedLockTemplate.execute(anyString(), any(Duration.class), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Transaction> supplier = invocation.getArgument(2);
                return supplier.get();
            });

        when(accountRepository.findByAccountNumberForUpdate(accountNumber))
            .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, 
            () -> transactionService.deposit(accountNumber, amount, "Test"));

        verify(auditService).logFailure(eq("DEPOSIT"), eq(accountNumber), anyString());
    }

    @Test
    void withdraw_WithSufficientFunds_ShouldSucceed() {
        String accountNumber = "123456789012";
        BigDecimal amount = new BigDecimal("300.00");
        String description = "Test withdrawal";
        
        Account account = createTestAccount(accountNumber, new BigDecimal("1000.00"));
        Transaction transaction = createTestTransaction(TransactionType.WITHDRAWAL, amount);

        when(distributedLockTemplate.execute(anyString(), any(Duration.class), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Transaction> supplier = invocation.getArgument(2);
                return supplier.get();
            });

        when(accountRepository.findByAccountNumberForUpdate(accountNumber))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionFactory.createTransaction(any(), eq(TransactionType.WITHDRAWAL), eq(amount), 
            eq(description), eq(TransactionStatus.COMPLETED))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.withdraw(accountNumber, amount, description);

        assertNotNull(result);
        assertEquals(new BigDecimal("700.00"), account.getBalance());
        verify(auditService).logSuccess(eq("WITHDRAWAL"), eq(accountNumber), anyString());
        verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
    }

    @Test
    void withdraw_WithInsufficientFunds_ShouldThrowException() {
        String accountNumber = "123456789012";
        BigDecimal amount = new BigDecimal("1500.00");
        
        Account account = createTestAccount(accountNumber, new BigDecimal("1000.00"));

        when(distributedLockTemplate.execute(anyString(), any(Duration.class), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Transaction> supplier = invocation.getArgument(2);
                return supplier.get();
            });

        when(accountRepository.findByAccountNumberForUpdate(accountNumber))
            .thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, 
            () -> transactionService.withdraw(accountNumber, amount, "Test"));

        verify(auditService).logFailure(eq("WITHDRAWAL"), eq(accountNumber), anyString());
    }

    @Test
    void transfer_WithValidAccounts_ShouldSucceed() {
        String fromAccountNumber = "123456789012";
        String toAccountNumber = "987654321098";
        BigDecimal amount = new BigDecimal("500.00");
        String description = "Test transfer";
        
        Account fromAccount = createTestAccount(fromAccountNumber, new BigDecimal("1000.00"));
        Account toAccount = createTestAccount(toAccountNumber, new BigDecimal("500.00"));
        Transaction transaction = createTestTransaction(TransactionType.TRANSFER, amount);

        when(distributedLockTemplate.execute(anyString(), any(Duration.class), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Transaction> supplier = invocation.getArgument(2);
                return supplier.get();
            });

        when(accountRepository.findByAccountNumberForUpdate(fromAccountNumber))
            .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumberForUpdate(toAccountNumber))
            .thenReturn(Optional.of(toAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(fromAccount, toAccount);
        when(transactionFactory.createTransaction(any(), eq(TransactionType.TRANSFER), eq(amount), 
            anyString(), eq(TransactionStatus.COMPLETED))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.transfer(fromAccountNumber, toAccountNumber, amount, description);

        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("1000.00"), toAccount.getBalance());
        verify(auditService).logSuccess(eq("TRANSFER"), eq(fromAccountNumber), anyString());
        verify(eventPublisher, times(2)).publishEvent(any(TransactionCompletedEvent.class));
    }

    @Test
    void deposit_WithOptimisticLockingFailure_ShouldRetry() {
        String accountNumber = "123456789012";
        BigDecimal amount = new BigDecimal("500.00");
        
        Account account = createTestAccount(accountNumber, new BigDecimal("1000.00"));

        when(distributedLockTemplate.execute(anyString(), any(Duration.class), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Transaction> supplier = invocation.getArgument(2);
                return supplier.get();
            });

        when(accountRepository.findByAccountNumberForUpdate(accountNumber))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
            .thenThrow(new OptimisticLockingFailureException("Version conflict"));

        assertThrows(OptimisticLockingFailureException.class, 
            () -> transactionService.deposit(accountNumber, amount, "Test"));

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    private Account createTestAccount(String accountNumber, BigDecimal balance) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setCurrency("USD");
        account.setAccountType("SAVINGS");
        return account;
    }

    private Transaction createTestTransaction(TransactionType type, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transaction;
    }
} 