package com.financetracker.repository;

import com.financetracker.entity.SplitExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SplitExpenseShareRepository extends JpaRepository<SplitExpenseShare, Long> {

    List<SplitExpenseShare> findByExpenseId(Long expenseId);

    List<SplitExpenseShare> findByMemberId(Long memberId);
}
