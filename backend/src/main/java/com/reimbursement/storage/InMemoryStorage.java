package com.reimbursement.storage;

import com.reimbursement.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStorage {
    private final Map<String, Employee> employees = new ConcurrentHashMap<>();
    private final Map<String, Reimbursement> reimbursements = new ConcurrentHashMap<>();
    private final Map<String, ApprovalRecord> approvalRecords = new ConcurrentHashMap<>();
    private final Map<String, PaymentRecord> paymentRecords = new ConcurrentHashMap<>();
    private final Map<String, FinancialLedger> financialLedgers = new ConcurrentHashMap<>();
    private final Set<String> usedInvoiceNumbers = ConcurrentHashMap.newKeySet();
    private final Map<String, List<String>> employeeReimbursementIds = new ConcurrentHashMap<>();
    private final Map<String, RiskAlert> riskAlerts = new ConcurrentHashMap<>();

    public InMemoryStorage() {
        initData();
    }

    private void initData() {
        employees.put("E001", new Employee("E001", "张三", "技术部", "EMPLOYEE"));
        employees.put("E002", new Employee("E002", "李四", "技术部", "MANAGER"));
        employees.put("E003", new Employee("E003", "王五", "财务部", "FINANCE"));
        employees.put("E004", new Employee("E004", "赵六", "市场部", "EMPLOYEE"));
        employees.put("E005", new Employee("E005", "钱七", "市场部", "MANAGER"));
        employees.put("E006", new Employee("E006", "孙八", "人事部", "EMPLOYEE"));
    }

    public Map<String, Employee> getEmployees() {
        return employees;
    }

    public Map<String, Reimbursement> getReimbursements() {
        return reimbursements;
    }

    public Map<String, ApprovalRecord> getApprovalRecords() {
        return approvalRecords;
    }

    public Map<String, PaymentRecord> getPaymentRecords() {
        return paymentRecords;
    }

    public Map<String, FinancialLedger> getFinancialLedgers() {
        return financialLedgers;
    }

    public Set<String> getUsedInvoiceNumbers() {
        return usedInvoiceNumbers;
    }

    public Map<String, List<String>> getEmployeeReimbursementIds() {
        return employeeReimbursementIds;
    }

    public Map<String, RiskAlert> getRiskAlerts() {
        return riskAlerts;
    }
}
