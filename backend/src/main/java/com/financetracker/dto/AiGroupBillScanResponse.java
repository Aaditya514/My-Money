package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Structured response for AI-assisted group bill splitting with Gemini AI.
 */
public class AiGroupBillScanResponse {

    private String merchantName;
    private String expenseTitle;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal tipOrServiceAmount;
    private Long categoryId;
    private String categoryName;
    private LocalDate expenseDate;
    private Long paidByMemberId;
    private String paidByMemberName;
    private String notes;
    private List<MemberShareDto> memberShares = new ArrayList<>();
    private List<BillItemDto> items = new ArrayList<>();

    public static class MemberShareDto {
        private Long memberId;
        private String memberName;
        private BigDecimal owedAmount;
        private String itemsSummary;

        public MemberShareDto() {}

        public MemberShareDto(Long memberId, String memberName, BigDecimal owedAmount, String itemsSummary) {
            this.memberId = memberId;
            this.memberName = memberName;
            this.owedAmount = owedAmount;
            this.itemsSummary = itemsSummary;
        }

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public BigDecimal getOwedAmount() {
            return owedAmount;
        }

        public void setOwedAmount(BigDecimal owedAmount) {
            this.owedAmount = owedAmount;
        }

        public String getItemsSummary() {
            return itemsSummary;
        }

        public void setItemsSummary(String itemsSummary) {
            this.itemsSummary = itemsSummary;
        }
    }

    public static class BillItemDto {
        private String name;
        private BigDecimal price;
        private List<String> assignedTo = new ArrayList<>();

        public BillItemDto() {}

        public BillItemDto(String name, BigDecimal price, List<String> assignedTo) {
            this.name = name;
            this.price = price;
            this.assignedTo = assignedTo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public List<String> getAssignedTo() {
            return assignedTo;
        }

        public void setAssignedTo(List<String> assignedTo) {
            this.assignedTo = assignedTo;
        }
    }

    public AiGroupBillScanResponse() {}

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getExpenseTitle() {
        return expenseTitle;
    }

    public void setExpenseTitle(String expenseTitle) {
        this.expenseTitle = expenseTitle;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTipOrServiceAmount() {
        return tipOrServiceAmount;
    }

    public void setTipOrServiceAmount(BigDecimal tipOrServiceAmount) {
        this.tipOrServiceAmount = tipOrServiceAmount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Long getPaidByMemberId() {
        return paidByMemberId;
    }

    public void setPaidByMemberId(Long paidByMemberId) {
        this.paidByMemberId = paidByMemberId;
    }

    public String getPaidByMemberName() {
        return paidByMemberName;
    }

    public void setPaidByMemberName(String paidByMemberName) {
        this.paidByMemberName = paidByMemberName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<MemberShareDto> getMemberShares() {
        return memberShares;
    }

    public void setMemberShares(List<MemberShareDto> memberShares) {
        this.memberShares = memberShares;
    }

    public List<BillItemDto> getItems() {
        return items;
    }

    public void setItems(List<BillItemDto> items) {
        this.items = items;
    }
}
