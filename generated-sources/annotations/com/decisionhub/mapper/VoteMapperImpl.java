package com.decisionhub.mapper;

import com.decisionhub.dto.VoteRequest;
import com.decisionhub.dto.VoteResponse;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.Poll;
import com.decisionhub.entity.User;
import com.decisionhub.entity.Vote;
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
public class VoteMapperImpl implements VoteMapper {

    @Override
    public VoteResponse toResponse(Vote vote) {
        if ( vote == null ) {
            return null;
        }

        UUID pollId = null;
        UUID optionId = null;
        UUID userId = null;
        UUID id = null;
        Integer rating = null;
        Instant createdAt = null;

        pollId = votePollId( vote );
        optionId = voteOptionId( vote );
        userId = voteUserId( vote );
        id = vote.getId();
        rating = vote.getRating();
        createdAt = vote.getCreatedAt();

        VoteResponse voteResponse = new VoteResponse( id, pollId, optionId, userId, rating, createdAt );

        return voteResponse;
    }

    @Override
    public Vote toEntity(VoteRequest request) {
        if ( request == null ) {
            return null;
        }

        Vote vote = new Vote();

        vote.setRating( request.rating() );

        return vote;
    }

    private UUID votePollId(Vote vote) {
        Poll poll = vote.getPoll();
        if ( poll == null ) {
            return null;
        }
        return poll.getId();
    }

    private UUID voteOptionId(Vote vote) {
        DecisionOption option = vote.getOption();
        if ( option == null ) {
            return null;
        }
        return option.getId();
    }

    private UUID voteUserId(Vote vote) {
        User user = vote.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }
}
