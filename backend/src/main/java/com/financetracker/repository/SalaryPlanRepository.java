package com.financetracker.repository;

import com.financetracker.entity.SalaryPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryPlanRepository extends JpaRepository<SalaryPlan, Long> {

    Optional<SalaryPlan> findByUserIdAndMonthAndYear(Long userId, int month, int year);

    List<SalaryPlan> findByUserIdOrderByYearDescMonthDesc(Long userId);
}
