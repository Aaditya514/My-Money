package com.financetracker.dto;

public class SendOtpResponse {

    private boolean success;
    private String message;
    private String phoneNumber;
    private String email;
    private String destination; // "EMAIL" or "SMS"
    private String simulatedOtp;

    public SendOtpResponse() {
    }

    public SendOtpResponse(boolean success, String message, String phoneNumber, String simulatedOtp) {
        this.success = success;
        this.message = message;
        this.phoneNumber = phoneNumber;
        this.simulatedOtp = simulatedOtp;
        this.destination = "SMS";
    }

    public SendOtpResponse(boolean success, String message, String phoneNumber, String email, String destination, String simulatedOtp) {
        this.success = success;
        this.message = message;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.destination = destination;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSimulatedOtp() {
        return simulatedOtp;
    }

    public void setSimulatedOtp(String simulatedOtp) {
        this.simulatedOtp = simulatedOtp;
    }
}
