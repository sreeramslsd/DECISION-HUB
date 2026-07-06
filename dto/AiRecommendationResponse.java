package com.decisionhub.dto;

import java.time.Instant;
import java.util.UUID;

public record AiRecommendationResponse(
    UUID id,
    UUID decisionId,
    String pros,
    String cons,
    String risks,
    String suggestions,
    String recommendation,
    String reasoning,
    Instant createdAt
) {}
