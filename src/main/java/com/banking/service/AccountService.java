package com.banking.service;

import com.banking.entity.Account;

public interface AccountService {
    Account createAccount(Account account);
    Account getAccount(String accountNumber);
    Account updateAccountBalance(String accountNumber, java.math.BigDecimal newBalance);
} 