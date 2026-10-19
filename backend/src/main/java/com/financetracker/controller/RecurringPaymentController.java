package com.financetracker.controller;

import com.financetracker.dto.RecurringPaymentRecordResult;
import com.financetracker.dto.RecurringPaymentRequest;
import com.financetracker.dto.RecurringPaymentResponse;
import com.financetracker.service.RecurringPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-payments")
public class RecurringPaymentController {

    private final RecurringPaymentService recurringPaymentService;

    public RecurringPaymentController(RecurringPaymentService recurringPaymentService) {
        this.recurringPaymentService = recurringPaymentService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecurringPaymentResponse>> getRecurringPaymentsByUser(@PathVariable Long userId) {
        List<RecurringPaymentResponse> list = recurringPaymentService.getRecurringPaymentsByUserId(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<RecurringPaymentResponse>> getUpcomingPayments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "14") int days) {
        List<RecurringPaymentResponse> list = recurringPaymentService.getUpcomingPayments(userId, days);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringPaymentResponse> getRecurringPaymentById(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        RecurringPaymentResponse response = recurringPaymentService.getRecurringPaymentById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<RecurringPaymentResponse> createRecurringPayment(@Valid @RequestBody RecurringPaymentRequest request) {
        RecurringPaymentResponse created = recurringPaymentService.createRecurringPayment(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringPaymentResponse> updateRecurringPayment(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody RecurringPaymentRequest request) {
        Long targetUserId = userId != null ? userId : (request.getUserId() != null ? request.getUserId() : 1L);
        RecurringPaymentResponse updated = recurringPaymentService.updateRecurringPayment(id, request, targetUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringPayment(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        recurringPaymentService.deleteRecurringPayment(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringPaymentResponse> toggleActive(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        RecurringPaymentResponse updated = recurringPaymentService.toggleActive(id, userId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/record")
    public ResponseEntity<RecurringPaymentRecordResult> recordPayment(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        RecurringPaymentRecordResult result = recurringPaymentService.recordPayment(id, userId);
        return ResponseEntity.ok(result);
    }
}
