package com.financetracker.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ReconciliationRequest {

    @NotNull(message = "Actual balance is required")
    private BigDecimal actualBalance;

    private String notes;

    private LocalDate reconciliationDate;

    private Long userId;

    public ReconciliationRequest() {
    }

    public ReconciliationRequest(BigDecimal actualBalance, String notes, LocalDate reconciliationDate, Long userId) {
        this.actualBalance = actualBalance;
        this.notes = notes;
        this.reconciliationDate = reconciliationDate;
        this.userId = userId;
    }

    public BigDecimal getActualBalance() {
        return actualBalance;
    }

    public void setActualBalance(BigDecimal actualBalance) {
        this.actualBalance = actualBalance;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getReconciliationDate() {
        return reconciliationDate;
    }

    public void setReconciliationDate(LocalDate reconciliationDate) {
        this.reconciliationDate = reconciliationDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
