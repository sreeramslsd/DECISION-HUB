package com.decisionhub.mapper;

import com.decisionhub.dto.AiRecommendationResponse;
import com.decisionhub.entity.AIRecommendation;
import com.decisionhub.entity.DecisionBoard;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-05T11:41:10+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class AiRecommendationMapperImpl implements AiRecommendationMapper {

    @Override
    public AiRecommendationResponse toResponse(AIRecommendation recommendation) {
        if ( recommendation == null ) {
            return null;
        }

        UUID decisionId = null;
        UUID id = null;
        String pros = null;
        String cons = null;
        String risks = null;
        String suggestions = null;
        String recommendation1 = null;
        String reasoning = null;
        Instant createdAt = null;

        decisionId = recommendationDecisionId( recommendation );
        id = recommendation.getId();
        pros = recommendation.getPros();
        cons = recommendation.getCons();
        risks = recommendation.getRisks();
        suggestions = recommendation.getSuggestions();
        recommendation1 = recommendation.getRecommendation();
        reasoning = recommendation.getReasoning();
        createdAt = recommendation.getCreatedAt();

        AiRecommendationResponse aiRecommendationResponse = new AiRecommendationResponse( id, decisionId, pros, cons, risks, suggestions, recommendation1, reasoning, createdAt );

        return aiRecommendationResponse;
    }

    private UUID recommendationDecisionId(AIRecommendation aIRecommendation) {
        DecisionBoard decision = aIRecommendation.getDecision();
        if ( decision == null ) {
            return null;
        }
        return decision.getId();
    }
}
