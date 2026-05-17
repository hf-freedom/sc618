package com.reimbursement.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovalRecord {
    private String id;
    private String reimbursementId;
    private String approverId;
    private String approverName;
    private String approverRole;
    private Boolean approved;
    private String comment;
    private LocalDateTime approvalTime;
    private Integer approvalLevel;
}
