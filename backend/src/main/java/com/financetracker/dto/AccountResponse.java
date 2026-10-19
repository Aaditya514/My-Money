package com.financetracker.dto;

import com.financetracker.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AccountResponse {

    private Long id;
    private String name;
    private AccountType accountType;
    private String accountNumber;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private LocalDate openingDate;
    private LocalDate lastReconciledDate;
    private BigDecimal lastReconciledBalance;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String name, AccountType accountType, String accountNumber,
                           BigDecimal openingBalance, BigDecimal currentBalance, LocalDate openingDate,
                           LocalDate lastReconciledDate, BigDecimal lastReconciledBalance,
                           Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.accountType = accountType;
        this.accountNumber = accountNumber;
        this.openingBalance = openingBalance;
        this.currentBalance = currentBalance;
        this.openingDate = openingDate;
        this.lastReconciledDate = lastReconciledDate;
        this.lastReconciledBalance = lastReconciledBalance;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    public LocalDate getLastReconciledDate() {
        return lastReconciledDate;
    }

    public void setLastReconciledDate(LocalDate lastReconciledDate) {
        this.lastReconciledDate = lastReconciledDate;
    }

    public BigDecimal getLastReconciledBalance() {
        return lastReconciledBalance;
    }

    public void setLastReconciledBalance(BigDecimal lastReconciledBalance) {
        this.lastReconciledBalance = lastReconciledBalance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
