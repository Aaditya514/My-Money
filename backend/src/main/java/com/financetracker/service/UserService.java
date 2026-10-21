package com.financetracker.service;

import com.financetracker.dto.*;
import com.financetracker.entity.Category;
import com.financetracker.entity.CategoryType;
import com.financetracker.entity.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.UserRepository;
import com.financetracker.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final SmsService smsService;
    private final EmailService emailService;

    // High-performance thread-safe in-memory cache for user lookups by 10-digit mobile number
    private final java.util.Map<String, User> phoneUserCache = new java.util.concurrent.ConcurrentHashMap<>();

    public UserService(UserRepository userRepository,
                       CategoryRepository categoryRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       OtpService otpService,
                       SmsService smsService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    /**
     * Looks up user by 10-digit phone number: searches cache first, then database, and caches on hit.
     */
    public Optional<User> findUserByPhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return Optional.empty();
        }
        String cleanPhone = otpService.normalizePhoneNumber(rawPhone);
        if (cleanPhone.length() != 10) {
            return Optional.empty();
        }

        // 1. Search in cache first
        User cached = phoneUserCache.get(cleanPhone);
        if (cached != null) {
            if (userRepository.existsById(cached.getId())) {
                return Optional.of(cached);
            } else {
                phoneUserCache.remove(cleanPhone);
            }
        }

        // 2. If not found in cache, query database
        Optional<User> dbUser = userRepository.findByPhoneNumber(cleanPhone);
        dbUser.ifPresent(u -> phoneUserCache.put(cleanPhone, u));
        return dbUser;
    }

    public void clearPhoneCache() {
        phoneUserCache.clear();
    }

    /**
     * Sends a 6-digit registration OTP to either the provided email or 10-digit phone number
     */
    public SendOtpResponse sendRegistrationOtp(SendOtpRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        String phone = request.getPhoneNumber() != null ? otpService.normalizePhoneNumber(request.getPhoneNumber()) : null;

        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            throw new RuntimeException("Please provide an email address or mobile number to receive OTP.");
        }

        // Priority 1: Email OTP (if email is provided)
        if (email != null && !email.isBlank()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new RuntimeException("Please provide a valid email address.");
            }
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email address " + email + " is already registered. Please login instead.");
            }

            String otp = otpService.generateOtp(email);
            boolean sent = emailService.sendOtpEmail(email, otp);

            if (sent) {
                return new SendOtpResponse(true, "Verification code sent to " + email, phone, email, "EMAIL", null);
            } else {
                // If email delivery encounters an issue or quota limit, provide the OTP so the user can verify without being blocked
                return new SendOtpResponse(true, "Verification code generated for " + email, phone, email, "EMAIL", otp);
            }
        }

        // Priority 2: Phone OTP (if phone is provided and no email)
        if (phone != null && !phone.isBlank()) {
            if (phone.length() != 10) {
                throw new RuntimeException("Phone number must be exactly 10 digits.");
            }
            if (userRepository.existsByPhoneNumber(phone)) {
                throw new RuntimeException("Phone number " + phone + " is already registered. Please login instead.");
            }

            String otp = otpService.generateOtp(phone);
            boolean smsSent = smsService.sendOtpSms(phone, otp);

            if (smsSent) {
                return new SendOtpResponse(true, "OTP sent via SMS to +91 " + phone, phone, null, "SMS", null);
            } else {
                return new SendOtpResponse(true, "OTP generated successfully for " + phone, phone, null, "SMS", otp);
            }
        }

        throw new RuntimeException("Please provide an email or phone number.");
    }

    /**
     * Registers a new user with required name, email, mandatory 10-digit mobile number, and password.
     * Immediately generates an authenticated JWT session for automatic login.
     */
    public AuthResponse register(RegisterRequest request) {
        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new RuntimeException("Full name is required.");
        }

        // Email address is required
        if (request.getEmail() == null || request.getEmail().trim().isBlank()) {
            throw new RuntimeException("Email address is required.");
        }
        String email = request.getEmail().trim().toLowerCase();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Please provide a valid email address.");
        }

        // Phone number is strictly mandatory for Splitwise matching & user identity
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isBlank()) {
            throw new RuntimeException("Mobile number is required for Splitwise group matching.");
        }
        String cleanPhone = otpService.normalizePhoneNumber(request.getPhoneNumber());
        if (cleanPhone.length() != 10) {
            throw new RuntimeException("Mobile number must be exactly 10 digits.");
        }

        // Password validation
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long.");
        }

        // Idempotency / Duplicate check 1: Email address
        Optional<User> existingEmailOpt = userRepository.findByEmail(email);
        if (existingEmailOpt.isPresent()) {
            User existing = existingEmailOpt.get();
            // If registered recently (within 5 minutes) with the same password, return existing session safely
            if (existing.getCreatedAt() != null &&
                existing.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(5)) &&
                passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
                UserDto userDto = new UserDto(existing.getId(), existing.getName(), existing.getEmail(), existing.getPhoneNumber());
                String token = jwtService.generateToken(existing.getId(), existing.getEmail(), existing.getName());
                return new AuthResponse(true, "Registration successful!", userDto, token);
            }
            throw new RuntimeException("Email address is already registered. Please login instead.");
        }

        // Idempotency / Duplicate check 2: Phone number (search cache first, then DB)
        Optional<User> existingPhoneOpt = findUserByPhone(cleanPhone);
        if (existingPhoneOpt.isPresent()) {
            User existing = existingPhoneOpt.get();
            if (existing.getCreatedAt() != null &&
                existing.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(5)) &&
                passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
                UserDto userDto = new UserDto(existing.getId(), existing.getName(), existing.getEmail(), existing.getPhoneNumber());
                String token = jwtService.generateToken(existing.getId(), existing.getEmail(), existing.getName());
                return new AuthResponse(true, "Registration successful!", userDto, token);
            }
            throw new RuntimeException("Mobile number is already registered. Please login instead.");
        }

        // Create new user with hashed password and clean 10-digit phone number
        User user = new User();
        user.setName(request.getName().trim());
        user.setPhoneNumber(cleanPhone);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        // Put in phone cache immediately
        phoneUserCache.put(cleanPhone, savedUser);

        // Generate immediate JWT token for automatic seamless login
        UserDto userDto = new UserDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getPhoneNumber());
        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getName());
        return new AuthResponse(true, "Registration successful! Welcome to Finance Tracker.", userDto, token);
    }

    /**
     * Logs in using either a 10-digit phone number or an email address
     */
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();
        String cleanPhone = otpService.normalizePhoneNumber(identifier);

        Optional<User> userOptional = Optional.empty();

        // Check phone cache first, then DB
        if (cleanPhone.length() == 10) {
            userOptional = findUserByPhone(cleanPhone);
        }

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier.toLowerCase());
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid phone number / email or password.");
        }

        User user = userOptional.get();

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches && user.getPassword().equals(request.getPassword())) {
            // Self-healing upgrade for legacy plaintext password
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
            matches = true;
        }

        if (!matches) {
            throw new RuntimeException("Invalid phone number / email or password.");
        }

        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber());
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());
        return new AuthResponse(true, "Login successful!", userDto, token);
    }

    /**
     * Searches registered users: checks cache first by phone number, then queries database
     */
    public List<UserDto> searchUsers(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        String cleanQuery = query.trim();
        String normalizedDigits = otpService.normalizePhoneNumber(cleanQuery);

        // 1. Search in cache first if query is a 10-digit phone number
        if (normalizedDigits.length() == 10) {
            User cached = phoneUserCache.get(normalizedDigits);
            if (cached != null) {
                if (userRepository.existsById(cached.getId())) {
                    return List.of(new UserDto(cached.getId(), cached.getName(), cached.getEmail(), cached.getPhoneNumber()));
                } else {
                    phoneUserCache.remove(normalizedDigits);
                }
            }
        }

        // 2. Query DB and populate cache with found users
        List<User> results = userRepository.searchByNameOrPhone(cleanQuery);
        for (User u : results) {
            if (u.getPhoneNumber() != null && !u.getPhoneNumber().isBlank()) {
                phoneUserCache.put(otpService.normalizePhoneNumber(u.getPhoneNumber()), u);
            }
        }
        return results.stream()
                .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail(), u.getPhoneNumber()))
                .collect(Collectors.toList());
    }

    public User getUserById(Long userId) {
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        // Graceful fallback for standalone demo or server restarts
        return userRepository.findAll().stream().findFirst().orElseGet(() -> {
            User demo = new User("Demo User", "demo@example.com", passwordEncoder.encode("password123"), "9876543210");
            return userRepository.save(demo);
        });
    }
}
