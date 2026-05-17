package com.reimbursement.controller;

import com.reimbursement.model.*;
import com.reimbursement.service.ReimbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reimbursement")
@CrossOrigin(origins = "*")
public class ReimbursementController {

    @Autowired
    private ReimbursementService reimbursementService;

    @PostMapping("/create")
    public ResponseEntity<Reimbursement> createReimbursement(@RequestBody Reimbursement reimbursement) {
        Reimbursement created = reimbursementService.createReimbursement(reimbursement);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/submit/{id}")
    public ResponseEntity<?> submitReimbursement(@PathVariable String id) {
        try {
            Reimbursement updated = reimbursementService.submitReimbursement(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/approve/manager/{id}")
    public ResponseEntity<?> approveByManager(
            @PathVariable String id,
            @RequestParam String approverId,
            @RequestParam(required = false) String comment) {
        try {
            Reimbursement updated = reimbursementService.approveByManager(id, approverId, comment != null ? comment : "");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/approve/finance/{id}")
    public ResponseEntity<?> financeReview(
            @PathVariable String id,
            @RequestParam String approverId,
            @RequestParam(required = false) String comment) {
        try {
            Reimbursement updated = reimbursementService.financeReview(id, approverId, comment != null ? comment : "");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectReimbursement(
            @PathVariable String id,
            @RequestParam String approverId,
            @RequestParam String reason) {
        try {
            Reimbursement updated = reimbursementService.rejectReimbursement(id, approverId, reason);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/resubmit/{id}")
    public ResponseEntity<?> resubmitReimbursement(
            @PathVariable String id,
            @RequestBody Reimbursement updated) {
        try {
            Reimbursement result = reimbursementService.resubmitReimbursement(id, updated);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/complete-payment/{id}")
    public ResponseEntity<?> completePayment(@PathVariable String id) {
        try {
            Reimbursement updated = reimbursementService.completePayment(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Reimbursement>> getReimbursementsByEmployee(@PathVariable String employeeId) {
        List<Reimbursement> reimbursements = reimbursementService.getReimbursementsByEmployee(employeeId);
        return ResponseEntity.ok(reimbursements);
    }

    @GetMapping("/pending/{approverId}")
    public ResponseEntity<List<Reimbursement>> getPendingApprovals(@PathVariable String approverId) {
        List<Reimbursement> reimbursements = reimbursementService.getPendingApprovals(approverId);
        return ResponseEntity.ok(reimbursements);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Reimbursement>> getAllReimbursements() {
        List<Reimbursement> reimbursements = reimbursementService.getAllReimbursements();
        return ResponseEntity.ok(reimbursements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reimbursement> getReimbursementById(@PathVariable String id) {
        Reimbursement reimbursement = reimbursementService.getReimbursementById(id);
        if (reimbursement == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reimbursement);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = reimbursementService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentRecord>> getAllPaymentRecords() {
        List<PaymentRecord> payments = reimbursementService.getAllPaymentRecords();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/ledgers")
    public ResponseEntity<List<FinancialLedger>> getAllFinancialLedgers() {
        List<FinancialLedger> ledgers = reimbursementService.getAllFinancialLedgers();
        return ResponseEntity.ok(ledgers);
    }

    @GetMapping("/risk-alerts")
    public ResponseEntity<List<RiskAlert>> getRiskAlerts() {
        List<RiskAlert> alerts = reimbursementService.getRiskAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/stats/department/{department}")
    public ResponseEntity<ReimbursementStats> getDepartmentStats(@PathVariable String department) {
        ReimbursementStats stats = reimbursementService.getDepartmentStats(department);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        List<String> departments = reimbursementService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/invoice/check")
    public ResponseEntity<Boolean> checkInvoiceNumber(@RequestParam String invoiceNumber) {
        boolean isUsed = reimbursementService.isInvoiceNumberUsed(invoiceNumber);
        return ResponseEntity.ok(isUsed);
    }

    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
