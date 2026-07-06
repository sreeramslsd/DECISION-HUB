package com.decisionhub.repository;

import com.decisionhub.entity.DecisionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DecisionHistoryRepository extends JpaRepository<DecisionHistory, UUID> {
    List<DecisionHistory> findByDecisionIdOrderByCreatedAtDesc(UUID decisionId);
}
