package com.financetracker.dto;

import com.financetracker.entity.RecurrenceFrequency;
import com.financetracker.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringPaymentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private TransactionType type = TransactionType.EXPENSE;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Frequency is required")
    private RecurrenceFrequency frequency = RecurrenceFrequency.MONTHLY;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate nextDueDate;

    private String notes;

    @NotNull(message = "User ID is required")
    private Long userId;

    private Boolean active;

    public RecurringPaymentRequest() {
    }

    public RecurringPaymentRequest(String title, BigDecimal amount, TransactionType type,
                                   Long categoryId, RecurrenceFrequency frequency,
                                   LocalDate startDate, LocalDate nextDueDate, String notes,
                                   Long userId, Boolean active) {
        this.title = title;
        this.amount = amount;
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.categoryId = categoryId;
        this.frequency = frequency != null ? frequency : RecurrenceFrequency.MONTHLY;
        this.startDate = startDate;
        this.nextDueDate = nextDueDate;
        this.notes = notes;
        this.userId = userId;
        this.active = active != null ? active : true;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public RecurrenceFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(RecurrenceFrequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
