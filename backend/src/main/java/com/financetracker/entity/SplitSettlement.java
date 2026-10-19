package com.financetracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "split_settlements")
public class SplitSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private SplitGroup group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paid_by_member_id", nullable = false)
    private SplitMember paidBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paid_to_member_id", nullable = false)
    private SplitMember paidTo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(length = 500)
    private String notes;

    @Column(name = "recorded_in_ledger", nullable = false)
    private boolean recordedInLedger = false;

    @Column(name = "ledger_transaction_id")
    private Long ledgerTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public SplitSettlement() {
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.settlementDate == null) {
            this.settlementDate = LocalDate.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public SplitMember getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(SplitMember paidTo) {
        this.paidTo = paidTo;
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

    public boolean isRecordedInLedger() {
        return recordedInLedger;
    }

    public void setRecordedInLedger(boolean recordedInLedger) {
        this.recordedInLedger = recordedInLedger;
    }

    public Long getLedgerTransactionId() {
        return ledgerTransactionId;
    }

    public void setLedgerTransactionId(Long ledgerTransactionId) {
        this.ledgerTransactionId = ledgerTransactionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
