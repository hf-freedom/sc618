package com.reimbursement.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Data
public class ReimbursementStats {
    private String department;
    private BigDecimal totalAmount;
    private Integer totalCount;
    private Integer approvedCount;
    private Integer rejectedCount;
    private Double averageApprovalDays;
    private Map<String, Integer> rejectReasons;
}
