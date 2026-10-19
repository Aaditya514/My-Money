package com.financetracker.dto;

public class RecurringPaymentRecordResult {

    private RecurringPaymentResponse recurringPayment;
    private TransactionResponse transaction;
    private String message;

    public RecurringPaymentRecordResult() {
    }

    public RecurringPaymentRecordResult(RecurringPaymentResponse recurringPayment, TransactionResponse transaction, String message) {
        this.recurringPayment = recurringPayment;
        this.transaction = transaction;
        this.message = message;
    }

    public RecurringPaymentResponse getRecurringPayment() {
        return recurringPayment;
    }

    public void setRecurringPayment(RecurringPaymentResponse recurringPayment) {
        this.recurringPayment = recurringPayment;
    }

    public TransactionResponse getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionResponse transaction) {
        this.transaction = transaction;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
