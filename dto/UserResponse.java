package com.decisionhub.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    String roleName,
    String status,
    String avatarUrl,
    Instant createdAt
) {}
