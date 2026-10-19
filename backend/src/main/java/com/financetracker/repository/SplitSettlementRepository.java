package com.financetracker.repository;

import com.financetracker.entity.SplitSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitSettlementRepository extends JpaRepository<SplitSettlement, Long> {

    List<SplitSettlement> findByGroupIdOrderBySettlementDateDescCreatedAtDesc(Long groupId);

    Optional<SplitSettlement> findByIdAndGroupId(Long id, Long groupId);

    List<SplitSettlement> findByUserId(Long userId);
}
