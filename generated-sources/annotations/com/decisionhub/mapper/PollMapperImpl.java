package com.decisionhub.mapper;

import com.decisionhub.dto.PollRequest;
import com.decisionhub.dto.PollResponse;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.Poll;
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
public class PollMapperImpl implements PollMapper {

    @Override
    public PollResponse toResponse(Poll poll) {
        if ( poll == null ) {
            return null;
        }

        UUID decisionId = null;
        UUID id = null;
        String question = null;
        String status = null;
        Instant createdAt = null;
        Instant updatedAt = null;
        Long version = null;

        decisionId = pollDecisionId( poll );
        id = poll.getId();
        question = poll.getQuestion();
        status = poll.getStatus();
        createdAt = poll.getCreatedAt();
        updatedAt = poll.getUpdatedAt();
        version = poll.getVersion();

        PollResponse pollResponse = new PollResponse( id, decisionId, question, status, createdAt, updatedAt, version );

        return pollResponse;
    }

    @Override
    public Poll toEntity(PollRequest request) {
        if ( request == null ) {
            return null;
        }

        Poll poll = new Poll();

        poll.setQuestion( request.question() );

        return poll;
    }

    private UUID pollDecisionId(Poll poll) {
        DecisionBoard decision = poll.getDecision();
        if ( decision == null ) {
            return null;
        }
        return decision.getId();
    }
}
