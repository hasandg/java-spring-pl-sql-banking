package com.banking.mapper;

import com.banking.dto.AccountDto;
import com.banking.dto.AccountCreateRequestDto;
import com.banking.dto.AccountResponseDto;
import com.banking.entity.Account;
import com.banking.enums.AccountType;
import com.banking.enums.Currency;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@Slf4j
public abstract class AccountMapper {

    @Mapping(target = "currency", source = "currency", qualifiedByName = "stringToCurrency")
    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "stringToAccountType")
    public abstract AccountDto toDto(Account account);

    @Mapping(target = "currency", source = "currency", qualifiedByName = "currencyToString")
    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "accountTypeToString")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Account toEntity(AccountDto dto);

    @Mapping(target = "currency", source = "currency", qualifiedByName = "stringToCurrency")
    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "stringToAccountType")
    public abstract AccountResponseDto toResponseDto(Account account);

    @Mapping(target = "currency", source = "currency", qualifiedByName = "currencyToString")
    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "accountTypeToString")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    public abstract Account toEntity(AccountCreateRequestDto dto);

    @Named("stringToCurrency")
    protected Currency stringToCurrency(String currencyValue) {
        if (currencyValue == null) {
            return Currency.USD;
        }
        try {
            return Currency.fromValue(currencyValue);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid currency value '{}', defaulting to USD", currencyValue);
            return Currency.USD;
        }
    }

    @Named("currencyToString")
    protected String currencyToString(Currency currency) {
        return currency != null ? currency.getValue() : Currency.USD.getValue();
    }

    @Named("stringToAccountType")
    protected AccountType stringToAccountType(String accountTypeValue) {
        if (accountTypeValue == null) {
            return AccountType.SAVINGS;
        }
        try {
            return AccountType.fromValue(accountTypeValue);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid account type value '{}', defaulting to SAVINGS", accountTypeValue);
            return AccountType.SAVINGS;
        }
    }

    @Named("accountTypeToString")
    protected String accountTypeToString(AccountType accountType) {
        return accountType != null ? accountType.getValue() : AccountType.SAVINGS.getValue();
    }
} 