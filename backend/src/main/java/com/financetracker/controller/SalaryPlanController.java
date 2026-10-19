package com.financetracker.controller;

import com.financetracker.dto.SalaryPlanCopyRequest;
import com.financetracker.dto.SalaryPlanRequest;
import com.financetracker.dto.SalaryPlanResponse;
import com.financetracker.service.SalaryPlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/salary-plan")
public class SalaryPlanController {

    private final SalaryPlanService salaryPlanService;

    public SalaryPlanController(SalaryPlanService salaryPlanService) {
        this.salaryPlanService = salaryPlanService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<SalaryPlanResponse> getSalaryPlan(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        LocalDate now = LocalDate.now();
        int resolvedMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();
        int resolvedYear = (year != null && year >= 2000 && year <= 2100) ? year : now.getYear();

        SalaryPlanResponse response = salaryPlanService.getSalaryPlan(userId, resolvedMonth, resolvedYear);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SalaryPlanResponse> saveSalaryPlan(@Valid @RequestBody SalaryPlanRequest request) {
        SalaryPlanResponse response = salaryPlanService.saveSalaryPlan(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/copy")
    public ResponseEntity<SalaryPlanResponse> copySalaryPlan(@Valid @RequestBody SalaryPlanCopyRequest request) {
        SalaryPlanResponse response = salaryPlanService.copySalaryPlan(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalaryPlan(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        salaryPlanService.deleteSalaryPlan(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteSalaryPlanByMonthYear(
            @PathVariable Long userId,
            @RequestParam int month,
            @RequestParam int year) {
        salaryPlanService.deleteSalaryPlanByMonthYear(userId, month, year);
        return ResponseEntity.noContent().build();
    }
}
