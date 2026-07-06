package com.decisionhub.mapper;

import com.decisionhub.dto.InvitationRequest;
import com.decisionhub.dto.InvitationResponse;
import com.decisionhub.dto.UserSummaryDto;
import com.decisionhub.entity.Community;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.Invitation;
import com.decisionhub.entity.InvitationStatus;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-05T11:41:10+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class InvitationMapperImpl implements InvitationMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public InvitationResponse toResponse(Invitation invitation) {
        if ( invitation == null ) {
            return null;
        }

        UUID decisionId = null;
        UUID communityId = null;
        UUID id = null;
        UserSummaryDto sender = null;
        String inviteeEmail = null;
        String token = null;
        InvitationStatus status = null;
        Instant expiresAt = null;
        Instant createdAt = null;

        decisionId = invitationDecisionId( invitation );
        communityId = invitationCommunityId( invitation );
        id = invitation.getId();
        sender = userMapper.toSummary( invitation.getSender() );
        inviteeEmail = invitation.getInviteeEmail();
        token = invitation.getToken();
        status = invitation.getStatus();
        expiresAt = invitation.getExpiresAt();
        createdAt = invitation.getCreatedAt();

        InvitationResponse invitationResponse = new InvitationResponse( id, sender, inviteeEmail, decisionId, communityId, token, status, expiresAt, createdAt );

        return invitationResponse;
    }

    @Override
    public Invitation toEntity(InvitationRequest request) {
        if ( request == null ) {
            return null;
        }

        Invitation invitation = new Invitation();

        invitation.setInviteeEmail( request.inviteeEmail() );

        return invitation;
    }

    private UUID invitationDecisionId(Invitation invitation) {
        DecisionBoard decision = invitation.getDecision();
        if ( decision == null ) {
            return null;
        }
        return decision.getId();
    }

    private UUID invitationCommunityId(Invitation invitation) {
        Community community = invitation.getCommunity();
        if ( community == null ) {
            return null;
        }
        return community.getId();
    }
}
