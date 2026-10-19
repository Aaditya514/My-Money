package com.financetracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalaryPlanResponse {

    private Long id;
    private int month;
    private int year;

    @JsonProperty("isConfigured")
    private boolean isConfigured = true;

    // Planned Totals
    private BigDecimal totalPlannedIncome = BigDecimal.ZERO;
    private BigDecimal totalPlannedExpenses = BigDecimal.ZERO;
    private BigDecimal totalPlannedSavings = BigDecimal.ZERO;
    private BigDecimal totalPlannedOther = BigDecimal.ZERO;
    private BigDecimal totalPlannedAllocations = BigDecimal.ZERO;
    private BigDecimal unallocatedCarryForward = BigDecimal.ZERO;
    private BigDecimal calculatedBuffer = BigDecimal.ZERO;

    @JsonProperty("isOverAllocated")
    private boolean isOverAllocated = false;
    private BigDecimal overAllocatedAmount = BigDecimal.ZERO;

    // Itemized breakdown
    private List<SalaryPlanIncomeSourceDto> incomeSources = new ArrayList<>();
    private List<SalaryPlanAllocationDto> allocations = new ArrayList<>();

    // Backward-compatible alias fields
    private BigDecimal takeHomeSalary = BigDecimal.ZERO;
    private BigDecimal plannedExpenses = BigDecimal.ZERO;
    private BigDecimal plannedEmergencyFund = BigDecimal.ZERO;
    private BigDecimal emergencyFundTarget = BigDecimal.ZERO;
    private BigDecimal initialEmergencyFundBalance = BigDecimal.ZERO;

    // Actual Tracked Values (from persisted database transactions)
    private BigDecimal actualIncome = BigDecimal.ZERO;
    private BigDecimal actualExpenses = BigDecimal.ZERO;
    private BigDecimal actualEmergencyFund = BigDecimal.ZERO;
    private BigDecimal actualNetSavings = BigDecimal.ZERO;

    // Variances
    private BigDecimal incomeVariance = BigDecimal.ZERO;
    private BigDecimal expenseVariance = BigDecimal.ZERO;
    private BigDecimal emergencyFundVariance = BigDecimal.ZERO;

    // Overall Emergency Fund Progress
    private BigDecimal emergencyFundTotalBalance = BigDecimal.ZERO;
    private double emergencyFundProgressPercentage = 0.0;

    private String notes;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SalaryPlanResponse() {
    }

    public SalaryPlanResponse(Long id, int month, int year, BigDecimal takeHomeSalary,
                              BigDecimal plannedExpenses, BigDecimal plannedEmergencyFund,
                              BigDecimal calculatedBuffer, BigDecimal emergencyFundTarget,
                              BigDecimal initialEmergencyFundBalance, BigDecimal actualIncome,
                              BigDecimal actualExpenses, BigDecimal actualEmergencyFund,
                              BigDecimal actualNetSavings, BigDecimal incomeVariance,
                              BigDecimal expenseVariance, BigDecimal emergencyFundVariance,
                              BigDecimal emergencyFundTotalBalance, double emergencyFundProgressPercentage,
                              String notes, Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.isConfigured = true;

        this.takeHomeSalary = takeHomeSalary != null ? takeHomeSalary : BigDecimal.ZERO;
        this.totalPlannedIncome = this.takeHomeSalary;

        this.plannedExpenses = plannedExpenses != null ? plannedExpenses : BigDecimal.ZERO;
        this.totalPlannedExpenses = this.plannedExpenses;

        this.plannedEmergencyFund = plannedEmergencyFund != null ? plannedEmergencyFund : BigDecimal.ZERO;
        this.totalPlannedSavings = this.plannedEmergencyFund;

        this.totalPlannedOther = BigDecimal.ZERO;
        this.totalPlannedAllocations = this.totalPlannedExpenses.add(this.totalPlannedSavings);

        this.unallocatedCarryForward = BigDecimal.ZERO;
        this.calculatedBuffer = calculatedBuffer != null ? calculatedBuffer : BigDecimal.ZERO;
        this.isOverAllocated = this.calculatedBuffer.compareTo(BigDecimal.ZERO) < 0;
        this.overAllocatedAmount = this.isOverAllocated ? this.calculatedBuffer.abs() : BigDecimal.ZERO;

        this.emergencyFundTarget = emergencyFundTarget != null ? emergencyFundTarget : BigDecimal.ZERO;
        this.initialEmergencyFundBalance = initialEmergencyFundBalance != null ? initialEmergencyFundBalance : BigDecimal.ZERO;
        this.actualIncome = actualIncome != null ? actualIncome : BigDecimal.ZERO;
        this.actualExpenses = actualExpenses != null ? actualExpenses : BigDecimal.ZERO;
        this.actualEmergencyFund = actualEmergencyFund != null ? actualEmergencyFund : BigDecimal.ZERO;
        this.actualNetSavings = actualNetSavings != null ? actualNetSavings : BigDecimal.ZERO;
        this.incomeVariance = incomeVariance != null ? incomeVariance : BigDecimal.ZERO;
        this.expenseVariance = expenseVariance != null ? expenseVariance : BigDecimal.ZERO;
        this.emergencyFundVariance = emergencyFundVariance != null ? emergencyFundVariance : BigDecimal.ZERO;
        this.emergencyFundTotalBalance = emergencyFundTotalBalance != null ? emergencyFundTotalBalance : BigDecimal.ZERO;
        this.emergencyFundProgressPercentage = emergencyFundProgressPercentage;
        this.notes = notes;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SalaryPlanResponse unconfigured(Long userId, int month, int year,
                                                  BigDecimal actualIncome, BigDecimal actualExpenses,
                                                  BigDecimal actualEmergencyFund,
                                                  BigDecimal actualNetSavings) {
        SalaryPlanResponse resp = new SalaryPlanResponse();
        resp.setId(null);
        resp.setMonth(month);
        resp.setYear(year);
        resp.setConfigured(false);
        resp.setUserId(userId);
        resp.setTotalPlannedIncome(BigDecimal.ZERO);
        resp.setTakeHomeSalary(BigDecimal.ZERO);
        resp.setTotalPlannedExpenses(BigDecimal.ZERO);
        resp.setPlannedExpenses(BigDecimal.ZERO);
        resp.setTotalPlannedSavings(BigDecimal.ZERO);
        resp.setPlannedEmergencyFund(BigDecimal.ZERO);
        resp.setTotalPlannedOther(BigDecimal.ZERO);
        resp.setTotalPlannedAllocations(BigDecimal.ZERO);
        resp.setUnallocatedCarryForward(BigDecimal.ZERO);
        resp.setCalculatedBuffer(BigDecimal.ZERO);
        resp.setOverAllocated(false);
        resp.setOverAllocatedAmount(BigDecimal.ZERO);
        resp.setIncomeSources(new ArrayList<>());
        resp.setAllocations(new ArrayList<>());

        resp.setEmergencyFundTarget(BigDecimal.ZERO);
        resp.setInitialEmergencyFundBalance(BigDecimal.ZERO);
        resp.setEmergencyFundTotalBalance(BigDecimal.ZERO);
        resp.setEmergencyFundProgressPercentage(0.0);

        resp.setActualIncome(actualIncome != null ? actualIncome : BigDecimal.ZERO);
        resp.setActualExpenses(actualExpenses != null ? actualExpenses : BigDecimal.ZERO);
        resp.setActualEmergencyFund(actualEmergencyFund != null ? actualEmergencyFund : BigDecimal.ZERO);
        resp.setActualNetSavings(actualNetSavings != null ? actualNetSavings : BigDecimal.ZERO);

        resp.setIncomeVariance(resp.getActualIncome());
        resp.setExpenseVariance(resp.getActualExpenses().negate());
        resp.setEmergencyFundVariance(resp.getActualEmergencyFund());
        return resp;
    }

    public static SalaryPlanResponse unconfigured(Long userId, int month, int year) {
        return unconfigured(userId, month, year, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @JsonProperty("isConfigured")
    public boolean isConfigured() {
        return isConfigured;
    }

    @JsonProperty("isConfigured")
    public void setConfigured(boolean configured) {
        isConfigured = configured;
    }

    public BigDecimal getTotalPlannedIncome() {
        return totalPlannedIncome;
    }

    public void setTotalPlannedIncome(BigDecimal totalPlannedIncome) {
        this.totalPlannedIncome = totalPlannedIncome;
        this.takeHomeSalary = totalPlannedIncome;
    }

    public BigDecimal getTotalPlannedExpenses() {
        return totalPlannedExpenses;
    }

    public void setTotalPlannedExpenses(BigDecimal totalPlannedExpenses) {
        this.totalPlannedExpenses = totalPlannedExpenses;
        this.plannedExpenses = totalPlannedExpenses;
    }

    public BigDecimal getTotalPlannedSavings() {
        return totalPlannedSavings;
    }

    public void setTotalPlannedSavings(BigDecimal totalPlannedSavings) {
        this.totalPlannedSavings = totalPlannedSavings;
        this.plannedEmergencyFund = totalPlannedSavings;
    }

    public BigDecimal getTotalPlannedOther() {
        return totalPlannedOther;
    }

    public void setTotalPlannedOther(BigDecimal totalPlannedOther) {
        this.totalPlannedOther = totalPlannedOther;
    }

    public BigDecimal getTotalPlannedAllocations() {
        return totalPlannedAllocations;
    }

    public void setTotalPlannedAllocations(BigDecimal totalPlannedAllocations) {
        this.totalPlannedAllocations = totalPlannedAllocations;
    }

    public BigDecimal getUnallocatedCarryForward() {
        return unallocatedCarryForward;
    }

    public void setUnallocatedCarryForward(BigDecimal unallocatedCarryForward) {
        this.unallocatedCarryForward = unallocatedCarryForward;
    }

    public BigDecimal getCalculatedBuffer() {
        return calculatedBuffer;
    }

    public void setCalculatedBuffer(BigDecimal calculatedBuffer) {
        this.calculatedBuffer = calculatedBuffer;
        this.isOverAllocated = calculatedBuffer != null && calculatedBuffer.compareTo(BigDecimal.ZERO) < 0;
        this.overAllocatedAmount = this.isOverAllocated ? calculatedBuffer.abs() : BigDecimal.ZERO;
    }

    @JsonProperty("isOverAllocated")
    public boolean isOverAllocated() {
        return isOverAllocated;
    }

    @JsonProperty("isOverAllocated")
    public void setOverAllocated(boolean overAllocated) {
        isOverAllocated = overAllocated;
    }

    public BigDecimal getOverAllocatedAmount() {
        return overAllocatedAmount;
    }

    public void setOverAllocatedAmount(BigDecimal overAllocatedAmount) {
        this.overAllocatedAmount = overAllocatedAmount;
    }

    public List<SalaryPlanIncomeSourceDto> getIncomeSources() {
        return incomeSources;
    }

    public void setIncomeSources(List<SalaryPlanIncomeSourceDto> incomeSources) {
        this.incomeSources = incomeSources;
    }

    public List<SalaryPlanAllocationDto> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<SalaryPlanAllocationDto> allocations) {
        this.allocations = allocations;
    }

    public BigDecimal getTakeHomeSalary() {
        return takeHomeSalary;
    }

    public void setTakeHomeSalary(BigDecimal takeHomeSalary) {
        this.takeHomeSalary = takeHomeSalary;
        this.totalPlannedIncome = takeHomeSalary;
    }

    public BigDecimal getPlannedExpenses() {
        return plannedExpenses;
    }

    public void setPlannedExpenses(BigDecimal plannedExpenses) {
        this.plannedExpenses = plannedExpenses;
        this.totalPlannedExpenses = plannedExpenses;
    }

    public BigDecimal getPlannedEmergencyFund() {
        return plannedEmergencyFund;
    }

    public void setPlannedEmergencyFund(BigDecimal plannedEmergencyFund) {
        this.plannedEmergencyFund = plannedEmergencyFund;
        this.totalPlannedSavings = plannedEmergencyFund;
    }

    public BigDecimal getEmergencyFundTarget() {
        return emergencyFundTarget;
    }

    public void setEmergencyFundTarget(BigDecimal emergencyFundTarget) {
        this.emergencyFundTarget = emergencyFundTarget;
    }

    public BigDecimal getInitialEmergencyFundBalance() {
        return initialEmergencyFundBalance;
    }

    public void setInitialEmergencyFundBalance(BigDecimal initialEmergencyFundBalance) {
        this.initialEmergencyFundBalance = initialEmergencyFundBalance;
    }

    public BigDecimal getActualIncome() {
        return actualIncome;
    }

    public void setActualIncome(BigDecimal actualIncome) {
        this.actualIncome = actualIncome;
    }

    public BigDecimal getActualExpenses() {
        return actualExpenses;
    }

    public void setActualExpenses(BigDecimal actualExpenses) {
        this.actualExpenses = actualExpenses;
    }

    public BigDecimal getActualEmergencyFund() {
        return actualEmergencyFund;
    }

    public void setActualEmergencyFund(BigDecimal actualEmergencyFund) {
        this.actualEmergencyFund = actualEmergencyFund;
    }

    public BigDecimal getActualNetSavings() {
        return actualNetSavings;
    }

    public void setActualNetSavings(BigDecimal actualNetSavings) {
        this.actualNetSavings = actualNetSavings;
    }

    public BigDecimal getIncomeVariance() {
        return incomeVariance;
    }

    public void setIncomeVariance(BigDecimal incomeVariance) {
        this.incomeVariance = incomeVariance;
    }

    public BigDecimal getExpenseVariance() {
        return expenseVariance;
    }

    public void setExpenseVariance(BigDecimal expenseVariance) {
        this.expenseVariance = expenseVariance;
    }

    public BigDecimal getEmergencyFundVariance() {
        return emergencyFundVariance;
    }

    public void setEmergencyFundVariance(BigDecimal emergencyFundVariance) {
        this.emergencyFundVariance = emergencyFundVariance;
    }

    public BigDecimal getEmergencyFundTotalBalance() {
        return emergencyFundTotalBalance;
    }

    public void setEmergencyFundTotalBalance(BigDecimal emergencyFundTotalBalance) {
        this.emergencyFundTotalBalance = emergencyFundTotalBalance;
    }

    public double getEmergencyFundProgressPercentage() {
        return emergencyFundProgressPercentage;
    }

    public void setEmergencyFundProgressPercentage(double emergencyFundProgressPercentage) {
        this.emergencyFundProgressPercentage = emergencyFundProgressPercentage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
