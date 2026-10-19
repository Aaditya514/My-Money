package com.financetracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "salary_plan_allocations")
public class SalaryPlanAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "planned_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal plannedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_type", nullable = false, length = 30)
    private SalaryPlanAllocationType allocationType = SalaryPlanAllocationType.EXPENSE;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "order_index")
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_plan_id", nullable = false)
    @JsonIgnore
    private SalaryPlan salaryPlan;

    public SalaryPlanAllocation() {
    }

    public SalaryPlanAllocation(String name, BigDecimal plannedAmount, SalaryPlanAllocationType allocationType,
                                Long categoryId, int orderIndex, SalaryPlan salaryPlan) {
        this.name = name;
        this.plannedAmount = plannedAmount != null ? plannedAmount : BigDecimal.ZERO;
        this.allocationType = allocationType != null ? allocationType : SalaryPlanAllocationType.EXPENSE;
        this.categoryId = categoryId;
        this.orderIndex = orderIndex;
        this.salaryPlan = salaryPlan;
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
        this.plannedAmount = plannedAmount != null ? plannedAmount : BigDecimal.ZERO;
    }

    public SalaryPlanAllocationType getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(SalaryPlanAllocationType allocationType) {
        this.allocationType = allocationType != null ? allocationType : SalaryPlanAllocationType.EXPENSE;
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

    public SalaryPlan getSalaryPlan() {
        return salaryPlan;
    }

    public void setSalaryPlan(SalaryPlan salaryPlan) {
        this.salaryPlan = salaryPlan;
    }
}
