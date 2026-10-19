package com.financetracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "salary_plan_income_sources")
public class SalaryPlanIncomeSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, length = 120)
    private String sourceName;

    @Column(name = "planned_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal plannedAmount = BigDecimal.ZERO;

    @Column(name = "order_index")
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_plan_id", nullable = false)
    @JsonIgnore
    private SalaryPlan salaryPlan;

    public SalaryPlanIncomeSource() {
    }

    public SalaryPlanIncomeSource(String sourceName, BigDecimal plannedAmount, int orderIndex, SalaryPlan salaryPlan) {
        this.sourceName = sourceName;
        this.plannedAmount = plannedAmount != null ? plannedAmount : BigDecimal.ZERO;
        this.orderIndex = orderIndex;
        this.salaryPlan = salaryPlan;
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
        this.plannedAmount = plannedAmount != null ? plannedAmount : BigDecimal.ZERO;
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
