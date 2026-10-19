package com.financetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalContributionRequest {

    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "1.00", message = "Contribution amount must be at least ₹1")
    private BigDecimal amount;

    private LocalDate contributionDate;

    private String notes;

    private Long sourceAccountId;

    private Long userId;

    public GoalContributionRequest() {
    }

    public GoalContributionRequest(BigDecimal amount, LocalDate contributionDate, String notes,
                                  Long sourceAccountId, Long userId) {
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.notes = notes;
        this.sourceAccountId = sourceAccountId;
        this.userId = userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
