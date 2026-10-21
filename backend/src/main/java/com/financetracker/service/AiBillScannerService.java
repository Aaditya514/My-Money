package com.financetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.dto.AiBillScanResponse;
import com.financetracker.dto.AiGroupBillScanRequest;
import com.financetracker.dto.AiGroupBillScanResponse;
import com.financetracker.entity.Category;
import com.financetracker.entity.CategoryType;
import com.financetracker.entity.TransactionType;
import com.financetracker.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to scan and categorize bill receipts and statements
 * using Google Gemini AI.
 * 
 * Beginner-friendly design: Uses Spring's built-in RestTemplate
 * and Jackson ObjectMapper for transparent, simple HTTP calls.
 */
@Service
public class AiBillScannerService {

    private static final Logger log = LoggerFactory.getLogger(AiBillScannerService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String modelName;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private volatile String dynamicApiKey = null;

    public AiBillScannerService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void setDynamicApiKey(String key) {
        this.dynamicApiKey = (key != null && !key.trim().isEmpty()) ? key.trim() : null;
    }

    public String getDynamicApiKey() {
        return this.dynamicApiKey;
    }

    public String getEffectiveApiKey(String requestKey) {
        String key = null;
        if (requestKey != null && !requestKey.trim().isEmpty()) {
            key = requestKey;
        } else if (this.dynamicApiKey != null && !this.dynamicApiKey.trim().isEmpty()) {
            key = this.dynamicApiKey;
        } else {
            key = this.apiKey;
        }
        if (key != null) {
            key = key.replaceAll("^[\"']+|[\"']+$", "").trim();
        }
        return key;
    }

    public String getMaskedApiKey() {
        String key = getEffectiveApiKey(null);
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        key = key.trim();
        if (key.length() <= 8) {
            return "••••••••";
        }
        return key.substring(0, 6) + "••••••••" + key.substring(key.length() - 4);
    }

    /**
     * Scan one or more uploaded bill/receipt images (JPG, PNG, WEBP, etc.)
     * Multi-photo support handles long bills, front & back, or multiple receipts.
     */
    public AiBillScanResponse scanReceiptImages(List<MultipartFile> files, String customApiKey) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Please provide at least one valid receipt or bill image file.");
        }

        List<MultipartFile> validFiles = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();

        if (validFiles.isEmpty()) {
            throw new IllegalArgumentException("All provided files are empty. Please upload valid receipt images.");
        }

        try {
            List<Category> allCategories = categoryRepository.findAll();
            String categoriesListStr = formatCategoriesForPrompt(allCategories);

            String promptDesc = validFiles.size() > 1
                    ? "the " + validFiles.size() + " provided bill/receipt images (which form a multi-page bill, long multi-part receipt, or multiple purchases — calculate the grand total across all images and list all itemized lines in the description)"
                    : "the provided bill/receipt image";

            String promptText = buildPrompt(categoriesListStr, promptDesc);

            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(Map.of("text", promptText));

            for (MultipartFile file : validFiles) {
                byte[] fileBytes = file.getBytes();
                String base64Data = Base64.getEncoder().encodeToString(fileBytes);
                String mimeType = file.getContentType();
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    mimeType = "image/jpeg";
                }
                parts.add(Map.of("inlineData", Map.of(
                        "mimeType", mimeType,
                        "data", base64Data
                )));
            }

            Map<String, Object> content = Map.of("parts", parts);
            Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", generationConfig
            );

            return callGeminiAndParseResponse(requestBody, allCategories, customApiKey);

        } catch (Exception e) {
            throw new RuntimeException("Error scanning bill image(s) with Gemini AI: " + e.getMessage(), e);
        }
    }

    public AiBillScanResponse scanReceiptImage(MultipartFile file) {
        return scanReceiptImage(file, null);
    }

    public AiBillScanResponse scanReceiptImage(MultipartFile file, String customApiKey) {
        if (file == null) {
            throw new IllegalArgumentException("Please provide at least one valid receipt or bill image file.");
        }
        return scanReceiptImages(List.of(file), customApiKey);
    }

    /**
     * Scan a text-based bill statement, invoice text, or SMS alert
     */
    public AiBillScanResponse scanBillStatementText(String statementText) {
        return scanBillStatementText(statementText, null);
    }

    public AiBillScanResponse scanBillStatementText(String statementText, String customApiKey) {
        if (statementText == null || statementText.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide bill statement text.");
        }

        try {
            List<Category> allCategories = categoryRepository.findAll();
            String categoriesListStr = formatCategoriesForPrompt(allCategories);

            String promptText = buildPrompt(categoriesListStr, "the following bill statement / receipt text:\n\"\"\"\n" + statementText + "\n\"\"\"");

            Map<String, Object> textPart = Map.of("text", promptText);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", generationConfig
            );

            return callGeminiAndParseResponse(requestBody, allCategories, customApiKey);

        } catch (Exception e) {
            throw new RuntimeException("Error analyzing bill statement with Gemini AI: " + e.getMessage(), e);
        }
    }

    /**
     * Scan one or more uploaded bill images and split items among group members using Gemini AI.
     */
    public AiGroupBillScanResponse scanGroupReceiptImages(
            List<MultipartFile> files,
            List<AiGroupBillScanRequest.GroupMemberContext> groupMembers,
            String orderNotes,
            Long defaultPayerId,
            String customApiKey) {

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Please provide at least one valid receipt or bill image file.");
        }

        List<MultipartFile> validFiles = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();

        if (validFiles.isEmpty()) {
            throw new IllegalArgumentException("All provided files are empty. Please upload valid receipt images.");
        }

        try {
            List<Category> allCategories = categoryRepository.findAll();
            String categoriesListStr = formatCategoriesForPrompt(allCategories);
            String membersListStr = formatGroupMembersForPrompt(groupMembers);

            String promptDesc = validFiles.size() > 1
                    ? "the " + validFiles.size() + " provided bill/receipt images forming a multi-page or multi-receipt bill"
                    : "the provided bill/receipt image";

            String promptText = buildGroupPrompt(categoriesListStr, membersListStr, orderNotes, promptDesc);

            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(Map.of("text", promptText));

            for (MultipartFile file : validFiles) {
                byte[] fileBytes = file.getBytes();
                String base64Data = Base64.getEncoder().encodeToString(fileBytes);
                String mimeType = file.getContentType();
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    mimeType = "image/jpeg";
                }
                parts.add(Map.of("inlineData", Map.of(
                        "mimeType", mimeType,
                        "data", base64Data
                )));
            }

            Map<String, Object> content = Map.of("parts", parts);
            Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", generationConfig
            );

            return callGeminiAndParseGroupResponse(requestBody, allCategories, groupMembers, defaultPayerId, customApiKey);

        } catch (Exception e) {
            throw new RuntimeException("Error analyzing group receipt with Gemini AI: " + e.getMessage(), e);
        }
    }

    /**
     * Scan a text bill / statement and split items among group members using Gemini AI.
     */
    public AiGroupBillScanResponse scanGroupBillStatementText(
            String statementText,
            List<AiGroupBillScanRequest.GroupMemberContext> groupMembers,
            String orderNotes,
            Long defaultPayerId,
            String customApiKey) {

        if (statementText == null || statementText.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide bill statement text.");
        }

        try {
            List<Category> allCategories = categoryRepository.findAll();
            String categoriesListStr = formatCategoriesForPrompt(allCategories);
            String membersListStr = formatGroupMembersForPrompt(groupMembers);

            String promptText = buildGroupPrompt(categoriesListStr, membersListStr, orderNotes,
                    "the following bill statement text:\n\"\"\"\n" + statementText + "\n\"\"\"");

            Map<String, Object> textPart = Map.of("text", promptText);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", generationConfig
            );

            return callGeminiAndParseGroupResponse(requestBody, allCategories, groupMembers, defaultPayerId, customApiKey);

        } catch (Exception e) {
            throw new RuntimeException("Error analyzing group bill statement with Gemini AI: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the Gemini generateContent call with automatic model fallback.
     * Tries configured model first, and if a 404 is encountered (e.g. deprecated model),
     * automatically cycles through supported fallback models (gemini-2.5-flash, gemini-2.0-flash, gemini-2.5-flash-lite).
     */
    private ResponseEntity<String> executeWithModelFallback(Map<String, Object> requestBody, String keyToUse) {
        List<String> modelsToTry = new ArrayList<>();
        if (modelName != null && !modelName.isBlank()) {
            modelsToTry.add(modelName.trim());
        }
        for (String m : List.of("gemini-3.5-flash-lite", "gemini-3.5-flash", "gemini-2.5-flash", "gemini-2.5-flash-lite")) {
            if (!modelsToTry.contains(m)) {
                modelsToTry.add(m);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Exception lastException = null;
        for (String candidate : modelsToTry) {
            String endpoint = String.format("%s/%s:generateContent?key=%s", apiUrl, candidate, keyToUse);
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    if (!candidate.equals(this.modelName)) {
                        log.info("Gemini model switched to working model: {}", candidate);
                        this.modelName = candidate;
                    }
                    return response;
                }
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                lastException = e;
                if (e.getStatusCode().value() == 404) {
                    log.warn("Gemini model '{}' not found (404), trying fallback model...", candidate);
                    continue;
                }
                throw e; // Non-404 error (e.g. 400 Bad Request, 403 Invalid Key, 429 Quota Exceeded)
            } catch (Exception e) {
                lastException = e;
                log.warn("Error calling Gemini model '{}': {}", candidate, e.getMessage());
            }
        }

        if (lastException instanceof RuntimeException re) {
            throw re;
        }
        throw new RuntimeException("Gemini AI request failed across candidate models: " + (lastException != null ? lastException.getMessage() : "Unknown error"), lastException);
    }

    public Map<String, Object> testApiKey(String keyToTest) {
        String key = (keyToTest != null && !keyToTest.trim().isEmpty()) ? keyToTest.trim() : getEffectiveApiKey(null);
        if (key == null || key.trim().isEmpty()) {
            return Map.of("valid", false, "message", "No API key provided or configured.");
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", "Respond with 'OK'"))))
            );
            ResponseEntity<String> response = executeWithModelFallback(requestBody, key);

            if (response.getStatusCode().is2xxSuccessful()) {
                return Map.of("valid", true, "model", modelName, "message", "API Key is valid and working!");
            } else {
                return Map.of("valid", false, "message", "Gemini returned status " + response.getStatusCode());
            }
        } catch (Exception e) {
            return Map.of("valid", false, "message", e.getMessage());
        }
    }

    /**
     * Sends the HTTP POST request to Google Gemini and parses the structured JSON output.
     */
    private AiBillScanResponse callGeminiAndParseResponse(Map<String, Object> requestBody, List<Category> categories, String customApiKey) throws Exception {
        String keyToUse = getEffectiveApiKey(customApiKey);
        if (keyToUse == null || keyToUse.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured. Please add your API key in settings.");
        }

        ResponseEntity<String> response = executeWithModelFallback(requestBody, keyToUse);

        // Parse Gemini top-level JSON response
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode candidates = rootNode.path("candidates");
        if (candidates.isEmpty()) {
            throw new RuntimeException("Gemini AI could not detect any readable transaction data in this bill.");
        }

        String rawJsonText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();

        // Parse extracted JSON from the model
        JsonNode parsedBill = objectMapper.readTree(rawJsonText);

        String title = parsedBill.path("title").asText("Bill Payment");
        double amountDouble = parsedBill.path("amount").asDouble(0.0);
        BigDecimal amount = BigDecimal.valueOf(amountDouble).setScale(2, java.math.RoundingMode.HALF_UP);

        String typeStr = parsedBill.path("type").asText("EXPENSE").toUpperCase();
        TransactionType transactionType = "INCOME".equalsIgnoreCase(typeStr) ? TransactionType.INCOME : TransactionType.EXPENSE;

        String suggestedCategory = parsedBill.path("category").asText("Other");
        String dateStr = parsedBill.path("date").asText("");
        String description = parsedBill.path("description").asText("");
        String summary = parsedBill.path("summary").asText("Extracted with Gemini AI");

        // Resolve Category to Database Category ID & Name
        Category matchedCategory = findBestMatchingCategory(suggestedCategory, categories, transactionType);

        // Resolve Date (fallback to today if unparseable)
        LocalDate transactionDate = parseDateOrDefault(dateStr);

        return new AiBillScanResponse(
                title,
                amount,
                transactionType,
                matchedCategory != null ? matchedCategory.getId() : null,
                matchedCategory != null ? matchedCategory.getName() : suggestedCategory,
                transactionDate,
                description,
                summary
        );
    }

    /**
     * Constructs a clean, strict prompt for Gemini to return a structured JSON object.
     */
    private String buildPrompt(String categoriesListStr, String inputDescription) {
        return "You are an intelligent financial assistant for a Personal Finance Tracker application.\n"
                + "Analyze " + inputDescription + " and extract the transaction details.\n\n"
                + "Available Categories in the user's database are:\n"
                + categoriesListStr + "\n\n"
                + "Instructions:\n"
                + "1. Extract the merchant/service name or vendor for 'title'. Keep it clean and concise (e.g. 'Starbucks Coffee', 'Electricity Bill', 'Uber Ride', 'Walmart').\n"
                + "2. Extract the total final amount as a numeric value for 'amount' (e.g. 24.50 or 450.00). Do not include currency symbols in the number.\n"
                + "3. Determine if it is 'EXPENSE' or 'INCOME' for 'type' (almost always 'EXPENSE' for bills/receipts).\n"
                + "4. Categorize this bill by choosing the closest matching category from the Available Categories list above for 'category'.\n"
                + "5. Extract the date in 'YYYY-MM-DD' format for 'date'. If no date is found, return null.\n"
                + "6. Provide a clean summary of purchased items or service details for 'description'.\n"
                + "7. Provide a short 1-sentence explanation of why this category was chosen for 'summary'.\n\n"
                + "Return ONLY a valid JSON object matching this exact schema:\n"
                + "{\n"
                + "  \"title\": \"string\",\n"
                + "  \"amount\": number,\n"
                + "  \"type\": \"EXPENSE\" | \"INCOME\",\n"
                + "  \"category\": \"string\",\n"
                + "  \"date\": \"YYYY-MM-DD\" | null,\n"
                + "  \"description\": \"string\",\n"
                + "  \"summary\": \"string\"\n"
                + "}";
    }

    /**
     * Formats category names with their type for prompt context
     */
    private String formatCategoriesForPrompt(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return "- Food, Transport, Shopping, Bills, Entertainment, Education, Healthcare, Other (EXPENSE)\n- Salary, Freelance, Bonus (INCOME)";
        }

        return categories.stream()
                .map(c -> "- " + c.getName() + " (" + c.getType() + ")")
                .collect(Collectors.joining("\n"));
    }

    /**
     * Matches the AI suggested category string with existing database categories.
     */
    private Category findBestMatchingCategory(String suggestedCategory, List<Category> categories, TransactionType type) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        String cleanSuggested = suggestedCategory != null ? suggestedCategory.trim().toLowerCase() : "";

        // 1. Exact match (case-insensitive)
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(cleanSuggested)) {
                return c;
            }
        }

        // 2. Partial match (e.g. "Groceries" -> "Food", "Electric" -> "Bills", "Taxi" -> "Transport")
        for (Category c : categories) {
            String catLower = c.getName().toLowerCase();
            if (cleanSuggested.contains(catLower) || catLower.contains(cleanSuggested)) {
                return c;
            }
        }

        // Common synonym helpers
        if (cleanSuggested.contains("grocery") || cleanSuggested.contains("restaurant") || cleanSuggested.contains("cafe") || cleanSuggested.contains("dining") || cleanSuggested.contains("coffee")) {
            return findCategoryByName(categories, "Food");
        }
        if (cleanSuggested.contains("electric") || cleanSuggested.contains("water") || cleanSuggested.contains("internet") || cleanSuggested.contains("utility") || cleanSuggested.contains("phone")) {
            return findCategoryByName(categories, "Bills");
        }
        if (cleanSuggested.contains("gas") || cleanSuggested.contains("fuel") || cleanSuggested.contains("cab") || cleanSuggested.contains("uber") || cleanSuggested.contains("metro")) {
            return findCategoryByName(categories, "Transport");
        }
        if (cleanSuggested.contains("cloth") || cleanSuggested.contains("mall") || cleanSuggested.contains("store") || cleanSuggested.contains("amazon")) {
            return findCategoryByName(categories, "Shopping");
        }
        if (cleanSuggested.contains("medical") || cleanSuggested.contains("pharmacy") || cleanSuggested.contains("doctor") || cleanSuggested.contains("hospital")) {
            return findCategoryByName(categories, "Healthcare");
        }

        // 3. Fallback: match "Other" or first category matching the transaction type
        Category otherCategory = findCategoryByName(categories, "Other");
        if (otherCategory != null) {
            return otherCategory;
        }

        CategoryType expectedType = (type == TransactionType.INCOME) ? CategoryType.INCOME : CategoryType.EXPENSE;
        return categories.stream()
                .filter(c -> c.getType() == expectedType)
                .findFirst()
                .orElse(categories.get(0));
    }

    private Category findCategoryByName(List<Category> categories, String name) {
        return categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private LocalDate parseDateOrDefault(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "null".equalsIgnoreCase(dateStr.trim())) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String formatGroupMembersForPrompt(List<AiGroupBillScanRequest.GroupMemberContext> groupMembers) {
        if (groupMembers == null || groupMembers.isEmpty()) {
            return "- ID: 1, Name: Member 1\n- ID: 2, Name: Member 2";
        }
        return groupMembers.stream()
                .map(m -> "- ID: " + m.getId() + ", Name: " + m.getName())
                .collect(Collectors.joining("\n"));
    }

    private String buildGroupPrompt(String categoriesListStr, String membersListStr, String orderNotes, String inputDescription) {
        String notesSection = (orderNotes != null && !orderNotes.trim().isEmpty())
                ? "User order notes / Who ordered what:\n\"\"\"\n" + orderNotes.trim() + "\n\"\"\"\n"
                : "No specific order notes provided. Split the bill equally among all group members, listing all itemized lines in summaryNotes.\n";

        return "You are an expert financial assistant and group bill-splitting AI.\n"
                + "Analyze " + inputDescription + " and split the expenses among group members based on the order notes.\n\n"
                + "Available Categories:\n"
                + categoriesListStr + "\n\n"
                + "Group Members in this group:\n"
                + membersListStr + "\n\n"
                + notesSection + "\n"
                + "Instructions:\n"
                + "1. Extract the merchant/place name for 'merchantName' and a descriptive title for 'expenseTitle' (e.g. 'Mainland China Dinner', 'Uber Ride to Airport').\n"
                + "2. Extract the subtotal, taxes, tips or service charges, and final grand total as 'totalAmount'.\n"
                + "3. Choose the closest matching category from the Available Categories list for 'category' (e.g. 'Food', 'Transport', 'Entertainment', 'Bills', 'Shopping').\n"
                + "4. Extract the date in 'YYYY-MM-DD' format for 'date'. If unparseable, use today's date.\n"
                + "5. Detect who paid the bill: If the user specified who paid in the notes (e.g. 'Rahul paid', 'Aman paid the bill'), set 'paidByMemberId' to that member's ID and 'paidByMemberName' to their name. Otherwise return null.\n"
                + "6. Itemized splitting logic:\n"
                + "   - Extract all itemized dishes/services with their prices in 'items'.\n"
                + "   - Assign items to members based on the order notes. If an item was shared between specific people, split its cost equally among them.\n"
                + "   - If an item was shared by everyone or unassigned, divide it equally among all members.\n"
                + "   - Distribute taxes, delivery fees, and service charges/tips proportionally (or equally) so that EVERY penny is accounted for.\n"
                + "   - CRITICAL: For every group member, calculate their exact share in 'memberShares' with 'memberId', 'memberName', 'owedAmount', and 'itemsSummary'.\n"
                + "   - THE SUM OF ALL memberShares owedAmount MUST EXACTLY EQUAL 'totalAmount'. Adjust rounding by a cent/paise on the highest share if necessary.\n"
                + "7. In 'summaryNotes', write a concise, clean, human-readable breakdown of who had what and where the money went (e.g. '• Rahul: Butter Chicken (₹350) + 1/2 Naan (₹60) + Tax (₹35) = ₹445\\n• Priya: Veg Biryani (₹280) + Tax (₹25) = ₹305').\n\n"
                + "Return ONLY a valid JSON object matching this schema:\n"
                + "{\n"
                + "  \"merchantName\": \"string\",\n"
                + "  \"expenseTitle\": \"string\",\n"
                + "  \"totalAmount\": number,\n"
                + "  \"subtotal\": number,\n"
                + "  \"taxAmount\": number,\n"
                + "  \"tipOrServiceAmount\": number,\n"
                + "  \"category\": \"string\",\n"
                + "  \"date\": \"YYYY-MM-DD\" | null,\n"
                + "  \"paidByMemberId\": number | null,\n"
                + "  \"paidByMemberName\": \"string\" | null,\n"
                + "  \"summaryNotes\": \"string\",\n"
                + "  \"memberShares\": [\n"
                + "    {\n"
                + "      \"memberId\": number,\n"
                + "      \"memberName\": \"string\",\n"
                + "      \"owedAmount\": number,\n"
                + "      \"itemsSummary\": \"string\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"items\": [\n"
                + "    {\n"
                + "      \"name\": \"string\",\n"
                + "      \"price\": number,\n"
                + "      \"assignedTo\": [\"string\"]\n"
                + "    }\n"
                + "  ]\n"
                + "}";
    }

    private AiGroupBillScanResponse callGeminiAndParseGroupResponse(
            Map<String, Object> requestBody,
            List<Category> categories,
            List<AiGroupBillScanRequest.GroupMemberContext> groupMembers,
            Long defaultPayerId,
            String customApiKey) throws Exception {

        String keyToUse = getEffectiveApiKey(customApiKey);
        if (keyToUse == null || keyToUse.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured. Please add your API key in settings.");
        }

        ResponseEntity<String> response = executeWithModelFallback(requestBody, keyToUse);

        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode candidates = rootNode.path("candidates");
        if (candidates.isEmpty()) {
            throw new RuntimeException("Gemini AI could not detect any readable transaction data in this bill.");
        }

        String rawJsonText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        JsonNode parsed = objectMapper.readTree(rawJsonText);

        AiGroupBillScanResponse res = new AiGroupBillScanResponse();
        res.setMerchantName(parsed.path("merchantName").asText("Group Expense"));
        res.setExpenseTitle(parsed.path("expenseTitle").asText(res.getMerchantName()));

        double totalDouble = parsed.path("totalAmount").asDouble(0.0);
        BigDecimal totalAmount = BigDecimal.valueOf(totalDouble).setScale(2, java.math.RoundingMode.HALF_UP);
        res.setTotalAmount(totalAmount);

        double subtotalDouble = parsed.path("subtotal").asDouble(totalDouble);
        res.setSubtotal(BigDecimal.valueOf(subtotalDouble).setScale(2, java.math.RoundingMode.HALF_UP));

        double taxDouble = parsed.path("taxAmount").asDouble(0.0);
        res.setTaxAmount(BigDecimal.valueOf(taxDouble).setScale(2, java.math.RoundingMode.HALF_UP));

        double tipDouble = parsed.path("tipOrServiceAmount").asDouble(0.0);
        res.setTipOrServiceAmount(BigDecimal.valueOf(tipDouble).setScale(2, java.math.RoundingMode.HALF_UP));

        String suggestedCat = parsed.path("category").asText("Food");
        Category matchedCat = findBestMatchingCategory(suggestedCat, categories, TransactionType.EXPENSE);
        if (matchedCat != null) {
            res.setCategoryId(matchedCat.getId());
            res.setCategoryName(matchedCat.getName());
        } else {
            res.setCategoryName(suggestedCat);
        }

        String dateStr = parsed.path("date").asText("");
        res.setExpenseDate(parseDateOrDefault(dateStr));

        // Payer resolution
        Long parsedPayerId = parsed.path("paidByMemberId").isNumber() ? parsed.path("paidByMemberId").asLong() : null;
        if (parsedPayerId == null && defaultPayerId != null) {
            parsedPayerId = defaultPayerId;
        }
        if (parsedPayerId == null && groupMembers != null && !groupMembers.isEmpty()) {
            parsedPayerId = groupMembers.get(0).getId();
        }
        res.setPaidByMemberId(parsedPayerId);

        String payerName = parsed.path("paidByMemberName").asText("");
        if ((payerName == null || payerName.trim().isEmpty()) && parsedPayerId != null && groupMembers != null) {
            for (AiGroupBillScanRequest.GroupMemberContext m : groupMembers) {
                if (m.getId() != null && m.getId().equals(parsedPayerId)) {
                    payerName = m.getName();
                    break;
                }
            }
        }
        res.setPaidByMemberName(payerName);

        res.setNotes(parsed.path("summaryNotes").asText(""));

        // Items
        JsonNode itemsNode = parsed.path("items");
        if (itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                String name = itemNode.path("name").asText("");
                BigDecimal price = BigDecimal.valueOf(itemNode.path("price").asDouble(0.0)).setScale(2, java.math.RoundingMode.HALF_UP);
                List<String> assigned = new ArrayList<>();
                JsonNode assignedNode = itemNode.path("assignedTo");
                if (assignedNode.isArray()) {
                    for (JsonNode a : assignedNode) {
                        assigned.add(a.asText());
                    }
                }
                res.getItems().add(new AiGroupBillScanResponse.BillItemDto(name, price, assigned));
            }
        }

        // Member Shares
        Map<Long, AiGroupBillScanResponse.MemberShareDto> shareMap = new LinkedHashMap<>();
        JsonNode sharesNode = parsed.path("memberShares");
        if (sharesNode.isArray()) {
            for (JsonNode sNode : sharesNode) {
                Long mId = sNode.path("memberId").asLong();
                String mName = sNode.path("memberName").asText("");
                BigDecimal owed = BigDecimal.valueOf(sNode.path("owedAmount").asDouble(0.0)).setScale(2, java.math.RoundingMode.HALF_UP);
                String summary = sNode.path("itemsSummary").asText("");
                shareMap.put(mId, new AiGroupBillScanResponse.MemberShareDto(mId, mName, owed, summary));
            }
        }

        // Guarantee all group members have a share entry
        if (groupMembers != null) {
            for (AiGroupBillScanRequest.GroupMemberContext m : groupMembers) {
                if (!shareMap.containsKey(m.getId())) {
                    shareMap.put(m.getId(), new AiGroupBillScanResponse.MemberShareDto(m.getId(), m.getName(), BigDecimal.ZERO.setScale(2), "No individual items"));
                }
            }
        }

        List<AiGroupBillScanResponse.MemberShareDto> finalShares = new ArrayList<>(shareMap.values());

        // Balance share sum to match totalAmount exactly
        BigDecimal sumShares = finalShares.stream()
                .map(AiGroupBillScanResponse.MemberShareDto::getOwedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal diff = totalAmount.subtract(sumShares);
        if (diff.abs().compareTo(new BigDecimal("0.50")) <= 0 && diff.compareTo(BigDecimal.ZERO) != 0) {
            // Apply discrepancy to largest share
            finalShares.stream()
                    .filter(s -> s.getOwedAmount().compareTo(BigDecimal.ZERO) > 0)
                    .max(Comparator.comparing(AiGroupBillScanResponse.MemberShareDto::getOwedAmount))
                    .ifPresent(largest -> largest.setOwedAmount(largest.getOwedAmount().add(diff).setScale(2, java.math.RoundingMode.HALF_UP)));
        }

        res.setMemberShares(finalShares);
        return res;
    }
}

