package com.banking.mapper;

import com.banking.dto.AccountDto;
import com.banking.entity.Account;
import com.banking.enums.AccountType;
import com.banking.enums.Currency;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountDto toDto(Account account) {
        if (account == null) {
            return null;
        }
        
        AccountDto dto = new AccountDto();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setCurrency(Currency.fromValue(account.getCurrency()));
        dto.setAccountType(AccountType.fromValue(account.getAccountType()));
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }

    public Account toEntity(AccountDto dto) {
        if (dto == null) {
            return null;
        }
        
        Account account = new Account();
        account.setAccountNumber(dto.getAccountNumber());
        account.setBalance(dto.getBalance());
        account.setCurrency(dto.getCurrency().getValue());
        account.setAccountType(dto.getAccountType().getValue());
        return account;
    }
} 