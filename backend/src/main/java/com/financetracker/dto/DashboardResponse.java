package com.financetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    // 1. Core Net Worth & Overview
    private BigDecimal netWorth;
    private BigDecimal totalLiquidBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private BigDecimal thisMonthExpenses;

    // 2. Liquid Financial Accounts
    private List<AccountResponse> accounts;

    // 3. Monthly Salary Plan & Buffer
    private SalaryPlanResponse salaryPlan;

    // 4. Savings Goals & Emergency Fund
    private SavingsGoalResponse emergencyFundGoal;
    private int totalGoalsCount;
    private BigDecimal totalGoalsTarget;
    private BigDecimal totalGoalsSaved;
    private int overallGoalsProgress;

    // 5. Recent Activity & Category Distribution
    private List<TransactionResponse> recentTransactions;
    private List<CategoryExpenseDto> categoryExpenses;

    public DashboardResponse() {
    }

    public DashboardResponse(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal balance,
                             BigDecimal thisMonthExpenses, List<TransactionResponse> recentTransactions,
                             List<CategoryExpenseDto> categoryExpenses) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.balance = balance;
        this.thisMonthExpenses = thisMonthExpenses;
        this.recentTransactions = recentTransactions;
        this.categoryExpenses = categoryExpenses;
        this.netWorth = balance;
        this.totalLiquidBalance = balance;
    }

    public DashboardResponse(BigDecimal netWorth, BigDecimal totalLiquidBalance,
                             BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal balance,
                             BigDecimal thisMonthExpenses, List<AccountResponse> accounts,
                             SalaryPlanResponse salaryPlan, SavingsGoalResponse emergencyFundGoal,
                             int totalGoalsCount, BigDecimal totalGoalsTarget, BigDecimal totalGoalsSaved,
                             int overallGoalsProgress, List<TransactionResponse> recentTransactions,
                             List<CategoryExpenseDto> categoryExpenses) {
        this.netWorth = netWorth;
        this.totalLiquidBalance = totalLiquidBalance;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.balance = balance;
        this.thisMonthExpenses = thisMonthExpenses;
        this.accounts = accounts;
        this.salaryPlan = salaryPlan;
        this.emergencyFundGoal = emergencyFundGoal;
        this.totalGoalsCount = totalGoalsCount;
        this.totalGoalsTarget = totalGoalsTarget;
        this.totalGoalsSaved = totalGoalsSaved;
        this.overallGoalsProgress = overallGoalsProgress;
        this.recentTransactions = recentTransactions;
        this.categoryExpenses = categoryExpenses;
    }

    public BigDecimal getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(BigDecimal netWorth) {
        this.netWorth = netWorth;
    }

    public BigDecimal getTotalLiquidBalance() {
        return totalLiquidBalance;
    }

    public void setTotalLiquidBalance(BigDecimal totalLiquidBalance) {
        this.totalLiquidBalance = totalLiquidBalance;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getThisMonthExpenses() {
        return thisMonthExpenses;
    }

    public void setThisMonthExpenses(BigDecimal thisMonthExpenses) {
        this.thisMonthExpenses = thisMonthExpenses;
    }

    public List<AccountResponse> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountResponse> accounts) {
        this.accounts = accounts;
    }

    public SalaryPlanResponse getSalaryPlan() {
        return salaryPlan;
    }

    public void setSalaryPlan(SalaryPlanResponse salaryPlan) {
        this.salaryPlan = salaryPlan;
    }

    public SavingsGoalResponse getEmergencyFundGoal() {
        return emergencyFundGoal;
    }

    public void setEmergencyFundGoal(SavingsGoalResponse emergencyFundGoal) {
        this.emergencyFundGoal = emergencyFundGoal;
    }

    public int getTotalGoalsCount() {
        return totalGoalsCount;
    }

    public void setTotalGoalsCount(int totalGoalsCount) {
        this.totalGoalsCount = totalGoalsCount;
    }

    public BigDecimal getTotalGoalsTarget() {
        return totalGoalsTarget;
    }

    public void setTotalGoalsTarget(BigDecimal totalGoalsTarget) {
        this.totalGoalsTarget = totalGoalsTarget;
    }

    public BigDecimal getTotalGoalsSaved() {
        return totalGoalsSaved;
    }

    public void setTotalGoalsSaved(BigDecimal totalGoalsSaved) {
        this.totalGoalsSaved = totalGoalsSaved;
    }

    public int getOverallGoalsProgress() {
        return overallGoalsProgress;
    }

    public void setOverallGoalsProgress(int overallGoalsProgress) {
        this.overallGoalsProgress = overallGoalsProgress;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<CategoryExpenseDto> getCategoryExpenses() {
        return categoryExpenses;
    }

    public void setCategoryExpenses(List<CategoryExpenseDto> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
    }
}
