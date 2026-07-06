package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryRequest(
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    String name,

    @Size(max = 100, message = "Icon name must be less than 100 characters")
    String icon,

    String description,

    UUID parentCategoryId
) {}
