package com.financetracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "split_expense_shares")
public class SplitExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @JsonIgnore
    private SplitExpense expense;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private SplitMember member;

    @Column(name = "owed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal owedAmount;

    @Column(name = "percentage")
    private Double percentage;

    public SplitExpenseShare() {
    }

    public SplitExpenseShare(SplitExpense expense, SplitMember member, BigDecimal owedAmount, Double percentage) {
        this.expense = expense;
        this.member = member;
        this.owedAmount = owedAmount;
        this.percentage = percentage;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SplitExpense getExpense() {
        return expense;
    }

    public void setExpense(SplitExpense expense) {
        this.expense = expense;
    }

    public SplitMember getMember() {
        return member;
    }

    public void setMember(SplitMember member) {
        this.member = member;
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
