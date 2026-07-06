package com.decisionhub.dto;

import java.time.Instant;
import java.util.UUID;

public record CommunityResponse(
    UUID id,
    String name,
    String description,
    String slug,
    CategoryResponse category,
    UserSummaryDto creator,
    Instant createdAt
) {}
