package com.financetracker.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SplitExpenseRequest {

    @NotBlank(message = "Expense title is required")
    @Size(min = 2, max = 150, message = "Title must be between 2 and 150 characters")
    private String title;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal totalAmount;

    private LocalDate expenseDate;

    @NotNull(message = "Payer member ID is required")
    private Long paidByMemberId;

    private String splitMethod = "EQUAL"; // EQUAL, EXACT, PERCENT

    private Long categoryId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private List<SplitExpenseShareRequest> shares = new ArrayList<>();

    private Long userId;

    public SplitExpenseRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Long getPaidByMemberId() {
        return paidByMemberId;
    }

    public void setPaidByMemberId(Long paidByMemberId) {
        this.paidByMemberId = paidByMemberId;
    }

    public String getSplitMethod() {
        return splitMethod;
    }

    public void setSplitMethod(String splitMethod) {
        this.splitMethod = splitMethod;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SplitExpenseShareRequest> getShares() {
        return shares;
    }

    public void setShares(List<SplitExpenseShareRequest> shares) {
        this.shares = shares;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
