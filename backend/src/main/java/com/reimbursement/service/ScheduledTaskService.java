package com.reimbursement.service;

import com.reimbursement.model.*;
import com.reimbursement.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduledTaskService {

    @Autowired
    private InMemoryStorage storage;

    private final Map<String, LocalDateTime> lastReminderSent = new HashMap<>();

    @Scheduled(fixedRate = 60000)
    public void checkPendingApprovalsAndRemind() {
        LocalDateTime now = LocalDateTime.now();
        List<Reimbursement> pendingReimbursements = storage.getReimbursements().values().stream()
                .filter(r -> r.getStatus() == ReimbursementStatus.PENDING_APPROVAL ||
                        r.getStatus() == ReimbursementStatus.MANAGER_APPROVED ||
                        r.getStatus() == ReimbursementStatus.FINANCE_REVIEW)
                .collect(Collectors.toList());

        for (Reimbursement reimbursement : pendingReimbursements) {
            long hoursSinceSubmission = ChronoUnit.HOURS.between(reimbursement.getLastUpdateTime(), now);
            if (hoursSinceSubmission >= 24) {
                String approverId = reimbursement.getCurrentApproverId();
                String reminderKey = reimbursement.getId() + "-" + approverId;
                LocalDateTime lastReminder = lastReminderSent.get(reminderKey);
                if (lastReminder == null || ChronoUnit.HOURS.between(lastReminder, now) >= 24) {
                    System.out.println("提醒：审批人 " + reimbursement.getCurrentApproverId() +
                            " 需要处理报销单 " + reimbursement.getId() +
                            "，已等待 " + hoursSinceSubmission + " 小时");
                    lastReminderSent.put(reminderKey, now);
                }
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void checkFrequentReimbursements() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        for (Map.Entry<String, List<String>> entry : storage.getEmployeeReimbursementIds().entrySet()) {
            String employeeId = entry.getKey();
            List<String> reimbursementIds = entry.getValue();

            List<Reimbursement> recentReimbursements = reimbursementIds.stream()
                    .map(id -> storage.getReimbursements().get(id))
                    .filter(Objects::nonNull)
                    .filter(r -> r.getSubmitTime().isAfter(sevenDaysAgo))
                    .collect(Collectors.toList());

            if (recentReimbursements.size() >= 5) {
                String alertKey = employeeId + "-" + now.toLocalDate();
                if (!storage.getRiskAlerts().containsKey(alertKey)) {
                    RiskAlert alert = new RiskAlert();
                    alert.setId(alertKey);
                    alert.setEmployeeId(employeeId);
                    alert.setEmployeeName(recentReimbursements.get(0).getEmployeeName());
                    alert.setAlertType("FREQUENT_REIMBURSEMENT");
                    alert.setDescription("员工7天内提交了" + recentReimbursements.size() + "笔报销");
                    alert.setReimbursementCount(recentReimbursements.size());
                    alert.setReimbursementIds(recentReimbursements.stream()
                            .map(Reimbursement::getId)
                            .collect(Collectors.toList()));
                    alert.setAlertTime(now);
                    alert.setResolved(false);
                    storage.getRiskAlerts().put(alertKey, alert);
                    System.out.println("风险预警：员工 " + employeeId + " 报销频繁");
                }
            }
        }
    }
}
