package com.decisionhub.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
    UUID id,
    String name,
    String icon,
    String description,
    UUID parentCategoryId,
    String parentCategoryName,
    Instant createdAt
) {}
