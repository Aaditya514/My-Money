package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SplitGroupDTO {

    private Long id;
    private String name;
    private String description;
    private String groupType;
    private int memberCount;
    private int expenseCount;
    private BigDecimal netBalanceForCurrentUser = BigDecimal.ZERO;
    private List<SplitMemberDTO> members = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SplitGroupDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(int expenseCount) {
        this.expenseCount = expenseCount;
    }

    public BigDecimal getNetBalanceForCurrentUser() {
        return netBalanceForCurrentUser;
    }

    public void setNetBalanceForCurrentUser(BigDecimal netBalanceForCurrentUser) {
        this.netBalanceForCurrentUser = netBalanceForCurrentUser;
    }

    public List<SplitMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<SplitMemberDTO> members) {
        this.members = members;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
