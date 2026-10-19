package com.financetracker.controller;

import com.financetracker.dto.*;
import com.financetracker.service.SplitwiseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/splitwise")
public class SplitwiseController {

    private final SplitwiseService splitwiseService;

    public SplitwiseController(SplitwiseService splitwiseService) {
        this.splitwiseService = splitwiseService;
    }

    // ==========================================
    // Groups
    // ==========================================

    @GetMapping("/groups/user/{userId}")
    public ResponseEntity<List<SplitGroupDTO>> getGroupsByUser(@PathVariable Long userId) {
        List<SplitGroupDTO> groups = splitwiseService.getGroupsByUserId(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<SplitGroupDTO> getGroupById(
            @PathVariable Long groupId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        SplitGroupDTO group = splitwiseService.getGroupById(groupId, userId);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/groups")
    public ResponseEntity<SplitGroupDTO> createGroup(
            @Valid @RequestBody SplitGroupRequest request,
            @RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : (request.getUserId() != null ? request.getUserId() : 1L);
        SplitGroupDTO created = splitwiseService.createGroup(request, targetUserId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/groups/{groupId}")
    public ResponseEntity<SplitGroupDTO> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody SplitGroupRequest request,
            @RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : (request.getUserId() != null ? request.getUserId() : 1L);
        SplitGroupDTO updated = splitwiseService.updateGroup(groupId, request, targetUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        splitwiseService.deleteGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // Members
    // ==========================================

    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<List<SplitMemberDTO>> getGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        List<SplitMemberDTO> members = splitwiseService.getMembers(groupId, userId);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<SplitMemberDTO> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody SplitMemberRequest request,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        SplitMemberDTO added = splitwiseService.addMember(groupId, request, userId);
        return new ResponseEntity<>(added, HttpStatus.CREATED);
    }

    @DeleteMapping("/groups/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        splitwiseService.removeMember(groupId, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // Expenses
    // ==========================================

    @GetMapping("/groups/{groupId}/expenses")
    public ResponseEntity<List<SplitExpenseResponse>> getGroupExpenses(
            @PathVariable Long groupId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        List<SplitExpenseResponse> expenses = splitwiseService.getExpensesByGroupId(groupId, userId);
        return ResponseEntity.ok(expenses);
    }

    @PostMapping("/groups/{groupId}/expenses")
    public ResponseEntity<SplitExpenseResponse> createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody SplitExpenseRequest request,
            @RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : (request.getUserId() != null ? request.getUserId() : 1L);
        SplitExpenseResponse created = splitwiseService.createExpense(groupId, request, targetUserId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/groups/{groupId}/expenses/{expenseId}")
    public ResponseEntity<SplitExpenseResponse> updateExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @Valid @RequestBody SplitExpenseRequest request,
            @RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : (request.getUserId() != null ? request.getUserId() : 1L);
        SplitExpenseResponse updated = splitwiseService.updateExpense(groupId, expenseId, request, targetUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/groups/{groupId}/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        splitwiseService.deleteExpense(groupId, expenseId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 1-Tap Ledger Synchronization
    // ==========================================

    @PostMapping("/groups/{groupId}/expenses/{expenseId}/sync-to-ledger")
    public ResponseEntity<SplitExpenseResponse> syncExpenseToLedger(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        SplitExpenseResponse res = splitwiseService.syncExpenseToLedger(groupId, expenseId, userId);
        return ResponseEntity.ok(res);
    }

    // ==========================================
    // Settlements
    // ==========================================

    @GetMapping("/groups/{groupId}/settlements")
    public ResponseEntity<List<SplitSettlementResponse>> getGroupSettlements(
            @PathVariable Long groupId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        List<SplitSettlementResponse> settlements = splitwiseService.getSettlementsByGroupId(groupId, userId);
        return ResponseEntity.ok(settlements);
    }

    @PostMapping("/groups/{groupId}/settlements")
    public ResponseEntity<SplitSettlementResponse> createSettlement(
            @PathVariable Long groupId,
            @Valid @RequestBody SplitSettlementRequest request,
            @RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : (request.getUserId() != null ? request.getUserId() : 1L);
        SplitSettlementResponse created = splitwiseService.createSettlement(groupId, request, targetUserId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ==========================================
    // Balances & Debt Simplification
    // ==========================================

    @GetMapping("/groups/{groupId}/balances")
    public ResponseEntity<SplitBalanceSummaryDTO> getBalanceSummary(
            @PathVariable Long groupId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        SplitBalanceSummaryDTO summary = splitwiseService.getBalanceSummary(groupId, userId);
        return ResponseEntity.ok(summary);
    }

    // ==========================================
    // Contacts & Registered User Search
    // ==========================================

    @GetMapping("/contacts")
    public ResponseEntity<List<SplitContactDTO>> getContacts(
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        List<SplitContactDTO> contacts = splitwiseService.getContacts(userId);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/search-users")
    public ResponseEntity<List<SplitContactDTO>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String query) {
        List<SplitContactDTO> results = splitwiseService.searchRegisteredUsers(query);
        return ResponseEntity.ok(results);
    }
}
