package com.financetracker.controller;

import com.financetracker.dto.DashboardResponse;
import com.financetracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<DashboardResponse> getDashboardData(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        DashboardResponse dashboardData = dashboardService.getDashboardData(userId, month, year);
        return ResponseEntity.ok(dashboardData);
    }
}
