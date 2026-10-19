package com.financetracker.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SplitBalanceSummaryDTO {

    private Long groupId;
    private String groupName;
    private BigDecimal totalGroupSpending = BigDecimal.ZERO;
    private BigDecimal userTotalPaid = BigDecimal.ZERO;
    private BigDecimal userTotalShare = BigDecimal.ZERO;
    private BigDecimal userNetBalance = BigDecimal.ZERO;
    private String userStatusHeadline; // "You are owed ₹1,450" or "You owe ₹500" or "All settled up"
    private List<SplitMemberDTO> memberBalances = new ArrayList<>();
    private List<DebtTransferDTO> simplifiedDebts = new ArrayList<>();

    public SplitBalanceSummaryDTO() {
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

    public BigDecimal getTotalGroupSpending() {
        return totalGroupSpending;
    }

    public void setTotalGroupSpending(BigDecimal totalGroupSpending) {
        this.totalGroupSpending = totalGroupSpending;
    }

    public BigDecimal getUserTotalPaid() {
        return userTotalPaid;
    }

    public void setUserTotalPaid(BigDecimal userTotalPaid) {
        this.userTotalPaid = userTotalPaid;
    }

    public BigDecimal getUserTotalShare() {
        return userTotalShare;
    }

    public void setUserTotalShare(BigDecimal userTotalShare) {
        this.userTotalShare = userTotalShare;
    }

    public BigDecimal getUserNetBalance() {
        return userNetBalance;
    }

    public void setUserNetBalance(BigDecimal userNetBalance) {
        this.userNetBalance = userNetBalance;
    }

    public String getUserStatusHeadline() {
        return userStatusHeadline;
    }

    public void setUserStatusHeadline(String userStatusHeadline) {
        this.userStatusHeadline = userStatusHeadline;
    }

    public List<SplitMemberDTO> getMemberBalances() {
        return memberBalances;
    }

    public void setMemberBalances(List<SplitMemberDTO> memberBalances) {
        this.memberBalances = memberBalances;
    }

    public List<DebtTransferDTO> getSimplifiedDebts() {
        return simplifiedDebts;
    }

    public void setSimplifiedDebts(List<DebtTransferDTO> simplifiedDebts) {
        this.simplifiedDebts = simplifiedDebts;
    }
}
