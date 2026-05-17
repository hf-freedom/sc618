package com.reimbursement.service;

import com.reimbursement.model.*;
import com.reimbursement.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReimbursementService {

    @Autowired
    private InMemoryStorage storage;

    public Reimbursement createReimbursement(Reimbursement reimbursement) {
        reimbursement.setId(UUID.randomUUID().toString());
        reimbursement.setStatus(ReimbursementStatus.DRAFT);
        reimbursement.setSubmitTime(LocalDateTime.now());
        reimbursement.setLastUpdateTime(LocalDateTime.now());
        reimbursement.setApprovalRecords(new ArrayList<>());
        storage.getReimbursements().put(reimbursement.getId(), reimbursement);

        storage.getEmployeeReimbursementIds()
                .computeIfAbsent(reimbursement.getEmployeeId(), k -> new ArrayList<>())
                .add(reimbursement.getId());

        return reimbursement;
    }

    public Reimbursement submitReimbursement(String reimbursementId) {
        Reimbursement reimbursement = storage.getReimbursements().get(reimbursementId);
        if (reimbursement == null) {
            throw new RuntimeException("报销单不存在");
        }

        if (reimbursement.getAmount().compareTo(reimbursement.getExpenseType().getMaxAmount()) > 0) {
            throw new RuntimeException("金额超过" + reimbursement.getExpenseType() + "的上限：" + reimbursement.getExpenseType().getMaxAmount());
        }

        int approvalLevel = determineApprovalLevel(reimbursement.getAmount());
        reimbursement.setApprovalLevel(approvalLevel);
        reimbursement.setStatus(ReimbursementStatus.PENDING_APPROVAL);
        reimbursement.setSubmitTime(LocalDateTime.now());
        reimbursement.setLastUpdateTime(LocalDateTime.now());

        String managerId = findManagerByDepartment(reimbursement.getDepartment());
        reimbursement.setCurrentApproverId(managerId);
        reimbursement.setCurrentApproverRole("MANAGER");

        return reimbursement;
    }

    private int determineApprovalLevel(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("5000")) <= 0) {
            return 1;
        } else if (amount.compareTo(new BigDecimal("20000")) <= 0) {
            return 2;
        } else {
            return 3;
        }
    }

    private String findManagerByDepartment(String department) {
        return storage.getEmployees().values().stream()
                .filter(e -> e.getDepartment().equals(department) && "MANAGER".equals(e.getRole()))
                .findFirst()
                .map(Employee::getId)
                .orElse("E002");
    }

    private String findFinanceApproverId() {
        return storage.getEmployees().values().stream()
                .filter(e -> "FINANCE".equals(e.getRole()))
                .findFirst()
                .map(Employee::getId)
                .orElse("E003");
    }

    public Reimbursement approveByManager(String reimbursementId, String approverId, String comment) {
        Reimbursement reimbursement = storage.getReimbursements().get(reimbursementId);
        if (reimbursement == null) {
            throw new RuntimeException("报销单不存在");
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setId(UUID.randomUUID().toString());
        record.setReimbursementId(reimbursementId);
        record.setApproverId(approverId);
        record.setApproverName(storage.getEmployees().get(approverId).getName());
        record.setApproverRole("MANAGER");
        record.setApproved(true);
        record.setComment(comment);
        record.setApprovalTime(LocalDateTime.now());
        record.setApprovalLevel(1);
        storage.getApprovalRecords().put(record.getId(), record);
        reimbursement.getApprovalRecords().add(record);
        reimbursement.setLastUpdateTime(LocalDateTime.now());

        String financeApproverId = findFinanceApproverId();
        reimbursement.setStatus(ReimbursementStatus.FINANCE_REVIEW);
        reimbursement.setCurrentApproverId(financeApproverId);
        reimbursement.setCurrentApproverRole("FINANCE");

        return reimbursement;
    }

    public Reimbursement financeReview(String reimbursementId, String approverId, String comment) {
        Reimbursement reimbursement = storage.getReimbursements().get(reimbursementId);
        if (reimbursement == null) {
            throw new RuntimeException("报销单不存在");
        }

        if (storage.getUsedInvoiceNumbers().contains(reimbursement.getInvoiceNumber())) {
            throw new RuntimeException("发票编号已被使用");
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setId(UUID.randomUUID().toString());
        record.setReimbursementId(reimbursementId);
        record.setApproverId(approverId);
        record.setApproverName(storage.getEmployees().get(approverId).getName());
        record.setApproverRole("FINANCE");
        record.setApproved(true);
        record.setComment(comment);
        record.setApprovalTime(LocalDateTime.now());
        record.setApprovalLevel(2);
        storage.getApprovalRecords().put(record.getId(), record);
        reimbursement.getApprovalRecords().add(record);

        storage.getUsedInvoiceNumbers().add(reimbursement.getInvoiceNumber());

        reimbursement.setStatus(ReimbursementStatus.APPROVED);
        reimbursement.setLastUpdateTime(LocalDateTime.now());
        reimbursement.setCurrentApproverId(null);
        reimbursement.setCurrentApproverRole(null);

        createPaymentRecord(reimbursement);
        createFinancialLedger(reimbursement);

        return reimbursement;
    }

    private void createPaymentRecord(Reimbursement reimbursement) {
        PaymentRecord payment = new PaymentRecord();
        payment.setId(UUID.randomUUID().toString());
        payment.setReimbursementId(reimbursement.getId());
        payment.setEmployeeId(reimbursement.getEmployeeId());
        payment.setEmployeeName(reimbursement.getEmployeeName());
        payment.setAmount(reimbursement.getAmount());
        payment.setCreateTime(LocalDateTime.now());
        payment.setPaymentStatus("PENDING");
        storage.getPaymentRecords().put(payment.getId(), payment);

        reimbursement.setStatus(ReimbursementStatus.PAYMENT_PENDING);
    }

    private void createFinancialLedger(Reimbursement reimbursement) {
        FinancialLedger ledger = new FinancialLedger();
        ledger.setId(UUID.randomUUID().toString());
        ledger.setReimbursementId(reimbursement.getId());
        ledger.setEmployeeId(reimbursement.getEmployeeId());
        ledger.setEmployeeName(reimbursement.getEmployeeName());
        ledger.setDepartment(reimbursement.getDepartment());
        ledger.setExpenseType(reimbursement.getExpenseType());
        ledger.setAmount(reimbursement.getAmount());
        ledger.setCreateTime(LocalDateTime.now());
        ledger.setInvoiceNumber(reimbursement.getInvoiceNumber());
        ledger.setStatus("COMPLETED");
        storage.getFinancialLedgers().put(ledger.getId(), ledger);
    }

    public Reimbursement rejectReimbursement(String reimbursementId, String approverId, String reason) {
        Reimbursement reimbursement = storage.getReimbursements().get(reimbursementId);
        if (reimbursement == null) {
            throw new RuntimeException("报销单不存在");
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setId(UUID.randomUUID().toString());
        record.setReimbursementId(reimbursementId);
        record.setApproverId(approverId);
        record.setApproverName(storage.getEmployees().get(approverId).getName());
        record.setApproved(false);
        record.setComment(reason);
        record.setApprovalTime(LocalDateTime.now());
        storage.getApprovalRecords().put(record.getId(), record);
        reimbursement.getApprovalRecords().add(record);

        reimbursement.setStatus(ReimbursementStatus.REJECTED);
        reimbursement.setRejectReason(reason);
        reimbursement.setLastUpdateTime(LocalDateTime.now());
        reimbursement.setCurrentApproverId(null);
        reimbursement.setCurrentApproverRole(null);

        return reimbursement;
    }

    public Reimbursement resubmitReimbursement(String reimbursementId, Reimbursement updated) {
        Reimbursement reimbursement = storage.getReimbursements().get(reimbursementId);
        if (reimbursement == null) {
            throw new RuntimeException("报销单不存在");
        }

        reimbursement.setExpenseType(updated.getExpenseType());
        reimbursement.setAmount(updated.getAmount());
        reimbursement.setDescription(updated.getDescription());
        reimbursement.setInvoiceNumber(updated.getInvoiceNumber());
        reimbursement.setRejectReason(null);
        reimbursement.getApprovalRecords().clear();

        return submitReimbursement(reimbursementId);
    }

    public Reimbursement completePayment(String reimbursementId) {
        Reimbursement reimbursement = storage.getReimbursements().get(reimbursementId);
        if (reimbursement == null) {
            throw new RuntimeException("报销单不存在");
        }

        PaymentRecord payment = storage.getPaymentRecords().values().stream()
                .filter(p -> p.getReimbursementId().equals(reimbursementId))
                .findFirst()
                .orElse(null);

        if (payment != null) {
            payment.setPaymentStatus("COMPLETED");
            payment.setPaymentTime(LocalDateTime.now());
        }

        reimbursement.setStatus(ReimbursementStatus.COMPLETED);
        reimbursement.setLastUpdateTime(LocalDateTime.now());

        return reimbursement;
    }

    public List<Reimbursement> getReimbursementsByEmployee(String employeeId) {
        return storage.getReimbursements().values().stream()
                .filter(r -> r.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    public List<Reimbursement> getPendingApprovals(String approverId) {
        return storage.getReimbursements().values().stream()
                .filter(r -> approverId.equals(r.getCurrentApproverId()))
                .collect(Collectors.toList());
    }

    public List<Reimbursement> getAllReimbursements() {
        return new ArrayList<>(storage.getReimbursements().values());
    }

    public Reimbursement getReimbursementById(String id) {
        return storage.getReimbursements().get(id);
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(storage.getEmployees().values());
    }

    public List<PaymentRecord> getAllPaymentRecords() {
        return new ArrayList<>(storage.getPaymentRecords().values());
    }

    public List<FinancialLedger> getAllFinancialLedgers() {
        return new ArrayList<>(storage.getFinancialLedgers().values());
    }

    public List<RiskAlert> getRiskAlerts() {
        return new ArrayList<>(storage.getRiskAlerts().values());
    }

    public ReimbursementStats getDepartmentStats(String department) {
        List<Reimbursement> deptReimbursements = storage.getReimbursements().values().stream()
                .filter(r -> department.equals(r.getDepartment()))
                .collect(Collectors.toList());

        ReimbursementStats stats = new ReimbursementStats();
        stats.setDepartment(department);
        stats.setTotalAmount(deptReimbursements.stream()
                .map(Reimbursement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.setTotalCount(deptReimbursements.size());
        stats.setApprovedCount((int) deptReimbursements.stream()
                .filter(r -> ReimbursementStatus.COMPLETED.equals(r.getStatus()) ||
                        ReimbursementStatus.APPROVED.equals(r.getStatus()) ||
                        ReimbursementStatus.PAYMENT_PENDING.equals(r.getStatus()))
                .count());
        stats.setRejectedCount((int) deptReimbursements.stream()
                .filter(r -> ReimbursementStatus.REJECTED.equals(r.getStatus()))
                .count());

        double avgDays = deptReimbursements.stream()
                .filter(r -> ReimbursementStatus.COMPLETED.equals(r.getStatus()))
                .mapToLong(r -> ChronoUnit.DAYS.between(r.getSubmitTime(), r.getLastUpdateTime()))
                .average()
                .orElse(0);
        stats.setAverageApprovalDays(avgDays);

        Map<String, Integer> rejectReasons = deptReimbursements.stream()
                .filter(r -> r.getRejectReason() != null)
                .collect(Collectors.groupingBy(Reimbursement::getRejectReason, Collectors.summingInt(r -> 1)));
        stats.setRejectReasons(rejectReasons);

        return stats;
    }

    public List<String> getAllDepartments() {
        return storage.getEmployees().values().stream()
                .map(Employee::getDepartment)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean isInvoiceNumberUsed(String invoiceNumber) {
        return storage.getUsedInvoiceNumbers().contains(invoiceNumber);
    }
}
