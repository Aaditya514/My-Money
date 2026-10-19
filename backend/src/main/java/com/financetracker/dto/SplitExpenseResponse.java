package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SplitExpenseResponse {

    private Long id;
    private String title;
    private BigDecimal totalAmount;
    private LocalDate expenseDate;
    private Long groupId;
    private String groupName;
    private Long paidByMemberId;
    private String paidByMemberName;
    private boolean isPaidByCurrentUser;
    private String splitMethod;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private boolean syncedToLedger;
    private Long ledgerTransactionId;
    private String notes;
    private LocalDateTime createdAt;
    private List<SplitExpenseShareDTO> shares = new ArrayList<>();

    // Current user perspective:
    private BigDecimal userShareAmount = BigDecimal.ZERO;
    private BigDecimal userNetEffect = BigDecimal.ZERO; // Positive means others owe user, negative means user owes payer
    private String statusText; // e.g., "You paid ₹3,000, you lent ₹2,000" or "Rahul paid, you owe ₹500"

    public SplitExpenseResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getPaidByMemberId() {
        return paidByMemberId;
    }

    public void setPaidByMemberId(Long paidByMemberId) {
        this.paidByMemberId = paidByMemberId;
    }

    public String getPaidByMemberName() {
        return paidByMemberName;
    }

    public void setPaidByMemberName(String paidByMemberName) {
        this.paidByMemberName = paidByMemberName;
    }

    public boolean isPaidByCurrentUser() {
        return isPaidByCurrentUser;
    }

    public void setPaidByCurrentUser(boolean paidByCurrentUser) {
        isPaidByCurrentUser = paidByCurrentUser;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public boolean isSyncedToLedger() {
        return syncedToLedger;
    }

    public void setSyncedToLedger(boolean syncedToLedger) {
        this.syncedToLedger = syncedToLedger;
    }

    public Long getLedgerTransactionId() {
        return ledgerTransactionId;
    }

    public void setLedgerTransactionId(Long ledgerTransactionId) {
        this.ledgerTransactionId = ledgerTransactionId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SplitExpenseShareDTO> getShares() {
        return shares;
    }

    public void setShares(List<SplitExpenseShareDTO> shares) {
        this.shares = shares;
    }

    public BigDecimal getUserShareAmount() {
        return userShareAmount;
    }

    public void setUserShareAmount(BigDecimal userShareAmount) {
        this.userShareAmount = userShareAmount;
    }

    public BigDecimal getUserNetEffect() {
        return userNetEffect;
    }

    public void setUserNetEffect(BigDecimal userNetEffect) {
        this.userNetEffect = userNetEffect;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
