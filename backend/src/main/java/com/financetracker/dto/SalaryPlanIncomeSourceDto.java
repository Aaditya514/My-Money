package com.financetracker.dto;

import java.math.BigDecimal;

public class SalaryPlanIncomeSourceDto {

    private Long id;
    private String sourceName;
    private BigDecimal plannedAmount;
    private int orderIndex;

    public SalaryPlanIncomeSourceDto() {
    }

    public SalaryPlanIncomeSourceDto(Long id, String sourceName, BigDecimal plannedAmount, int orderIndex) {
        this.id = id;
        this.sourceName = sourceName;
        this.plannedAmount = plannedAmount;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(BigDecimal plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
