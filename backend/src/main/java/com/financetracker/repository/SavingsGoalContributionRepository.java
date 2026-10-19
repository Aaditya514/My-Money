package com.financetracker.repository;

import com.financetracker.entity.SavingsGoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SavingsGoalContributionRepository extends JpaRepository<SavingsGoalContribution, Long> {

    List<SavingsGoalContribution> findBySavingsGoalIdOrderByContributionDateDescCreatedAtDesc(Long savingsGoalId);

    List<SavingsGoalContribution> findByUserIdOrderByContributionDateDescCreatedAtDesc(Long userId);

    List<SavingsGoalContribution> findByUserIdAndContributionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
