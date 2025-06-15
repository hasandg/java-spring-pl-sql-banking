package com.banking.service;

import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.entity.TransactionStatus;
import com.banking.entity.TransactionType;
import com.banking.exception.InsufficientFundsException;
import com.banking.factory.TransactionFactory;
import com.banking.repository.TransactionRepository;
import com.banking.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionFactory transactionFactory;

    @Mock
    private AuditService auditService;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(
            transactionRepository,
            accountService,
            transactionFactory,
            auditService
        );
    }

    @Test
    void deposit_ShouldCreateTransactionAndUpdateBalance() {
        String accountNumber = "123456789012";
        BigDecimal amount = new BigDecimal("500.00");
        String description = "Test deposit";
        
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("1000.00"));
        
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.DEPOSIT);

        when(accountService.getAccount(accountNumber)).thenReturn(account);
        when(accountService.updateAccountBalance(eq(accountNumber), any(BigDecimal.class))).thenReturn(account);
        when(transactionFactory.createTransaction(any(), eq(TransactionType.DEPOSIT), eq(amount), eq(description), eq(TransactionStatus.COMPLETED)))
            .thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.deposit(accountNumber, amount, description);

        assertNotNull(result);
        verify(accountService).updateAccountBalance(accountNumber, new BigDecimal("1500.00"));
        verify(transactionRepository).save(transaction);
        verify(auditService).logSuccess(eq("DEPOSIT"), eq(accountNumber), anyString());
    }

    @Test
    void withdraw_WhenInsufficientFunds_ShouldThrowException() {
        String accountNumber = "123456789012";
        BigDecimal amount = new BigDecimal("1500.00");
        
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("1000.00"));

        when(accountService.getAccount(accountNumber)).thenReturn(account);

        assertThrows(InsufficientFundsException.class, 
            () -> transactionService.withdraw(accountNumber, amount, "Test withdrawal"));
        
        verify(auditService).logFailure(eq("WITHDRAWAL"), eq(accountNumber), anyString());
    }

    @Test
    void withdraw_WhenSufficientFunds_ShouldCreateTransaction() {
        String accountNumber = "123456789012";
        BigDecimal amount = new BigDecimal("500.00");
        String description = "Test withdrawal";
        
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("1000.00"));
        
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);

        when(accountService.getAccount(accountNumber)).thenReturn(account);
        when(accountService.updateAccountBalance(eq(accountNumber), any(BigDecimal.class))).thenReturn(account);
        when(transactionFactory.createTransaction(any(), eq(TransactionType.WITHDRAWAL), eq(amount), eq(description), eq(TransactionStatus.COMPLETED)))
            .thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.withdraw(accountNumber, amount, description);

        assertNotNull(result);
        verify(accountService).updateAccountBalance(accountNumber, new BigDecimal("500.00"));
        verify(transactionRepository).save(transaction);
        verify(auditService).logSuccess(eq("WITHDRAWAL"), eq(accountNumber), anyString());
    }
} 