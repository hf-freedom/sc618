package com.reimbursement.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RiskAlert {
    private String id;
    private String employeeId;
    private String employeeName;
    private String alertType;
    private String description;
    private Integer reimbursementCount;
    private List<String> reimbursementIds;
    private LocalDateTime alertTime;
    private Boolean resolved;
}
