package com.decisionhub.dto;

import java.time.Instant;
import java.util.UUID;

public record PollResponse(
    UUID id,
    UUID decisionId,
    String question,
    String status,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {}
