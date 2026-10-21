package com.financetracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account.sid:${TWILIO_ACCOUNT_SID:}}")
    private String accountSid;

    @Value("${twilio.auth.token:${TWILIO_AUTH_TOKEN:}}")
    private String authToken;

    @Value("${twilio.phone.number:${TWILIO_PHONE_NUMBER:+19124931390}}")
    private String fromNumber;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Sends a 6-digit OTP to the phone number via Twilio REST API.
     * Returns true if sent successfully, false otherwise.
     */
    public boolean sendOtpSms(String phoneNumber, String otp) {
        String sid = (accountSid != null && !accountSid.isBlank()) ? accountSid : System.getenv("TWILIO_ACCOUNT_SID");
        String token = (authToken != null && !authToken.isBlank()) ? authToken : System.getenv("TWILIO_AUTH_TOKEN");
        String from = (fromNumber != null && !fromNumber.isBlank()) ? fromNumber : System.getenv("TWILIO_PHONE_NUMBER");

        if (sid != null) sid = sid.replaceAll("^[\"']+|[\"']+$", "").trim();
        if (token != null) token = token.replaceAll("^[\"']+|[\"']+$", "").trim();
        if (from != null) from = from.replaceAll("^[\"']+|[\"']+$", "").trim();

        if (sid == null || sid.isBlank() || token == null || token.isBlank()) {
            log.info("Twilio credentials not configured. Falling back to in-memory OTP display.");
            return false;
        }

        try {
            String to = formatE164(phoneNumber);
            String body = "Your Finance Tracker verification code is: " + otp + ". Valid for 10 minutes. Do not share this OTP.";

            String formData = "To=" + URLEncoder.encode(to, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(from != null ? from : "+19124931390", StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(body, StandardCharsets.UTF_8);

            String auth = sid + ":" + token;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + sid + "/Messages.json"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Twilio SMS sent successfully to {}", to);
                return true;
            } else {
                log.error("Twilio SMS rejected with HTTP {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Error sending Twilio SMS to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    private String formatE164(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            return "+91" + digits;
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            return "+" + digits;
        }
        if (!phone.startsWith("+")) {
            return "+" + phone;
        }
        return phone;
    }
}
