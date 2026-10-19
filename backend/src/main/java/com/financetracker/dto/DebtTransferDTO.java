package com.financetracker.dto;

import java.math.BigDecimal;

public class DebtTransferDTO {

    private Long fromMemberId;
    private String fromMemberName;
    private boolean isFromCurrentUser;

    private Long toMemberId;
    private String toMemberName;
    private boolean isToCurrentUser;

    private BigDecimal amount;
    private String narrative; // e.g. "Rahul pays You ₹500"

    public DebtTransferDTO() {
    }

    public DebtTransferDTO(Long fromMemberId, String fromMemberName, boolean isFromCurrentUser,
                           Long toMemberId, String toMemberName, boolean isToCurrentUser,
                           BigDecimal amount, String narrative) {
        this.fromMemberId = fromMemberId;
        this.fromMemberName = fromMemberName;
        this.isFromCurrentUser = isFromCurrentUser;
        this.toMemberId = toMemberId;
        this.toMemberName = toMemberName;
        this.isToCurrentUser = isToCurrentUser;
        this.amount = amount;
        this.narrative = narrative;
    }

    public Long getFromMemberId() {
        return fromMemberId;
    }

    public void setFromMemberId(Long fromMemberId) {
        this.fromMemberId = fromMemberId;
    }

    public String getFromMemberName() {
        return fromMemberName;
    }

    public void setFromMemberName(String fromMemberName) {
        this.fromMemberName = fromMemberName;
    }

    public boolean isFromCurrentUser() {
        return isFromCurrentUser;
    }

    public void setFromCurrentUser(boolean fromCurrentUser) {
        isFromCurrentUser = fromCurrentUser;
    }

    public Long getToMemberId() {
        return toMemberId;
    }

    public void setToMemberId(Long toMemberId) {
        this.toMemberId = toMemberId;
    }

    public String getToMemberName() {
        return toMemberName;
    }

    public void setToMemberName(String toMemberName) {
        this.toMemberName = toMemberName;
    }

    public boolean isToCurrentUser() {
        return isToCurrentUser;
    }

    public void setToCurrentUser(boolean toCurrentUser) {
        isToCurrentUser = toCurrentUser;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }
}
