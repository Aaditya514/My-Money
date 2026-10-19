package com.financetracker.dto;

import com.financetracker.entity.SalaryPlanAllocationType;
import java.math.BigDecimal;

public class SalaryPlanAllocationDto {

    private Long id;
    private String name;
    private BigDecimal plannedAmount;
    private SalaryPlanAllocationType allocationType;
    private Long categoryId;
    private int orderIndex;

    public SalaryPlanAllocationDto() {
    }

    public SalaryPlanAllocationDto(Long id, String name, BigDecimal plannedAmount,
                                   SalaryPlanAllocationType allocationType, Long categoryId, int orderIndex) {
        this.id = id;
        this.name = name;
        this.plannedAmount = plannedAmount;
        this.allocationType = allocationType;
        this.categoryId = categoryId;
        this.orderIndex = orderIndex;
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

    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(BigDecimal plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public SalaryPlanAllocationType getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(SalaryPlanAllocationType allocationType) {
        this.allocationType = allocationType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
