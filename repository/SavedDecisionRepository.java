package com.decisionhub.repository;

import com.decisionhub.entity.SavedDecision;
import com.decisionhub.entity.SavedDecisionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedDecisionRepository extends JpaRepository<SavedDecision, SavedDecisionId> {
    List<SavedDecision> findByUserId(UUID userId);
    boolean existsByUserIdAndDecisionId(UUID userId, UUID decisionId);
}
