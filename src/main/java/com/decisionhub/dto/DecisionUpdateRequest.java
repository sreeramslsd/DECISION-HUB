package com.decisionhub.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record DecisionUpdateRequest(
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    String title,

    String description,

    UUID categoryId,

    Instant deadline,

    Set<String> tags
) {}
