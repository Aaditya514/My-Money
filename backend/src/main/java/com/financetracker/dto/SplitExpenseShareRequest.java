package com.financetracker.dto;

import java.math.BigDecimal;

public class SplitExpenseShareRequest {

    private Long memberId;
    private BigDecimal owedAmount;
    private Double percentage;

    public SplitExpenseShareRequest() {
    }

    public SplitExpenseShareRequest(Long memberId, BigDecimal owedAmount, Double percentage) {
        this.memberId = memberId;
        this.owedAmount = owedAmount;
        this.percentage = percentage;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public BigDecimal getOwedAmount() {
        return owedAmount;
    }

    public void setOwedAmount(BigDecimal owedAmount) {
        this.owedAmount = owedAmount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
