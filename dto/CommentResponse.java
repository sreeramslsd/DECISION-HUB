package com.decisionhub.dto;

import com.decisionhub.entity.CommentStatus;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
    UUID id,
    UUID decisionId,
    UserSummaryDto author,
    UUID parentCommentId,
    String content,
    CommentStatus status,
    Instant createdAt
) {}
