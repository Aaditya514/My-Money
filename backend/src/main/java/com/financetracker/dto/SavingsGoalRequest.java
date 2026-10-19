package com.financetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsGoalRequest {

    @NotBlank(message = "Goal name is required")
    @Size(max = 150, message = "Goal name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "1.00", message = "Target amount must be at least ₹1")
    private BigDecimal targetAmount;

    private BigDecimal initialAmount;

    private LocalDate targetDate;

    private String notes;

    private Long accountId;

    private Long userId;

    public SavingsGoalRequest() {
    }

    public SavingsGoalRequest(String name, BigDecimal targetAmount, BigDecimal initialAmount,
                              LocalDate targetDate, String notes, Long accountId, Long userId) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.initialAmount = initialAmount;
        this.targetDate = targetDate;
        this.notes = notes;
        this.accountId = accountId;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(BigDecimal initialAmount) {
        this.initialAmount = initialAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
