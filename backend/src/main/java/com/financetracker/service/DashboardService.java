package com.financetracker.service;

import com.financetracker.dto.*;
import com.financetracker.entity.*;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.SavingsGoalRepository;
import com.financetracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final SalaryPlanService salaryPlanService;
    private final SavingsGoalRepository savingsGoalRepository;
    private final com.financetracker.repository.UserRepository userRepository;

    public DashboardService(TransactionRepository transactionRepository,
                            AccountRepository accountRepository,
                            SalaryPlanService salaryPlanService,
                            SavingsGoalRepository savingsGoalRepository,
                            com.financetracker.repository.UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.salaryPlanService = salaryPlanService;
        this.savingsGoalRepository = savingsGoalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(Long userId) {
        return getDashboardData(userId, null, null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(Long userId, Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int targetMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();
        int targetYear = (year != null && year >= 2000) ? year : now.getYear();

        if (userId == null || !userRepository.existsById(userId)) {
            return new DashboardResponse(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    Collections.emptyList(), SalaryPlanResponse.unconfigured(userId != null ? userId : 0L, targetMonth, targetYear), null,
                    0, BigDecimal.ZERO, BigDecimal.ZERO, 0,
                    Collections.emptyList(), Collections.emptyList()
            );
        }

        // 1. All Transactions & Core Totals
        List<Transaction> allTransactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        YearMonth selectedYearMonth = YearMonth.of(targetYear, targetMonth);
        LocalDate startOfMonth = selectedYearMonth.atDay(1);
        LocalDate endOfMonth = selectedYearMonth.atEndOfMonth();

        BigDecimal thisMonthExpenses = BigDecimal.ZERO;
        Map<String, BigDecimal> monthExpenseByCategoryMap = new HashMap<>();

        for (Transaction t : allTransactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getType() == TransactionType.REFUND) {
                totalExpenses = totalExpenses.subtract(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(t.getAmount());
            }

            // Category breakdown for selected month
            if (!t.getTransactionDate().isBefore(startOfMonth) && !t.getTransactionDate().isAfter(endOfMonth)) {
                if (t.getType() == TransactionType.EXPENSE) {
                    thisMonthExpenses = thisMonthExpenses.add(t.getAmount());
                    String catName = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
                    monthExpenseByCategoryMap.put(catName, monthExpenseByCategoryMap.getOrDefault(catName, BigDecimal.ZERO).add(t.getAmount()));
                } else if (t.getType() == TransactionType.REFUND) {
                    thisMonthExpenses = thisMonthExpenses.subtract(t.getAmount());
                    String catName = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
                    monthExpenseByCategoryMap.put(catName, monthExpenseByCategoryMap.getOrDefault(catName, BigDecimal.ZERO).subtract(t.getAmount()));
                }
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        // 2. Liquid Accounts
        List<Account> accounts = accountRepository.findByUserIdOrderByCreatedAtAsc(userId);
        List<AccountResponse> accountResponses = new ArrayList<>();
        BigDecimal totalLiquidBalance = BigDecimal.ZERO;

        for (Account a : accounts) {
            totalLiquidBalance = totalLiquidBalance.add(a.getCurrentBalance());
            accountResponses.add(new AccountResponse(
                    a.getId(),
                    a.getName(),
                    a.getAccountType(),
                    a.getAccountNumber(),
                    a.getOpeningBalance(),
                    a.getCurrentBalance(),
                    a.getOpeningDate(),
                    a.getLastReconciledDate(),
                    a.getLastReconciledBalance(),
                    userId,
                    a.getCreatedAt(),
                    a.getUpdatedAt()
            ));
        }

        BigDecimal netWorth = totalLiquidBalance;

        // 3. Monthly Salary Plan & Buffer
        SalaryPlanResponse salaryPlan = null;
        try {
            salaryPlan = salaryPlanService.getSalaryPlan(userId, targetMonth, targetYear);
        } catch (Exception ignored) {
        }

        // 4. Savings Goals & Emergency Fund
        List<SavingsGoal> goals = savingsGoalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        SavingsGoal emergencyGoalEntity = goals.stream()
                .filter(g -> g.getName().toLowerCase().contains("emergency"))
                .findFirst()
                .orElse(goals.isEmpty() ? null : goals.get(0));

        SavingsGoalResponse emergencyFundGoal = null;
        if (emergencyGoalEntity != null) {
            Long accId = emergencyGoalEntity.getAccount() != null ? emergencyGoalEntity.getAccount().getId() : null;
            String accName = emergencyGoalEntity.getAccount() != null ? emergencyGoalEntity.getAccount().getName() : null;

            BigDecimal remainingAmount = emergencyGoalEntity.getTargetAmount().subtract(emergencyGoalEntity.getCurrentAmount());
            if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
                remainingAmount = BigDecimal.ZERO;
            }

            double progressPercentage = 0.0;
            if (emergencyGoalEntity.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                progressPercentage = emergencyGoalEntity.getCurrentAmount()
                        .divide(emergencyGoalEntity.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            emergencyFundGoal = new SavingsGoalResponse(
                    emergencyGoalEntity.getId(),
                    emergencyGoalEntity.getName(),
                    emergencyGoalEntity.getTargetAmount(),
                    emergencyGoalEntity.getCurrentAmount(),
                    remainingAmount,
                    progressPercentage,
                    emergencyGoalEntity.getTargetDate(),
                    emergencyGoalEntity.isCompleted(),
                    emergencyGoalEntity.getCompletedAt(),
                    emergencyGoalEntity.getNotes(),
                    accId,
                    accName,
                    userId,
                    emergencyGoalEntity.getCreatedAt()
            );
        }

        BigDecimal totalGoalsTarget = BigDecimal.ZERO;
        BigDecimal totalGoalsSaved = BigDecimal.ZERO;
        for (SavingsGoal g : goals) {
            totalGoalsTarget = totalGoalsTarget.add(g.getTargetAmount());
            totalGoalsSaved = totalGoalsSaved.add(g.getCurrentAmount());
        }
        int overallGoalsProgress = totalGoalsTarget.compareTo(BigDecimal.ZERO) > 0
                ? totalGoalsSaved.multiply(BigDecimal.valueOf(100)).divide(totalGoalsTarget, 0, RoundingMode.HALF_UP).intValue()
                : 0;

        // 5. Recent 8 Transactions
        List<TransactionResponse> recentTransactions = new ArrayList<>();
        int limit = Math.min(allTransactions.size(), 8);
        for (int i = 0; i < limit; i++) {
            Transaction t = allTransactions.get(i);
            Long accId = t.getAccount() != null ? t.getAccount().getId() : null;
            String accName = t.getAccount() != null ? t.getAccount().getName() : null;
            String catName = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
            Long catId = t.getCategory() != null ? t.getCategory().getId() : null;

            recentTransactions.add(new TransactionResponse(
                    t.getId(),
                    t.getTitle(),
                    t.getAmount(),
                    t.getType(),
                    t.getDescription(),
                    t.getTransactionDate(),
                    catId,
                    catName,
                    accId,
                    accName,
                    userId,
                    t.getCreatedAt()
            ));
        }

        // 6. Category Expenses
        List<CategoryExpenseDto> categoryExpenses = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : monthExpenseByCategoryMap.entrySet()) {
            double percentage = 0.0;
            if (thisMonthExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = entry.getValue()
                        .divide(thisMonthExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
            categoryExpenses.add(new CategoryExpenseDto(entry.getKey(), entry.getValue(), percentage));
        }
        categoryExpenses.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        return new DashboardResponse(
                netWorth,
                totalLiquidBalance,
                totalIncome,
                totalExpenses,
                balance,
                thisMonthExpenses,
                accountResponses,
                salaryPlan,
                emergencyFundGoal,
                goals.size(),
                totalGoalsTarget,
                totalGoalsSaved,
                overallGoalsProgress,
                recentTransactions,
                categoryExpenses
        );
    }
}
