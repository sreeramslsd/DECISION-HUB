package com.decisionhub.mapper;

import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.ComparisonFactorResponse;
import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.dto.ComparisonScoreResponse;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.ComparisonScore;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.User;
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
public class ComparisonMapperImpl implements ComparisonMapper {

    @Override
    public ComparisonFactorResponse toResponse(ComparisonFactor factor) {
        if ( factor == null ) {
            return null;
        }

        UUID decisionId = null;
        UUID id = null;
        String name = null;
        String description = null;
        Instant createdAt = null;
        Instant updatedAt = null;
        Long version = null;

        decisionId = factorDecisionId( factor );
        id = factor.getId();
        name = factor.getName();
        description = factor.getDescription();
        createdAt = factor.getCreatedAt();
        updatedAt = factor.getUpdatedAt();
        version = factor.getVersion();

        ComparisonFactorResponse comparisonFactorResponse = new ComparisonFactorResponse( id, decisionId, name, description, createdAt, updatedAt, version );

        return comparisonFactorResponse;
    }

    @Override
    public ComparisonFactor toEntity(ComparisonFactorRequest request) {
        if ( request == null ) {
            return null;
        }

        ComparisonFactor comparisonFactor = new ComparisonFactor();

        comparisonFactor.setName( request.name() );
        comparisonFactor.setDescription( request.description() );

        return comparisonFactor;
    }

    @Override
    public ComparisonScoreResponse toResponse(ComparisonScore score) {
        if ( score == null ) {
            return null;
        }

        UUID optionId = null;
        UUID factorId = null;
        UUID userId = null;
        int score1 = 0;
        String remarks = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        optionId = scoreOptionId( score );
        factorId = scoreFactorId( score );
        userId = scoreUserId( score );
        score1 = score.getScore();
        remarks = score.getRemarks();
        createdAt = score.getCreatedAt();
        updatedAt = score.getUpdatedAt();

        ComparisonScoreResponse comparisonScoreResponse = new ComparisonScoreResponse( optionId, factorId, userId, score1, remarks, createdAt, updatedAt );

        return comparisonScoreResponse;
    }

    @Override
    public ComparisonScore toEntity(ComparisonScoreRequest request) {
        if ( request == null ) {
            return null;
        }

        ComparisonScore comparisonScore = new ComparisonScore();

        comparisonScore.setScore( request.score() );
        comparisonScore.setRemarks( request.remarks() );

        return comparisonScore;
    }

    private UUID factorDecisionId(ComparisonFactor comparisonFactor) {
        DecisionBoard decision = comparisonFactor.getDecision();
        if ( decision == null ) {
            return null;
        }
        return decision.getId();
    }

    private UUID scoreOptionId(ComparisonScore comparisonScore) {
        DecisionOption option = comparisonScore.getOption();
        if ( option == null ) {
            return null;
        }
        return option.getId();
    }

    private UUID scoreFactorId(ComparisonScore comparisonScore) {
        ComparisonFactor factor = comparisonScore.getFactor();
        if ( factor == null ) {
            return null;
        }
        return factor.getId();
    }

    private UUID scoreUserId(ComparisonScore comparisonScore) {
        User user = comparisonScore.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }
}
