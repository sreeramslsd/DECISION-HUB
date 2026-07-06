package com.decisionhub.repository;

import com.decisionhub.entity.DecisionVoter;
import com.decisionhub.entity.DecisionVoterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DecisionVoterRepository extends JpaRepository<DecisionVoter, DecisionVoterId> {
    boolean existsByDecisionIdAndUserId(UUID decisionId, UUID userId);
}
