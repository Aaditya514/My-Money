package com.financetracker.service;

import com.financetracker.dto.GoalContributionRequest;
import com.financetracker.dto.GoalContributionResponse;
import com.financetracker.dto.SavingsGoalRequest;
import com.financetracker.dto.SavingsGoalResponse;
import com.financetracker.entity.Account;
import com.financetracker.entity.SavingsGoal;
import com.financetracker.entity.SavingsGoalContribution;
import com.financetracker.entity.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.SavingsGoalContributionRepository;
import com.financetracker.repository.SavingsGoalRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final SavingsGoalContributionRepository contributionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public SavingsGoalService(SavingsGoalRepository goalRepository,
                              SavingsGoalContributionRepository contributionRepository,
                              AccountRepository accountRepository,
                              UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.contributionRepository = contributionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoalsByUserId(Long userId) {
        List<SavingsGoal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<SavingsGoalResponse> responses = new ArrayList<>();
        for (SavingsGoal g : goals) {
            responses.add(mapToResponse(g));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoalById(Long id, Long userId) {
        SavingsGoal goal = getGoalEntity(id, userId);
        return mapToResponse(goal);
    }

    @Transactional(readOnly = true)
    public SavingsGoal getGoalEntity(Long id, Long userId) {
        if (userId != null) {
            return goalRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new RuntimeException("Savings goal not found with id: " + id));
        }
        return goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found with id: " + id));
    }

    public SavingsGoalResponse createGoal(SavingsGoalRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findByIdAndUserId(request.getAccountId(), user.getId())
                    .orElse(null);
        }

        BigDecimal current = request.getInitialAmount() != null ? request.getInitialAmount() : BigDecimal.ZERO;

        SavingsGoal goal = new SavingsGoal(
                request.getName(),
                request.getTargetAmount(),
                current,
                request.getTargetDate(),
                request.getNotes(),
                account,
                user
        );

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCompleted(true);
            goal.setCompletedAt(LocalDateTime.now());
        }

        SavingsGoal saved = goalRepository.save(goal);
        return mapToResponse(saved);
    }

    public SavingsGoalResponse updateGoal(Long id, SavingsGoalRequest request, Long userId) {
        SavingsGoal goal = getGoalEntity(id, userId);

        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setNotes(request.getNotes());

        if (request.getAccountId() != null) {
            Account account = accountRepository.findByIdAndUserId(request.getAccountId(), goal.getUser().getId())
                    .orElse(null);
            goal.setAccount(account);
        } else {
            goal.setAccount(null);
        }

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            if (!goal.isCompleted()) {
                goal.setCompleted(true);
                goal.setCompletedAt(LocalDateTime.now());
            }
        } else {
            goal.setCompleted(false);
            goal.setCompletedAt(null);
        }

        SavingsGoal updated = goalRepository.save(goal);
        return mapToResponse(updated);
    }

    public void deleteGoal(Long id, Long userId) {
        SavingsGoal goal = getGoalEntity(id, userId);
        goalRepository.delete(goal);
    }

    public GoalContributionResponse contributeToGoal(Long goalId, GoalContributionRequest request, Long userId) {
        SavingsGoal goal = getGoalEntity(goalId, userId);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Contribution amount must be greater than zero.");
        }

        LocalDate date = request.getContributionDate() != null ? request.getContributionDate() : LocalDate.now();

        Account sourceAccount = null;
        if (request.getSourceAccountId() != null) {
            sourceAccount = accountRepository.findByIdAndUserId(request.getSourceAccountId(), goal.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Source account not found: " + request.getSourceAccountId()));

            // Debit source account
            sourceAccount.setCurrentBalance(sourceAccount.getCurrentBalance().subtract(request.getAmount()));
            accountRepository.save(sourceAccount);

            // If goal has a distinct linked savings account, credit it
            if (goal.getAccount() != null && !goal.getAccount().getId().equals(sourceAccount.getId())) {
                Account goalAccount = goal.getAccount();
                goalAccount.setCurrentBalance(goalAccount.getCurrentBalance().add(request.getAmount()));
                accountRepository.save(goalAccount);
            }
        }

        // Increase goal amount
        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0 && !goal.isCompleted()) {
            goal.setCompleted(true);
            goal.setCompletedAt(LocalDateTime.now());
        }
        goalRepository.save(goal);

        SavingsGoalContribution contribution = new SavingsGoalContribution(
                goal,
                request.getAmount(),
                date,
                request.getNotes(),
                sourceAccount,
                goal.getUser()
        );

        SavingsGoalContribution saved = contributionRepository.save(contribution);
        return mapContributionToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GoalContributionResponse> getContributions(Long goalId, Long userId) {
        SavingsGoal goal = getGoalEntity(goalId, userId);
        List<SavingsGoalContribution> list = contributionRepository
                .findBySavingsGoalIdOrderByContributionDateDescCreatedAtDesc(goal.getId());
        List<GoalContributionResponse> responses = new ArrayList<>();
        for (SavingsGoalContribution c : list) {
            responses.add(mapContributionToResponse(c));
        }
        return responses;
    }

    public SavingsGoalResponse toggleComplete(Long id, Long userId) {
        SavingsGoal goal = getGoalEntity(id, userId);
        goal.setCompleted(!goal.isCompleted());
        goal.setCompletedAt(goal.isCompleted() ? LocalDateTime.now() : null);
        SavingsGoal saved = goalRepository.save(goal);
        return mapToResponse(saved);
    }

    public SavingsGoalResponse mapToResponse(SavingsGoal g) {
        BigDecimal remaining = g.getTargetAmount().subtract(g.getCurrentAmount());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        double percentage = 0.0;
        if (g.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = g.getCurrentAmount()
                    .divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            if (percentage > 100.0) {
                percentage = 100.0;
            }
        }

        Long accountId = g.getAccount() != null ? g.getAccount().getId() : null;
        String accountName = g.getAccount() != null ? g.getAccount().getName() : null;

        return new SavingsGoalResponse(
                g.getId(),
                g.getName(),
                g.getTargetAmount(),
                g.getCurrentAmount(),
                remaining,
                percentage,
                g.getTargetDate(),
                g.isCompleted(),
                g.getCompletedAt(),
                g.getNotes(),
                accountId,
                accountName,
                g.getUser().getId(),
                g.getCreatedAt()
        );
    }

    public GoalContributionResponse mapContributionToResponse(SavingsGoalContribution c) {
        Long sourceAccountId = c.getSourceAccount() != null ? c.getSourceAccount().getId() : null;
        String sourceAccountName = c.getSourceAccount() != null ? c.getSourceAccount().getName() : null;

        return new GoalContributionResponse(
                c.getId(),
                c.getSavingsGoal().getId(),
                c.getSavingsGoal().getName(),
                c.getAmount(),
                c.getContributionDate(),
                c.getNotes(),
                sourceAccountId,
                sourceAccountName,
                c.getUser().getId(),
                c.getCreatedAt()
        );
    }
}
