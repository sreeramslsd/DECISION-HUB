package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PollRequest(
    @NotBlank(message = "Question is required")
    @Size(max = 255, message = "Question cannot exceed 255 characters")
    String question
) {}
