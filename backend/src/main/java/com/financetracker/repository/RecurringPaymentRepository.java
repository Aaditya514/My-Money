package com.financetracker.repository;

import com.financetracker.entity.RecurringPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {

    List<RecurringPayment> findByUserIdOrderByNextDueDateAsc(Long userId);

    List<RecurringPayment> findByUserIdAndActiveTrueOrderByNextDueDateAsc(Long userId);

    Optional<RecurringPayment> findByIdAndUserId(Long id, Long userId);

    List<RecurringPayment> findByUserIdAndActiveTrueAndNextDueDateLessThanEqual(Long userId, LocalDate date);
}
