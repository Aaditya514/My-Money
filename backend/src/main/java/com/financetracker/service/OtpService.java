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
     * Normalizes either an email address (lowercased) or a phone number (10 digits)
     */
    public String normalizeTarget(String target) {
        if (target == null) return "";
        String trimmed = target.trim();
        if (trimmed.contains("@")) {
            return trimmed.toLowerCase();
        }
        return normalizePhoneNumber(trimmed);
    }

    /**
     * Generates a 6-digit numeric OTP valid for 10 minutes
     */
    public String generateOtp(String target) {
        String cleanTarget = normalizeTarget(target);
        int num = 100000 + random.nextInt(900000);
        String code = String.valueOf(num);

        otpStorage.put(cleanTarget, new OtpEntry(code, Instant.now().plusSeconds(OTP_VALIDITY_SECONDS)));
        return code;
    }

    /**
     * Verifies provided OTP against stored active entry
     */
    public boolean verifyOtp(String target, String code) {
        String cleanTarget = normalizeTarget(target);
        if (cleanTarget.isEmpty() || code == null) return false;

        OtpEntry entry = otpStorage.get(cleanTarget);
        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            otpStorage.remove(cleanTarget);
            return false;
        }

        if (entry.code.equals(code.trim())) {
            otpStorage.remove(cleanTarget); // One-time use
            return true;
        }

        return false;
    }

    /**
     * Retrieves currently active OTP for demo/testing display
     */
    public String getActiveOtp(String target) {
        String cleanTarget = normalizeTarget(target);
        OtpEntry entry = otpStorage.get(cleanTarget);
        if (entry != null && !entry.isExpired()) {
            return entry.code;
        }
        return null;
    }
}
