package com.reimbursement.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinancialLedger {
    private String id;
    private String reimbursementId;
    private String employeeId;
    private String employeeName;
    private String department;
    private ExpenseType expenseType;
    private BigDecimal amount;
    private LocalDateTime createTime;
    private String invoiceNumber;
    private String status;
}
