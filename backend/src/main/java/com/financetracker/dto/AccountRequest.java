package com.financetracker.dto;

import com.financetracker.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must be less than 100 characters")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @Size(max = 50, message = "Account number must be less than 50 characters")
    private String accountNumber;

    @NotNull(message = "Opening balance is required")
    private BigDecimal openingBalance;

    private LocalDate openingDate;

    private Long userId;

    public AccountRequest() {
    }

    public AccountRequest(String name, AccountType accountType, String accountNumber,
                          BigDecimal openingBalance, LocalDate openingDate, Long userId) {
        this.name = name;
        this.accountType = accountType;
        this.accountNumber = accountNumber;
        this.openingBalance = openingBalance;
        this.openingDate = openingDate;
        this.userId = userId;
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

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
