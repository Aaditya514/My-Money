package com.financetracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_payments")
public class RecurringPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type = TransactionType.EXPENSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceFrequency frequency = RecurrenceFrequency.MONTHLY;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RecurringPayment() {
    }

    public RecurringPayment(String title, BigDecimal amount, TransactionType type, Category category,
                            RecurrenceFrequency frequency, LocalDate startDate, LocalDate nextDueDate,
                            String notes, User user) {
        this.title = title;
        this.amount = amount;
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.category = category;
        this.frequency = frequency != null ? frequency : RecurrenceFrequency.MONTHLY;
        this.startDate = startDate;
        this.nextDueDate = nextDueDate != null ? nextDueDate : startDate;
        this.notes = notes;
        this.user = user;
        this.active = true;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.nextDueDate == null && this.startDate != null) {
            this.nextDueDate = this.startDate;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Advance the next due date based on the recurrence frequency.
     */
    public void advanceNextDueDate() {
        if (this.nextDueDate == null) {
            this.nextDueDate = LocalDate.now();
        }
        switch (this.frequency) {
            case DAILY:
                this.nextDueDate = this.nextDueDate.plusDays(1);
                break;
            case WEEKLY:
                this.nextDueDate = this.nextDueDate.plusWeeks(1);
                break;
            case BI_WEEKLY:
                this.nextDueDate = this.nextDueDate.plusWeeks(2);
                break;
            case MONTHLY:
                this.nextDueDate = this.nextDueDate.plusMonths(1);
                break;
            case QUARTERLY:
                this.nextDueDate = this.nextDueDate.plusMonths(3);
                break;
            case YEARLY:
                this.nextDueDate = this.nextDueDate.plusYears(1);
                break;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public RecurrenceFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(RecurrenceFrequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
