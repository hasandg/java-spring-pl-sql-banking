package com.banking.dto;

import com.banking.enums.AccountType;
import com.banking.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequestDto {
    
    @NotNull(message = "Initial balance is required")
    @PositiveOrZero(message = "Initial balance must be positive or zero")
    private BigDecimal balance;
    
    @NotNull(message = "Currency is required")
    private Currency currency;
    
    @NotNull(message = "Account type is required")
    private AccountType accountType;
} 