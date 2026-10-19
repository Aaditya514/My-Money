package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalContributionResponse {

    private Long id;
    private Long savingsGoalId;
    private String savingsGoalName;
    private BigDecimal amount;
    private LocalDate contributionDate;
    private String notes;
    private Long sourceAccountId;
    private String sourceAccountName;
    private Long userId;
    private LocalDateTime createdAt;

    public GoalContributionResponse() {
    }

    public GoalContributionResponse(Long id, Long savingsGoalId, String savingsGoalName,
                                  BigDecimal amount, LocalDate contributionDate, String notes,
                                  Long sourceAccountId, String sourceAccountName,
                                  Long userId, LocalDateTime createdAt) {
        this.id = id;
        this.savingsGoalId = savingsGoalId;
        this.savingsGoalName = savingsGoalName;
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.notes = notes;
        this.sourceAccountId = sourceAccountId;
        this.sourceAccountName = sourceAccountName;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSavingsGoalId() {
        return savingsGoalId;
    }

    public void setSavingsGoalId(Long savingsGoalId) {
        this.savingsGoalId = savingsGoalId;
    }

    public String getSavingsGoalName() {
        return savingsGoalName;
    }

    public void setSavingsGoalName(String savingsGoalName) {
        this.savingsGoalName = savingsGoalName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getContributionDate() {
        return contributionDate;
    }

    public void setContributionDate(LocalDate contributionDate) {
        this.contributionDate = contributionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getSourceAccountName() {
        return sourceAccountName;
    }

    public void setSourceAccountName(String sourceAccountName) {
        this.sourceAccountName = sourceAccountName;
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
}
