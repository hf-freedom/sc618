package com.reimbursement.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRecord {
    private String id;
    private String reimbursementId;
    private String employeeId;
    private String employeeName;
    private BigDecimal amount;
    private LocalDateTime createTime;
    private LocalDateTime paymentTime;
    private String paymentStatus;
    private String remark;
}
