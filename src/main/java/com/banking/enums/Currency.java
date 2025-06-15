package com.banking.enums;

public enum Currency {
    USD("USD"),
    EUR("EUR"),
    GBP("GBP");

    private final String value;

    Currency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Currency fromValue(String value) {
        for (Currency currency : Currency.values()) {
            if (currency.value.equals(value)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Invalid currency: " + value);
    }
} 