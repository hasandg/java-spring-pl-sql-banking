package com.banking.service;

import com.banking.entity.Account;
import com.banking.enums.AccountType;
import com.banking.enums.Currency;
import com.banking.exception.AccountNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.service.impl.AccountServiceImpl;
import com.banking.util.AccountNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private AuditService auditService;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(
            accountRepository,
            accountNumberGenerator,
            auditService
        );
    }

    @Test
    void createAccount_ShouldReturnAccountWithGeneratedNumber() {
        Account account = new Account();
        account.setBalance(new BigDecimal("1000.00"));
        account.setCurrency(Currency.USD.getValue());
        account.setAccountType(AccountType.SAVINGS.getValue());

        when(accountNumberGenerator.generateAccountNumber()).thenReturn("123456789012");
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(account);

        assertNotNull(result);
        verify(accountNumberGenerator).generateAccountNumber();
        verify(accountRepository).save(account);
        verify(auditService).logSuccess(eq("CREATE_ACCOUNT"), anyString(), anyString());
    }

    @Test
    void getAccount_WhenAccountExists_ShouldReturnAccount() {
        String accountNumber = "123456789012";
        Account account = new Account();
        account.setAccountNumber(accountNumber);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        Account result = accountService.getAccount(accountNumber);

        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());
    }

    @Test
    void getAccount_WhenAccountNotExists_ShouldThrowException() {
        String accountNumber = "123456789012";

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(accountNumber));
    }

    @Test
    void updateAccountBalance_ShouldUpdateAndReturnAccount() {
        String accountNumber = "123456789012";
        BigDecimal newBalance = new BigDecimal("500.00");
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("1000.00"));

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.updateAccountBalance(accountNumber, newBalance);

        assertNotNull(result);
        assertEquals(newBalance, account.getBalance());
        verify(accountRepository).save(account);
    }
} 