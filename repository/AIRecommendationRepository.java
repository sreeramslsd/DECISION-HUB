package com.decisionhub.repository;

import com.decisionhub.entity.AIRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, UUID> {
    Optional<AIRecommendation> findByDecisionId(UUID decisionId);
}
