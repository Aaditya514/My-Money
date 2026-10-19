package com.financetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SalaryPlanRequest {

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private int month;

    @Min(value = 2000, message = "Year must be a valid 4-digit year")
    private int year;

    @DecimalMin(value = "0.00", message = "Take-home salary cannot be negative")
    private BigDecimal takeHomeSalary = BigDecimal.ZERO;

    private List<SalaryPlanIncomeSourceDto> incomeSources = new ArrayList<>();

    private List<SalaryPlanAllocationDto> allocations = new ArrayList<>();

    private BigDecimal unallocatedCarryForward = BigDecimal.ZERO;

    // Optional legacy fields for backward compatibility
    private BigDecimal plannedExpenses;
    private BigDecimal plannedEmergencyFund;
    private BigDecimal emergencyFundTarget;
    private BigDecimal initialEmergencyFundBalance;

    private String notes;

    @NotNull(message = "User ID is required")
    private Long userId;

    public SalaryPlanRequest() {
    }

    public SalaryPlanRequest(int month, int year, BigDecimal takeHomeSalary, BigDecimal plannedExpenses,
                             BigDecimal plannedEmergencyFund,
                             BigDecimal emergencyFundTarget, BigDecimal initialEmergencyFundBalance,
                             String notes, Long userId) {
        this.month = month;
        this.year = year;
        this.takeHomeSalary = takeHomeSalary != null ? takeHomeSalary : BigDecimal.ZERO;
        this.plannedExpenses = plannedExpenses;
        this.plannedEmergencyFund = plannedEmergencyFund;
        this.emergencyFundTarget = emergencyFundTarget;
        this.initialEmergencyFundBalance = initialEmergencyFundBalance;
        this.notes = notes;
        this.userId = userId;
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

    public BigDecimal getTakeHomeSalary() {
        return takeHomeSalary;
    }

    public void setTakeHomeSalary(BigDecimal takeHomeSalary) {
        this.takeHomeSalary = takeHomeSalary;
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

    public BigDecimal getUnallocatedCarryForward() {
        return unallocatedCarryForward;
    }

    public void setUnallocatedCarryForward(BigDecimal unallocatedCarryForward) {
        this.unallocatedCarryForward = unallocatedCarryForward;
    }

    public BigDecimal getPlannedExpenses() {
        return plannedExpenses;
    }

    public void setPlannedExpenses(BigDecimal plannedExpenses) {
        this.plannedExpenses = plannedExpenses;
    }

    public BigDecimal getPlannedEmergencyFund() {
        return plannedEmergencyFund;
    }

    public void setPlannedEmergencyFund(BigDecimal plannedEmergencyFund) {
        this.plannedEmergencyFund = plannedEmergencyFund;
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
}
