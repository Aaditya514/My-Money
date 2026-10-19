package com.financetracker.service;

import com.financetracker.dto.SalaryPlanAllocationDto;
import com.financetracker.dto.SalaryPlanIncomeSourceDto;
import com.financetracker.dto.SalaryPlanResponse;
import com.financetracker.dto.TransactionResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private final TransactionService transactionService;
    private final SalaryPlanService salaryPlanService;

    public ExportService(TransactionService transactionService, SalaryPlanService salaryPlanService) {
        this.transactionService = transactionService;
        this.salaryPlanService = salaryPlanService;
    }

    public byte[] exportTransactionsCsv(Long userId, LocalDate fromDate, LocalDate toDate) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByUserId(userId);

        StringBuilder sb = new StringBuilder();
        // BOM for Excel compatibility with UTF-8
        sb.append('\ufeff');
        sb.append("ID,Date,Title,Type,Category,Account,Amount,Description\n");

        for (TransactionResponse tx : transactions) {
            LocalDate txDate = tx.getTransactionDate();
            if (fromDate != null && txDate != null && txDate.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && txDate != null && txDate.isAfter(toDate)) {
                continue;
            }

            sb.append(tx.getId()).append(",");
            sb.append(tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : "").append(",");
            sb.append(escapeCsv(tx.getTitle())).append(",");
            sb.append(tx.getType() != null ? tx.getType().name() : "").append(",");
            sb.append(escapeCsv(tx.getCategoryName())).append(",");
            sb.append(escapeCsv(tx.getAccountName())).append(",");
            sb.append(tx.getAmount() != null ? tx.getAmount().toPlainString() : "0.00").append(",");
            sb.append(escapeCsv(tx.getDescription())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportSalaryPlanCsv(Long userId, int month, int year) {
        SalaryPlanResponse plan = salaryPlanService.getSalaryPlan(userId, month, year);

        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff');
        sb.append("MONTHLY SALARY PLAN & BUDGET REPORT,").append(month).append("/").append(year).append("\n\n");

        // Section 1: Overview
        sb.append("PLAN OVERVIEW METRICS,PLANNED,ACTUAL,VARIANCE\n");
        sb.append("Total Income / Take-Home,")
                .append(plan.getTotalPlannedIncome()).append(",")
                .append(plan.getActualIncome()).append(",")
                .append(plan.getIncomeVariance()).append("\n");
        sb.append("Total Expenses,")
                .append(plan.getTotalPlannedExpenses()).append(",")
                .append(plan.getActualExpenses()).append(",")
                .append(plan.getExpenseVariance()).append("\n");
        sb.append("Emergency Fund / Savings,")
                .append(plan.getTotalPlannedSavings()).append(",")
                .append(plan.getActualEmergencyFund()).append(",")
                .append(plan.getEmergencyFundVariance()).append("\n");
        sb.append("Unallocated Carry Forward,").append(plan.getUnallocatedCarryForward()).append(",N/A,N/A\n");
        sb.append("Calculated Buffer (Net Leftover),").append(plan.getCalculatedBuffer()).append(",N/A,N/A\n");
        sb.append("Emergency Fund Overall Progress,").append(plan.getEmergencyFundProgressPercentage()).append("% of Target (")
                .append(plan.getEmergencyFundTarget()).append("),N/A,N/A\n\n");

        // Section 2: Income Sources
        sb.append("INCOME SOURCES\n");
        sb.append("Source Name,Planned Amount\n");
        if (plan.getIncomeSources() != null && !plan.getIncomeSources().isEmpty()) {
            for (SalaryPlanIncomeSourceDto inc : plan.getIncomeSources()) {
                sb.append(escapeCsv(inc.getSourceName())).append(",")
                        .append(inc.getPlannedAmount() != null ? inc.getPlannedAmount().toPlainString() : "0.00").append("\n");
            }
        } else {
            sb.append("Primary Salary,").append(plan.getTakeHomeSalary()).append("\n");
        }
        sb.append("\n");

        // Section 3: Allocations
        sb.append("EXPENSE & SAVINGS ALLOCATIONS\n");
        sb.append("Allocation Name,Type,Category ID,Planned Amount\n");
        if (plan.getAllocations() != null && !plan.getAllocations().isEmpty()) {
            for (SalaryPlanAllocationDto alloc : plan.getAllocations()) {
                sb.append(escapeCsv(alloc.getName())).append(",")
                        .append(alloc.getAllocationType() != null ? alloc.getAllocationType().name() : "").append(",")
                        .append(alloc.getCategoryId() != null ? alloc.getCategoryId().toString() : "").append(",")
                        .append(alloc.getPlannedAmount() != null ? alloc.getPlannedAmount().toPlainString() : "0.00").append("\n");
            }
        } else {
            sb.append("General Planned Expenses,EXPENSE,,")
                    .append(plan.getPlannedExpenses()).append("\n");
        }

        if (plan.getNotes() != null && !plan.getNotes().isBlank()) {
            sb.append("\nNOTES,").append(escapeCsv(plan.getNotes())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
