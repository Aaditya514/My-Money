package com.financetracker.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpEntry {
        final String code;
        final Instant expiresAt;

        OtpEntry(String code, Instant expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private static final long OTP_VALIDITY_SECONDS = 600; // 10 minutes

    /**
     * Clean phone number (strip whitespace, hyphens, +91 prefix) to standard 10-digit number
     */
    public String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() > 10 && digits.startsWith("91")) {
            digits = digits.substring(2);
        }
        return digits;
    }

    /**
     * Generates a 6-digit numeric OTP valid for 10 minutes
     */
    public String generateOtp(String phoneNumber) {
        String cleanPhone = normalizePhoneNumber(phoneNumber);
        int num = 100000 + random.nextInt(900000);
        String code = String.valueOf(num);

        otpStorage.put(cleanPhone, new OtpEntry(code, Instant.now().plusSeconds(OTP_VALIDITY_SECONDS)));
        return code;
    }

    /**
     * Verifies provided OTP against stored active entry
     */
    public boolean verifyOtp(String phoneNumber, String code) {
        String cleanPhone = normalizePhoneNumber(phoneNumber);
        if (cleanPhone.isEmpty() || code == null) return false;

        OtpEntry entry = otpStorage.get(cleanPhone);
        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            otpStorage.remove(cleanPhone);
            return false;
        }

        if (entry.code.equals(code.trim())) {
            otpStorage.remove(cleanPhone); // One-time use
            return true;
        }

        return false;
    }

    /**
     * Retrieves currently active OTP for demo/testing display
     */
    public String getActiveOtp(String phoneNumber) {
        String cleanPhone = normalizePhoneNumber(phoneNumber);
        OtpEntry entry = otpStorage.get(cleanPhone);
        if (entry != null && !entry.isExpired()) {
            return entry.code;
        }
        return null;
    }
}
