package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SplitSettlementResponse {

    private Long id;
    private Long groupId;
    private String groupName;
    private Long paidByMemberId;
    private String paidByMemberName;
    private boolean isPaidByCurrentUser;
    private Long paidToMemberId;
    private String paidToMemberName;
    private boolean isPaidToCurrentUser;
    private BigDecimal amount;
    private LocalDate settlementDate;
    private String notes;
    private boolean recordedInLedger;
    private Long ledgerTransactionId;
    private LocalDateTime createdAt;
    private String narrative; // e.g. "Rahul paid You ₹500" or "You paid Priya ₹300"

    public SplitSettlementResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getPaidToMemberId() {
        return paidToMemberId;
    }

    public void setPaidToMemberId(Long paidToMemberId) {
        this.paidToMemberId = paidToMemberId;
    }

    public String getPaidToMemberName() {
        return paidToMemberName;
    }

    public void setPaidToMemberName(String paidToMemberName) {
        this.paidToMemberName = paidToMemberName;
    }

    public boolean isPaidToCurrentUser() {
        return isPaidToCurrentUser;
    }

    public void setPaidToCurrentUser(boolean paidToCurrentUser) {
        isPaidToCurrentUser = paidToCurrentUser;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }
}
