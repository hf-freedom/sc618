package com.reimbursement.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Reimbursement {
    private String id;
    private String employeeId;
    private String employeeName;
    private String department;
    private ExpenseType expenseType;
    private BigDecimal amount;
    private String description;
    private String invoiceNumber;
    private ReimbursementStatus status;
    private String currentApproverId;
    private String currentApproverRole;
    private List<ApprovalRecord> approvalRecords;
    private String rejectReason;
    private LocalDateTime submitTime;
    private LocalDateTime lastUpdateTime;
    private Integer approvalLevel;

    public Reimbursement() {
        this.approvalRecords = new ArrayList<>();
        this.status = ReimbursementStatus.DRAFT;
        this.submitTime = LocalDateTime.now();
        this.lastUpdateTime = LocalDateTime.now();
        this.approvalLevel = 0;
    }
}
