package com.financetracker.controller;

import com.financetracker.dto.GoalContributionRequest;
import com.financetracker.dto.GoalContributionResponse;
import com.financetracker.dto.SavingsGoalRequest;
import com.financetracker.dto.SavingsGoalResponse;
import com.financetracker.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService goalService;

    public SavingsGoalController(SavingsGoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SavingsGoalResponse>> getGoalsByUserId(@PathVariable Long userId) {
        List<SavingsGoalResponse> goals = goalService.getGoalsByUserId(userId);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoalById(@PathVariable Long id,
                                                           @RequestParam(required = false) Long userId) {
        SavingsGoalResponse goal = goalService.getGoalById(id, userId);
        return ResponseEntity.ok(goal);
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(@Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse created = goalService.createGoal(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(@PathVariable Long id,
                                                          @RequestParam(required = false) Long userId,
                                                          @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse updated = goalService.updateGoal(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id,
                                           @RequestParam(required = false) Long userId) {
        goalService.deleteGoal(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<GoalContributionResponse> contributeToGoal(@PathVariable Long id,
                                                                     @RequestParam(required = false) Long userId,
                                                                     @Valid @RequestBody GoalContributionRequest request) {
        Long resolvedUserId = userId != null ? userId : request.getUserId();
        GoalContributionResponse response = goalService.contributeToGoal(id, request, resolvedUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/contributions")
    public ResponseEntity<List<GoalContributionResponse>> getContributions(@PathVariable Long id,
                                                                          @RequestParam(required = false) Long userId) {
        List<GoalContributionResponse> list = goalService.getContributions(id, userId);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/toggle-complete")
    public ResponseEntity<SavingsGoalResponse> toggleComplete(@PathVariable Long id,
                                                              @RequestParam(required = false) Long userId) {
        SavingsGoalResponse updated = goalService.toggleComplete(id, userId);
        return ResponseEntity.ok(updated);
    }
}
