package com.banking.enums;

public enum AccountType {
    SAVINGS("SAVINGS"),
    CHECKING("CHECKING"),
    BUSINESS("BUSINESS");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AccountType fromValue(String value) {
        for (AccountType type : AccountType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid account type: " + value);
    }
} 