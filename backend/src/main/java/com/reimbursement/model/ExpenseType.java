package com.reimbursement.model;

import java.math.BigDecimal;

public enum ExpenseType {
    TRANSPORTATION(new BigDecimal("5000")),
    ACCOMMODATION(new BigDecimal("10000")),
    MEAL(new BigDecimal("3000")),
    OFFICE_SUPPLY(new BigDecimal("2000")),
    TRAVEL(new BigDecimal("20000")),
    OTHER(new BigDecimal("1000"));

    private final BigDecimal maxAmount;

    ExpenseType(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
}
