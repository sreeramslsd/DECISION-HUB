package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CommentRequest(
    @NotBlank(message = "Comment content is required")
    String content,

    UUID parentCommentId
) {}
