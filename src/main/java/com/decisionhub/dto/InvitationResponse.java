package com.decisionhub.dto;

import com.decisionhub.entity.InvitationStatus;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
    UUID id,
    UserSummaryDto sender,
    String inviteeEmail,
    UUID decisionId,
    UUID communityId,
    String token,
    InvitationStatus status,
    Instant expiresAt,
    Instant createdAt
) {}
