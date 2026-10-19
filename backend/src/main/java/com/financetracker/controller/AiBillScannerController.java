package com.financetracker.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.dto.AiBillScanResponse;
import com.financetracker.dto.AiGroupBillScanRequest;
import com.financetracker.dto.AiGroupBillScanResponse;
import com.financetracker.dto.AiTextScanRequest;
import com.financetracker.service.AiBillScannerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for AI-powered bill scanning and categorization
 * using Google Gemini AI.
 */
@RestController
@RequestMapping("/api/ai")
public class AiBillScannerController {

    private final AiBillScannerService aiBillScannerService;
    private final ObjectMapper objectMapper;

    public AiBillScannerController(AiBillScannerService aiBillScannerService) {
        this.aiBillScannerService = aiBillScannerService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Upload one or multiple images of a receipt/bill and receive AI categorized transaction data
     */
    @PostMapping(value = "/scan-receipt", consumes = "multipart/form-data")
    public ResponseEntity<AiBillScanResponse> scanReceiptImages(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "X-Gemini-Api-Key", required = false) String customApiKey) {

        List<MultipartFile> allFiles = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            allFiles.addAll(files);
        }
        if (file != null && !file.isEmpty()) {
            allFiles.add(file);
        }

        if (allFiles.isEmpty()) {
            throw new IllegalArgumentException("Please upload at least one receipt image.");
        }

        AiBillScanResponse response = aiBillScannerService.scanReceiptImages(allFiles, customApiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Send a text-based bill statement, SMS alert, or invoice text and receive AI categorization
     */
    @PostMapping("/scan-statement")
    public ResponseEntity<AiBillScanResponse> scanBillStatement(
            @Valid @RequestBody AiTextScanRequest request,
            @RequestHeader(value = "X-Gemini-Api-Key", required = false) String customApiKey) {
        AiBillScanResponse response = aiBillScannerService.scanBillStatementText(request.getStatement(), customApiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload receipt images and split among group members with custom order notes
     */
    @PostMapping(value = "/split-group-bill", consumes = "multipart/form-data")
    public ResponseEntity<AiGroupBillScanResponse> splitGroupBillImages(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "groupMembers", required = false) String groupMembersJson,
            @RequestParam(value = "orderNotes", required = false) String orderNotes,
            @RequestParam(value = "defaultPayerMemberId", required = false) Long defaultPayerMemberId,
            @RequestHeader(value = "X-Gemini-Api-Key", required = false) String customApiKey) {

        List<MultipartFile> allFiles = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            allFiles.addAll(files);
        }
        if (file != null && !file.isEmpty()) {
            allFiles.add(file);
        }

        if (allFiles.isEmpty()) {
            throw new IllegalArgumentException("Please upload at least one receipt image.");
        }

        List<AiGroupBillScanRequest.GroupMemberContext> memberList = new ArrayList<>();
        if (groupMembersJson != null && !groupMembersJson.trim().isEmpty()) {
            try {
                memberList = objectMapper.readValue(groupMembersJson, new TypeReference<List<AiGroupBillScanRequest.GroupMemberContext>>() {});
            } catch (Exception e) {
                // fallback
            }
        }

        AiGroupBillScanResponse response = aiBillScannerService.scanGroupReceiptImages(
                allFiles, memberList, orderNotes, defaultPayerMemberId, customApiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Text bill statement group split
     */
    @PostMapping("/split-group-statement")
    public ResponseEntity<AiGroupBillScanResponse> splitGroupStatement(
            @Valid @RequestBody AiGroupBillScanRequest request,
            @RequestHeader(value = "X-Gemini-Api-Key", required = false) String customApiKey) {

        AiGroupBillScanResponse response = aiBillScannerService.scanGroupBillStatementText(
                request.getStatement(), request.getGroupMembers(), request.getOrderNotes(), request.getDefaultPayerMemberId(), customApiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if Gemini API key is configured
     */
    @GetMapping("/key/status")
    public ResponseEntity<Map<String, Object>> getKeyStatus() {
        String masked = aiBillScannerService.getMaskedApiKey();
        boolean isConfigured = masked != null && !masked.isEmpty();
        return ResponseEntity.ok(Map.of(
                "configured", isConfigured,
                "maskedKey", isConfigured ? masked : "Not Configured"
        ));
    }

    /**
     * Test a Gemini API key
     */
    @PostMapping("/key/test")
    public ResponseEntity<Map<String, Object>> testKey(@RequestBody(required = false) Map<String, String> body) {
        String key = (body != null) ? body.get("apiKey") : null;
        Map<String, Object> result = aiBillScannerService.testApiKey(key);
        return ResponseEntity.ok(result);
    }

    /**
     * Update the active in-memory Gemini API key on the backend
     */
    @PostMapping("/key/update")
    public ResponseEntity<Map<String, Object>> updateKey(@RequestBody Map<String, String> body) {
        String key = body != null ? body.get("apiKey") : null;
        aiBillScannerService.setDynamicApiKey(key);
        String masked = aiBillScannerService.getMaskedApiKey();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "maskedKey", masked != null ? masked : "Cleared"
        ));
    }
}
