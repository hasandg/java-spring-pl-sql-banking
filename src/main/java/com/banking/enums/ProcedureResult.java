package com.banking.enums;

import java.util.Arrays;

public enum ProcedureResult {
    SUCCESS(1),
    ACCOUNT_NOT_FOUND(-1),
    DATABASE_ERROR(-2),
    INSUFFICIENT_FUNDS(-3);

    private final int code;

    ProcedureResult(int code) {
        this.code = code;
    }

    public static ProcedureResult fromCode(int code) {
        return Arrays.stream(values())
            .filter(r -> r.code == code)
            .findFirst()
            .orElse(DATABASE_ERROR);
    }
} 