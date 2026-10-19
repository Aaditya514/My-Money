package com.financetracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "split_expenses")
public class SplitExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private SplitGroup group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paid_by_member_id", nullable = false)
    private SplitMember paidBy;

    @Column(name = "split_method", length = 30)
    private String splitMethod = "EQUAL"; // EQUAL, EXACT, PERCENT

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "synced_to_ledger", nullable = false)
    private boolean syncedToLedger = false;

    @Column(name = "ledger_transaction_id")
    private Long ledgerTransactionId;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SplitExpenseShare> shares = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public SplitExpense() {
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expenseDate == null) {
            this.expenseDate = LocalDate.now();
        }
    }

    public void addShare(SplitExpenseShare share) {
        shares.add(share);
        share.setExpense(this);
    }

    public void removeShare(SplitExpenseShare share) {
        shares.remove(share);
        share.setExpense(null);
    }

    // Getters and Setters
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

    public SplitGroup getGroup() {
        return group;
    }

    public void setGroup(SplitGroup group) {
        this.group = group;
    }

    public SplitMember getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(SplitMember paidBy) {
        this.paidBy = paidBy;
    }

    public String getSplitMethod() {
        return splitMethod;
    }

    public void setSplitMethod(String splitMethod) {
        this.splitMethod = splitMethod;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<SplitExpenseShare> getShares() {
        return shares;
    }

    public void setShares(List<SplitExpenseShare> shares) {
        this.shares = shares;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
