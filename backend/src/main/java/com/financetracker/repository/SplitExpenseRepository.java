package com.financetracker.repository;

import com.financetracker.entity.SplitExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitExpenseRepository extends JpaRepository<SplitExpense, Long> {

    List<SplitExpense> findByGroupIdOrderByExpenseDateDescCreatedAtDesc(Long groupId);

    Optional<SplitExpense> findByIdAndGroupId(Long id, Long groupId);

    List<SplitExpense> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SplitExpense> findByPaidById(Long memberId);
}
