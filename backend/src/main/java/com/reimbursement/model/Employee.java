package com.reimbursement.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Employee {
    private String id;
    private String name;
    private String department;
    private String role;
    private LocalDateTime createdAt;

    public Employee(String id, String name, String department, String role) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }
}
