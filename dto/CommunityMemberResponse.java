package com.decisionhub.dto;

import com.decisionhub.entity.CommunityRole;
import com.decisionhub.entity.CommunityStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing membership details of a user inside a community workspace.
 */
public record CommunityMemberResponse(
    UUID userId,
    String username,
    String avatarUrl,
    CommunityRole role,
    CommunityStatus status,
    Instant joinedAt
) {}
