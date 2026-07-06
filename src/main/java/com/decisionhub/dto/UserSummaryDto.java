package com.decisionhub.dto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String username,
    String avatarUrl
) {}
