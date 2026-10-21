package com.financetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    // Brevo API Configuration
    @Value("${brevo.api.key:${BREVO_API_KEY:}}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:${BREVO_SENDER_EMAIL:ghostrider121322@gmail.com}}")
    private String brevoSenderEmail;

    @Value("${brevo.sender.name:${BREVO_SENDER_NAME:Finance Tracker}}")
    private String brevoSenderName;

    // SMTP Fallback Configuration
    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:noreply@financetracker.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Finance Tracker}")
    private String fromName;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                        @Autowired(required = false) ObjectMapper objectMapper) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private volatile String lastError;

    public String getLastError() {
        return lastError;
    }

    /**
     * Extracts and sanitizes the Brevo API key, stripping quotes and extra text.
     * Guaranteed fallback to the valid Brevo API key.
     */
    public String getEffectiveBrevoKey() {
        String raw = brevoApiKey != null ? brevoApiKey : "";
        if (raw.isBlank()) {
            raw = System.getenv("BREVO_API_KEY");
        }
        if (raw != null && !raw.isBlank()) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(xkeysib-[a-zA-Z0-9]+-[a-zA-Z0-9]+)").matcher(raw);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "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";
    }

    /**
     * Extracts and sanitizes the Brevo sender email.
     */
    public String getEffectiveSenderEmail() {
        String raw = brevoSenderEmail != null ? brevoSenderEmail : "";
        if (raw.isBlank()) {
            raw = System.getenv("BREVO_SENDER_EMAIL");
        }
        if (raw != null && !raw.isBlank()) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})").matcher(raw);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "ghostrider121322@gmail.com";
    }

    /**
     * Checks if either Brevo API or standard SMTP is configured
     */
    public boolean isConfigured() {
        String key = getEffectiveBrevoKey();
        boolean hasBrevo = key != null && !key.isBlank();
        boolean hasSmtp = mailSender != null && mailHost != null && !mailHost.trim().isEmpty();
        return hasBrevo || hasSmtp;
    }

    /**
     * Sends an HTML verification code email to the target recipient.
     * Tries Brevo REST API first, then SMTP fallback, or returns false for simulated dev mode.
     */
    public boolean sendOtpEmail(String toEmail, String otp) {
        // Priority 1: Brevo REST API (Fast, reliable, free 300 emails/day, no SMTP port blocks)
        String activeKey = getEffectiveBrevoKey();
        if (activeKey != null && !activeKey.isBlank()) {
            boolean sent = sendViaBrevo(toEmail.trim(), otp);
            if (sent) return true;
        }

        // Priority 2: Standard JavaMail SMTP
        if (mailSender != null && mailHost != null && !mailHost.trim().isEmpty()) {
            boolean sent = sendViaSmtp(toEmail.trim(), otp);
            if (sent) return true;
        }

        log.info("No active email gateway configured or dispatch failed. Falling back to simulated OTP for {}", toEmail);
        return false;
    }

    /**
     * Extracts and sanitizes the Brevo sender name, stripping quotes.
     */
    public String getEffectiveSenderName() {
        String raw = brevoSenderName != null ? brevoSenderName : "";
        if (raw.isBlank()) {
            raw = System.getenv("BREVO_SENDER_NAME");
        }
        if (raw != null && !raw.isBlank()) {
            raw = raw.replaceAll("^[\"']+|[\"']+$", "").trim();
            if (!raw.isEmpty()) {
                return raw;
            }
        }
        return "Finance Tracker";
    }

    /**
     * Sends transactional email using Brevo's v3 REST API
     */
    private boolean sendViaBrevo(String toEmail, String otp) {
        String activeKey = getEffectiveBrevoKey();
        String senderEmail = getEffectiveSenderEmail();
        String senderName = getEffectiveSenderName();

        try {
            String html = buildHtmlContent(otp);
            String subject = "🔐 " + otp + " is your Finance Tracker verification code";

            Map<String, Object> payload = Map.of(
                    "sender", Map.of(
                            "name", senderName,
                            "email", senderEmail
                    ),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", subject,
                    "htmlContent", html
            );

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(15))
                    .header("api-key", activeKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Brevo Email OTP successfully dispatched to {}", toEmail);
                this.lastError = null;
                return true;
            } else {
                this.lastError = "Brevo HTTP " + response.statusCode() + ": " + response.body();
                log.error("Brevo API rejected email send with HTTP {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            this.lastError = "Brevo Error: " + e.getMessage();
            log.error("Failed to send email via Brevo to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Sends transactional email using JavaMailSender SMTP
     */
    private boolean sendViaSmtp(String toEmail, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail.trim(), fromName.trim()));
            helper.setTo(toEmail.trim());
            helper.setSubject("🔐 " + otp + " is your Finance Tracker verification code");
            helper.setText(buildHtmlContent(otp), true);

            mailSender.send(mimeMessage);
            log.info("SMTP Email OTP sent successfully to {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send Email OTP via SMTP to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Generates a modern, responsive HTML email template
     */
    private String buildHtmlContent(String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Finance Tracker Verification Code</title>
            </head>
            <body style="margin:0; padding:0; background-color:#f8fafc; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color:#0f172a;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:#f8fafc; padding:30px 15px;">
                <tr>
                  <td align="center">
                    <table width="100%" max-width="520px" border="0" cellspacing="0" cellpadding="0" style="max-width:520px; background-color:#ffffff; border-radius:16px; border:1px solid #e2e8f0; box-shadow:0 4px 6px -1px rgba(0,0,0,0.05); overflow:hidden;">
                      
                      <!-- Header -->
                      <tr>
                        <td style="padding:28px 32px 20px; border-bottom:1px solid #f1f5f9; text-align:center;">
                          <div style="display:inline-block; background-color:#f0fdf4; border:1px solid #bbf7d0; border-radius:12px; padding:8px 16px;">
                            <span style="font-weight:700; font-size:16px; color:#0f766e; letter-spacing:0.5px;">💰 Finance Tracker</span>
                          </div>
                        </td>
                      </tr>

                      <!-- Main Content -->
                      <tr>
                        <td style="padding:32px 32px 24px;">
                          <h1 style="margin:0 0 12px; font-size:20px; font-weight:700; color:#0f172a; text-align:center;">Verify Your Email Address</h1>
                          <p style="margin:0 0 24px; font-size:14px; line-height:1.6; color:#475569; text-align:center;">
                            Thank you for signing up for Finance Tracker. Use the 6-digit verification code below to verify your account:
                          </p>

                          <!-- OTP Box -->
                          <div style="margin:0 auto 24px; max-width:280px; background-color:#f0fdf4; border:2px dashed #14b8a6; border-radius:12px; padding:16px; text-align:center;">
                            <div style="font-size:32px; font-weight:800; letter-spacing:8px; color:#0f766e; font-family:Consolas, Monaco, monospace; margin-left:8px;">
                              {{OTP}}
                            </div>
                          </div>

                          <p style="margin:0 0 16px; font-size:13px; line-height:1.5; color:#64748b; text-align:center;">
                            ⏱️ This code will expire in <b>10 minutes</b>.
                          </p>
                          <p style="margin:0; font-size:12px; line-height:1.5; color:#94a3b8; text-align:center;">
                            If you did not request this verification, you can safely ignore this email.
                          </p>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background-color:#f8fafc; padding:20px 32px; border-top:1px solid #f1f5f9; text-align:center; font-size:12px; color:#94a3b8;">
                          Finance Tracker • Personal Wealth &amp; Expense Management
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.replace("{{OTP}}", otp);
    }
}
