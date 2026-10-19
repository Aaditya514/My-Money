package com.financetracker.repository;

import com.financetracker.entity.SplitGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitGroupRepository extends JpaRepository<SplitGroup, Long> {

    List<SplitGroup> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<SplitGroup> findByIdAndUserId(Long id, Long userId);
}
