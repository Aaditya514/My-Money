package com.financetracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salary_plans", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "plan_month", "plan_year"})
})
public class SalaryPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_month", nullable = false)
    private int month;

    @Column(name = "plan_year", nullable = false)
    private int year;

    @Column(name = "take_home_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal takeHomeSalary = BigDecimal.ZERO;

    @Column(name = "planned_expenses", nullable = false, precision = 14, scale = 2)
    private BigDecimal plannedExpenses = BigDecimal.ZERO;

    @Column(name = "planned_emergency_fund", nullable = false, precision = 14, scale = 2)
    private BigDecimal plannedEmergencyFund = BigDecimal.ZERO;

    @Column(name = "unallocated_carry_forward", nullable = false, precision = 14, scale = 2)
    private BigDecimal unallocatedCarryForward = BigDecimal.ZERO;

    @Column(name = "emergency_fund_target", precision = 14, scale = 2)
    private BigDecimal emergencyFundTarget = BigDecimal.ZERO;

    @Column(name = "initial_emergency_fund_balance", precision = 14, scale = 2)
    private BigDecimal initialEmergencyFundBalance = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "salaryPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC, id ASC")
    private List<SalaryPlanIncomeSource> incomeSources = new ArrayList<>();

    @OneToMany(mappedBy = "salaryPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC, id ASC")
    private List<SalaryPlanAllocation> allocations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SalaryPlan() {
    }

    public SalaryPlan(int month, int year, BigDecimal takeHomeSalary, BigDecimal plannedExpenses,
                      BigDecimal plannedEmergencyFund,
                      BigDecimal emergencyFundTarget, BigDecimal initialEmergencyFundBalance,
                      String notes, User user) {
        this.month = month;
        this.year = year;
        this.takeHomeSalary = takeHomeSalary != null ? takeHomeSalary : BigDecimal.ZERO;
        this.plannedExpenses = plannedExpenses != null ? plannedExpenses : BigDecimal.ZERO;
        this.plannedEmergencyFund = plannedEmergencyFund != null ? plannedEmergencyFund : BigDecimal.ZERO;
        this.emergencyFundTarget = emergencyFundTarget != null ? emergencyFundTarget : BigDecimal.ZERO;
        this.initialEmergencyFundBalance = initialEmergencyFundBalance != null ? initialEmergencyFundBalance : BigDecimal.ZERO;
        this.notes = notes;
        this.user = user;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.unallocatedCarryForward == null) {
            this.unallocatedCarryForward = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculateTotalPlannedIncome() {
        if (incomeSources != null && !incomeSources.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (SalaryPlanIncomeSource s : incomeSources) {
                if (s.getPlannedAmount() != null) {
                    sum = sum.add(s.getPlannedAmount());
                }
            }
            return sum;
        }
        return takeHomeSalary != null ? takeHomeSalary : BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalPlannedExpenses() {
        if (allocations != null && !allocations.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (SalaryPlanAllocation a : allocations) {
                if (a.getAllocationType() == SalaryPlanAllocationType.EXPENSE && a.getPlannedAmount() != null) {
                    sum = sum.add(a.getPlannedAmount());
                }
            }
            return sum;
        }
        return plannedExpenses != null ? plannedExpenses : BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalPlannedSavings() {
        if (allocations != null && !allocations.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (SalaryPlanAllocation a : allocations) {
                if (a.getAllocationType() == SalaryPlanAllocationType.SAVINGS && a.getPlannedAmount() != null) {
                    sum = sum.add(a.getPlannedAmount());
                }
            }
            return sum;
        }
        return plannedEmergencyFund != null ? plannedEmergencyFund : BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalPlannedOther() {
        if (allocations != null && !allocations.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (SalaryPlanAllocation a : allocations) {
                if (a.getAllocationType() == SalaryPlanAllocationType.OTHER && a.getPlannedAmount() != null) {
                    sum = sum.add(a.getPlannedAmount());
                }
            }
            return sum;
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalAllocations() {
        return calculateTotalPlannedExpenses()
                .add(calculateTotalPlannedSavings())
                .add(calculateTotalPlannedOther());
    }

    public BigDecimal calculateRemainingBuffer() {
        BigDecimal totalPool = calculateTotalPlannedIncome()
                .add(unallocatedCarryForward != null ? unallocatedCarryForward : BigDecimal.ZERO);
        return totalPool.subtract(calculateTotalAllocations());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getTakeHomeSalary() {
        return takeHomeSalary;
    }

    public void setTakeHomeSalary(BigDecimal takeHomeSalary) {
        this.takeHomeSalary = takeHomeSalary;
    }

    public BigDecimal getPlannedExpenses() {
        return plannedExpenses;
    }

    public void setPlannedExpenses(BigDecimal plannedExpenses) {
        this.plannedExpenses = plannedExpenses;
    }

    public BigDecimal getPlannedEmergencyFund() {
        return plannedEmergencyFund;
    }

    public void setPlannedEmergencyFund(BigDecimal plannedEmergencyFund) {
        this.plannedEmergencyFund = plannedEmergencyFund;
    }

    public BigDecimal getUnallocatedCarryForward() {
        return unallocatedCarryForward;
    }

    public void setUnallocatedCarryForward(BigDecimal unallocatedCarryForward) {
        this.unallocatedCarryForward = unallocatedCarryForward;
    }

    public BigDecimal getEmergencyFundTarget() {
        return emergencyFundTarget;
    }

    public void setEmergencyFundTarget(BigDecimal emergencyFundTarget) {
        this.emergencyFundTarget = emergencyFundTarget;
    }

    public BigDecimal getInitialEmergencyFundBalance() {
        return initialEmergencyFundBalance;
    }

    public void setInitialEmergencyFundBalance(BigDecimal initialEmergencyFundBalance) {
        this.initialEmergencyFundBalance = initialEmergencyFundBalance;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SalaryPlanIncomeSource> getIncomeSources() {
        return incomeSources;
    }

    public void setIncomeSources(List<SalaryPlanIncomeSource> incomeSources) {
        this.incomeSources = incomeSources;
    }

    public List<SalaryPlanAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<SalaryPlanAllocation> allocations) {
        this.allocations = allocations;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
