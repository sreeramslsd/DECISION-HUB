package com.decisionhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VoteRequest(
    @NotNull(message = "Option ID is required")
    UUID optionId,

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    Integer rating // Only required for RATING_BASED voting
) {}
