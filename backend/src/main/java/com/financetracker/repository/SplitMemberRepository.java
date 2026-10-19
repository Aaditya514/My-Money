package com.financetracker.repository;

import com.financetracker.entity.SplitMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitMemberRepository extends JpaRepository<SplitMember, Long> {

    List<SplitMember> findByGroupIdOrderByIdAsc(Long groupId);

    Optional<SplitMember> findByGroupIdAndIsCurrentUserTrue(Long groupId);

    Optional<SplitMember> findByIdAndGroupId(Long id, Long groupId);

    List<SplitMember> findByUserId(Long userId);
}
