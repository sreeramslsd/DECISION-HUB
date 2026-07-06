package com.decisionhub.dto;

import java.time.Instant;
import java.util.UUID;

public record VoteResponse(
    UUID id,
    UUID pollId,
    UUID optionId,
    UUID userId, // Null for anonymous votes
    Integer rating,
    Instant createdAt
) {}
