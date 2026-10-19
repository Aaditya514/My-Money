package com.financetracker.dto;

public class SendOtpResponse {

    private boolean success;
    private String message;
    private String phoneNumber;
    private String simulatedOtp;

    public SendOtpResponse() {
    }

    public SendOtpResponse(boolean success, String message, String phoneNumber, String simulatedOtp) {
        this.success = success;
        this.message = message;
        this.phoneNumber = phoneNumber;
        this.simulatedOtp = simulatedOtp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSimulatedOtp() {
        return simulatedOtp;
    }

    public void setSimulatedOtp(String simulatedOtp) {
        this.simulatedOtp = simulatedOtp;
    }
}
