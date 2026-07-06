package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommunityRequest(
    @NotBlank(message = "Community name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    String name,

    String description,

    @NotBlank(message = "Community slug is required")
    @Size(max = 100, message = "Slug must be less than 100 characters")
    String slug,

    UUID categoryId
) {}
