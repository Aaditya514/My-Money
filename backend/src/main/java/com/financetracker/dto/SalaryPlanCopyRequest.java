package com.financetracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SalaryPlanCopyRequest {

    @Min(1) @Max(12)
    private int fromMonth;

    @Min(2000)
    private int fromYear;

    @Min(1) @Max(12)
    private int toMonth;

    @Min(2000)
    private int toYear;

    private boolean carryForwardUnallocated = false;

    @NotNull
    private Long userId;

    public SalaryPlanCopyRequest() {
    }

    public SalaryPlanCopyRequest(int fromMonth, int fromYear, int toMonth, int toYear, boolean carryForwardUnallocated, Long userId) {
        this.fromMonth = fromMonth;
        this.fromYear = fromYear;
        this.toMonth = toMonth;
        this.toYear = toYear;
        this.carryForwardUnallocated = carryForwardUnallocated;
        this.userId = userId;
    }

    public int getFromMonth() {
        return fromMonth;
    }

    public void setFromMonth(int fromMonth) {
        this.fromMonth = fromMonth;
    }

    public int getFromYear() {
        return fromYear;
    }

    public void setFromYear(int fromYear) {
        this.fromYear = fromYear;
    }

    public int getToMonth() {
        return toMonth;
    }

    public void setToMonth(int toMonth) {
        this.toMonth = toMonth;
    }

    public int getToYear() {
        return toYear;
    }

    public void setToYear(int toYear) {
        this.toYear = toYear;
    }

    public boolean isCarryForwardUnallocated() {
        return carryForwardUnallocated;
    }

    public void setCarryForwardUnallocated(boolean carryForwardUnallocated) {
        this.carryForwardUnallocated = carryForwardUnallocated;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
