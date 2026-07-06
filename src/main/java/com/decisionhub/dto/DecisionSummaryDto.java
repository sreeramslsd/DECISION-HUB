package com.decisionhub.dto;

import com.decisionhub.entity.DecisionStatus;

import java.time.Instant;
import java.util.UUID;

public record DecisionSummaryDto(
    UUID id,
    String title,
    DecisionStatus status,
    Instant deadline,
    Instant createdAt
) {}
