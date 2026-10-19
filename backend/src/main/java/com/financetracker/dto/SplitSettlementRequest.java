package com.financetracker.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SplitSettlementRequest {

    @NotNull(message = "Payer member ID is required")
    private Long paidByMemberId;

    @NotNull(message = "Recipient member ID is required")
    private Long paidToMemberId;

    @NotNull(message = "Settlement amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private LocalDate settlementDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private boolean recordInLedger = false;

    private Long categoryId;

    private Long userId;

    public SplitSettlementRequest() {
    }

    public Long getPaidByMemberId() {
        return paidByMemberId;
    }

    public void setPaidByMemberId(Long paidByMemberId) {
        this.paidByMemberId = paidByMemberId;
    }

    public Long getPaidToMemberId() {
        return paidToMemberId;
    }

    public void setPaidToMemberId(Long paidToMemberId) {
        this.paidToMemberId = paidToMemberId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isRecordInLedger() {
        return recordInLedger;
    }

    public void setRecordInLedger(boolean recordInLedger) {
        this.recordInLedger = recordInLedger;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
